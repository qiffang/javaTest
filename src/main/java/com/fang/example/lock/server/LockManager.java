package com.fang.example.lock.server;

import com.fang.example.lock.Pair;
import com.fang.example.lock.protocol.LockProtocol;
import com.fang.example.lock.reactor.Acceptor;
import com.fang.example.lock.reactor.Handler;
import com.fang.example.lock.reactor.Protocols;
import com.fang.example.spring.di.util.Entry;
import org.elasticsearch.common.netty.channel.socket.ServerSocketChannel;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by andy on 5/12/16.
 */
public class LockManager implements LockProtocol {

    public final static int THREAD_NUM = 4;
    public static Acceptor accepter;
    public static Handler[] handlers;
    protected static ConcurrentHashMap<String/*lock key*/, Pair<String/*identify*/,Long/*expireTime*/>> _lockMap = new ConcurrentHashMap<>();
    protected static ConcurrentHashMap<String/*lock key*/, ConcurrentLinkedQueue<Pair<String/*identify*/,Long/*expireTime*/>>> _waitMap = new ConcurrentHashMap<>();
    private static LockManager _manager = null;
    public static  LockManager getInstance() {
        if (_manager != null)
            return _manager;
        synchronized (LockManager.class) {
            if (_manager != null)
                return _manager;
            _manager = new LockManager();
        }
        return _manager;
    }
    public static void registerProtocol(String name , Class clazz){
        Protocols.protocolMap.put(name, clazz);
    }

    public static void startServer(int port) throws IOException {
        accepter = new Acceptor(port);
        new Thread(accepter).start();

        handlers = new Handler[THREAD_NUM];
        for(int i = 0 ; i < THREAD_NUM; i++){
            Handler handler = new Handler(accepter.getReaders());
            handler.id = i;
            new Thread(handler).start();
        }
    }
    public static void stopServer(){
        accepter.setRunning(false);
        for(int i = 0 ; i < THREAD_NUM ; i++){
            accepter.getReaders()[i].running = false;
        }
    }

    private LockManager() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, new ThreadFactory() {
            final AtomicInteger _threadNumber = new AtomicInteger(1);
            @Override
            public Thread newThread(Runnable r) {
                ThreadGroup g = System.getSecurityManager() == null ? Thread.currentThread().getThreadGroup() : System.getSecurityManager().getThreadGroup();
                Thread t = new Thread(g,  r, "scan expire-" + "-" + this._threadNumber.getAndIncrement(), 0L);
                return t;
            }
        });

        executor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                Iterator<Map.Entry<String, Pair<String, Long>>> iterator = _lockMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Pair<String, Long>> entry = iterator.next();
                    Pair<String, Long> pair = entry.getValue();
                    long expireTime = pair.getSecond();

                    if (System.currentTimeMillis() >= expireTime) {
                        iterator.remove();
                        _addLock(entry.getKey());
                        continue;
                    }
                }
            }
        }, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public String tryLock(String key, String identify, Long expireTimeInMS) {
        Pair<String, Long> pair = _lockMap.get(key);
        if (pair != null) {
            if (!pair.getFirst().equals(identify)) {
                System.out.println("can not get lock-" + String.format(",key=%s, identify=%s", key, identify));
                return "false";
            }
        }
        System.out.println("Get lock-" + String.format(",key=%s, identify=%s", key, identify));
        return _addLock(key, identify, Long.valueOf(expireTimeInMS.toString()));

    }
    @Override
    public String lock(String key, String identify, Long expireTimeInMS) {
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

                waitingList.add(new Pair<String, Long>(identify, Long.valueOf(expireTimeInMS.toString())));
                System.out.println("block to wait lock-" + String.format(",key=%s, identify=%s", key, identify));
                return "false";
            }
        }
        System.out.println("get lock - " + String.format(",key=%s, identify=%s", key, identify));
        return  _addLock(key, identify, Long.valueOf(expireTimeInMS.toString()));

    }
    @Override
    public String unLock(String key, String identify) {
        System.out.println("unlock");
        Pair<String, Long> pair = _lockMap.get(key);
        if (pair == null)
            return "true";

        if (pair.getFirst().equals(identify)) {
            _lockMap.remove(key);
        }
        _addLock(key);
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
        ConcurrentLinkedQueue<Pair<String, Long>> waitingList = _waitMap.get(key);
        if (waitingList != null) {
            Pair<String, Long> nextPair = waitingList.peek();
            if (_lockMap.get(key) == null || _lockMap.get(key).getFirst().equals(nextPair.getFirst())) {
                _lockMap.put(key, new Pair<String, Long>(nextPair.getFirst(), nextPair.getSecond() + System.currentTimeMillis()));
                waitingList.remove(nextPair);
            }
        }
    }

    public static void main(String[]args) {
//        LockManager manager = new LockManager();
//        manager.init();
        LockManager.registerProtocol("LockProtocol", LockManager.class);
        try {
            LockManager.startServer(8889);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
