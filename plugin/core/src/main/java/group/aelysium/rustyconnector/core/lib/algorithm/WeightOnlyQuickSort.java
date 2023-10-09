package group.aelysium.rustyconnector.core.lib.algorithm;

import group.aelysium.rustyconnector.core.lib.model.Sortable;

import java.util.Collections;
import java.util.List;

public class WeightOnlyQuickSort {
    static <I extends Sortable> void swap(List<I> array, int i, int j) {
        I temp = array.get(i);
        array.set(i, array.get(j));
        array.set(j, temp);
    }

    static int partition(List<? extends Sortable> array, int low, int high) {
        // pivot
        int pivot = array.get(high).weight();
  
        // Index of smaller element and
        // indicates the right position
        // of pivot found so far
        int i = (low - 1);
  
        for (int j = low; j <= high - 1; j++) {
  
            // If current element is smaller
            // than the pivot
            if (array.get(j).weight() < pivot) {
  
                // Increment index of
                // smaller element
                i++;
                swap(array, i, j);
            }
        }
        swap(array, i + 1, high);
        return (i + 1);
    }

    static void innerSort(List<? extends Sortable> array, int start, int end) {
        if (start < end) {
            int partitionIndex = partition(array, start, end);

            innerSort(array, start, partitionIndex - 1);
            innerSort(array, partitionIndex + 1, end);

            Collections.reverse(array);
        }
    }

    /**
     * A weight only quicksort implementation.
     * This will only sort entries by the weight value associated with them.
     * @param array The array to sort.
     */
    public static void sort(List<? extends Sortable> array) {
        innerSort(array, 0, array.size() - 1);
    }
}