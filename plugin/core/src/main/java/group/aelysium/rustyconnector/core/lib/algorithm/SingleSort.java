package group.aelysium.rustyconnector.core.lib.algorithm;

import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ISortable;

import java.util.List;

public class SingleSort {
    /**
     * Sorts a single index back into the entry set.
     * Great for inserting items into the array and approximately sorting them into where they belong.
     * Thus reducing how frequently you'll have to perform a full sort.
     * @param index The index to sort.
     */
    public static <I extends ISortable> void sortDesc(List<I> array, int index) {
        if (array.size() <= 1) return;

        I item = array.get(index);
        array.remove(item);

        for (I entry : array) {
            if(item.sortIndex() < entry.sortIndex()) continue;
            array.add(array.indexOf(entry), item);
            return;
        }

        // If there are no items in the list smaller than the one we're
        // sorting in, add it after the loop finishes.
        array.add(item);
    }
    /**
     * Sorts a single index back into the entry set.
     * Great for inserting items into the array and approximately sorting them into where they belong.
     * Thus reducing how frequently you'll have to perform a full sort.
     * @param index The index to sort.
     */
    public static <I extends ISortable> void sortAsc(List<I> array, int index) {
        if (array.size() <= 1) return;

        I item = array.get(index);
        array.remove(item);

        for (I entry : array) {
            if(item.sortIndex() > entry.sortIndex()) continue;
            array.add(array.indexOf(entry), item);
            return;
        }

        // If there are no items in the list bigger than the one we're
        // sorting in, add it after the loop finishes.
        array.add(item);
    }
}