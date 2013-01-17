#!/bin/bash

JOBGROUP=UC32tree1440

# Local config for script
SHA_LOCAL=/Users/pmpowers/projects/OpenSHA

# Remote config for jobs
BASEDIR=/home/scec-00/pmpowers
JAVA_LIB=$BASEDIR/lib
SRCDIR=$BASEDIR/UC3/src/tree/
OUTDIR=$BASEDIR/UC3/maps/$DIRNAME

# Calc config
SOL_FILE=2013_01_14-UC32-COMPOUND_SOL.zip
GRIDS='CA_RELM'
SPACING='0.1'
PERIODS='GM0P00'
HRS=1
NODES=48
QUEUE=nbns

mkdir $JOBGROUP
COUNT=0
for BRANCH in $(cat $BRANCHLIST.txt) ; do
    java -cp $JAVA_LIB/OpenSHA_complete.jar:$JAVA_LIB/commons-cli-1.2.jar \
    	dev.scratch.peter.ucerf3.scripts.ScriptGenMaps \
    	$QUEUE $NODES $HRS $JAVA_LIB \
    	$SOL_FILE $BRANCH $GRID $SPACING $PERIOD $OUTDIR
    mv UC3mapJob.pbs $JOBGROUP/job$i.pbs
    echo "created: $COUNT $BRANCH"
    COUNT=$((COUNT+1))
done
