/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package edu.sorting;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author bourgesl
 */
public class TraceDPQS {

    private final static int M = 100000;
    private final static int N = 1;

    public static void main(String[] args) {

        testRandom(Impl.DPQS_RADIX_2);
        testRandom(Impl.DPQS);
        testRandom(Impl.DPQS_NoAlloc);
    }

    public static void testRandom(Impl impl) {
        final int[] data = new int[M];
        final int[] copy = new int[M];

        for (int i = 0; i < N; ++i) {
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
        };

        abstract void sort(int[] data);
    }
}
