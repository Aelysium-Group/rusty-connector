package group.aelysium.rustyconnector.core.lib.util;

import group.aelysium.rustyconnector.core.lib.model.Sortable;

public class SortableInteger implements Sortable {
    int value;

    public SortableInteger(int value) {
        this.value = value;
    }

    @Override
    public int getSortIndex() {
        return this.value;
    }

    @Override
    public int getWeight() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SortableInteger temp = (SortableInteger) o;
        return value == temp.intValue();
    }

    public int intValue() {
        return this.value;
    }
}
