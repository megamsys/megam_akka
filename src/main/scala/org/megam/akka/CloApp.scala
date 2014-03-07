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
import akka.actor._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import akka.kernel.Bootable
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.cluster.MemberStatus
import org.megam.akka.Constants._
import org.megam.akka.master.CloMaster
import org.megam.akka.slave._
import org.megam.akka.master._
import com.typesafe.config.ConfigFactory
import org.megam.akka.extn.SettingsImpl
import org.megam.akka.extn.Settings
import org.megam.akka.Config._
/**
 * @author ram
 *
 * The megam_akka is a cloud bridge to cloud manage megam platform. It contains two main apps.
 *  CloApp is the master cluster which starts up two actors
 *      1)closervice => closervice is responsible for connecting to the global subscriber queue and send a clojob
 *      to the clomaster
 *      2)clomaster  => clomaster is responsible for executing  various workers to run the clojob. On completion this sends
 *      a cloresult back.
 *  GulpActor is the node actor which is started as a result of creation of a cloud_book from the interface (UI, CLI, Mob)
 */
class CloApp extends Bootable {

  val config = ConfigFactory.load()
  val system = ActorSystem(MEGAMCLOUD_CLUSTER)
  var clo_clusters = Set.empty[Address]

  def startup = {
    println("[MEGAM]: >> Booting up Megam --> Cloud Herk 0.1")
    val clusterListener = system.actorOf(Props(new Actor with ActorLogging {
      def receive = {
        case state: CurrentClusterState =>
          clo_clusters = state.members.collect {
            case m if m.status == MemberStatus.Up ⇒ m.address
          }
        case MemberUp(m)           => clo_clusters += m.address
        case other: MemberEvent    => clo_clusters -= other.member.address
        case UnreachableMember(m)  => clo_clusters -= m.address
        case _: ClusterDomainEvent ⇒ // ignore
      }
    }), name = "cloclusterlistener")
    Cluster(system).subscribe(clusterListener, classOf[ClusterDomainEvent])

    //Every cluster[megamcloud_cluster] starts with a closervice and master=><x number of workers>     
    system.actorOf(Props[CloService], name = CLOSERVICE)    
    system.actorOf(Props[CloudRecipeActor], name = CLOUDRECIPEACTOR)
    system.actorOf(Props[RiakStashActor], name = RIAKSTASHACTOR)
    system.actorOf(Props[CloMaster], name = CLOMASTER)    

    println("[MEGAM]: >> Clo Workers -----------------------------> %d")
    val clo_workers = 1 to WORKER_COUNT map { x => worker(CLOMASTER) }
    println("[MEGAM]: >> Clo Workers --> created")

  }

  def worker(name: String) =
    system.actorOf(Props(new Slave(ActorPath.fromString("akka://%s/user/%s".format(system.name, name)))))

  def shutdown = {
    //TO-DO: if the clo_clusters are not empty (clo_clusters, then send a message and gracefully shut it down
    //       send a message to clo_workers and gracefully shut it down.
    system.shutdown()
  }
}
