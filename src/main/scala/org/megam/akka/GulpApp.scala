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
import org.megam.akka.gulps._
import org.megam.akka.slave._
import com.typesafe.config.ConfigFactory
/**
 * @author ram
 * 
 * The megam_akka contains two apps. GulpApp is the master cluster which in runs in each of the cloud nodes.
 * When a cloud node is created a script(ciakka_init.sh) downloads the megam_akka_<ver>.deb and installs it in the node.
 * Post installation, as its a node, the script "gulp" gets called which starts up GulpApp.
 * The GulpApp call a configuration file in the predefined location. The configuration fill will contain
 * specifics about how to connect to the messaging server. This acts as a conduit between nodes and cloud and 
 * helps us to instrument the nodes. 
 * 
 */
class GulpApp extends Bootable {  
  
  val gulpsystem = ActorSystem("megamgulp")
  def startup = {
    val clusterListener = gulpsystem.actorOf(Props(new Actor with ActorLogging {
      def receive = {
        case state: CurrentClusterState => log.info("Current members: {}", state.members)
        case MemberUp(member) ⇒
          log.info("Member is Up: {}", member)
        case UnreachableMember(member) ⇒
          log.info("Member detected as unreachable: {}", member)
        case _: ClusterDomainEvent ⇒ // ignore
      }
    }), name = "clusterlistener")
    Cluster(gulpsystem).subscribe(clusterListener, classOf[ClusterDomainEvent])
     gulpsystem.actorOf(Props[GulpActor], name = "gulpactor")
     gulpsystem.actorOf(Props[GulpMaster], name = "gulpmaster")
  }

  //Create 10 workers, use a "configurable flag" and Range over it to create the workers
    val w1 = worker("gulpmaster")
    val w2 = worker("gulpmaster")
    val w3 = worker("gulpmaster")
    val w4 = worker("gulpmaster")
    val w5 = worker("gulpmaster")
    val w6 = worker("gulpmaster")
    val w7 = worker("gulpmaster")
    val w8 = worker("gulpmaster")
    val w9 = worker("gulpmaster")
    val w10 = worker("gulpmaster")
 

  def worker(name: String) =
    gulpsystem.actorOf(Props(new GulpSlave(ActorPath.fromString("akka://%s/user/%s".format(gulpsystem.name, name)))))
  
  def shutdown = {
    gulpsystem.shutdown()
  }
}