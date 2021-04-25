#!/bin/bash

PREFIX=`date -I`

source ~/test-jdk11u.sh
./run.sh 2>&1 > $PREFIX-run-11.log

source ~/test-jdk16.sh
./run.sh 2>&1 > $PREFIX-run-16.log

tail -60 $PREFIX-run-11.log > $PREFIX-cmp-11.log
tail -60 $PREFIX-run-16.log > $PREFIX-cmp-16.log

