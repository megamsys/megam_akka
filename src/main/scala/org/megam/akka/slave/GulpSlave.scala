/* 
** Copyright [2012] [Megam Systems]
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
import org.megam.akka.Constants._
import org.megam.akka.extn.Settings
import org.megam.akka.gulps.GulpProtocol._
import org.megam.akka.master.MasterWorkerProtocol._
import net.liftweb.json._
import scala.io.Source
import java.io.File
import org.megam.common._
import com.twitter.zk._
import com.twitter.util.{ Duration, Promise, TimeoutException, Timer, Return, Await }
import org.apache.zookeeper.data.{ ACL, Stat }
import org.apache.zookeeper.KeeperException
/**
 * @author rajthilak
 *
 */

case class Queue_gulpinfo(name: String, exchange: String, queue: String, rails: String, scm: String, db: String)

class GulpSlave(masterLocation: ActorPath) extends AbstractSlave(masterLocation)  {
  // We'll use the current dispatcher for the execution context.
  // You can use whatever you want.
  implicit val ec = context.dispatcher
  implicit val formats = DefaultFormats
  val settings = Settings(context.system)
  val uris = settings.ZooUri
  val findMe = "|\"/|"
  log.info("Gulp Slave started")

  def doWork(workSender: ActorRef, msg: Any): Unit = {
    Future {     
      msg match {
        case GulpJob(x) => {
          val str1 = new java.io.File("/home/rajthilak/.megam/").listFiles.filter(_.getName.endsWith(".json"))
          str1.map(a => {
            val lines = Source.fromFile(a)
            val data = parse(lines mkString).extract[Queue_gulpinfo]
            val zoo = new Zoo(uris, "nodes")
            zoo.create(data.name, "Running")
            context.actorSelection(ActorPath.fromString("akka://%s/user/%s".format(MEGAMGULP, NODEACTOR))) ! new NodeJob(data.name)
            context.actorSelection(ActorPath.fromString("akka://%s/user/%s".format(MEGAMGULP, WATCHACTOR))) ! new WatchJob((zoo.zknode).path)
          })
        }
        case NodeJob(x) => {
          println("------------>>>" + x)

          val zoo1 = new Zoo(uris, "nodes/" + x)
          val str1 = new java.io.File("/home/rajthilak/.megam/").listFiles.filter(_.getName.endsWith(".json"))
          str1.map(a => {
            val lines = Source.fromFile(a)
            val data = parse(lines mkString).extract[Queue_gulpinfo]
            zoo1.create(data.rails, "Running")
            zoo1.create(data.scm, "Running")
            zoo1.create(data.db, "Running")
          })
        }
        case WatchJob(x) => {
          import com.twitter.util.Future
          val zoo = new Zoo(uris, "nodes")
          val t = zoo.watch[ValidationNel[Throwable, Future[ZNode.Watch[ZNode.Children]]]](zoo.watchChildren, x)
          val res = t match {
            case Success(zn) => zn.onSuccess {
              case ZNode.Watch(Return(z), u) => {
                //println("----------------------------------Node path: %s".format(z))
                u onSuccess {
                  //case NodeEvent.ChildrenChanged(name) => logger.debug("Node Name: %s".format(name))
                  case e => {                     
                     println("Event: %s".format(e))
                    //context.actorSelection(ActorPath.fromString("akka://%s/user/%s".format("megamgulp", "watchactor"))) ! new WatchJob((zoo.zknode).path)
                  }
                }
              }
              case _ => log.info("{} Event: {}", findMe, "unexpected return value")
            }
            case Failure(err) => {
              log.info("{} Failure: {}", findMe, err)
              null
            }
          }
        }
      }
      WorkComplete("done")
    } pipeTo self
  }

}