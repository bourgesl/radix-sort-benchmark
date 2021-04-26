package io.github.richardstartin.radixsort;

import edu.sorting.DualPivotQuickSort2011;
import edu.sorting.DualPivotQuicksort20210424;
import java.util.Arrays;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

@State(Scope.Benchmark)
public class RadixSortBenchmark {

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
        data = scenario.generate(size, seed, (1 << bits) - 1);
        byte[] paddingAllocation = new byte[padding];
        copy = Arrays.copyOf(data, data.length);
        paddingAllocation = new byte[padding];
        buffer = new int[size];
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
    public int[] dpqs11Sort() {
        DualPivotQuickSort2011.sortNoAlloc(data);
        return data;
    }

    /* @Benchmark */
    public int[] dpqs21_04Sort() {
        DualPivotQuicksort20210424.sortStd(data);
        return data;
    }

    @Benchmark
    public int[] dpqs21_04NoAllocSort() {
        DualPivotQuicksort20210424.sortNoAlloc(data);
        return data;
    }

    @Benchmark
    public int[] dpqs21_04RadixSort() {
        DualPivotQuicksort20210424.sortRadix(data);
        return data;
    }

    @Benchmark
    public int[] dpqs21_04RadixSort2() {
        DualPivotQuicksort20210424.sortRadix2(data);
        return data;
    }

}
