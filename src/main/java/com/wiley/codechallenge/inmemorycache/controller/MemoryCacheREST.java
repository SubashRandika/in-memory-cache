package com.wiley.codechallenge.inmemorycache.controller;

import com.wiley.codechallenge.inmemorycache.lfu.LFUCache;
import com.wiley.codechallenge.inmemorycache.lru.LRUCache;
import com.wiley.codechallenge.inmemorycache.model.EvictionPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.wiley.codechallenge.inmemorycache.util.Constants.*;

@RestController
@RequestMapping("/api/cache")
@PropertySource("classpath:cacheConfig.properties")
public class MemoryCacheREST {

    @Value("${cache.eviction_policy}")
    private String evictionPolicy;

    @Autowired
    private LRUCache lruCache;

    @Autowired
    private LFUCache lfuCache;

    @GetMapping("/{key}")
    public ResponseEntity<Object> getFromCache(@PathVariable Integer key) {
        Map<String, Object> responseObject = new HashMap<>();

        if(evictionPolicy != null && !evictionPolicy.isEmpty()) {
            if(EvictionPolicy.LRU.getEvictionType().equals(evictionPolicy)) {
                Object lruCachedObject = lruCache.get(key);

                if(lruCachedObject != null) {
                    return new ResponseEntity<>(lruCachedObject, HttpStatus.OK);
                } else {
                    responseObject.put("code", 404);
                    responseObject.put("message", String.format(OBJECT_GET_FAILURE_MESSAGE, evictionPolicy.toUpperCase()));

                    return new ResponseEntity<>(responseObject, HttpStatus.NOT_FOUND);
                }
            }

            if(EvictionPolicy.LFU.getEvictionType().equals(evictionPolicy)) {
                Object lfuCachedObject = lfuCache.get(key);

                if(lfuCachedObject != null) {
                    return new ResponseEntity<>(lfuCachedObject, HttpStatus.OK);
                } else {
                    responseObject.put("code", 404);
                    responseObject.put("message", String.format(OBJECT_GET_FAILURE_MESSAGE, evictionPolicy.toUpperCase()));

                    return new ResponseEntity<>(responseObject, HttpStatus.NOT_FOUND);
                }
            }
        }

        responseObject.put("code", 404);
        responseObject.put("message", CACHE_EVICT_NOT_SET_MESSAGE);

        return new ResponseEntity<>(responseObject, HttpStatus.NOT_FOUND);
    }

    @PostMapping("/{key}")
    public ResponseEntity<Object> setToCache(@PathVariable Integer key, @RequestBody Object value) {
        Map<String, Object> responseObject = new HashMap<>();

        if(evictionPolicy != null && !evictionPolicy.isEmpty()) {
            if(EvictionPolicy.LRU.getEvictionType().equals(evictionPolicy)) {
                lruCache.set(key, value);
                responseObject.put("code", 200);
                responseObject.put("message", String.format(OBJECT_ADD_SUCCESS_MESSAGE, evictionPolicy.toUpperCase()));

                return new ResponseEntity<>(responseObject, HttpStatus.OK);
            }

            if(EvictionPolicy.LFU.getEvictionType().equals(evictionPolicy)) {
                lfuCache.set(key, value);
                responseObject.put("code", 200);
                responseObject.put("message",
                        String.format(OBJECT_ADD_SUCCESS_MESSAGE, evictionPolicy.toUpperCase()));

                return new ResponseEntity<>(responseObject, HttpStatus.OK);
            }
        }

        responseObject.put("code", 404);
        responseObject.put("message", CACHE_EVICT_NOT_SET_MESSAGE);

        return new ResponseEntity<>(responseObject, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/size")
    public ResponseEntity<Object> getCurrentCacheSize() {
        Map<String, Object> responseObject = new HashMap<>();

        if(evictionPolicy != null && !evictionPolicy.isEmpty()) {
            responseObject.put("cacheType", evictionPolicy);

            if(EvictionPolicy.LRU.getEvictionType().equals(evictionPolicy)) {
                int lruSize = lruCache.getLRUCacheSize();
                responseObject.put("size", lruSize);

                return new ResponseEntity<>(responseObject, HttpStatus.OK);
            }

            if(EvictionPolicy.LFU.getEvictionType().equals(evictionPolicy)) {
                int lfuSize = lfuCache.getLFUCacheSize();
                responseObject.put("size", lfuSize);

                return new ResponseEntity<>(responseObject, HttpStatus.OK);
            }
        }

        responseObject.put("code", 404);
        responseObject.put("message", CACHE_EVICT_NOT_SET_MESSAGE);

        return new ResponseEntity<>(responseObject, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/check")
    public ResponseEntity<Object> printCurrentCache() {
        Map<String, Object> responseObject = new HashMap<>();

        if(evictionPolicy != null && !evictionPolicy.isEmpty()) {
            if(EvictionPolicy.LRU.getEvictionType().equals(evictionPolicy)) {
                Map<Integer, Object> lruFullCache = lruCache.lookInsideLRUCache();

                return new ResponseEntity<>(lruFullCache, HttpStatus.OK);
            }

            if(EvictionPolicy.LFU.getEvictionType().equals(evictionPolicy)) {
                Map<Integer, Object> lfuFullCache = lfuCache.lookInsideLFUCache();

                return new ResponseEntity<>(lfuFullCache, HttpStatus.OK);
            }
        }

        responseObject.put("code", 404);
        responseObject.put("message", CACHE_EVICT_NOT_SET_MESSAGE);

        return new ResponseEntity<>(responseObject, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/clear")
    public ResponseEntity<Object> clearCache() {
        Map<String, Object> responseObject = new HashMap<>();

        if(evictionPolicy != null && !evictionPolicy.isEmpty()) {
            if(EvictionPolicy.LRU.getEvictionType().equals(evictionPolicy)) {
                int size = lruCache.getLRUCacheSize();
                lruCache.clearLRUCache();
                responseObject.put("code", 200);
                responseObject.put("message", String.format(CACHE_CLEAR_SUCCESS_MESSAGE, size));

                return new ResponseEntity<>(responseObject, HttpStatus.OK);
            }

            if(EvictionPolicy.LFU.getEvictionType().equals(evictionPolicy)) {
                int size = lfuCache.getLFUCacheSize();
                lfuCache.clearLFUCache();

                responseObject.put("code", 200);
                responseObject.put("message", String.format(CACHE_CLEAR_SUCCESS_MESSAGE, size));

                return new ResponseEntity<>(responseObject, HttpStatus.OK);
            }
        }

        responseObject.put("code", 404);
        responseObject.put("message", CACHE_EVICT_NOT_SET_MESSAGE);

        return new ResponseEntity<>(responseObject, HttpStatus.NOT_FOUND);
    }

}
