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
package org.megam.akka

import scalaz._
import Scalaz._
import scalaz.Validation._
import akka.actor._
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import java.util.concurrent.atomic.AtomicInteger
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import org.megam.akka.extn.Settings

/**
 * @author rajthilak
 *
 */
class NodeInstanceActor extends Actor with ActorLogging {
  import org.megam.akka.CloProtocol._

  val cluster = Cluster(context.system)

  // This a var. Too bad. Fix it later.
  var masters = IndexedSeq.empty[ActorRef]

  val jobCounter: AtomicInteger = new AtomicInteger(0)
  log.info("==================================NodeInstanceActor Started========================================")
  val settings = Settings(context.system)
  val findMe = "|^^^/@\"^^^|"

  override def preStart(): Unit = {
    log.debug("{} Starting ========={}", findMe, "NodeInstanceActor....")

  }

  override def postStop(): Unit = {
    log.debug("{} Stopping {} App. Not implemented yet.", findMe, "NodeInstanceActor....")
  }

  /**
   * Bunch of case classes, which knows how to handle a Nodejob, handle the result,
   * handle result failure and process registration of masters.
   */
  def receive = {

    case job: NodeJob if masters.isEmpty =>
      sender ! NodeFail("Service unavailable, try again later" + masters.size, job)

    case job: NodeJob => {
      log.debug("{} NodeJob Forwarded.{}", findMe, job)
      masters(jobCounter.getAndIncrement() % masters.size) forward job
    }
    case result: NodeRes  => println(result)
    case failed: NodeFail => log.info("{} NodeFail.{}", findMe, failed)
    case NodeReg if !masters.contains(sender) => {
      context watch sender
      masters = masters :+ sender
      log.info("=========NodeReg created============")
      log.debug("{} NodeReg.{}", findMe, masters.size)
    }
    case Terminated(a) => {
      log.debug("{} Terminated.{}", findMe, masters.size)
      masters = masters.filterNot(_ == a)
    }

  }

}