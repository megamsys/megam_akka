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

import org.megam.common.amqp._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._

/**
 * @author ram
 * Closervice is responsible for receiving the Clojob, and sending it to the master to execute the job.
 * During a prestart a subscribe is done to the global exchange, queue. The properites as available in the 
 * settings file is used.
 * => Verify if this is a blocking call or non blocking. It will be non blocking. Then we need to use 
 * techniques like tick, or anything else to periodically subscribe in the queue, or Await (Blocking mode)
 * as seen here http://www.smartjava.org/content/connect-rabbitmq-amqp-using-scala-play-and-akka
 * to gather the input.
 */
class CloService extends Actor with ActorLogging {

  case object CloReg
  case class CloJob(job: String)
  case class CloRes(res: String)
  case class CloFail(fail: String, job: CloJob)

  val cluster = Cluster(context.system)
  
  // This a var. Too bad. Fix it later.
  var clomasters = IndexedSeq.empty[ActorRef]
  
  val jobCounter: AtomicInteger = new AtomicInteger(0)
  
  override def preStart(): Unit = {
    val rmq = new RabbitMQClient("uris", "exchange", "queue")
    rmq.subscribe(quenchThirst, "key")
  }

  override def postStop(): Unit = {
    log.info("Stopping {} App. Not implemented yet.", "CloService")
  }

  /**
   * Bunch of case classes, which knows how to handle a clojob, handle the result, 
   * handle result failure and process registration of masters.
   */
  def receive = {

    case job: CloJob if clomasters.isEmpty ⇒
      sender ! CloFail("Service unavailable, try again later", job)

    case job: CloJob =>
      clomasters(jobCounter.getAndIncrement() % clomasters.size) forward job
      
    case result: CloRes ⇒
      println(result)
      
    case failed: CloFail ⇒
      println(failed)

    case CloReg if !clomasters.contains(sender) ⇒
      context watch sender
      clomasters = clomasters :+ sender
    case Terminated(a) =>
      clomasters = clomasters.filterNot(_ == a)

  }

  /**
   * This is a callback function invoked when an consumer thirsty for a response wants it to be quenched.
   * The response is a either a success or  a failure delivered as scalaz (ValidationNel).
   * See if a message "CloJob with the result JSON" can be sent to the sender (The sender in this case shall 
   * be CloService"
   */
  def quenchThirst(h: AMQPResponse) = {
    val result = h.toJson(true) // the response is parsed back

    val res: ValidationNel[Error, String] = result match {
      case respJSON => respJSON.successNel
      case _        => UncategorizedError("request type", "unsupported response type", List()).failNel

    }
    res
  }

  
}

