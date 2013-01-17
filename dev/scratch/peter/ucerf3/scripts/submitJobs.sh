#!/bin/bash
DIR=/home/scec-00/pmpowers/pathToJobDirectory
for JOB in $DIR/*.pbs ; do
#    qsub $JOB
    echo "submitted: $JOB"
done
