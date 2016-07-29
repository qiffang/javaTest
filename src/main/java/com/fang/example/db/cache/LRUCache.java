package com.fang.example.db.cache;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by andy on 6/26/16.
 */
public class LRUCache< K, V > extends LinkedHashMap< K, V > {

    private int _maxCacheSize = 100;

    public LRUCache(int maximumCacheSize) {
        this(0, maximumCacheSize, 0.75F, true);
    }

    public LRUCache(int initialCapacity, int maximumCacheSize, float loadFactor, boolean accessOrder) {
        super(initialCapacity, loadFactor, accessOrder);
        _maxCacheSize = maximumCacheSize;
    }

    public int getMaxCacheSize() {
        return _maxCacheSize;
    }

    public void setMaxCacheSize(int maxCacheSize) {
        _maxCacheSize = maxCacheSize;
    }

    protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
        return this.size() > _maxCacheSize;
    }
}
