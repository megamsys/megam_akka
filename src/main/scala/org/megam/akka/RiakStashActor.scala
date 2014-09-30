/* 
# ** Copyright [2012-2013] [Megam Systems]
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
import scala.concurrent._
import scala.concurrent.duration.Duration
import org.megam.akka.Constants._
import org.megam.akka.extn.Settings
import org.megam.common._
import org.megam.common.amqp._
import org.megam.common.amqp.request._
import org.megam.common.amqp.response._
import org.megam.common.concurrent._
import akka.actor._
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import java.util.concurrent.atomic.AtomicInteger
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._

/**
 * @author rajthilak
 *
 */

class RiakStashActor extends Actor with ActorLogging {
  import org.megam.akka.master.MasterWorkerProtocol._

  val cluster = Cluster(context.system)

  // This a var. Too bad. Fix it later.
  var clomasters = IndexedSeq.empty[ActorRef]

  val jobCounter: AtomicInteger = new AtomicInteger(0)
  val settings = Settings(context.system)
  val uris = settings.AMQPUri
  val exchange_name = settings.stash_exchange
  val queue_name = settings.stash_queue
  val routingKey = "megam_key"
  val findMe = "[^-^|"

  override def preStart(): Unit = {
    println("Riak Stash preStart Entry")
    log.info("[{}]: >>  {} --> {}", "RiakStash", findMe + "preStart", "Entry")  
    val rmq = new RabbitMQClient(uris, exchange_name, queue_name)
    execute(rmq.subscribe(qThirst, routingKey))
    println("RiakStash prestart Exit")

  }

  override def postStop(): Unit = {
    log.info("[{}]: >>  {} --> {}", "RiakStash", findMe + "postStop", "TO-DO: Not implemented yet.")
  }

  protected def execute(ampq_request: AMQPRequest, duration: Duration = org.megam.common.concurrent.duration) = {
    import org.megam.common.concurrent.SequentialExecutionContext
    val responseFuture: Future[ValidationNel[Throwable, AMQPResponse]] = ampq_request.apply
    responseFuture.block(duration)
  }

  /**
   * Bunch of case classes, which knows how to handle a Stashjob, handle the result,
   * handle result failure and process registration of masters.
   */
  def receive = {

    case job: StashJob if clomasters.isEmpty =>
      sender ! StashFail("Service unavailable, try again later" + clomasters.size, job)

    case job: StashJob => {
      log.info("{} StashJob Forwarded.{}", findMe, job)
      clomasters(jobCounter.getAndIncrement() % clomasters.size) forward job
    }
    case result: StashRes  => println(result)
    case failed: StashFail => log.debug("[{}]: >>  {} --> {}", "StashService", findMe + "StashFail", failed)
    case StashReg if !clomasters.contains(sender) => {
      context watch sender
      clomasters = clomasters :+ sender
      log.info("[{}]: >>  {} --> {}", "StashService", findMe + "StashReg", clomasters.size)
    }
    case Terminated(a) => {
      log.info("[{}]: >>  {} --> {}", "StashService", findMe + "Terminated", clomasters.size)
      clomasters = clomasters.filterNot(_ == a)
    }

  }

  /**
   * This is a callback function invoked when an consumer thirsty for a response wants it to be quenched.
   * The response is a either a success or  a failure delivered as scalaz (ValidationNel).
   * be RiakStashActor"
   */
  def qThirst(h: AMQPResponse) = {
    log.info("[{}]: >>  {} --> {}", "StashService", findMe + "qThirst", h.toJson(true))
    val result = h.toJson(false) // the response is parsed back    
    println("Received "+result)
    self ! new StashJob(result)
    val res: ValidationNel[Throwable, Option[String]] = result.some match {
      case Some(resp) => {
        log.info("[{}]: >>  {} --> {}", "StashService", findMe + "qThirst", "Received some")
        result.some.successNel
      }
      case None => new java.lang.Error("I received nothing in the amqp response for my subscription, contains invalid JSON. counldn't parse it.").failureNel
    }
    res
  }

}

