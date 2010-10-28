#!/bin/bash

echo "starting RMI!"

webappsDir="/usr/local/tomcat/default/webapps"
prodDir="${webappsDir}/OpenSHA/WEB-INF"
devDir="${webappsDir}/OpenSHA_dev/WEB-INF"
cd $prodDir
# this command is removed now, as registry is created in java
#rmiregistry & now this is done in java
sleep 2
$prodDir/scripts/rmi/startRMIServer.sh &
$prodDir/scripts/rmi/startRMI_ERF_ListServer.sh &
cd $devDir
sleep 2
$devDir/scripts/rmi/startRMIServer.sh &
$devDir/scripts/rmi/startRMI_ERF_ListServer.sh &
