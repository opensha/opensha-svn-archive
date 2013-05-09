#!/bin/bash

JOBGROUP=UC33compSolTest

# Local config for script
SHA_LOCAL=/Users/pmpowers/projects/OpenSHA
DIST_LOCAL=$SHA_LOCAL/dist
LIB_LOCAL=$SHA_LOCAL/lib
TMP_LOCAL=$SHA_LOCAL/tmp/UC33/mapjobs
BRANCHFILE=$TMP_LOCAL/branches/test.txt
SCRIPT=$TMP_LOCAL/$JOBGROUP.pbs

# Remote config for jobs
BASEDIR=/home/scec-00/pmpowers
JAVADIR=$BASEDIR/lib
SRCDIR=$BASEDIR/UC3/src/tree
OUTDIR=$BASEDIR/UC33/maps/$JOBGROUP

# Calc config
SOL_FILE=$SRCDIR/2013_01_14-UC32-COMPOUND_SOL.zip
GRID='CA_RELM'
SPACING='0.1'
PERIOD='GM0P00'
HRS=3
NODES=36
QUEUE=nbns

java -cp $DIST_LOCAL/OpenSHA_complete.jar:$LIB_LOCAL/commons-cli-1.2.jar \
	scratch.peter.ucerf3.scripts.MapsFromCompound \
	$QUEUE $NODES $HRS $JAVADIR $SCRIPT $BRANCHFILE \
	$SOL_FILE $GRID $SPACING $PERIOD $OUTDIR
	
if [[ $? == 0 ]] ; then
	echo 'PBS script is here:'
	echo $SCRIPT
fi
