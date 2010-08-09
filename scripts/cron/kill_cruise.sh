#!/bin/bash

ps gx | grep java | grep cruisecontrol-launcher.jar | awk '{ print $1 }' | xargs kill
