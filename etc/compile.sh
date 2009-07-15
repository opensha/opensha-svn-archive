#!/bin/bash

set -o errexit

ant=${1-"ant"}

$ant -f compile.xml -lib ../lib:../dev/scratch/ISTI/isti.util.jar
exit $?