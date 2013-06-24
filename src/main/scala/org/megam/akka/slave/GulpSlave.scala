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
/**
 * @author rajthilak
 *
 */
class GulpSlave (masterLocation: ActorPath) extends AbstractSlave(masterLocation) {
  // We'll use the current dispatcher for the execution context.
  // You can use whatever you want.
  implicit val ec = context.dispatcher
 
  println("Gulp Slave started")  
 
  
  def doWork(workSender: ActorRef, msg: Any): Unit = {
    Future {
      workSender ! msg   
      println("----------"+msg)
      WorkComplete("done")
    } pipeTo self
  }

}