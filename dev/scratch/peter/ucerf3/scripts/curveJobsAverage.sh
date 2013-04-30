#!/bin/bash

JOBGROUP=UC33conv
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

# Convergence runs need to be called by index
OUTDIR=$BASEDIR/UC3/curves/conv/$JOBGROUP
FILENAME=/conv/FM3_1_ZENGBB_Shaw09Mod_DsrTap_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3_mean_sol.zip
SOLFILE=$SRCDIR$FILENAME
ERF_COUNT=100

# build pbs
java -cp $DIST_LOCAL/OpenSHA_complete.jar:$LIB_LOCAL/commons-cli-1.2.jar \
	scratch.peter.ucerf3.scripts.CurvesFromAverage \
	$QUEUE $NODES $HOURS $JAVA_LIB \
	$SCRIPT $SOLFILE $SITEFILE $ERF_COUNT $PERIODS $OUTDIR
