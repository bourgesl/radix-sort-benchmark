#!/bin/bash

source ~/test-jdk-gh-lbo.sh

java -version

PREFIX=`date -I`


JAVA_OPTS="-Xms8g -Xmx8g -verbose:gc -Djava.util.concurrent.ForkJoinPool.common.parallelism=4"
echo "JAVA_OPTS: $JAVA_OPTS"

echo "Running Tests ..." 

java $JAVA_OPTS -cp target/benchmarks.jar edu.sorting.TraceDPQS 2>&1 > $PREFIX-test-17ea.log

