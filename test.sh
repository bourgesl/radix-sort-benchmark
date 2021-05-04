#!/bin/bash

source ~/test-jdk16.sh

java -version

PREFIX=`date -I`

MEM=1
JAVA_OPTS="-Xms${MEM}g -Xmx${MEM}g -verbose:gc -Djava.util.concurrent.ForkJoinPool.common.parallelism=4"
echo "JAVA_OPTS: $JAVA_OPTS"

echo "Running Tests ..." 

java $JAVA_OPTS -cp target/benchmarks.jar edu.sorting.TraceDPQS 2>&1 > $PREFIX-test-16.log

