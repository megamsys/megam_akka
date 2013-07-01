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
import org.megam.akka.slave._
import org.megam.akka.master._
import com.typesafe.config.ConfigFactory
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

  println("---------------------Clo app Started---------------------")
  val system = ActorSystem("megamcluster")

  var nodes = Set.empty[Address]
  val servicename = "closervice"
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

    //Every cluster[megamcluster] starts with a closervice and master=><x number of workers>     
    system.actorOf(Props[CloService], name = "closervice")
    system.actorOf(Props[NodeInstanceActor], name = "nodeactor")
    system.actorOf(Props[CloMaster], name = "clomaster")

    //Create 10 workers, use a "configurable flag" and Range over it to create the workers
    val w1 = worker("clomaster")
    val w2 = worker("clomaster")
    val w3 = worker("clomaster")
    val w4 = worker("clomaster")
    val w5 = worker("clomaster")
    val w6 = worker("clomaster")
    val w7 = worker("clomaster")
    val w8 = worker("clomaster")
    val w9 = worker("clomaster")
    val w10 = worker("clomaster")

  }

  def worker(name: String) =
    system.actorOf(Props(new Slave(ActorPath.fromString("akka://%s/user/%s".format(system.name, name)))))

  def shutdown = {
    system.shutdown()
  }
}
