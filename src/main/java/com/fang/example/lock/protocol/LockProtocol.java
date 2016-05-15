package com.fang.example.lock.protocol;

/**
 * Created by andy on 5/15/16.
 */
public interface LockProtocol {
    public String tryLock(String lockName, String value, Long expireTime);
    public String lock(String lockName, String value, Long expireTime);
    public String unLock(String lockName, String value);
}
