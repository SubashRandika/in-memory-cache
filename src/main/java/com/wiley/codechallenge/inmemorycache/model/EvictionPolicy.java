package com.wiley.codechallenge.inmemorycache.model;

public enum EvictionPolicy {

    LRU("lru"),
    LFU("lfu");

    private final String evictionType;

    EvictionPolicy(String evictionType) {
        this.evictionType = evictionType;
    }

    public String getEvictionType() {
        return evictionType;
    }
}
