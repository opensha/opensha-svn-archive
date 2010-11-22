#!/bin/bash

# clean operational log files older than 14 days
find /usr/local/cruise/main/logs/OpenSHA-operational/ -ctime +14 -exec rm {} \;

# clean nightly log files older than 30 days
find /usr/local/cruise/main/logs/OpenSHA-nightly/ -ctime +30 -exec rm {} \;

# clean continuous log files older than 14 days
find /usr/local/cruise/main/logs/OpenSHA-continuous/ -ctime +14 -exec rm {} \;
