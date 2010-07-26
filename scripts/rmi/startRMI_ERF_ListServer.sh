#!/bin/bash

mainDir="/usr/local/tomcat/default/webapps/OpenSHA/WEB-INF"
java="/usr/java/default/bin/java"
#java="/usr/java/jdk1.6.0_10/jre/bin/java"

$java -Xmx500M -classpath ${mainDir}/dist/OpenSHA_complete.jar -Djava.rmi.server.codebase=file:${mainDir}/dist/OpenSHA_complete.jar -Djava.security.policy=file:$mainDir/SimpleRMI.policy -Djava.rmi.server.hostname=opensha.usc.edu org.opensha.sha.earthquake.rupForecastImpl.remote.RegisterRemoteERF_ListFactory &
