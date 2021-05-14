/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package edu.sorting;

import edu.sorting.util.WelfordVariance;
import io.github.richardstartin.radixsort.DataScenario;
import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;

/**
 *
 * @author bourgesl
 */
public class TestDPQS {

    private final static int M = 2000000;
    private final static int N = 100;

    private final static int bits = 27;

    private final static boolean TRACE_TIME = true;
    private final static boolean TEST_PARALLEL = true;

    private final static boolean DO_REF = false;

    public static void main(String[] args) {
        System.out.println("ForkJoinPool.getCommonPoolParallelism(): " + ForkJoinPool.getCommonPoolParallelism());

        if (true) {
            test(DataScenario.SORTED, Impl.DPQS_2105, Impl.SYSTEM);
            System.exit(0);
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

                // Stats:
                final WelfordVariance stats = new WelfordVariance();
                final WelfordVariance statsDelta = new WelfordVariance();

                final long start = System.nanoTime();

                for (int j = 0; j < data.length; j++) {
                    stats.add(data[j]);
                }

                for (int j = 1; j < data.length; j++) {
                    statsDelta.add(data[j] - data[j - 1]); // delta = V(i) - V(i-1)
                }

                System.out.println("Test[" + scenario + "] stats elapsed: " + (1e-6 * (System.nanoTime() - start)) + " ms");
                System.out.println("Test[" + scenario + "] stats: " + stats.toString());
                System.out.println("Test[" + scenario + "] statsDelta: " + statsDelta.toString());
                
                /*
Test[SORTED] stats elapsed: 37.764829 ms
Test[SORTED] stats: [2000000: µ=6.674691370463236E7 σ=3.8540604537873566E7 (57.741403158238874 %) rms=1.0528751824250592E8 min=125.0 max=1.34217662E8 sum=1.3349382740926472E14]
Test[SORTED] statsDelta: [1999999: µ=60.0227560113793 σ=355103.74505215697 (591615.1950517489 %) rms=355163.76780816837 min=-1.34216964E8 max=14675.0 sum=1.200454520000026E8]
                */
            }

            System.arraycopy(data, 0, copy, 0, copy.length);

            System.out.println("Test[" + scenario + " | " + impl + "| M=" + M + "] ---------------------------------------------------------------------");

            boolean test = false;
            long start = System.nanoTime();

            try {
                impl.sort(data);
                test = true;
            } catch (OutOfMemoryError oome) {
                System.err.println("Test[" + scenario + " | " + impl + "| M=" + M + "] failed (OOME)");
                oome.printStackTrace(System.err);
            }

            final long elapsed = System.nanoTime() - start;

            System.out.println("Test[" + scenario + " | " + implRef + "| M=" + M + "] ------------------------------------------------------------------------");

            boolean ref = false;
            start = System.nanoTime();

            if (DO_REF) {
                try {
                    implRef.sort(copy);
                    ref = true;
                } catch (OutOfMemoryError oome) {
                    System.err.println("Test[" + scenario + " | " + implRef + "| M=" + M + "] failed (OOME)");
                    oome.printStackTrace(System.err);
                }
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
