package com.wiley.codechallenge.inmemorycache.lru;

import java.util.Map;

public interface LRUCache {

    Object get(Integer key);
    void set(Integer key, Object value);
    int getLRUCacheSize();
    Map<Integer, Object> lookInsideLRUCache();
    void clearLRUCache();

}
