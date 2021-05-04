#!/bin/bash

PREFIX=`date -I`

source ~/test-jdk16.sh
./run-l.sh 2>&1 > $PREFIX-run-16.log

tail -60 $PREFIX-run-16.log > $PREFIX-cmp-16.log

