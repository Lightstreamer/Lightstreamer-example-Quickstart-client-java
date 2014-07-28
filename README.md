# Lightstreamer - Quickstart Example - Java SE Client 
<!-- START DESCRIPTION lightstreamer-example-quickstart-client-java -->

The *Quickstart Example* provides the source code to build a very simple and basic client application which can be used to test the capability of the Client API to connect and receive data from Lightstreamer Server, can be used to familiarize with the Client API and as a reference of how to use them, and can be used as a starting point for a Client application implementation.

This project contains the Java source files of a sample application that shows how the [Lightstreamer Java SE Client API](http://www.lightstreamer.com/docs/client_javase_api/index.html) can be used to connect to Lightstreamer Server.

<!-- END DESCRIPTION lightstreamer-example-quickstart-client-java -->

## Details

![Screenshot](screen_large.png)<br>

Each source is an independent test with its own main() method. Please, refer to the instructions included in each source file in order to configure and run the tests.

### Dig the Code

This example is comprised of the following folders:
* `/src`Contains the sources to build the java examples.
  
* `/lib` Drop here the `ls-client.jar` from the Lighstreamer SDK for Java SE Clients, to be used for the build process and execution.

## Build, Install and Run

If you want to build and install a version of this demo, pointing to your local Lightstreamer Server instance, follow the steps below.

* The *Flex Client Portfolio Demo*, needs both the *PORTFOLIO_ADAPTER*, from the *Portfolio Demo*, and the *QUOTE_ADAPTER*, from the *Stock-List Demo* (see [Lightstreamer - Stock-List Demo - Java Adapter](https://github.com/Weswit/Lightstreamer-example-StockList-adapter-java)). As a prerequisite, the full version of the [Lightstreamer - Portfolio Demo - Java Adapter](https://github.com/Weswit/Lightstreamer-example-Portfolio-adapter-java) has to be deployed on your local Lightstreamer Server instance. Please follow the instruction in [Install the Portfolio Demo](https://github.com/Weswit/Lightstreamer-example-Portfolio-adapter-java#install-the-portfolio-demo) to install it.
* Get the `ls-client.jar` file from `DOCS-SDKs/sdk_client_java_se/lib` folder of the [latest Lightstreamer distribution](http://www.lightstreamer.com/download), and copy it into the `lib` directory of the project.
* Build the `Test.java` class:
```sh
javac -classpath lib/ls-client.jar -d bin src/ls_client/Test.java
```

By the current configuration, the host name and the port number on which the Lightstreamer server is listening have to be specified as arguments on the command line.<br>
A couple of shell/batch files that can be used to run the ls_client example:
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

## See Also 

### Lightstreamer Adapters Needed by This Client 
<!-- START RELATED_ENTRIES -->

* [Lightstreamer - Stock-List Demo - Java Adapter](https://github.com/Weswit/Lightstreamer-example-Stocklist-adapter-java)
* [Lightstreamer - Portfolio Demo - Java Adapter](https://github.com/Weswit/Lightstreamer-example-Portfolio-adapter-java)
* [Lightstreamer - Reusable Metadata Adapters - Java Adapter](https://github.com/Weswit/Lightstreamer-example-ReusableMetadata-adapter-java)

<!-- END RELATED_ENTRIES -->

### Related Projects ##

* [Lightstreamer - Basic Stock-List Demo - Java SE (Swing) Client](https://github.com/Weswit/Lightstreamer-example-StockList-client-java)

## Lightstreamer Compatibility Notes 

- Compatible with Lightstreamer Java SE Client API v. 2.5.2 or newer.
- For Lightstreamer Allegro (+ Java SE Client API), Presto, Vivace.

