package com.fang.example.java.lru;

/**
 * Created by andy on 7/26/16.
 */
public interface CachePolicyListener {
    public void cacheObjectEvicted(Object obj) throws Exception;
}
