package group.aelysium.rustyconnector.toolkit.common.algorithm;

import group.aelysium.rustyconnector.toolkit.proxy.family.load_balancing.ISortable;

import java.util.List;

public class QuickSort {
    static <I extends ISortable> void swap(List<I> array, int i, int j) {
        I temp = array.get(i);
        array.set(i, array.get(j));
        array.set(j, temp);
    }

    static int partition(List<? extends ISortable> array, int low, int high) {
        double pivot = array.get(high).sortIndex();

        int i = (low - 1);
  
        for (int j = low; j <= high - 1; j++) {

            if (array.get(j).sortIndex() < pivot) {
                i++;
                swap(array, i, j);
            }
        }
        swap(array, i + 1, high);
        return (i + 1);
    }

    static void innerSort(List<? extends ISortable> array, int start, int end) {
        if (start < end) {
            int partitionIndex = partition(array, start, end);

            innerSort(array, start, partitionIndex - 1);
            innerSort(array, partitionIndex + 1, end);
        }
    }

    /**
     * The main quicksort implementation.
     * Assumes that start is 0 and the final index is equal to size.
     * This method will mutate the array that is passed.
     * Sorts from least to greatest.
     * @param array The array to sort.
     */
    public static void sort(List<? extends ISortable> array) {
        innerSort(array, 0, array.size() - 1);
    }
}