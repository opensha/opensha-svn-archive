#!/bin/bash

# Get the aliases and functions
if [ -f ~/.bashrc ]; then
	. ~/.bashrc
fi

dir=`dirname $0`

jar="${dir}/../../dist/OpenSHA_complete.jar"

java -classpath $jar org.opensha.refFaultParamDb.dao.db.PrefFaultSectionDataDB_DAO opensha_cron $1
java -classpath $jar org.opensha.refFaultParamDb.dao.db.DeformationModelPrefDataDB_DAO opensha_cron $1