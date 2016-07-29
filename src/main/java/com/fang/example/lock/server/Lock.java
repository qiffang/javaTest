package com.fang.example.lock.server;

import com.fang.example.lock.Pair;
import com.fang.example.lock.protocol.LockProtocol;
import com.fang.example.lock.reactor.ReactorExption;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;

/**
 * Created by andy on 5/18/16.
 */
public class Lock  implements LockProtocol {

    private ConcurrentHashMap<String/*key*/, Pair<String, Long>> _lockMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String/*key*/, ConcurrentLinkedQueue<Pair<String, Long>>> _waitMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String/*key_identify*/, CallBack> _waitCallBack = new ConcurrentHashMap<>();
    @Override
    public synchronized String tryLock(String key, String identify, Long expireTimeInMS) {
        Pair<String, Long> pair = _lockMap.get(key);
        if (pair != null) {
            if (!pair.getFirst().equals(identify)) {
                System.out.println("can not get lock-" + String.format(",key=%s, identify=%s", key, identify));
                return "false";
            }
        }
        System.out.println("Get lock-" + String.format(",key=%s, identify=%s", key, identify));
        return _addLock(key, identify, expireTimeInMS);

    }
    @Override
    public synchronized String lock(String key, String identify, Long expireTimeInMS) {
        Pair<String, Long> pair = _lockMap.get(key);
        if (pair != null) {
            if (!pair.getFirst().equals(identify)) {
                ConcurrentLinkedQueue<Pair<String,Long>> waitingList = null;

                if (!_waitMap.containsKey(key)) {
                    waitingList = new ConcurrentLinkedQueue<>();
                    _waitMap.put(key, waitingList);
                }
                else {
                    waitingList = _waitMap.get(key);
                }
                waitingList.add(new Pair<String, Long>(identify, expireTimeInMS));
                _waitCallBack.put(String.format("%s_%s", key, identify), new CallBack() {
                    @Override
                    public void call() {
                        return;
                    }
                });
                System.out.println("block to wait lock-" + String.format(",key=%s, identify=%s", key, identify));

            }
        }
        System.out.println("get lock - " + String.format(",key=%s, identify=%s", key, identify));
        return  _addLock(key, identify, expireTimeInMS);

    }
    @Override
    public synchronized String unLock(String key, String identify) {
        System.out.println("unlock");
        Pair<String, Long> pair = _lockMap.get(key);
        if (pair == null)
            return "true";

        if (pair.getFirst().equals(identify)) {
            _lockMap.remove(key);
            _addLock(key);

        }
        return "true";
    }

    private String _addLock(String key, String identify, long expireTimeInMS) {
        // if the lock does not exist in lockmap, add new lock
        // else reset the expireTime
        synchronized (_lockMap) {
            if (_lockMap.get(key) == null || _lockMap.get(key).getFirst().equals(identify)) {
                _lockMap.put(key, new Pair<String, Long>(identify, expireTimeInMS + System.currentTimeMillis()));
                return "true";
            }
            return "false";
        }
    }

    /**
     * take a lock from wait map, and add it to the lockMap
     */
    private synchronized  void _addLock(String key) {
//        ConcurrentLinkedQueue<Pair<String, Long>> waitingList = _waitMap.get(key);
//        ConcurrentHashMap<String, Condition> waitLockList = _waitLock.get(key);
//        if (waitingList != null) {
//            Pair<String, Long> nextPair = waitingList.peek();
//
//            if (_lockMap.get(key) == null || _lockMap.get(key).getFirst().equals(nextPair.getFirst())) {
//                _lockMap.put(key, new Pair<String, Long>(nextPair.getFirst(), nextPair.getSecond() + System.currentTimeMillis()));
//                waitingList.remove(nextPair);
//                _lock.lock();
//                try {
//                    Condition c = waitLockList.get(nextPair.getFirst());
//                    c.signal();
//                }
//                finally {
//                    _lock.unlock();
//                }
//
//            }
//        }
    }

    public static void main(String[]args) throws UnknownHostException {
//        ExecutorService executor = Executors.newFixedThreadPool(5);
//        final   Random r = new Random(1000);
//        for (int i = 0; i < 5; i++) {
//            executor.submit(new Runnable() {
//                @Override
//                public void run() {
//                    long threadId = Thread.currentThread().getId() + 20;
//                    int nex = r.nextInt();
//                    int s = nex > 0 ? nex :0  + 2000;
//                    System.out.println("threadId=" + threadId + ",sleep=" + s);
//                    _local.set(Integer.valueOf(String.valueOf(threadId)));
//                    try {
//                        Thread.sleep(s);
//                    } catch (Exception e) {
//
//                    }
//
//                    System.out.println("threadId=" + threadId + ",wakeup=" + _local.get());
//                }
//            });
//        }

//        CountDownLatch latch = new CountDownLatch(1);
//        while (true) {
//            latch.await(1, TimeUnit.SECONDS);
//            System.out.println("wait");
//        }

        System.out.println(System.currentTimeMillis() / 1000);

        System.out.println((System.currentTimeMillis() / 1000) - 3600);





    }

    public static ThreadLocal<Integer> _local = new ThreadLocal<>();

}
