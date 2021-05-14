package io.github.richardstartin.radixsort;

import edu.sorting.DualPivotQuickSort2011;
import edu.sorting.DualPivotQuicksort20210424;
import edu.sorting.DualPivotQuicksort202105;
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

    private final static boolean ALLOC_BUFFER = true;

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

    /* The obvious approach to find minimum (involves branching) */
    static int minCond(int x, int y) {
        return (x < y) ? x : y;
    }

    static int maxCond(int x, int y) {
        return (x > y) ? x : y;
    }

    /*Function to find minimum of x and y*/
    static int minShift(int x, int y) {
        return y + ((x - y) & ((x - y) >> 31));
    }

    /*Function to find maximum of x and y*/
    static int maxShift(int x, int y) {
        return x - ((x - y) & ((x - y) >> 31));
    }

    /*  @Benchmark */
    public int[] min_max_cond() {
        int min = data[0], max = min;

        for (int i = 1; i < data.length; i++) {
            min = minCond(min, data[i]);
            max = maxCond(max, data[i]);
        }
        data[0] = min;
        data[1] = max;
        return data;
    }

    /*  @Benchmark */
    public int[] min_max_shift() {
        int min = data[0], max = min;

        for (int i = 1; i < data.length; i++) {
            min = minShift(min, data[i]);
            max = maxShift(max, data[i]);
        }
        data[0] = min;
        data[1] = max;
        return data;
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

    @Benchmark
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

    @Benchmark
    public int[] dpqs2105New() {
        DualPivotQuicksort202105.sortStd(data);
        return data;
    }

    @Benchmark
    public int[] dpqs2105Radix() {
        DualPivotQuicksort202105.sortRadix(data);
        return data;
    }

    @Benchmark
    public int[] dpqs2105RadixNew() {
        DualPivotQuicksort202105.sortRadixNew(data);
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
    /* @Benchmark */
    public int[] parallelArraysSort() {
        Arrays.parallelSort(data);
        return data;
    }

    /* @Benchmark */
    public int[] parallelDpqs2105Ref() {
        edu.sorting.ref.Arrays.parallelSort(data);
        return data;
    }

}
