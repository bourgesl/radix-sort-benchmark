#!/bin/bash

java -version

#SIZES="1000"
SIZES="10000"
#SIZES="100000"
#SIZES="1000000"
#SIZES="100000000"

# TODO: longer runs for larger arrays: 100000000

# do not force GC as setupTrial does cleanup() and sorters use pre-allocation
GC=false
FORK=1

# min iter = 10 (to sample all distributions)
WITER=3
WTIME=1s
ITER=5
TIME=1s

OPTS="-p size=$SIZES -p bits=23"
# bits=17,23,30

echo "OPTS: $OPTS"


JAVA_OPTS="-Xms256m -Xmx256m"
echo "JAVA_OPTS: $JAVA_OPTS"

#FORK_OPTS="-Xms4g -Xmx4g -XX:+UseParallelGC -XX:-BackgroundCompilation"
FORK_OPTS="-Xms1g -Xmx1g -XX:+UseParallelGC -XX:-BackgroundCompilation"
echo "FORK_OPTS: $FORK_OPTS"

echo "Running JMH ..." 

# show help
#java -jar target/benchmarks.jar -h

# show benchmarks & parameters
#java -jar target/benchmarks.jar -lp

# single-threaded:
java $JAVA_OPTS -jar target/benchmarks.jar -gc $GC -wf $FORK -wi $WITER -w $WTIME -wbs 2 -i $ITER -r $TIME -f $FORK -t 1 $OPTS -jvmArgs "$FORK_OPTS"
# 1> "sort-$SIZES.log" 2> "sort-$SIZES.err" 

