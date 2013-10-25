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
package org.megam.akka.extn

import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import akka.actor.ExtendedActorSystem
import com.typesafe.config.Config

/**
 * @author ram
 *
 */
class SettingsImpl(config: Config) extends Extension {

  val AMQPUri: String = config.getString("app.amqp.uris")
  
  val exchange: String = config.getString("app.amqp.exchange")
  
  val queue: String = config.getString("app.amqp.queue")
 
  val ZooUri: String = config.getString("app.zoo.uris")
  
  //val TotalWorker: Int = config.getInt("app.worker.totalworkers")
  
}

object Settings extends ExtensionId[SettingsImpl] with ExtensionIdProvider {
  
  override def lookup = Settings
  
  override def createExtension(system: ExtendedActorSystem) = new SettingsImpl(system.settings.config) 
  
  
}




