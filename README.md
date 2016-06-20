

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
* Integration rules
* Business rules
* Data queries
* Document templates

##### BPMN 2.0 – Business Process Model and Notation

ist eine grafische Spezifikationssprache.
Sie stellt Symbole zur Verfügung, mit denen Fach- und Informatikspezialisten
Geschäftsprozesse und Arbeitsabläufe modellieren und dokumentieren können.

##### Business-Process-Engine

Führt die in BPMN2.0 definierten Prozessmodelle aus 
und orchestriert die Integrationengine sowie die Humantasks

##### Rule based Integration-Engine

The integration engine connects in a flexible manner simpl4 building blocks together.
The integration rules are defined in an visual editor.

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

