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
package org.megam.akka.master

import akka.actor.ActorRef

/**
 * @author ram
 *
 */
object MasterWorkerProtocol {

  // Messages from Workers
  case class WorkerCreated(worker: ActorRef)
  case class WorkerRequestsWork(worker: ActorRef)
  case class WorkIsDone(worker: ActorRef)

  // Messages to Workers
  case class WorkToBeDone(work: Any)
  case object WorkIsReady
  case object NoWorkToBeDone

  case object NodeReg
  case class NodeJob(job: String)
  case class NodeRes(res: String)
  case class NodeFail(fail: String, job: NodeJob)

  case object WatchReg
  case class WatchJob(job: String)
  case class WatchRes(res: String)
  case class WatchFail(fail: String, job: WatchJob)

  case object RecipeReg
  case class RecipeJob(job: String)
  case class RecipeRes(res: String)
  case class RecipeFail(fail: String, job: RecipeJob)

}