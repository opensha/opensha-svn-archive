#!/bin/bash

JOBGROUP=UC32refBranch

# Local config for script
SHA_LOCAL=/Users/pmpowers/projects/OpenSHA
DIST_LOCAL=/$SHA_LOCAL/dist
LIB_LOCAL=$SHA_LOCAL/lib
TMP_LOCAL=$SHA_LOCAL/tmp/invSolSets
BRANCHLIST=$TMP_LOCAL/$JOBGROUP.txt
SCRIPT_DIR=$TMP_LOCAL/$JOBGROUP
SCRIPT=$SCRIPT_DIR/UC3mapJob.pbs

# Remote config for jobs
BASEDIR=/home/scec-00/pmpowers
JAVADIR=$BASEDIR/lib
SRCDIR=$BASEDIR/UC3/src/tree
OUTDIR=$BASEDIR/UC3/maps/$JOBGROUP

# Calc config
SOL_FILE=$SRCDIR/2013_01_14-UC32-COMPOUND_SOL.zip
GRID='CA_RELM'
SPACING='0.1'
PERIOD='GM0P00'
HRS=3
NODES=16
QUEUE=nbns

mkdir $SCRIPT_DIR
COUNT=0
for BRANCH in $(cat $BRANCHLIST) ; do
    java -cp $DIST_LOCAL/OpenSHA_complete.jar:$LIB_LOCAL/commons-cli-1.2.jar \
    	scratch.peter.ucerf3.scripts.ScriptGenMaps \
    	$QUEUE $NODES $HRS $JAVADIR $SCRIPT \
    	$SOL_FILE $BRANCH $GRID $SPACING $PERIOD $OUTDIR
    mv $SCRIPT $SCRIPT_DIR/job$COUNT.pbs
    echo "created: $COUNT $BRANCH"
    COUNT=$((COUNT+1))
done
