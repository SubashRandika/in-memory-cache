package com.wiley.codechallenge.inmemorycache.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LRUNode {

    private Integer key;
    private Object data;
    private LRUNode prevNode;
    private LRUNode nextNode;

    public LRUNode(Integer key, Object data) {
        this.key = key;
        this.data = data;
        this.prevNode = null;
        this.nextNode = null;
    }

}
