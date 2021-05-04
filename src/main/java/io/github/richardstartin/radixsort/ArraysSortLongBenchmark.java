package io.github.richardstartin.radixsort;

import java.util.Arrays;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

@State(Scope.Benchmark)
public class ArraysSortLongBenchmark {

    @Param({"1000000"})
    int size;

    @Param({"17", "23", "30", "47", "63"})
    int bits;

    @Param("0")
    int seed;

    @Param("7")
    int padding;

    @Param
    LongDataScenario scenario;

    private long[] data;
    private long[] copy;

    @Setup(Level.Trial)
    public void setup() {
        byte[] paddingAllocation = new byte[padding];
        data = scenario.generate(size, seed, (1 << bits) - 1);
        paddingAllocation = new byte[padding];
        copy = Arrays.copyOf(data, data.length);
    }

    @TearDown(Level.Invocation)
    public void restore() {
        System.arraycopy(copy, 0, data, 0, data.length);
    }

    @Benchmark
    public long[] arraysSort() {
        Arrays.sort(data);
        return data;
    }

    @Benchmark
    public long[] dpqs21Ref() {
        edu.sorting.ref.Arrays.sort(data);
        return data;
    }

    // parallel
    @Benchmark
    public long[] parallelArraysSort() {
        Arrays.parallelSort(data);
        return data;
    }

    @Benchmark
    public long[] parallelDpqs21Ref() {
        edu.sorting.ref.Arrays.parallelSort(data);
        return data;
    }

}
