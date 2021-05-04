package io.github.richardstartin.radixsort;

import java.util.Random;
import java.util.SplittableRandom;

public enum LongDataScenario {
    UNIFORM {
        @Override
        public long[] generate(int size, int seed, long mask) {
            SplittableRandom random = new SplittableRandom(seed);
            long[] data = new long[size];
            for (int i = 0; i < data.length; ++i) {
                data[i] = random.nextLong() & mask;
            }
            return data;
        }
    },
    GAUSSIAN {
        // LBO: NEW
        @Override
        public long[] generate(int size, int seed, long mask) {
            Random random = new Random(seed);
            long[] data = new long[size];

            final int max = size;
            final double avg = max / 2.0;
            final double stddev = avg / 2.0; // [-2; +2] sigma

            for (int i = 0; i < data.length; ++i) {
                data[i] = (int) Math.round(avg + stddev * random.nextGaussian());
                if (data[i] < 0) {
                    data[i] = 0;
                } else if (data[i] > max) {
                    data[i] = max;
                }
            }
            return data;
        }
    },
    CONTIGUOUS {
        @Override
        public long[] generate(int size, int seed, long mask) {
            long[] data = new long[size];
            for (int i = 0; i < data.length; ++i) {
                data[i] = i & mask;
            }
            return data;
        }
    },
    CONTIGUOUS_REVERSE {
        @Override
        public long[] generate(int size, int seed, long mask) {
            long[] data = new long[size];
            for (int i = 0; i < size; ++i) {
                data[i] = size - i;
            }
            return data;
        }
    },
    ALMOST_CONTIGUOUS {
        @Override
        public long[] generate(int size, int seed, long mask) {
            long[] data = new long[size];
            for (int i = 0; i < data.length; ++i) {
                data[i] = (i ^ 0xFF) & mask;
            }
            return data;
        }
    },
    SORTED {
        @Override
        public long[] generate(int size, int seed, long mask) {
            SplittableRandom random = new SplittableRandom(seed);
            long[] data = new long[size];
            long x = (long) (Math.log(random.nextDouble()) / Math.log(0.999)) + 1;
            for (int i = 0; i < size; ++i) {
                data[i] = x & mask;
                x += (long) (Math.log(random.nextDouble()) / Math.log(0.999)) + 1;
            }
            return data;
        }
    },
    ALMOST_SORTED {
        @Override
        public long[] generate(int size, int seed, long mask) {
            long[] data = new long[size];
            SplittableRandom random = new SplittableRandom(seed);
            long x = (long) (Math.log(random.nextDouble()) / Math.log(0.999)) + 1;
            for (int i = 0; i < size; ++i) {
                data[i] = (x ^ 0xFF) & mask;
                x += (long) (Math.log(random.nextDouble()) / Math.log(0.999)) + 1;
            }
            return data;
        }
    },
    EXP {
        @Override
        public long[] generate(int size, int seed, long mask) {
            SplittableRandom random = new SplittableRandom(seed);
            long[] data = new long[size];
            for (int i = 0; i < data.length; ++i) {
                long x = (long) (Math.log(random.nextDouble()) / Math.log(0.9999)) + 1;
                data[i] = x & mask;
            }
            return data;
        }
    },;

    public abstract long[] generate(int size, int seed, long mask);
}
