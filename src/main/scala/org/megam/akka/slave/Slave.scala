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

import akka.actor.Actor
import akka.actor.ActorPath
import akka.actor.ActorRef
import akka.pattern.pipe
import scala.concurrent.Future

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
class Slave(masterLocation: ActorPath) extends AbstractSlave(masterLocation) {
  // We'll use the current dispatcher for the execution context.
  // You can use whatever you want.
  implicit val ec = context.dispatcher
println("Slave started")
  def doWork(workSender: ActorRef, msg: Any): Unit = {
    Future {
      workSender ! msg
      println("======WorkSender=============================>"+workSender)
      WorkComplete("done")
    } pipeTo self
  }
}

/**
// Downloading could take a while :)
implicit val askTimeout = Timeout(5 minutes)
 
// Some list of URLs we want to download
val urls = List(url1, ulr2, url3, ..., urlN)
 
// The Master
val m = system.actorOf(Props[Master], "master")
 
// Assume Workers are now present
 
// Get a Future to a bunch of eventual downloaded pages
Future.sequence(urls.map { url =>
m ? Download(url)
}) map { pages =>
PagesReady(pages)
} pipeTo pageRenderer
**/
