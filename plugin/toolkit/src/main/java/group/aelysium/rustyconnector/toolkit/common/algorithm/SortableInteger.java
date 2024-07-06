package group.aelysium.rustyconnector.toolkit.common.algorithm;

import group.aelysium.rustyconnector.toolkit.proxy.family.load_balancing.ISortable;

public class SortableInteger implements ISortable {
    int value;

    public SortableInteger(int value) {
        this.value = value;
    }

    @Override
    public double sortIndex() {
        return this.value;
    }

    @Override
    public int weight() {
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
