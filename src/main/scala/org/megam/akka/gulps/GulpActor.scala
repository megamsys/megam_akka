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
package org.megam.akka.gulps

import scalaz._
import Scalaz._
import scalaz.Validation._
import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.Terminated
import akka.actor.ActorRef
import scala.collection.IndexedSeq
import java.util.concurrent.atomic.AtomicInteger
import org.megam.akka.gulps.GulpProtocol._
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import java.util.concurrent.atomic.AtomicInteger
import org.megam.common.amqp._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import org.megam.akka.extn.Settings
import scala.io.Source
/**
 * @author ram
 *
 */
case class Queue_info(name: String, exchange: String, queue: String)
class GulpActor extends Actor with ActorLogging {

  import org.megam.akka.gulps.GulpProtocol._
  implicit val formats = DefaultFormats
  
  val cluster = Cluster(context.system)
  var masters = IndexedSeq.empty[ActorRef]
  var jobCounter = 0
  
  println("====================Gulp Actor Started====================")

  val settings = Settings(context.system)
  val lines = Source.fromFile("/home/rajthilak/.megam/redis.json")
  val data = parse(lines mkString).extract[Queue_info]
  println("+++++++++++++++++++++++++++++++++++++++++" + data)
  val uris = settings.AMQPUri
  val exchange_name = settings.exchange
  val queue_name = settings.queue
  val routingKey = "megam_key"
  val findMe = "|^^^/@\"^^^|"

  override def preStart(): Unit = {
    log.debug("{} Starting {}", findMe, "GulpService....")
    val rmq = new RabbitMQClient(uris, exchange_name, queue_name)
    execute(rmq.subscribe(quenchThirst, routingKey))
  }

  override def postStop(): Unit = {
    log.debug("{} Stopping {} App. Not implemented yet.", findMe, "GulpService....")
  }

  protected def execute[T](t: AMQPRequest, expectedCode: AMQPResponseCode = AMQPResponseCode.Ok) = {
    log.debug("{} Execute AMQP SUB {}.", findMe, "GulpService....")
    val r = t.executeUnsafe
  }

  def receive = {
    case job: GulpJob if masters.isEmpty => {
      println("Service unavailable, try again later----" + job)
      sender ! JobFailed("Service unavailable, try again later", job)
    }
    case job: GulpJob => {
      jobCounter += 1
      println("master size---->" + job)
      masters(jobCounter % masters.size) forward job
    }
    case MasterRegistration if !masters.contains(sender) => {
      context watch sender
      masters = masters :+ sender
      log.info("=========Master Registered============")
    }
    case Terminated(a) =>
      masters = masters.filterNot(_ == a)
  }

  /**
   * This is a callback function invoked when an consumer thirsty for a response wants it to be quenched.
   * The response is a either a success or  a failure delivered as scalaz (ValidationNel).
   * See if a message "GulpJob with the result JSON" can be sent to the sender (The sender in this case shall
   * be GulpActor"
   */
  def quenchThirst(h: AMQPResponse) = {
    log.info("{} Quench.{}", findMe, "GulpService")
    val result = h.toJson(false) // the response is parsed back       
    self ! new GulpJob(result)
    val res: ValidationNel[Error, String] = result match {
      case respJSON => {
        log.info("{} Quench.{}", findMe, "Successs ....")
        respJSON.successNel
      }
      case _ => UncategorizedError("request type", "unsupported response type", List()).failNel
    }
    res
  }
}

