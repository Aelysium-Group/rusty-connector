package group.aelysium.rustyconnector.toolkit.velocity.util;

import java.util.Map;

public class Entry<K, V> implements Map.Entry {
    private K key;
    private V value;

    public Entry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return this.key;
    }

    @Override
    public V getValue() {
        return this.value;
    }

    @Override
    public V setValue(Object value) {
        this.value = (V) value;
        return this.value;
    }
}
