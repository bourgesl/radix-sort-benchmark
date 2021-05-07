package io.github.richardstartin.radixsort;

import edu.sorting.DualPivotQuickSort2011;
import edu.sorting.DualPivotQuicksort20210424;
import edu.sorting.DualPivotQuicksort202105LowMem;
import java.util.Arrays;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

@State(Scope.Benchmark)
public class ArraysSortIntBenchmark {

    private final static boolean ALLOC_BUFFER = false;

    @Param({"1000000"})
    int size;

    @Param({"17", "23", "30"})
    int bits;

    @Param("0")
    int seed;

    @Param("7")
    int padding;

    @Param
    DataScenario scenario;

    private int[] data;
    private int[] copy;
    private int[] buffer;

    @Setup(Level.Trial)
    public void setup() {
        byte[] paddingAllocation = new byte[padding];
        data = scenario.generate(size, seed, (1 << bits) - 1);
        paddingAllocation = new byte[padding];
        copy = Arrays.copyOf(data, data.length);
        paddingAllocation = new byte[padding];
        buffer = (ALLOC_BUFFER) ? new int[size] : null;
    }

    @TearDown(Level.Invocation)
    public void restore() {
        System.arraycopy(copy, 0, data, 0, data.length);
    }


    /*  @Benchmark */
    public int[] basic() {
        RadixSort.basic(data);
        return data;
    }

    /*  @Benchmark */
    public int[] basicBuffer() {
        RadixSort.basic(data, buffer);
        return data;
    }

    /*  @Benchmark */
    public int[] unroll() {
        RadixSort.unroll(data);
        return data;
    }

    /*  @Benchmark */
    public int[] unrollBuffer() {
        RadixSort.unroll(data, buffer);
        return data;
    }


    /*  @Benchmark */
    public int[] unrollOnePass() {
        RadixSort.unrollOnePassHistograms(data);
        return data;
    }

    /*  @Benchmark */
    public int[] unrollOnePassBuffer() {
        RadixSort.unrollOnePassHistograms(data, buffer);
        return data;
    }

    /*  @Benchmark */
    public int[] unrollOnePassSkipLevels() {
        RadixSort.unrollOnePassHistogramsSkipLevels(data);
        return data;
    }

    /*  @Benchmark */
    public int[] unrollOnePassSkipLevelsBuffer() {
        RadixSort.unrollOnePassHistogramsSkipLevels(data, buffer);
        return data;
    }

    /*  @Benchmark */
    public int[] unrollOnePassSkipLevelsWithDetection() {
        RadixSort.unrollOnePassHistogramsSkipLevelsWithDetection(data);
        return data;
    }

    /* @Benchmark */
    public int[] unrollOnePassSkipLevelsBufferWithDetection() {
        RadixSort.unrollOnePassHistogramsSkipLevels(data, buffer);
        return data;
    }

    @Benchmark
    public int[] arraysSort() {
        Arrays.sort(data);
        return data;
    }

    @Benchmark
    public int[] dpqs2105Ref() {
        edu.sorting.ref.Arrays.sort(data);
        return data;
    }

    /* @Benchmark */
    public int[] dpqs21LowMem() {
        DualPivotQuicksort202105LowMem.sortNoAlloc(data);
        return data;
    }
    
    /* @Benchmark */
    public int[] dpqs11Sort() {
        DualPivotQuickSort2011.sortNoAlloc(data);
        return data;
    }

    /* @Benchmark */
    public int[] dpqs2104Sort() {
        DualPivotQuicksort20210424.sortStd(data);
        return data;
    }

    /* @Benchmark */
    public int[] dpqs2104NoAllocSort() {
        DualPivotQuicksort20210424.sortNoAlloc(data);
        return data;
    }

    /* @Benchmark */
    public int[] dpqs2104RadixSort() {
        DualPivotQuicksort20210424.sortRadix(data);
        return data;
    }

    // parallel
    @Benchmark
    public int[] parallelArraysSort() {
        Arrays.parallelSort(data);
        return data;
    }

    @Benchmark
    public int[] parallelDpqs2105Ref() {
        edu.sorting.ref.Arrays.parallelSort(data);
        return data;
    }

}
