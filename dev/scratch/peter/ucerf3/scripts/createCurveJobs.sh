#!/bin/bash
DIRNAME=AFconvTestNoBG
BASEDIR=/home/scec-00/pmpowers
SRCDIR=$BASEDIR/UC3/src
JAVA_LIB=$BASEDIR/lib
SITEFILE=$BASEDIR/UC3/jobs/sites/AFsites.txt

OUTDIR=$BASEDIR/UC3/curves/conv/$DIRNAME
FILENAME=/conv/FM3_1_ZENG_Shaw09Mod_DsrTap_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3_mean_sol.zip
# FILENAME=/conv/FM3_1_ZENG_Shaw09Mod_DsrTap_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3_VarZeros_mean_sol.zip
ERF_COUNT=99

#OUTDIR=$BASEDIR/UC3/curves/tree/$DIRNAME
#FILENAME=/tree/2012_10_14-fm31-tree-x1-COMPOUND_SOL.zip
#FILENAME=/tree/2012_10_14-fm31-tree-x5-COMPOUND_SOL.zip
#FILENAME=/tree/2012_10_28-fm32-tree-x1-COMPOUND_SOL.zip
#FILENAME=/tree/2012_10_29-fm31-tree-x7-COMPOUND_SOL.zip
#ERF_COUNT=719

#OUTDIR=$BASEDIR/UC3/curves/tree/$DIRNAME
#FILENAME=/tree/2012_10_29-tree-fm31_x7-fm32_x1_COMPOUND_SOL.zip
#FILENAME=/tree/2013_01_14-UC32-COMPOUND_SOL.zip
#ERF_COUNT=1439

FILEPATH=$SRCDIR$FILENAME
mkdir $DIRNAME
# build pbs
for i in `seq 0 $ERF_COUNT`
do
    java -cp /Users/pmpowers/projects/OpenSHA/dist/OpenSHA_complete.jar:/Users/pmpowers/projects/OpenSHA/lib/commons-cli-1.2.jar org.opensha.nshmp2.calc.ScriptGenUC3 $FILEPATH $SITEFILE $i $JAVA_LIB $OUTDIR
    mv UC3job.pbs $DIRNAME/job$i.pbs
    echo "created: $i"
done
