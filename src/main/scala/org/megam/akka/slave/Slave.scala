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
import akka.actor.{ Actor, ActorPath, ActorRef }
import akka.pattern.pipe
import scala.concurrent.Future
import org.megam.akka.CloProtocol._
import net.liftweb.json._
import org.megam.chef.ChefServiceRunner
import org.megam.scm.git.GitRepository
import org.megam.chef.DropIn
import org.megam.chef.ProvisionerFactory.TYPE
import org.megam.chef.exception._
import org.megam.akka.Constants._
import org.megam.akka.extn.Settings
import org.megam.common._
import org.megam.common.s3._
import com.twitter.zk._
import com.twitter.util.{ Duration, Promise, TimeoutException, Timer, Return, Await }
import org.apache.zookeeper.data.{ ACL, Stat }
import org.apache.zookeeper.KeeperException
import org.megam.akka.master.MasterWorkerProtocol._
import java.net.URI
import java.io.File;
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
case class BodyJson(id: String)
case class RecipeBodyJson(vault_loc: String, repo_path: String)

class Slave(masterLocation: ActorPath) extends AbstractSlave(masterLocation) {
  // We'll use the current dispatcher for the execution context.
  // You can use whatever you want.
  implicit val ec = context.dispatcher
  implicit val formats = DefaultFormats

  log.info("[{}]: >> {} --> {}", "Slave:" + masterLocation.name, "Started", "Entry")

  val settings = Settings(context.system)
  val uris = settings.ZooUri
  val access_key = settings.access_key
  val secret_key = settings.secret_key
  val recipe_bucket = settings.recipe_bucket  
  val clone_file_name = settings.clone_file_name
  //val megam_home = settings.megam_home

  def jsonValue(msg: Any): Tuple2[String, String] = {
    msg match {
      case CloJob(x) => {
        val json = parse(x)
        log.info("[{}]: >> {} --> {}", "Slave", "jsonvalue", json)
        val m = json.extract[MessageJson]
        val n = (parse(m.body)).extract[BodyJson]        
        val mm = n.id        
        Tuple2(mm, "")
      }
      case NodeJob(x) => {
        val json = parse(x)
        val m = json.extract[MessageJson]
        Tuple2(m.body, "")
      }
      case RecipeJob(x) => {
        val json = parse(x)
        log.info("[{}]: >> {} --> {}", "Slave", "jsonvalue", json)
        val m = json.extract[MessageJson]
        val n = (parse(m.body)).extract[BodyJson]
        val l = (parse(n.id)).extract[RecipeBodyJson]
        Tuple2(l.vault_loc, l.repo_path)
      }
      case None => ("", "")
    }
  }

  def vaultLocationParser(url: String) = {
    var str = url
    val lst = str.lastIndexOf("/")
    val file = str.substring(lst)
    str = str.replace(str.substring(lst), "")
    val cts = str.substring(str.lastIndexOf("/"))
    str = str.replace(str.substring(str.lastIndexOf("/")), "")
    val email = str.substring(str.lastIndexOf("/") + 1)
    email + cts + file
  }

  def doWork(workSender: ActorRef, msg: Any): Unit = {
    Future {
      msg match {
        case CloJob(x) => {
          //TO-DO: separate it into individual computation of monads.
          log.info("[{}]: >> {} --> {}", "Slave", "Future", msg)
          val tuple_succ = jsonValue(msg)
          val id = tuple_succ._1
          log.info("[{}]: >> {} --> {}", "Slave", "id", id)
          Validation.fromTryCatch {
            (new ChefServiceRunner()).withType(TYPE.CHEF_WITH_SHELL).input(new DropIn(id)).control()
          } leftMap { t: Throwable =>
            val u = new java.io.StringWriter
            t.printStackTrace(new java.io.PrintWriter(u))
            log.error("[{}]: >> {} --> {}", "Slave-" + id, "Future:Failure", u.toString)
            t
          } flatMap { x =>
            (context.actorSelection(ActorPath.fromString("akka://%s/user/%s".format(MEGAMCLOUD_CLUSTER, NODEACTOR))) ! new NodeJob(id)).successNel
          }
        }
        case NodeJob(x) => {
          log.info("[{}]: >> {} --> {}", "Slave", "NodeJob", x)
          log.info("[{}]: >> {} --> {}", "Slave", "URIS for ZooKeeper", uris)
          val zoo = new Zoo(uris, "nodes")
          zoo.create(x, "Request ID started")
        }
        case RecipeJob(x) => {
          log.info("[{}]: >> {} --> {}", "Slave", "vault_location", x)
          val tuple_succ = jsonValue(msg)
          val vl = tuple_succ._1
          val repo_file = tuple_succ._2
          val loc = vaultLocationParser(vl)
          val download_loc = loc.replace(loc.substring(loc.lastIndexOf("/")), "")
          Future {            
            Validation.fromTryCatch {
              val s3 = new S3(Tuple2(access_key, secret_key))
              log.info("[{}]: >> {} ------------> {}", "Slave", "Download_location", loc)
              s3.download(recipe_bucket, loc)
              (new ZipArchive).unZip(recipe_bucket + "/" + loc, recipe_bucket + "/" + download_loc)
              (new GitRepository(recipe_bucket + "/" + download_loc + "/" + new File(clone_file_name))).clone(repo_file)
            } leftMap { t: Throwable =>
              val u = new java.io.StringWriter
              t.printStackTrace(new java.io.PrintWriter(u))
              log.error("[{}]: >> {} --> {}", "Slave-" + id, "Future:Failure", u.toString)
              t
            }
          }
        }
      }
      WorkComplete("done")
    } pipeTo self
  }
}