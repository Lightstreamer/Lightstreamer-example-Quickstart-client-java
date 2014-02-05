# Lightstreamer - Quickstart Example - Java SE Client #
<!-- START DESCRIPTION lightstreamer-example-quickstart-client-java -->

This project contains Java example source files that show how the [Lightstreamer Java SE Client API](http://www.lightstreamer.com/docs/client_javase_api/index.html) can be used to connect to Lightstreamer Server.

![Screenshot](screen_large.png)<br>

Each source is an independent test with its own main() method. Please, refer to the instructions included in each source file in order to configure and run the tests.

<!-- END DESCRIPTION lightstreamer-example-quickstart-client-java -->


# Build #

This example is comprised of the following folders:
* /src<br>
  Contains the sources to build the java examples.
  
* /lib<br>
  Drop here the ls-client.jar from the Lighstreamer SDK for Java SE Clients, to be used for the build process and execution.
  
Example of build commands (refered to ls_client):
```sh
javac -classpath lib/ls-client.jar -d bin src/ls_client/Test.java
```

# Deploy #
  
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

The samples are suitable for running with the [QUOTE_ADAPTER](https://github.com/Weswit/Lightstreamer-example-Stocklist-adapter-java) and [PORTFOLIO_ADAPTER](https://github.com/Weswit/Lightstreamer-example-Portfolio-adapter-java) Data Adapters.

# See Also #

## Lightstreamer Adapters Needed by This Demo Client ##
<!-- START RELATED_ENTRIES -->

* [Lightstreamer - Stock-List Demo - Java Adapter](https://github.com/Weswit/Lightstreamer-example-Stocklist-adapter-java)
* [Lightstreamer - Portfolio Demo - Java Adapter](https://github.com/Weswit/Lightstreamer-example-Portfolio-adapter-java)
* [Lightstreamer - Reusable Metadata Adapters - Java Adapter](https://github.com/Weswit/Lightstreamer-example-ReusableMetadata-adapter-java)

<!-- END RELATED_ENTRIES -->

## Related Projects ##

* [Lightstreamer - Basic Stock-List Demo - Java SE (Swing) Client](https://github.com/Weswit/Lightstreamer-example-StockList-client-java)

# Lightstreamer Compatibility Notes #

- Compatible with Lightstreamer Java SE Client API v. 2.5.2 or newer.
- For Lightstreamer Allegro (+ Java SE Client API), Presto, Vivace.

