package com.wiley.codechallenge.inmemorycache.lfu;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

@Component
@PropertySource("classpath:cacheConfig.properties")
public class LFUCacheImpl implements LFUCache {

    @Value("${cache.lfu.max_capacity}")
    private int MAX_LFU_CAPACITY;
    private int minFrequency = -1;
    private final HashMap<Integer, Object> dataMap;                             // (key, data) as an entry.
    private final HashMap<Integer, Integer> frequencyMap;                       // (key, frequency) as an entry.
    private final HashMap<Integer, LinkedHashSet<Integer>> frequencyKeysListMap;// (frequency, list_of_keys) as an entry.


    public LFUCacheImpl() {
        this.dataMap = new HashMap<>();
        this.frequencyMap = new HashMap<>();
        this.frequencyKeysListMap = new HashMap<>();
        this.frequencyKeysListMap.put(0, new LinkedHashSet<>());
    }

    @Override
    public void set(Integer key, Object value) {
        if(!isValidLFUCapacity()) {
            return;
        }

        if(dataMap.containsKey(key)) {
            dataMap.put(key, value);
            rearrangeDataOrder(key);
            return;
        }

        if(isLFUCacheFull()) {
            Integer keyToRemove = frequencyKeysListMap.get(minFrequency).iterator().next();
            frequencyKeysListMap.get(minFrequency).remove(keyToRemove);
            dataMap.remove(keyToRemove);
            frequencyMap.remove(keyToRemove);
        }

        this.minFrequency = 0;
        dataMap.put(key, value);
        frequencyMap.put(key, this.minFrequency);
        frequencyKeysListMap.get(0).add(key);
    }

    @Override
    public Object get(Integer key) {
        if(dataMap.containsKey(key)) {
            rearrangeDataOrder(key);
            return dataMap.get(key);
        }

        //TODO: Cache miss scenario, need to re-fetch from DB to store in cache.
        return null;
    }

    @Override
    public int getLFUCacheSize() {
        return dataMap.size();
    }

    @Override
    public Map<Integer, Object> lookInsideLFUCache() {
        return dataMap;
    }

    @Override
    public void clearLFUCache() {
        dataMap.clear();
    }

    private void rearrangeDataOrder(Integer key) {
        int oldFrequency = frequencyMap.get(key);
        int newFrequency = oldFrequency + 1;
        frequencyMap.put(key, newFrequency);
        frequencyKeysListMap.get(oldFrequency).remove(key);

        if(frequencyKeysListMap.get(oldFrequency).size() == 0 && oldFrequency == minFrequency) {
            minFrequency++;
        }

        if(!frequencyKeysListMap.containsKey(newFrequency)) {
            frequencyKeysListMap.put(newFrequency, new LinkedHashSet<>());
        }

        frequencyKeysListMap.get(newFrequency).add(key);
    }

    private boolean isValidLFUCapacity() {
        return MAX_LFU_CAPACITY > 0;
    }

    private boolean isLFUCacheFull() {
        return dataMap.size() >= MAX_LFU_CAPACITY;
    }

}
