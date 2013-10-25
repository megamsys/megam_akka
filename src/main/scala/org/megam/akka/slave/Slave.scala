/* 
** Copyright [2012-2013] [Megam Systems]
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
** http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/
package org.megam.akka.slave

import scalaz._
import Scalaz._
import akka.actor.Actor
import akka.actor.ActorPath
import akka.actor.ActorRef
import akka.pattern.pipe
import scala.concurrent.Future
import org.megam.akka.CloProtocol._
import net.liftweb.json._
import org.megam.chef.ChefServiceRunner
import org.megam.chef.DropIn
import org.megam.chef.ProvisionerFactory.TYPE
import org.megam.chef.exception._
import org.megam.akka.Constants._
import org.megam.akka.extn.Settings
import org.megam.common._
import com.twitter.zk._
import com.twitter.util.{ Duration, Promise, TimeoutException, Timer, Return, Await }
import org.apache.zookeeper.data.{ ACL, Stat }
import org.apache.zookeeper.KeeperException
import org.megam.akka.master.MasterWorkerProtocol._
/**
 * @author ram
 *
 * Any retry-logic that you might want would have to live in the InstrumenteeMaster,
 * but should we support idempotency (or) retry from stale state ?.
 *
 * Implement retry in InstrumenteeMaster and / or dovetail this concept into the
 * Clustering features of akka.
 *
 * Implement doWork() in the future, that the Worker is responsive to the InstrumenteeMaster’s
 * requests for status (or) streaming log updates to redis....
 */
case class MessageJson(code: Int, body: String, time_received: String)
case class BodyJson(message: String)

class Slave(masterLocation: ActorPath) extends AbstractSlave(masterLocation) {
  // We'll use the current dispatcher for the execution context.
  // You can use whatever you want.
  implicit val ec = context.dispatcher
  implicit val formats = DefaultFormats
 
  log.info("[{}]: >>  {} --> {}", "Slave:"+masterLocation.name, "Started", "Entry")

  val settings = Settings(context.system)
  val uris = settings.ZooUri

  
  def jsonValue(msg: Any): String = {
    msg match {
      case CloJob(x) => {
        val json = parse(x)
        log.info("[{}]: >>  {} --> {}", "Slave", "jsonvalue", json)
        val m = json.extract[MessageJson]
        val n = (parse(m.body)).extract[BodyJson]
        val mm = n.message
        mm
      }
      case NodeJob(x) => {
        val json = parse(x)
        val m = json.extract[MessageJson]
        m.body
      }
      case None => ""
    }
  }

  def doWork(workSender: ActorRef, msg: Any): Unit = {
    Future {     
      msg match {
        case CloJob(x) => {
          //TO-DO: separate it into individual computation of monads.
          log.info("[{}]: >>  {} --> {}", "Slave", "Future", msg)
          val id = jsonValue(msg)
          log.info("[{}]: >>  {} --> {}", "Slave", "id", id)
          Validation.fromTryCatch {
            (new ChefServiceRunner()).withType(TYPE.CHEF_WITH_SHELL).input(new DropIn(id)).control()
          } leftMap { t: Throwable =>
            val u = new java.io.StringWriter
            t.printStackTrace(new java.io.PrintWriter(u))
            log.error("[{}]: >>  {} --> {}", "Slave-" + id, "Future:Failure", u.toString)
            t
          } flatMap { x => 
            (context.actorSelection(ActorPath.fromString("akka://%s/user/%s".format(MEGAMCLOUD_CLUSTER, NODEACTOR))) ! new NodeJob(id)).successNel
          }
        }
        case NodeJob(x) => {
          log.info("[{}]: >>  {} --> {}", "Slave", "NodeJob", x)
          log.info("[{}]: >>  {} --> {}", "Slave", "URIS for ZooKeeper", uris)
          val zoo = new Zoo(uris, "nodes")         
          zoo.create(x, "Request ID started")          
        }
      }
      WorkComplete("done")
    } pipeTo self
  }
}
