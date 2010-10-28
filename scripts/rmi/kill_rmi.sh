#!/bin/bash

echo "killing RMI!"

ps gx | grep 'Djava.rmi.server.hostname' | grep -v grep | awk '{ print $1 }' | xargs kill
sleep 2
pids=`ps gx | grep 'Djava.rmi.server.hostname' | grep -v grep | awk '{ print $1 }'`
for pid in $pids
do
	echo "killing -9: $pid"
	kill -9 $pid
done
