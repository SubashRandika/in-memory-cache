package com.wiley.codechallenge.inmemorycache.lfu;

import java.util.Map;

public interface LFUCache {

    Object get(Integer key);
    void set(Integer key, Object value);
    int getLFUCacheSize();
    Map<Integer, Object> lookInsideLFUCache();
    void clearLFUCache();

}
