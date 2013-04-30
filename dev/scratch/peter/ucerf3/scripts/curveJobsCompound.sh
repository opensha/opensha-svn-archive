#!/bin/bash

JOBGROUP=UC33
QUEUE=nbns
NODES=2
HOURS=2
PERIODS=GM0P00,GM0P20,GM1P00

#Local config for script
SHA_LOCAL=/Users/pmpowers/projects/OpenSHA
DIST_LOCAL=$SHA_LOCAL/dist
LIB_LOCAL=$SHA_LOCAL/lib
TMP_LOCAL=$SHA_LOCAL/tmp/curves
SCRIPT=$TMP_LOCAL/$JOBGROUP.pbs

# Remote config for jobs
BASEDIR=/home/scec-00/pmpowers
SRCDIR=$BASEDIR/UC3/src
JAVA_LIB=$BASEDIR/lib
SITEFILE=$BASEDIR/UC3/curvejobs/sites/test.txt
BRANCHFILE=$BASEDIR/UC3/curvejobs/branches/test.txt

# Equation set wewight runs need to be called by branchID
#OUTDIR=$BASEDIR/UC3/curves/vars/$JOBGROUP
#FILENAME=/vars/2013_02_01-ucerf3p2-weights_COMPOUND_SOL.zip
#BRANCHFILE=$BASEDIR/UC3/curvejobs/branches/test.txt

OUTDIR=$BASEDIR/UC3/curves/tree/$JOBGROUP
FILENAME=/tree/2013_01_14-UC32-COMPOUND_SOL.zip
SOLFILE=$SRCDIR$FILENAME

# build pbs
java -cp $DIST_LOCAL/OpenSHA_complete.jar:$LIB_LOCAL/commons-cli-1.2.jar \
	scratch.peter.ucerf3.scripts.CurvesFromCompound \
	$QUEUE $NODES $HOURS $JAVA_LIB \
	$SCRIPT $SOLFILE $SITEFILE $BRANCHFILE $PERIODS $OUTDIR
