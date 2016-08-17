

[![alt text](https://raw.githubusercontent.com/ms123s/simpl4-deployed/master/etc/images/simpl4_logo.png  "simpl4 logo")] (http://www.simpl4.org) building
=================

If you're only interested in a installation, go to [*simpl4-deployed*](https://github.com/ms123s/simpl4-deployed) or [*see here*] (http://web.simpl4.org/repo/webdemo/start.html#links)

##### Open-Source- development environment

simpl4 is licenced under GPLV3 (General Public License), and because of this  it can be used free of any.

##### Flexible responsive applications because of HTML5-Frontend for Smartphones, Tablets and Desktops
Create a application, and use it on +
all popular devices.

##### Quickly create processes,tables, forms ...

* Business processes (with BPMN 2.0 modeler)
* Datamodel (comfortable editor for defining the data model)
* Databasetables (automatically generated, derived from the data model)
* Forms (freely defined or derived from the data model)
* Service models
* Business rules
* Data queries
* Document templates

##### BPMN 2.0 – Business Process Model and Notation

is a graphical representation for specifying business processes. 
The objective of BPMN is to support business process management, 
for both technical users and business users, 
by providing a notation that is intuitive to business users, 
yet able to represent complex process semantics.

##### Business-Process-Engine

Executes the BPMN process models
and orchestrates the service engine and the Human Tasks.

##### Service-Engine

The serviceengine executes service models, that are created in the simpl4 serviceeditor.
In this editor you can join, in a flexible way simpl4 building blocks together.

##### Cloud-ready (PaaS,SaaS)

simpl4 and simpl4 applications fill the cloud layer PaaS and SaaS

##### Integration of existing software systems

##### Development in the browser
##### Minimal Project-Setup
##### Web Application Messaging Protocol (Websocket Subprotocol)

WAMP is an open standard WebSocket subprotocol 
that provides two application messaging patterns.

* Publish/Subscribe (PubSub)
* Remote Procedure Calls (RPC)

##### Git-based simpl4-Application-Store
-




##Building simpl4##

####Requirement
* java jdk1.8.0  or openjdk 8
* git

----

####Cloning this repo
```bash
$ git clone https://github.com/ms123s/simpl4-src.git simpl4-src
```
----

####Going to sourcerepository and start the build
```bash
$ cd simpl4-src
$ gradlew
```
clone *simpl4-deploy*, parallel to *simpl4-src*   directory
Directory arrangment:  
simpl4-src  
simpl4-deploy

and now update the "deploy directory"
```bash
$ cd simpl4-src
$ gradlew deploy 
```
----

####Setup 
```bash
$ cd simpl4-deployed
$ bin/setup.sh -p port
```
----
####Start 
```bash
$ cd simpl4-deployed
$ bin/start.sh start
```
----
####Stop 
```bash
$ cd simpl4-deployed
$ bin/start.sh stop
```
----
####[*Website*](http://www.simpl4.org) and [*Demo-applications*](https://github.com/simpl4-apps?tab=repositories)

