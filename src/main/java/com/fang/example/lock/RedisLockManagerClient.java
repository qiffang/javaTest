package com.fang.example.lock;

import com.fang.example.spring.di.util.NotNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by andy on 5/11/16.
 */
public class RedisLockManagerClient {
    private JedisPool _pool;
    private static RedisLockManagerClient _manager;
    private RedisLockManagerClient() {}

    public static RedisLockManagerClient getInstance() {
        if (_manager == null) {
            _manager = new RedisLockManagerClient();
        }
        return _manager;
    }

    public void init(@NotNull String redisIp,
                     @NotNull int port) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(10);
        config.setMaxIdle(5);
        config.setTestOnBorrow(true);
        config.setTestWhileIdle(false);
        _pool = new JedisPool(config, redisIp, port, Protocol.DEFAULT_TIMEOUT);
    }


    /**
     * The value is a randomUUID.
     * The client1 which get lock may be handle too long time beyond the expireTime, and then other client2 will get the lock, but if the value is the same
     * the client1 will delete the lock in redis which client2 create .
     */
    public boolean lock(String key, String value, long expireTimeInMS) {

        String script = "local key     = KEYS[1]\n" +
                "local ttl     = KEYS[2]\n" +
                "local value = KEYS[3]\n" +
                " \n" +
                "local lockSet = redis.call('setnx', key, value)\n" +
                " \n" +
                "if lockSet == 1 then\n" +
                "  redis.call('pexpire', key, ttl)\n" +
                "end\n" +
                " \n" +
                "return lockSet";

        Jedis jedis = _pool.getResource();
        int locked = Integer.valueOf(jedis.eval(script, Arrays.asList(key, String.valueOf(expireTimeInMS), value), new ArrayList<String>()).toString());
        _pool.returnResource(jedis);
        if(locked == 1)
            return true;
        return false;
    }

    public void unlock(String key, String value) {
        Jedis jedis = _pool.getResource();
        try {
            if (jedis.get(key) == null) {
                return;
            }
            if (jedis.get(key).equalsIgnoreCase(value)) {
                jedis.del(key);
            }
            else {
                System.out.println("handle too long time,and lock is expired and does not exsit");
            }
        }
        finally {
            System.out.println("unlock");
            _pool.returnResource(jedis);
        }
    }

    public static void main(String[]args) {
        final String key = "lock";
        RedisLockManagerClient.getInstance().init("127.0.0.1", 6379);
        ExecutorService executor = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 3; i++) {
            executor.execute(new Runnable() {
                String value = UUID.randomUUID().toString();
                @Override
                public void run() {
                    try {
                        while (true) {
                            if (RedisLockManagerClient.getInstance().lock(key, value, 5000)) {
                                break;
                            }
                            System.out.println("wait lock");
                        }
                        try {
                            System.out.println("getlock");
                            Thread.sleep(200);
                            RedisLockManagerClient.getInstance().unlock(key, value);
                        } catch (Exception e) {
                            System.out.println("failed");
                        } finally {
                            RedisLockManagerClient.getInstance().unlock(key, value);
                        }

                    } catch (Throwable t) {
                        System.out.println("failed2");
                        t.printStackTrace();
                    }
                }
            });
        }
    }


}

