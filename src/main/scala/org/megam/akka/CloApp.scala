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

import akka.actor._
import akka.kernel.Bootable
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.cluster.MemberStatus

import org.megam.akka.master.CloMaster

/**
 * @author ram
 *
 * The megam_akk contains two apps. CloApp is the master cluster which starts up two actors
 * 1.closervice => closervice is responsible for connecting to the global subscriber queue and send a clojob
 * to the clomaster
 * 2.clomaster  => clomaster is responsible for executing  various workers to run the clojob. On completion this sends
 * a cloresult back.
 */
class CloApp extends Bootable {

  val system = ActorSystem("megamclo")

  var nodes = Set.empty[Address]

  def startup = {
    val clusterListener = system.actorOf(Props(new Actor with ActorLogging {
      def receive = {
        case state: CurrentClusterState =>
          nodes = state.members.collect {
            case m if m.status == MemberStatus.Up ⇒ m.address
          }
        case MemberUp(m)           => nodes += m.address
        case other: MemberEvent    => nodes -= other.member.address
        case UnreachableMember(m)  => nodes -= m.address
        case _: ClusterDomainEvent ⇒ // ignore
      }
    }), name = "clusterlistener")
    Cluster(system).subscribe(clusterListener, classOf[ClusterDomainEvent])
    /**
     *  Every cluster[clo] starts with a service and master=>workers
     */
    system.actorOf(Props[CloService], name = "closervice")
    system.actorOf(Props[CloMaster], name = "clomaster")
  }

  def shutdown = {
    system.shutdown()
  }
}