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
package org.megam.akka.metrics

import akka.actor.{Actor, ActorLogging}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.ClusterMetricsChanged
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.NodeMetrics
import akka.cluster.StandardMetrics.HeapMemory
import akka.cluster.StandardMetrics.Cpu

/**
 * @author ram
 *
 */
class MetricsListener extends Actor with ActorLogging {
  
  val selfAddress = Cluster(context.system).selfAddress // subscribe to ClusterMetricsChanged 
 
  // re-subscribe when restart override 
  override def preStart(): Unit = Cluster(context.system).subscribe(self, classOf[ClusterMetricsChanged])

  override def postStop(): Unit = Cluster(context.system).unsubscribe(self)

  def receive = {
    case ClusterMetricsChanged(clusterMetrics) => clusterMetrics.filter(_.address == selfAddress) foreach {
      nodeMetrics => 
        logHeap(nodeMetrics) 
        logCpu (nodeMetrics)
    }
    case state: CurrentClusterState ⇒ // ignore 
  }

  def logHeap(nodeMetrics: NodeMetrics): Unit = nodeMetrics match {
    case HeapMemory(address, timestamp, used, committed, max) => log.info("Used heap: {} MB", used.doubleValue / 1024 / 1024)
    case _ => // no heap info
  }

  def logCpu(nodeMetrics: NodeMetrics): Unit = nodeMetrics match {
    case Cpu(address, timestamp, Some(systemLoadAverage), cpuCombined, processors) => log.info("Load: {} ({} processors)", systemLoadAverage, processors)
    case _ => // no cpu info
  }
}
  
