#!/bin/bash

JOBGROUP=UC32tree1440

#Local config for script
SHA_LOCAL=/Users/pmpowers/projects/OpenSHA
DIST_LOCAL=$SHA_LOCAL/dist
LIB_LOCAL=$SHA_LOCAL/lib
TMP_LOCAL=$SHA_LOCAL/tmp/curves
SCRIPT_DIR=$TMP_LOCAL/$JOBGROUP
SCRIPT=$SCRIPT_DIR/UC3curveJob.pbs

# Remote config for jobs
BASEDIR=/home/scec-00/pmpowers
SRCDIR=$BASEDIR/UC3/src
JAVA_LIB=$BASEDIR/lib
SITEFILE=$BASEDIR/UC3/curvejobs/sites/all.txt

#OUTDIR=$BASEDIR/UC3/curves/conv/$JOBGROUP
#FILENAME=/conv/FM3_1_ZENG_Shaw09Mod_DsrTap_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3_mean_sol.zip
#FILENAME=/conv/FM3_1_ZENG_Shaw09Mod_DsrTap_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3_VarZeros_mean_sol.zip
#ERF_COUNT=99

#OUTDIR=$BASEDIR/UC3/curves/tree/$JOBGROUP
#FILENAME=/tree/2012_10_14-fm31-tree-x1-COMPOUND_SOL.zip
#FILENAME=/tree/2012_10_14-fm31-tree-x5-COMPOUND_SOL.zip
#FILENAME=/tree/2012_10_28-fm32-tree-x1-COMPOUND_SOL.zip
#FILENAME=/tree/2012_10_29-fm31-tree-x7-COMPOUND_SOL.zip
#ERF_COUNT=719

OUTDIR=$BASEDIR/UC3/curves/tree/$JOBGROUP
#FILENAME=/tree/2012_10_29-tree-fm31_x7-fm32_x1_COMPOUND_SOL.zip
FILENAME=/tree/2013_01_14-UC32-COMPOUND_SOL.zip
ERF_COUNT=1439

SOLFILE=$SRCDIR$FILENAME
mkdir $SCRIPT_DIR
# build pbs
for i in `seq 0 $ERF_COUNT`
do
    java -cp $DIST_LOCAL/OpenSHA_complete.jar:$LIB_LOCAL/commons-cli-1.2.jar \
    	scratch.peter.ucerf3.scripts.ScriptGenCurves \
    	$SCRIPT $SOLFILE $SITEFILE $i $JAVA_LIB $OUTDIR
    mv $SCRIPT $SCRIPT_DIR/job$i.pbs
    echo "created: $i"
done
