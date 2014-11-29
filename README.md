==================================================================================================================
This code is DEPRECATED. We built a golang (http://golang.org) based system https://github.com/megamsys/megamd.git.
==================================================================================================================



megam_akka
==========

Cloud instrumentation allows the lifecycle of cloud infrastructure provisioning to be managed, ease repeatable deployments, change the behaviour of instances by injecting behaviour. 

This is needed for automatic provisioning of customers, repeatable infra can be done in a single click. For customers a dynamic model appears where when needed the instances are created on the fly.

A Cloud instrumentation agent, which wraps on top of a messaging layer (AMQP). These run on each of the nodes.

There are two types of "Cloud instrumentation agents". 

* `clo`   : Clo is the master cloud process that is responsible for instrumenting cloud processes.
* `gulp`  : Glup is the slave cloud process that acts as a bridge between the cloud processes and messaging layer.


### Requirements

> 
[RabbitMQ 3.0.4 +](http://www.rabbitmq.com)
[Chef 11.08 +](http://opscode.com)
[OpenJDK 7.0](http://openjdk.java.net/install/index.html)
[Megam Platform]

* chef.megam.co.in  (Chef server) 
* rabbit1.megam.co.in running (RabbitMQ) 
* megam_akka installed 
* Onboarded an Organization (account_name=node_name)
* updated by the customer in Rails App. (or) CLI 


#### Tested on Ubuntu 13.04, AWS - EC2

## Usage




### Prepare your program

Before your run it,



   




We are glad to help if you have questions, or request for new features..

[twitter](http://twitter.com/indykish) [email](<rajthilak@megam.co.in>)

#### For future reading, 
* [megam_api](https:\\github.com\indykish\megam_api.git)
* [docs.megam.co](http:\\docs.megam.co)
* [slideshare - indykish](https:\\slideshare.net\indykish)


#### TO - DO

* Zookeeper

	
# License


|                      |                                          |
|:---------------------|:-----------------------------------------|
| **Author:**          | Rajthilak (<rajthilak@megam.co.in>)
|		       | KishorekumarNeelamegam (<nkishore@megam.co.in>)
| **Copyright:**       | Copyright (c) 2012-2013 Megam Systems.
| **License:**         | Apache License, Version 2.0

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 
