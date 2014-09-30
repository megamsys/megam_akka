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
import scala.concurrent._
import scala.concurrent.duration.Duration
import org.megam.akka.extn.Settings
import org.megam.akka.extn.Settings
import org.megam.akka.gulps.GulpProtocol._
import org.megam.common._
import org.megam.common.amqp._
import org.megam.common.amqp.request._
import org.megam.common.amqp.response._
import org.megam.common.concurrent._
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.Terminated
import akka.actor.ActorRef
import scala.collection.IndexedSeq
import scala.io.Source
import java.util.concurrent.atomic.AtomicInteger
import java.io.File
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import com.twitter.zk._
/**
 * @author ram
 *
 */
case class Queue_info(name: String, exchange: String, queue: String)

class GulpActor extends Actor with ActorLogging {

  import org.megam.akka.gulps.GulpProtocol._

  implicit val formats = DefaultFormats

  val cluster = Cluster(context.system)
  val jobCounter: AtomicInteger = new AtomicInteger(0)
  // This a var. Too bad. Fix it later.
  var masters = IndexedSeq.empty[ActorRef]

  val settings = Settings(context.system)
  val uris = settings.AMQPUri
  val exchange_name = settings.exchange
  val queue_name = settings.queue
  val routingKey = "megam_key"
  val findMe = "|^^^/@\"^^^|"

  override def preStart(): Unit = {
    log.debug(("%-20s -->[%s]").format("GulpActor", "preStart:Entry"))
    val nodeBootPath = scala.util.Properties.envOrElse("MEGAM_HOME", scala.util.Properties.userHome)
    val nodeJsons = new java.io.File(nodeBootPath).listFiles.filter(_.getName.endsWith(".json"))
    nodeJsons.map(a => {
      val lines = Source.fromFile(a)
      val data = parse(lines mkString).extract[Queue_info]
      val rmq = new RabbitMQClient(uris, data.exchange, data.queue)
      execute(rmq.subscribe(qThirst, routingKey))
    })
  }

  override def postStop(): Unit = {
    log.debug("{} Stopping {} App. Not implemented yet.", findMe, "GulpService....")
  }

  protected def execute(ampq_request: AMQPRequest, duration: Duration = org.megam.common.concurrent.duration) = {
    import org.megam.common.concurrent.SequentialExecutionContext
    val responseFuture: Future[ValidationNel[Throwable, AMQPResponse]] = ampq_request.apply
    responseFuture.block(duration)
  }

  def receive = {
    case job: GulpJob if masters.isEmpty => {
      println("Service unavailable, try again later----" + job)
      sender ! JobFailed("Service unavailable, try again later", job)
    }
    case job: GulpJob => {
      log.debug("master size---->" + job)
      //masters(jobCounter % masters.size) forward job
      masters(jobCounter.getAndIncrement() % masters.size) forward job
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
   */
  def qThirst(h: AMQPResponse) = {
    log.info("{} Quench.{}", findMe, "GulpService")
    val result = h.toJson(false).some // the response is parsed back       
    val res: ValidationNel[Throwable, Option[String]] = result match {
      case Some(resp) => {
        log.info("{} Quench.{}", findMe, "Successs ...." + resp)
        self ! new GulpJob(result.getOrElse("none-gulpres"))
        result.successNel
      }
      case None => new java.lang.Error("I received nothing in the amqp response for my subscription, contains invalid JSON. counldn't parse it.").failureNel
    }
    res
  }

}

