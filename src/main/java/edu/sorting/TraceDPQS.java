/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package edu.sorting;

import io.github.richardstartin.radixsort.DataScenario;
import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;

/**
 *
 * @author bourgesl
 */
public class TraceDPQS {

    private final static int M = (1 << 29); // 512 10^6 ints = 2Gb (per array)
    private final static int N = 1;

    private final static int bits = 31;

    private final static boolean TRACE_TIME = true;
    private final static boolean TEST_PARALLEL = true;

    public static void main(String[] args) {
        System.out.println("ForkJoinPool.getCommonPoolParallelism(): " + ForkJoinPool.getCommonPoolParallelism());

        if (false) {
            test(DataScenario.SORTED, Impl.DPQS, Impl.SYSTEM);
        }

//        final DataScenario[] scenarios = DataScenario.values();
        final DataScenario[] scenarios = new DataScenario[]{DataScenario.UNIFORM,
                                                            DataScenario.CONTIGUOUS, DataScenario.CONTIGUOUS_REVERSE,
                                                            DataScenario.ALMOST_CONTIGUOUS,
                                                            DataScenario.SORTED};

        for (DataScenario data : scenarios) {
            test(data, Impl.DPQS_2105, Impl.SYSTEM);
        }

        if (false) {
            for (DataScenario data : scenarios) {
                test(data, Impl.DPQS_2105_LowMem, Impl.SYSTEM);
            }
        }

        if (TEST_PARALLEL) {
            for (DataScenario data : scenarios) {
                test(data, Impl.P_DPQS_2105, Impl.P_SYSTEM);
            }
        }
    }

    public static void test(final DataScenario scenario, final Impl impl, final Impl implRef) {
        for (int i = 0; i < N; i++) {
            System.out.println("Preparing Test[" + scenario + " | " + impl + "| M=" + M + "] --------------------------------------");

            final int[] data = scenario.generate(M, 0, (1 << bits) - 1);
            final int[] copy = Arrays.copyOf(data, M);

            if (true) {
                System.out.println("data[0 : 100]: " + Arrays.toString(Arrays.copyOfRange(data, 0, 100)));
                System.out.println("data[-100 : 0]: " + Arrays.toString(Arrays.copyOfRange(data, M - 100, M)));
            }

            System.arraycopy(data, 0, copy, 0, copy.length);

            System.out.println("Test[" + scenario + " | " + impl + "| M=" + M + "] ---------------------------------------------------------------------");

            boolean test;
            long start = System.nanoTime();

            try {
                impl.sort(data);
                test = true;
            } catch (OutOfMemoryError oome) {
                System.err.println("Test[" + scenario + " | " + impl + "| M=" + M + "] failed (OOME)");
                oome.printStackTrace(System.err);
                test = false;
            }

            final long elapsed = System.nanoTime() - start;

            System.out.println("Test[" + scenario + " | " + implRef + "| M=" + M + "] ------------------------------------------------------------------------");

            boolean ref;
            start = System.nanoTime();

            try {
                implRef.sort(copy);
                ref = true;
            } catch (OutOfMemoryError oome) {
                System.err.println("Test[" + scenario + " | " + implRef + "| M=" + M + "] failed (OOME)");
                oome.printStackTrace(System.err);
                ref = false;
            }

            final long elapsed_std = System.nanoTime() - start;

            if (!test || !ref || Arrays.equals(data, copy)) {
                // OK
                if (TRACE_TIME) {
                    System.out.println("Test[" + scenario + " | " + impl + "| M=" + M + "] elapsed: "
                            + ((test) ? ((elapsed * 1e-6) + " ms") : "OOME")
                            + " ms, reference: " + ((ref) ? ((elapsed_std * 1e-6) + " ms") : "OOME"));
                }
            } else {
                for (int j = 0; j < M; j++) {
                    if (data[j] != copy[j]) {
                        System.out.println("Test[" + scenario + " | " + impl + "| M=" + M + "] Mismatch[" + j + "]: " + data[j] + " != " + copy[j]);
                    }
                }
                System.out.flush();
                throw new IllegalStateException("Test[" + scenario + " | " + impl + "| M=" + M + "] Bad sort !");
            }
        }
    }

    enum Impl {
        SYSTEM {
            @Override
            void sort(int[] data) {
                Arrays.sort(data);
            }
        },
        DPQS_RADIX {
            @Override
            void sort(int[] data) {
                DualPivotQuicksort20210424.sortRadix(data);
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
        },
        DPQS_2105 {
            @Override
            void sort(int[] data) {
                edu.sorting.ref.Arrays.sort(data);
            }
        },
        DPQS_2105_LowMem {
            @Override
            void sort(int[] data) {
                DualPivotQuicksort202105LowMem.sortNoAlloc(data);
            }
        },
        P_SYSTEM {
            @Override
            void sort(int[] data) {
                Arrays.parallelSort(data);
            }
        },
        P_DPQS_2105 {
            @Override
            void sort(int[] data) {
                edu.sorting.ref.Arrays.parallelSort(data);
            }
        };

        abstract void sort(int[] data);
    }
}
