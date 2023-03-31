package group.aelysium.rustyconnector.core.lib.util;

import group.aelysium.rustyconnector.core.central.PluginLogger;
import group.aelysium.rustyconnector.core.lib.model.Sortable;

import java.util.*;

public class WeightedQuickSort {
    // This can def be optimized. Will have to revisit in the future.
    /**
     * The main quicksort implementation.
     * Assumes that start is 0 and the final index is equal to size.
     * @param array The array to sort.
     */
    public static <I extends Sortable> void sort(List<I> array) {
        QuickSort.sort(array); // Put array in order of index.

        // Pull out different weight levels and put them into their own lists.
        List<SortableInteger> indexes = new ArrayList<>();
        Map<Integer, List<I>> weighted = new HashMap<>();
        for (I entry : array) {
            if(!(indexes.contains(new SortableInteger(entry.getWeight()))))
                indexes.add(new SortableInteger(entry.getWeight()));

            if(weighted.containsKey(entry.getWeight()))
                weighted.get(entry.getWeight()).add(entry);
            else {
                weighted.put(entry.getWeight(), new ArrayList<>());
                weighted.get(entry.getWeight()).add(entry);
            }
        }

        QuickSort.sort(indexes);
        Collections.reverse(indexes);

        // Compile the lists back together
        List<I> compiledMap = new ArrayList<>();
        for (SortableInteger index : indexes) {
            compiledMap.addAll(weighted.get(index.intValue()));
            weighted.remove(index.intValue());
        }

        // Adjust the original array
        array.clear();
        array.addAll(compiledMap);
    }
}