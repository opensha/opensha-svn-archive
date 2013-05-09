#!/bin/bash

JOBGROUP=UC33compoundTest
QUEUE=nbns
NODES=2
HOURS=2
PERIODS=GM0P00,GM0P20,GM1P00

#Local config for script
SHA_LOCAL=/Users/pmpowers/projects/OpenSHA
DIST_LOCAL=$SHA_LOCAL/dist
LIB_LOCAL=$SHA_LOCAL/lib
TMP_LOCAL=$SHA_LOCAL/tmp/UC33/curvejobs
SCRIPT=$TMP_LOCAL/$JOBGROUP.pbs

# Remote config for jobs
BASEDIR=/home/scec-00/pmpowers
SRCDIR=$BASEDIR/UC33/src
JAVA_LIB=$BASEDIR/lib
JOBDIR=$BASEDIR/UC33/curvejobs
SITEFILE=$JOBDIR/sites/test.txt
BRANCHFILE=$JOBDIR/branches/test.txt

# Equation set wewight runs need to be called by branchID
#OUTDIR=$BASEDIR/UC3/curves/vars/$JOBGROUP
#FILENAME=/vars/2013_02_01-ucerf3p2-weights_COMPOUND_SOL.zip
#BRANCHFILE=$BASEDIR/UC3/curvejobs/branches/test.txt

# !!!!! UPDATE FILENAME FOR ACUTAL UC33 RUNS !!!!!
OUTDIR=$BASEDIR/UC33/curves/tree/$JOBGROUP
FILENAME=/tree/2013_01_14-UC32-COMPOUND_SOL.zip
SOLFILE=/home/scec-00/pmpowers/UC3/src$FILENAME

# build pbs
java -cp $DIST_LOCAL/OpenSHA_complete.jar:$LIB_LOCAL/commons-cli-1.2.jar \
	scratch.peter.ucerf3.scripts.CurvesFromCompound \
	$QUEUE $NODES $HOURS $JAVA_LIB \
	$SCRIPT $SOLFILE $SITEFILE $BRANCHFILE $PERIODS $OUTDIR

if [[ $? == 0 ]] ; then
	echo 'PBS script is here:'
	echo $SCRIPT
fi
	