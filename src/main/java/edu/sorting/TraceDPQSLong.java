/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package edu.sorting;

import io.github.richardstartin.radixsort.LongDataScenario;
import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author bourgesl
 */
public class TraceDPQSLong {

    private final static int M = 1000000;
    private final static int N = 100;

    private final static int bits = 47;

    public static void main(String[] args) {

        System.out.println("ForkJoinPool.getCommonPoolParallelism(): " + ForkJoinPool.getCommonPoolParallelism());

        for (LongDataScenario data : LongDataScenario.values()) {
            test(data, Impl.P_DPQS_2105);
        }

        for (LongDataScenario data : LongDataScenario.values()) {
            test(data, Impl.DPQS_2105);
        }
    }

    public static void test(LongDataScenario scenario, Impl impl) {
        for (int i = 0; i < N; i++) {
            final long[] data = scenario.generate(M, 0, (1L << bits) - 1L);
            final long[] copy = Arrays.copyOf(data, M);

            System.out.println("Test[" + scenario + " | " + impl + "] M=" + M + "----------------------------------------------------------");

            if (true) {
                System.out.println("data[0-512]: " + Arrays.toString(Arrays.copyOfRange(data, 0, 512)));
                System.out.println("data[-100:0]: " + Arrays.toString(Arrays.copyOfRange(data, M - 100, M)));
            }

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
    }

    public static void testRandom(Impl impl) {
        final long[] data = new long[M];
        final long[] copy = new long[M];

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

    private static void fillRandom(long[] data) {
        for (int i = 0; i < data.length; ++i) {
            // data[i] = ThreadLocalRandom.current().nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
            data[i] = ThreadLocalRandom.current().nextInt(0, data.length);
        }
    }

    enum Impl {
        DPQS_2105 {
            @Override
            void sort(long[] data) {
                edu.sorting.ref.Arrays.sort(data);
            }
        },
        P_DPQS_2105 {
            @Override
            void sort(long[] data) {
                edu.sorting.ref.Arrays.parallelSort(data);
            }
        };

        abstract void sort(long[] data);
    }
}
