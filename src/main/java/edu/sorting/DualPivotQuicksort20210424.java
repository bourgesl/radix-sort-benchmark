/*
 * Copyright (c) 2009, 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package edu.sorting;

import java.util.Arrays; // TODO
// import java.util.concurrent.CountedCompleter;
// import java.util.concurrent.RecursiveTask;

/**
 * This class implements powerful and fully optimized versions, both
 * sequential and parallel, of the Dual-Pivot Quicksort algorithm by
 * Vladimir Yaroslavskiy, Jon Bentley and Josh Bloch. This algorithm
 * offers O(n log(n)) performance on all data sets, and is typically
 * faster than traditional (one-pivot) Quicksort implementations.
 *
 * There are also additional algorithms, invoked from the Dual-Pivot
 * Quicksort, such as mixed insertion sort, merging of runs and heap
 * sort, counting sort and parallel merge sort.
 *
 * @author Vladimir Yaroslavskiy
 * @author Jon Bentley
 * @author Josh Bloch
 * @author Doug Lea
 *
 * @version 2018.08.18
 *
 * @since 1.7 * 14
 */
public final class DualPivotQuicksort20210424 {

    public static void sortStd(final int[] A) {
        sortStd(A, 0, A.length - 1);
    }
    
    public static void sortStd(final int[] A, final int low, final int high) {
        final Sorter sorter = new Sorter();
        // preallocation of temporary arrays into custom Sorter class
        sorter.initBuffers(high - low + 1);

        sort(sorter, A, 0, low, high + 1); // exclusive
    }

    // avoid allocations:
    public final static Sorter SORTER = new Sorter();

    public static void sortNoAlloc(final int[] A) {
        sortStd(A, 0, A.length - 1);
    }

    public static void sortNoAlloc(final int[] A, final int low, final int high) {
        final Sorter sorter = SORTER;
        // avoid parallelism on sorter state:
        synchronized(sorter) {
            // preallocation of temporary arrays into custom Sorter class
            sorter.initBuffers(high - low + 1);

            sort(sorter, A, 0, low, high + 1); // exclusive
        }
    }
    
    /**
     * Prevents instantiation.
     */
    private DualPivotQuicksort20210424() {
    }

    public void sort(final int[] A, final int low, final int high) {
        final Sorter sorter = SORTER;
        // avoid parallelism on sorter state:
        synchronized(sorter) {
            // preallocation of temporary arrays into custom Sorter class
            sorter.initBuffers(high - low + 1);

            sort(sorter, A, 0, low, high + 1); // exclusive
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    /*
    From Vladimir's 2021.04.23 source code:
     */

    /**
     * Prevents instantiation.
     */
//  private DualPivotQuicksort() {} // TODO

    /**
     * Max array size to use mixed insertion sort.
     */
    private static final int MAX_MIXED_INSERTION_SORT_SIZE = 65;

    /**
     * Max array size to use insertion sort.
     */
    private static final int MAX_INSERTION_SORT_SIZE = 44;

    /**
     * Min array size to try merging of runs.
     */
    private static final int MIN_TRY_MERGE_SIZE = 4 << 10;

    /**
     * Min size of the first run to continue with scanning.
     */
    private static final int MIN_FIRST_RUN_SIZE = 16;

    /**
     * Min factor for the first runs to continue scanning.
     */
    private static final int MIN_FIRST_RUNS_FACTOR = 7;

    /**
     * Max capacity of the index array for tracking runs.
     */
    private static final int MAX_RUN_CAPACITY = 5 << 10;

    /**
     * Threshold of mixed insertion sort is incremented by this value.
     */
    private static final int DELTA = 3 << 1;
    private static final int DELTA4 = DELTA << 2; // TODO
    private static final int RADIX_MIN_SIZE = 4096;

    /**
     * Max recursive partitioning depth before using heap sort.
     */
    private static final int MAX_RECURSION_DEPTH = 64 * DELTA;

    /**
     * Sorts the specified array using the Dual-Pivot Quicksort and/or
     * other sorts in special-cases, possibly with parallel partitions.
     *
     * @param sorter parallel context
     * @param a the array to be sorted
     * @param bits the combination of recursion depth and bit flag, where
     *        the right bit "0" indicates that array is the leftmost part
     * @param low the index of the first element, inclusive, to be sorted
     * @param high the index of the last element, exclusive, to be sorted
     */
    private static void sort(Sorter sorter, int[] a, int bits, int low, int high) {
        while (true) {
            int end = high - 1, size = high - low;

            /*
             * Run mixed insertion sort on small non-leftmost parts.
             */
            if (size < MAX_MIXED_INSERTION_SORT_SIZE + bits && (bits & 1) > 0) {
                mixedInsertionSort(a, low, high - 3 * ((size >> 5) << 3), high);
                return;
            }

            /*
             * Invoke insertion sort on small leftmost part.
             */
            if (size < MAX_INSERTION_SORT_SIZE) {
                insertionSort(a, low, high);
                return;
            }

            /*
             * Check if the whole array or large non-leftmost
             * parts are nearly sorted and then merge runs.
             */
            if ((bits == 0 || size > MIN_TRY_MERGE_SIZE && (bits & 1) > 0)
                    && tryMergeRuns(sorter, a, low, size)) {
                return;
            }

            /*
             * Switch to heap sort if execution
             * time is becoming quadratic.
             */
            if ((bits += DELTA) > MAX_RECURSION_DEPTH) {
                heapSort(a, low, high);
                return;
            }

            /*
             * Use an inexpensive approximation of the golden ratio
             * to select five sample elements and determine pivots.
             */
            int step = (size >> 3) * 3 + 3;

            /*
             * Five elements around (and including) the central element
             * will be used for pivot selection as described below. The
             * unequal choice of spacing these elements was empirically
             * determined to work well on a wide variety of inputs.
             */
            int e1 = low + step;
            int e5 = end - step;
            int e3 = (e1 + e5) >>> 1;
            int e2 = (e1 + e3) >>> 1;
            int e4 = (e3 + e5) >>> 1;
            int a3 = a[e3];

            /*
             * Sort these elements in place by the combination
             * of 4-element sorting network and insertion sort.
             *
             *    5 ------o-----------o------------
             *            |           |
             *    4 ------|-----o-----o-----o------
             *            |     |           |
             *    2 ------o-----|-----o-----o------
             *                  |     |
             *    1 ------------o-----o------------
             */
            if (a[e5] < a[e2]) { int t = a[e5]; a[e5] = a[e2]; a[e2] = t; }
            if (a[e4] < a[e1]) { int t = a[e4]; a[e4] = a[e1]; a[e1] = t; }
            if (a[e5] < a[e4]) { int t = a[e5]; a[e5] = a[e4]; a[e4] = t; }
            if (a[e2] < a[e1]) { int t = a[e2]; a[e2] = a[e1]; a[e1] = t; }
            if (a[e4] < a[e2]) { int t = a[e4]; a[e4] = a[e2]; a[e2] = t; }

            if (a3 < a[e2]) {
                if (a3 < a[e1]) {
                    a[e3] = a[e2]; a[e2] = a[e1]; a[e1] = a3;
                } else {
                    a[e3] = a[e2]; a[e2] = a3;
                }
            } else if (a3 > a[e4]) {
                if (a3 > a[e5]) {
                    a[e3] = a[e4]; a[e4] = a[e5]; a[e5] = a3;
                } else {
                    a[e3] = a[e4]; a[e4] = a3;
                }
            }

            // Pointers
            int lower = low; // The index of the last element of the left part
            int upper = end; // The index of the first element of the right part

            /*
             * Partitioning with 2 pivots in case of different elements.
             */
            if (a[e1] < a[e2] && a[e2] < a[e3] && a[e3] < a[e4] && a[e4] < a[e5]) {

                // TODD add comment
                if ((bits > DELTA4 /* || sorter == null*/) && size > RADIX_MIN_SIZE) {
                    radixSort(sorter, a, low, high);
                    return;
                }

                /*
                 * Use the first and fifth of the five sorted elements as
                 * the pivots. These values are inexpensive approximation
                 * of tertiles. Note, that pivot1 < pivot2.
                 */
                int pivot1 = a[e1];
                int pivot2 = a[e5];

                /*
                 * The first and the last elements to be sorted are moved
                 * to the locations formerly occupied by the pivots. When
                 * partitioning is completed, the pivots are swapped back
                 * into their final positions, and excluded from the next
                 * subsequent sorting.
                 */
                a[e1] = a[lower];
                a[e5] = a[upper];

                /*
                 * Skip elements, which are less or greater than the pivots.
                 */
                while (a[++lower] < pivot1);
                while (a[--upper] > pivot2);

                /*
                 * Backward 3-interval partitioning
                 *
                 *   left part                 central part          right part
                 * +------------------------------------------------------------+
                 * |  < pivot1  |   ?   |  pivot1 <= && <= pivot2  |  > pivot2  |
                 * +------------------------------------------------------------+
                 *             ^       ^                            ^
                 *             |       |                            |
                 *           lower     k                          upper
                 *
                 * Invariants:
                 *
                 *              all in (low, lower] < pivot1
                 *    pivot1 <= all in (k, upper)  <= pivot2
                 *              all in [upper, end) > pivot2
                 *
                 * Pointer k is the last index of ?-part
                 */
                for (int unused = --lower, k = ++upper; --k > lower; ) {
                    int ak = a[k];

                    if (ak < pivot1) { // Move a[k] to the left side
                        while (lower < k) {
                            if (a[++lower] >= pivot1) {
                                if (a[lower] > pivot2) {
                                    a[k] = a[--upper];
                                    a[upper] = a[lower];
                                } else {
                                    a[k] = a[lower];
                                }
                                a[lower] = ak;
                                break;
                            }
                        }
                    } else if (ak > pivot2) { // Move a[k] to the right side
                        a[k] = a[--upper];
                        a[upper] = ak;
                    }
                }

                /*
                 * Swap the pivots into their final positions.
                 */
                a[low] = a[lower]; a[lower] = pivot1;
                a[end] = a[upper]; a[upper] = pivot2;

                /*
                 * Sort non-left parts recursively (possibly in parallel),
                 * excluding known pivots.
                 */
/*
                if (size > MIN_PARALLEL_SORT_SIZE && sorter != null) {
                    sorter.forkSorter(bits | 1, lower + 1, upper);
                    sorter.forkSorter(bits | 1, upper + 1, high);
                } else {
*/
                    sort(sorter, a, bits | 1, lower + 1, upper);
                    sort(sorter, a, bits | 1, upper + 1, high);
//                }

            } else { // Use single pivot in case of many equal elements

                /*
                 * Use the third of the five sorted elements as the pivot.
                 * This value is inexpensive approximation of the median.
                 */
                int pivot = a[e3];

                /*
                 * The first element to be sorted is moved to the
                 * location formerly occupied by the pivot. After
                 * completion of partitioning the pivot is swapped
                 * back into its final position, and excluded from
                 * the next subsequent sorting.
                 */
                a[e3] = a[lower];

                /*
                 * Traditional 3-way (Dutch National Flag) partitioning
                 *
                 *   left part                 central part    right part
                 * +------------------------------------------------------+
                 * |   < pivot   |     ?     |   == pivot   |   > pivot   |
                 * +------------------------------------------------------+
                 *              ^           ^                ^
                 *              |           |                |
                 *            lower         k              upper
                 *
                 * Invariants:
                 *
                 *   all in (low, lower] < pivot
                 *   all in (k, upper)  == pivot
                 *   all in [upper, end] > pivot
                 *
                 * Pointer k is the last index of ?-part
                 */
                for (int k = ++upper; --k > lower; ) {
                    int ak = a[k];

                    if (ak != pivot) {
                        a[k] = pivot;

                        if (ak < pivot) { // Move a[k] to the left side
                            while (a[++lower] < pivot);

                            if (a[lower] > pivot) {
                                a[--upper] = a[lower];
                            }
                            a[lower] = ak;
                        } else { // ak > pivot - Move a[k] to the right side
                            a[--upper] = ak;
                        }
                    }
                }

                /*
                 * Swap the pivot into its final position.
                 */
                a[low] = a[lower]; a[lower] = pivot;

                /*
                 * Sort the right part (possibly in parallel), excluding
                 * known pivot. All elements from the central part are
                 * equal and therefore already sorted.
                 */
/*
                if (size > MIN_PARALLEL_SORT_SIZE && sorter != null) {
                    sorter.forkSorter(bits | 1, upper, high);
                } else {
*/
                    sort(sorter, a, bits | 1, upper, high);
//                }
            }
            high = lower; // Iterate along the left part
        }
    }

    /**
     * Sorts the specified range of the array using mixed insertion sort.
     *
     * Mixed insertion sort is combination of simple insertion sort,
     * pin insertion sort and pair insertion sort.
     *
     * In the context of Dual-Pivot Quicksort, the pivot element
     * from the left part plays the role of sentinel, because it
     * is less than any elements from the given part. Therefore,
     * expensive check of the left range can be skipped on each
     * iteration unless it is the leftmost call.
     *
     * @param a the array to be sorted
     * @param low the index of the first element, inclusive, to be sorted
     * @param end the index of the last element for simple insertion sort
     * @param high the index of the last element, exclusive, to be sorted
     */
    private static void mixedInsertionSort(int[] a, int low, int end, int high) {
        if (end == high) {

            /*
             * Invoke simple insertion sort on tiny array.
             */
            for (int i; ++low < end; ) {
                int ai = a[i = low];

                while (ai < a[--i]) {
                    a[i + 1] = a[i];
                }
                a[i + 1] = ai;
            }
        } else {

            /*
             * Start with pin insertion sort on small part.
             *
             * Pin insertion sort is extended simple insertion sort.
             * The main idea of this sort is to put elements larger
             * than an element called pin to the end of array (the
             * proper area for such elements). It avoids expensive
             * movements of these elements through the whole array.
             */
            int pin = a[end];

            for (int i, p = high; ++low < end; ) {
                int ai = a[i = low];

                if (ai < a[i - 1]) { // Small element

                    /*
                     * Insert small element into sorted part.
                     */
                    a[i] = a[--i];

                    while (ai < a[--i]) {
                        a[i + 1] = a[i];
                    }
                    a[i + 1] = ai;

                } else if (p > i && ai > pin) { // Large element

                    /*
                     * Find element smaller than pin.
                     */
                    while (a[--p] > pin);

                    /*
                     * Swap it with large element.
                     */
                    if (p > i) {
                        ai = a[p];
                        a[p] = a[i];
                    }

                    /*
                     * Insert small element into sorted part.
                     */
                    while (ai < a[--i]) {
                        a[i + 1] = a[i];
                    }
                    a[i + 1] = ai;
                }
            }

            /*
             * Continue with pair insertion sort on remain part.
             */
            for (int i; low < high; ++low) {
                int a1 = a[i = low], a2 = a[++low];

                /*
                 * Insert two elements per iteration: at first, insert the
                 * larger element and then insert the smaller element, but
                 * from the position where the larger element was inserted.
                 */
                if (a1 > a2) {

                    while (a1 < a[--i]) {
                        a[i + 2] = a[i];
                    }
                    a[++i + 1] = a1;

                    while (a2 < a[--i]) {
                        a[i + 1] = a[i];
                    }
                    a[i + 1] = a2;

                } else if (a1 < a[i - 1]) {

                    while (a2 < a[--i]) {
                        a[i + 2] = a[i];
                    }
                    a[++i + 1] = a2;

                    while (a1 < a[--i]) {
                        a[i + 1] = a[i];
                    }
                    a[i + 1] = a1;
                }
            }
        }
    }

    /**
     * Sorts the specified range of the array using insertion sort.
     *
     * @param a the array to be sorted
     * @param low the index of the first element, inclusive, to be sorted
     * @param high the index of the last element, exclusive, to be sorted
     */
    private static void insertionSort(int[] a, int low, int high) {
        for (int i, k = low; ++k < high; ) {
            int ai = a[i = k];

            if (ai < a[i - 1]) {
                while (--i >= low && ai < a[i]) {
                    a[i + 1] = a[i];
                }
                a[i + 1] = ai;
            }
        }
    }

    // TODO add javadoc
    private static void radixSort(Sorter sorter, int[] a, int low, int high) {
        int[] b;
        // LBO: prealloc (high - low) +1 element:
        if (sorter == null || (b = sorter.b) == null || b.length < (high - low)) {
            // System.out.println("alloc b: " + (high - low));
            b = new int[high - low];
        }
        
        int[] count1, count2, count3, count4;
        if (sorter != null) {
            sorter.resetRadixBuffers();
            count1 = sorter.count1;
            count2 = sorter.count2;
            count3 = sorter.count3;
            count4 = sorter.count4;
        } else {
            // System.out.println("alloc radix buffers(4x256)");
            count1 = new int[256];
            count2 = new int[256];
            count3 = new int[256];
            count4 = new int[256];
        }

        for (int i = low; i < high; ++i) {
            --count1[ a[i]         & 0xFF ];
            --count2[(a[i] >>>  8) & 0xFF ];
            --count3[(a[i] >>> 16) & 0xFF ];
            --count4[(a[i] >>> 24) ^ 0x80 ];
        }

        boolean skipLevel4 = canSkipLevel(count4, low - high);
        boolean skipLevel3 = skipLevel4 && canSkipLevel(count3, low - high);
        boolean skipLevel2 = skipLevel3 && canSkipLevel(count2, low - high);

        count1[255] += high;
        count2[255] += high;
        count3[255] += high;
        count4[255] += high;

        // 1 todo process LSD
        for (int i = 255; i > 0; --i) {
            count1[i - 1] += count1[i];
        }

        for (int i = low; i < high; ++i) {
            b[count1[a[i] & 0xFF]++ - low] = a[i];
        }

        if (skipLevel2) {
            System.arraycopy(b, 0, a, low, high - low);
            return;
        }

        // 2
        for (int i = 255; i > 0; --i) {
            count2[i - 1] += count2[i];
        }

        //for (int value : b) {
        //    a[count2[(value >> 8) & 0xFF]++] = value;
        for (int i = low; i < high; ++i) {
            a[count2[(b[i] >> 8) & 0xFF]++] = b[i];
        }

        if (skipLevel3) {
            return;
        }
        
        // 3
        for (int i = 255; i > 0; --i) {
            count3[i - 1] += count3[i];
        }

        for (int i = low; i < high; ++i) {
            b[count3[(a[i] >> 16) & 0xFF]++ - low] = a[i];
        }

        if (skipLevel4) {
            System.arraycopy(b, 0, a, low, high - low);
            return;
        }

        // 4
        for (int i = 255; i > 0; --i) {
            count4[i - 1] += count4[i];
        }

        // for (int value : b) {
        //    a[count4[ (value >>> 24) ^ 0x80]++] = value;
        for (int i = low; i < high; ++i) {
            a[count4[ (b[i] >>> 24) ^ 0x80]++] = b[i];
        }
    }

    // TODO: add javadoc
    private static boolean canSkipLevel(int[] count, int total) {
        for (int c : count) {
            if (c == 0) {
                continue;
            }
            return c == total;
        }
        return true;
    }

    /**
     * Sorts the specified range of the array using heap sort.
     *
     * @param a the array to be sorted
     * @param low the index of the first element, inclusive, to be sorted
     * @param high the index of the last element, exclusive, to be sorted
     */
    private static void heapSort(int[] a, int low, int high) {
        for (int k = (low + high) >>> 1; k > low; ) {
            pushDown(a, --k, a[k], low, high);
        }
        while (--high > low) {
            int max = a[low];
            pushDown(a, low, a[high], low, high);
            a[high] = max;
        }
    }

    /**
     * Pushes specified element down during heap sort.
     *
     * @param a the given array
     * @param p the start index
     * @param value the given element
     * @param low the index of the first element, inclusive, to be sorted
     * @param high the index of the last element, exclusive, to be sorted
     */
    private static void pushDown(int[] a, int p, int value, int low, int high) {
        for (int k ;; a[p] = a[p = k]) {
            k = (p << 1) - low + 2; // Index of the right child

            if (k > high) {
                break;
            }
            if (k == high || a[k] < a[k - 1]) {
                --k;
            }
            if (a[k] <= value) {
                break;
            }
        }
        a[p] = value;
    }

    /**
     * Tries to sort the specified range of the array.
     *
     * @param sorter parallel context
     * @param a the array to be sorted
     * @param low the index of the first element to be sorted
     * @param size the array size
     * @return true if finally sorted, false otherwise
     */
    private static boolean tryMergeRuns(Sorter sorter, int[] a, int low, int size) {

        /*
         * The run array is constructed only if initial runs are
         * long enough to continue, run[i] then holds start index
         * of the i-th sequence of elements in non-descending order.
         */
        int[] run = null;
        int high = low + size;
        int count = 1, last = low;

        /*
         * Identify all possible runs.
         */
        for (int k = low + 1; k < high; ) {

            /*
             * Find the end index of the current run.
             */
            if (a[k - 1] < a[k]) {

                // Identify ascending sequence
                while (++k < high && a[k - 1] <= a[k]);

            } else if (a[k - 1] > a[k]) {

                // Identify descending sequence
                while (++k < high && a[k - 1] >= a[k]);

                // Reverse into ascending order
                for (int i = last - 1, j = k; ++i < --j && a[i] > a[j]; ) {
                    int ai = a[i]; a[i] = a[j]; a[j] = ai;
                }
            } else { // Identify constant sequence
                for (int ak = a[k]; ++k < high && ak == a[k]; );

                if (k < high) {
                    continue;
                }
            }

            /*
             * Check special cases.
             */
            if (sorter.runInit || run == null) {
                sorter.runInit = false; // LBO

                if (k == high) {

                    /*
                     * The array is monotonous sequence,
                     * and therefore already sorted.
                     */
                    return true;
                }

                if (k - low < MIN_FIRST_RUN_SIZE) {

                    /*
                     * The first run is too small
                     * to proceed with scanning.
                     */
                    return false;
                }

//                System.out.println("alloc run");
//                run = new int[((size >> 10) | 0x7F) & 0x3FF];
                run = sorter.run; // LBO: prealloc
                run[0] = low;

            } else if (a[last - 1] > a[last]) {

                if (count > (k - low) >> MIN_FIRST_RUNS_FACTOR) {

                    /*
                     * The first runs are not long
                     * enough to continue scanning.
                     */
                    return false;
                }

                if (++count == MAX_RUN_CAPACITY) {

                    /*
                     * Array is not highly structured.
                     */
                    return false;
                }

                if (false && count == run.length) {

                    /*
                     * Increase capacity of index array.
                     */
//                  System.out.println("alloc run (resize)");
                    run = Arrays.copyOf(run, count << 1);
                }
            }
            run[count] = (last = k);
        }

        /*
         * Merge runs of highly structured array.
         */
        if (count > 1) {
            int[] b; int offset = low;

            // LBO: prealloc
            if (sorter == null || (b = sorter.b) == null || b.length < size) {
//                System.out.println("alloc b: "+size);
                b = new int[size];
//            } else {
//                offset = sorter.offset;
            }
            mergeRuns(a, b, offset, 1, /*sorter != null,*/ run, 0, count);
        }
        return true;
    }

    /**
     * Merges the specified runs.
     *
     * @param a the source array
     * @param b the temporary buffer used in merging
     * @param offset the start index in the source, inclusive
     * @param aim specifies merging: to source ( > 0), buffer ( < 0) or any ( == 0)
     * @param run the start indexes of the runs, inclusive
     * @param lo the start index of the first run, inclusive
     * @param hi the start index of the last run, inclusive
     * @return the destination where runs are merged
     */
    private static int[] mergeRuns(int[] a, int[] b, int offset,
            int aim, /*boolean parallel,*/ int[] run, int lo, int hi) {

        if (hi - lo == 1) {
            if (aim >= 0) {
                return a;
            }
            for (int i = run[hi], j = i - offset, low = run[lo]; i > low;
                b[--j] = a[--i]
            );
            return b;
        }

        /*
         * Split into approximately equal parts.
         */
        int mi = lo, rmi = (run[lo] + run[hi]) >>> 1;
        while (run[++mi + 1] <= rmi);

        /*
         * Merge the left and right parts.
         */
        int[] a1, a2;
/*
        if (parallel && hi - lo > MIN_RUN_COUNT) {
            RunMerger merger = new RunMerger(a, b, offset, 0, run, mi, hi).forkMe();
            a1 = mergeRuns(a, b, offset, -aim, true, run, lo, mi);
            a2 = (int[]) merger.getDestination();
        } else {
*/
            a1 = mergeRuns(a, b, offset, -aim, /*false,*/ run, lo, mi);
            a2 = mergeRuns(a, b, offset,    0, /*false,*/ run, mi, hi);
//        }

        int[] dst = a1 == a ? b : a;

        int k   = a1 == a ? run[lo] - offset : run[lo];
        int lo1 = a1 == b ? run[lo] - offset : run[lo];
        int hi1 = a1 == b ? run[mi] - offset : run[mi];
        int lo2 = a2 == b ? run[mi] - offset : run[mi];
        int hi2 = a2 == b ? run[hi] - offset : run[hi];

/*
        if (parallel) {
            new Merger(null, dst, k, a1, lo1, hi1, a2, lo2, hi2).invoke();
        } else {
*/
            mergeParts(/*null,*/dst, k, a1, lo1, hi1, a2, lo2, hi2);
//        }
        return dst;
    }

    /**
     * Merges the sorted parts.
     *
     * @param dst the destination where parts are merged
     * @param k the start index of the destination, inclusive
     * @param a1 the first part
     * @param lo1 the start index of the first part, inclusive
     * @param hi1 the end index of the first part, exclusive
     * @param a2 the second part
     * @param lo2 the start index of the second part, inclusive
     * @param hi2 the end index of the second part, exclusive
     */
    private static void mergeParts(/*Merger merger,*/int[] dst, int k,
            int[] a1, int lo1, int hi1, int[] a2, int lo2, int hi2) {
        // ...
        /*
         * Merge small parts sequentially.
         */
        while (lo1 < hi1 && lo2 < hi2) {
            dst[k++] = a1[lo1] < a2[lo2] ? a1[lo1++] : a2[lo2++];
        }
        if (dst != a1 || k < lo1) {
            while (lo1 < hi1) {
                dst[k++] = a1[lo1++];
            }
        }
        if (dst != a2 || k < lo2) {
            while (lo2 < hi2) {
                dst[k++] = a2[lo2++];
            }
        }
    }

    static final class Sorter {

        final int[] run;
        int[] b;
        boolean runInit;
        
        final int[] count1 = new int[256];
        final int[] count2 = new int[256];
        final int[] count3 = new int[256];
        final int[] count4 = new int[256];        

        Sorter() {
            // preallocate max runs:
            run = new int[MAX_RUN_CAPACITY];
        }

        void initBuffers(int length) {
            if (b == null || b.length < length) {
                b = new int[length];
            }
            runInit = true;
        }
        
        void resetRadixBuffers() {
            Arrays.fill(count1, 0);
            Arrays.fill(count2, 0);
            Arrays.fill(count3, 0);
            Arrays.fill(count4, 0);
        }
    }
}