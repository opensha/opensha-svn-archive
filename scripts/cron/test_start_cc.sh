#!/bin/bash

if [ -f ~/.bashrc ]; then
	. ~/.bashrc
fi

pids=`ps gx | grep java | grep cruisecontrol-launcher.jar | awk '{ print $1 }'`
if [[ $pids ]];then
	echo "cruise control is already running! proc(s): $pids"
	exit
fi
cd /usr/local/cruise/config/opensha
/usr/local/cruise/default/main/bin/cruisecontrol.sh -dashboardurl http://localhost:8080/dashboard -configfile /usr/local/cruise/config/opensha/config.xml
