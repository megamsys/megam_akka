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

import akka.actor.Actor
import akka.actor.Terminated
import akka.actor.ActorRef
import scala.collection.IndexedSeq

import org.megam.akka.gulps.GulpProtocol._

/**
 * @author ram
 *
 */
class GulpActor extends Actor {
  

  var masters = IndexedSeq.empty[ActorRef]
  var jobCounter = 0

  def receive = {
    case job: GulpJob if masters.isEmpty ⇒
      sender ! JobFailed("Service unavailable, try again later", job)

    case job: GulpJob =>
      jobCounter += 1
      masters(jobCounter % masters.size) forward job

    case MasterRegistration if !masters.contains(sender) ⇒
      context watch sender
      masters = masters :+ sender

    case Terminated(a) =>
      masters = masters.filterNot(_ == a)
  }
}

