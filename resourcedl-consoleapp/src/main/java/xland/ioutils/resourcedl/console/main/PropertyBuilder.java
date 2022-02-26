package xland.ioutils.resourcedl.console.main;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final class PropertyBuilder<K, V> {
    private final HashMap<K, V> map;
    
    public PropertyBuilder() {
        map = new HashMap<>();
    }
    
    public static <K, V> PropertyBuilder<K, V> of() {
        return new PropertyBuilder<>();
    }
    
    public PropertyBuilder(Map<? extends K, ? extends V> m) {
        map = new HashMap<>(m);
    }
    
    public static <K, V> PropertyBuilder<K, V> of(Map<? extends K, ? extends V> m) {
        return new PropertyBuilder<>(m);
    }
    
    public PropertyBuilder<K, V> add(K k, V v) {
        map.put(k, v);
        return this;
    }
    
    public Map<K, V> build() {
        return Collections.unmodifiableMap(map);
    }
}
