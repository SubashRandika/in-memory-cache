package com.wiley.codechallenge.inmemorycache.lru;

import com.wiley.codechallenge.inmemorycache.model.LRUNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@PropertySource("classpath:cacheConfig.properties")
public class LRUCacheImpl implements LRUCache {

    @Value("${cache.lru.max_capacity}")
    private int MAX_LRU_CAPACITY;
    private final Map<Integer, LRUNode> map;
    private final LRUNode headNode;
    private final LRUNode tailNode;

    public LRUCacheImpl() {
        this.map = new HashMap<>();
        this.headNode = new LRUNode(-1, null);
        this.tailNode = new LRUNode(-1, null);
        headNode.setNextNode(this.tailNode);
        tailNode.setPrevNode(this.headNode);
    }

    @Override
    public void set(Integer key, Object value) {
        if(!isValidLRUCapacity()) {
            return;
        }

        if(map.containsKey(key)) {
            LRUNode node = map.get(key);
            node.setData(value);
            node.getPrevNode().setNextNode(node.getNextNode());
            node.getNextNode().setPrevNode(node.getPrevNode());
            moveToBeginning(node);
            return;
        }

        if(isLRUCacheFull()) {
            map.remove(tailNode.getPrevNode().getKey());
            tailNode.setPrevNode(tailNode.getPrevNode().getPrevNode());
            tailNode.getPrevNode().setNextNode(tailNode);
        }

        LRUNode newNode = new LRUNode(key, value);
        map.put(key, newNode);
        moveToBeginning(newNode);
    }

    @Override
    public Object get(Integer key) {
        if(map.containsKey(key)) {
            LRUNode node = map.get(key);
            node.getPrevNode().setNextNode(node.getNextNode());
            node.getNextNode().setPrevNode(node.getPrevNode());
            moveToBeginning(node);
            return node.getData();
        }

        //TODO: Cache miss scenario, need to re-fetch from DB to store in cache.
        return null;
    }

    @Override
    public int getLRUCacheSize() {
        return map.size();
    }

    @Override
    public Map<Integer, Object> lookInsideLRUCache() {
        return map.entrySet().stream()
                  .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getData()));
    }

    @Override
    public void clearLRUCache() {
        map.clear();
    }

    private void moveToBeginning(LRUNode node) {
        node.setPrevNode(headNode);
        node.setNextNode(headNode.getNextNode());
        node.getNextNode().setPrevNode(node);
        headNode.setNextNode(node);
    }

    private boolean isValidLRUCapacity() {
        return MAX_LRU_CAPACITY > 0;
    }

    private boolean isLRUCacheFull() {
        return map.size() == MAX_LRU_CAPACITY;
    }

}
