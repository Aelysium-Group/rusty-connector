package group.aelysium.rustyconnector.core.lib.util;

import group.aelysium.rustyconnector.core.lib.model.Sortable;

import java.util.List;

public class SingleSort {
    /**
     * Sorts a single index back into the entry set.
     * @param index The index to sort.
     */
    public static <I extends Sortable> void sort(List<I> array, int index) {
        I item = array.get(index);
        array.remove(item);

        for (I entry : array) {
            if(item.getSortIndex() < entry.getSortIndex()) continue;
            array.add(array.indexOf(entry), item);
            return;
        }
    }
}