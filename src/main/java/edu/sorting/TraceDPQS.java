/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package edu.sorting;

import io.github.richardstartin.radixsort.DataScenario;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author bourgesl
 */
public class TraceDPQS {

    private final static int M = 100000;
    private final static int N = 1;

    private final static int bits = 23;

    public static void main(String[] args) {
        if (false) {
            testRandom(Impl.DPQS_11);
            // testRandom(Impl.DPQS_RADIX_2);
            testRandom(Impl.DPQS);
            // testRandom(Impl.DPQS_NoAlloc);
            test(DataScenario.ALMOST_CONTIGUOUS, Impl.DPQS_11);
        }
        
        test(DataScenario.SORTED, Impl.DPQS);
        
        for (DataScenario data : DataScenario.values()) {
            test(data, Impl.DPQS);
        }
    }

    public static void test(DataScenario scenario, Impl impl) {
        final int[] data = scenario.generate(M, 0, (1 << bits) - 1);
        final int[] copy = Arrays.copyOf(data, M);

        if (true) {
            System.out.println("data[0-512]: " + Arrays.toString(Arrays.copyOfRange(data, 0, 512)));
            System.out.println("data[-100:0]: " + Arrays.toString(Arrays.copyOfRange(data, M - 100, M)));
        }

        System.out.println("Test[" + scenario + " | " + impl + "] M=" + M + "----------------------------------------------------------");
        System.arraycopy(data, 0, copy, 0, copy.length);
        impl.sort(data);
        Arrays.sort(copy);

        if (!Arrays.equals(data, copy)) {
            for (int j = 0; j < M; j++) {
                if (data[j] != copy[j]) {
                    System.out.println("Mismatch[" + j + "]: " + data[j] + " != " + copy[j]);
                }
            }
            System.out.flush();
            throw new IllegalStateException("Bad sort : " + impl);
        }
    }

    public static void testRandom(Impl impl) {
        final int[] data = new int[M];
        final int[] copy = new int[M];

        System.out.println("Test[" + impl + "] M=" + M + "----------------------------------------------------------");
        fillRandom(data);
        System.arraycopy(data, 0, copy, 0, copy.length);
        impl.sort(data);
        Arrays.sort(copy);

        if (!Arrays.equals(data, copy)) {
            for (int j = 0; j < M; j++) {
                if (data[j] != copy[j]) {
                    System.out.println("Mismatch[" + j + "]: " + data[j] + " != " + copy[j]);
                }
            }
            System.out.flush();
            throw new IllegalStateException("Bad sort : " + impl);
        }
    }

    private static void fillRandom(int[] data) {
        for (int i = 0; i < data.length; ++i) {
            // data[i] = ThreadLocalRandom.current().nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
            data[i] = ThreadLocalRandom.current().nextInt(0, data.length);
        }
    }

    enum Impl {

        DPQS_RADIX_1 {
            @Override
            void sort(int[] data) {
                DualPivotQuicksort20210424.sortRadix(data);
            }
        },
        DPQS_RADIX_2 {
            @Override
            void sort(int[] data) {
                DualPivotQuicksort20210424.sortRadix2(data);
            }
        },
        DPQS {
            @Override
            void sort(int[] data) {
                DualPivotQuicksort20210424.sortStd(data);
            }
        },
        DPQS_NoAlloc {
            @Override
            void sort(int[] data) {
                DualPivotQuicksort20210424.sortNoAlloc(data);
            }
        },
        DPQS_11 {
            @Override
            void sort(int[] data) {
                DualPivotQuickSort2011.sortNoAlloc(data);
            }
        };

        abstract void sort(int[] data);
    }
}
