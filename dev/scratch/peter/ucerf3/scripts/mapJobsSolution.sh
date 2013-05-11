#!/bin/bash

JOBGROUP=UC32brAvg5x_fm31_nobg

# Local config for script
SHA_LOCAL=/Users/pmpowers/projects/OpenSHA
DIST_LOCAL=$SHA_LOCAL/dist
LIB_LOCAL=$SHA_LOCAL/lib
TMP_LOCAL=$SHA_LOCAL/tmp/UC33/mapjobs
SCRIPT=$TMP_LOCAL/$JOBGROUP.pbs

# Remote config for jobs
BASEDIR=/home/scec-00/pmpowers
JAVADIR=$BASEDIR/lib
SRCDIR=$BASEDIR/UC33/src/bravg
OUTDIR=$BASEDIR/UC33/maps/$JOBGROUP

# Calc config
SOL_FILE=$SRCDIR/2013_01_14-UC32-MEAN_BRANCH_AVG_SOL_FM31.zip
#SOL_FILE=$SRCDIR/2013_05_03-ucerf3p3-production-first-five_MEAN_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip
#SOL_FILE=$SRCDIR/2013_05_01-ucerf3p3-proposed-subset-hpcc-salmonfix_COMPOUND_SOL_MEAN_BRANCH_AVG_SOL.zip
GRID='CA_RELM'
SPACING='0.1'
PERIOD='GM0P00'
HRS=1
NODES=84
QUEUE=nbns
BG=INCLUDE

java -cp $DIST_LOCAL/OpenSHA_complete.jar:$LIB_LOCAL/commons-cli-1.2.jar \
	scratch.peter.ucerf3.scripts.MapsFromSolution \
	$QUEUE $NODES $HRS $JAVADIR $SCRIPT \
	$SOL_FILE $GRID $SPACING $PERIOD $BG $OUTDIR

if [[ $? == 0 ]] ; then
	echo 'PBS script is here:'
	echo $SCRIPT
fi
	
