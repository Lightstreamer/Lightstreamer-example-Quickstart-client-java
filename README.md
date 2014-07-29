# Lightstreamer - Quickstart Example - Java SE Client 
<!-- START DESCRIPTION lightstreamer-example-quickstart-client-java -->

The *Quickstart Example* provides the source code to build very simple and basic client applications, used to test the capability of a Client API to connect and receive data from Lightstreamer Server. The examples can be used to familiarize with the Client APIs and as a reference on how to use them, and can be used as a starting point for client application implementations.

This project contains the Java source files of a sample application, that shows how the [Lightstreamer Java SE Client API](http://www.lightstreamer.com/docs/client_javase_api/index.html) can be used to connect to Lightstreamer Server.

<!-- END DESCRIPTION lightstreamer-example-quickstart-client-java -->

## Details

The [Lightstreamer Java SE Client API](http://www.lightstreamer.com/docs/client_javase_api/index.html) is made up of two layers: 
* a basic layer, with package name `com.lightstreamer.ls_client`, which exposes a simple interface to communicate with Lightstreamer Server, by opening and closing a connection, performing subscriptions and unsubscriptions and receiving data;
* an advanced layer, with package name `com.lightstreamer.ls_proxy`, which builds upon the basic layer and exposes a "data oriented" interface, in which the access to Lightstreamer Server is hidden and optimized.

### Dig the Code

The project contains two folders: 
* `/ls_client`, containing source code to test the `com.lightstreamer.ls_client` package;
* `/ls_proxy`, containing source code to test the `com.lightstreamer.ls_proxy` package, and to demonstrate advanced Server access support, through LSProxy facade;
 
The `/ls_client` contains the following source class.
* `Test.java` A simple client, which opens a connection, performs a table subscription and unsubscription and closes the connection after some time.

The `/ls_proxy` contains the following source classes.
* `Test.java`: shows subscriptions and unsubscriptions of interleaving sets of items with interleaving sets of fields, and demonstrates also the recovery features offered, if Lightstreamer Server is shut down and restarted during the test.
* `LoadTest.java`: shows impacts of connections/disconnections on pushing activity and the impact of transactions on subscription/unsubscription management, and shows also the impact of refused subscription requests on Server, and Client Library behaviour.

Each source class is an independent test with its own main() method. 
They basically connect to the server and perform a subscription, printing on the console the incoming Item Updates.

![Screenshot](screen_large.png)


## Build

To build and install a version of this demo, pointing to your local Lightstreamer Server instance, follow the steps below.

* The *Quickstart Example*, needs both the *PORTFOLIO_ADAPTER* ( see the [Lightstreamer - Portfolio Demo - Java Adapter](https://github.com/Weswit/Lightstreamer-example-Portfolio-adapter-java)), and the *QUOTE_ADAPTER* (see the [Lightstreamer - Stock-List Demo - Java Adapter](https://github.com/Weswit/Lightstreamer-example-StockList-adapter-java)). Therefore, as a prerequisite, the full version of the [Lightstreamer - Portfolio Demo - Java Adapter](https://github.com/Weswit/Lightstreamer-example-Portfolio-adapter-java) has to be deployed on your local Lightstreamer Server instance. Please follow the instruction in [Install the Portfolio Demo](https://github.com/Weswit/Lightstreamer-example-Portfolio-adapter-java#install-the-portfolio-demo) to install it.
* Get the `ls-client.jar` file from `DOCS-SDKs/sdk_client_java_se/lib` folder of the [latest Lightstreamer distribution](http://www.lightstreamer.com/download), and copy it into the `lib` directory of the project.
* Build the `Test.java` class:
```sh
javac -classpath lib/ls-client.jar -d bin src/ls_client/Test.java
```
* Run the test with a command like:<BR/>
`> java -classpath lib/ls-client.jar ls_client.Test localhost 8080`<BR/>
specifying, as arguments on the command line, the host name and the port number, on which the Lightstreamer server is listening.

*Please, refer to the instructions included in each source file for more details on how to configure and run the tests.*


A couple of shell/batch files that can be useful to run the ls_client example:
* batch command:

```cmd
@echo off

set JAVA_HOME=C:\Program Files\Java\jdk1.7.0
set CONF=localhost 8080
set DEMO_HOME=C:\Lightstreamer\Dev\

call "%JAVA_HOME%\bin\java.exe" -classpath %DEMO_HOME%bin\;%DEMO_HOME%lib\ls-client.jar ls_client.Test %CONF%
pause
```

* shell command:

```sh
#! /bin/sh

JAVA_HOME=/usr/jdk1.7.0
CONF="localhost 8080"
DEMO_HOME=/Lightstreamer/Dev

exec $JAVA_HOME/bin/java -classpath $DEMO_HOME/bin;$DEMO_HOME/lib/ls-client.jar ls_client.Test %CONF% $CONF
```

* If you want to connect to the [Online Demo Lightstreamer Server](http://push.lightstreamer.com/) instead of your local server, you have to change the Adapter Set to connect to. Edit the .java class and replace:<BR/>
`this.adapter = "FULLPORTFOLIODEMO";`<BR/>
with<BR/>
`this.adapter = "DEMO";`


## See Also 

### Lightstreamer Adapters Needed by This Client 
<!-- START RELATED_ENTRIES -->

* [Lightstreamer - Stock-List Demo - Java Adapter](https://github.com/Weswit/Lightstreamer-example-Stocklist-adapter-java)
* [Lightstreamer - Portfolio Demo - Java Adapter](https://github.com/Weswit/Lightstreamer-example-Portfolio-adapter-java)
* [Lightstreamer - Reusable Metadata Adapters - Java Adapter](https://github.com/Weswit/Lightstreamer-example-ReusableMetadata-adapter-java)

<!-- END RELATED_ENTRIES -->

### Related Projects

* [Lightstreamer - Basic Stock-List Demo - Java SE (Swing) Client](https://github.com/Weswit/Lightstreamer-example-StockList-client-java)

## Lightstreamer Compatibility Notes 

- Compatible with Lightstreamer Java SE Client API v. 2.5.2 or newer.
- For Lightstreamer Allegro (+ Java SE Client API), Presto, Vivace.

