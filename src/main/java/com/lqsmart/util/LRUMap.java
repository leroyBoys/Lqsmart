package com.lqsmart.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by leroy:656515489@qq.com
 * 2018/6/14.
 */
public class LRUMap<K, V> extends LinkedHashMap<K, V> {
    private static final long serialVersionUID = 1L;
    private final int maxSize;

    public LRUMap(int maxSize) {
        this(maxSize, 16, 0.75F, false);
    }

    public LRUMap(int maxSize, int initialCapacity, float loadFactor, boolean accessOrder) {
        super(initialCapacity, loadFactor, accessOrder);
        this.maxSize = maxSize;
    }

    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return this.size() > this.maxSize;
    }
}
