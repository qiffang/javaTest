package com.fang.example.lock.client;

import com.fang.example.lock.protocol.LockProtocol;
import com.fang.example.lock.reactor.ReactorExption;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * Created by andy on 5/15/16.
 */
public class Client {

    private static ConcurrentHashMap<InetSocketAddress , Connection> connMap =
            new ConcurrentHashMap<InetSocketAddress , Connection>();
    private static ExecutorService pool = Executors.newCachedThreadPool();

    private static volatile int counter = 0;

    public static Connection getConnection(InetSocketAddress address) throws IOException{
        return connMap.get(address);
    }

    public static Object getInstance(Class clazz , InetSocketAddress address,int timeout) throws IOException{
        ProtocolHandler handler = new ProtocolHandler();
        handler.setAddress(address);
        handler.setTimeOut(timeout);
        if(!connMap.containsKey(address)){
            Connection conn = new Connection(address);
            connMap.put(address, conn);
            pool.execute(conn);
        }
        else {
            Connection isoldcon=connMap.get(address);
            if(isoldcon.running==false)
            {
                Connection conn = new Connection(address);
                connMap.put(address, conn);
                pool.execute(conn);
            }
        }
        return Proxy.newProxyInstance(clazz.getClassLoader(),
                new Class[]{clazz}, handler);
    }

    public static synchronized int getCounter(){
        if (counter < 1000 * 1000 * 1000) {
            return counter ++;
        }
        else {
            counter = 0;
            return counter;
        }
    }

    public static ExecutorService getPool() {
        return pool;
    }

    public static void shutdown(){
        //pool.shutdown();
        //Set set = connMap.entrySet();
        Enumeration<Connection> enumeration =connMap.elements();
        while(enumeration.hasMoreElements()){
            Connection conn = enumeration.nextElement();
            conn.running = false;
        }
    }

    public static void main(String[]args) throws IOException {
        final int bolckTime = 60 * 60;
        ExecutorService executor = Executors.newFixedThreadPool(4);
        final InetSocketAddress address = new InetSocketAddress("127.0.0.1", 8889);
        LockProtocol lock = null;
        try {
            lock = (LockProtocol) Client
                    .getInstance(LockProtocol.class, address,
                            bolckTime);
        } catch (IOException e) {
            throw new ReactorExption("error" + e.getMessage(), e);
        }
//        String locked = lock.lock("key", "clientid1", 40000l);
        String unlock = lock.unLock("key", "clientid2");
        System.out.println("status-" + unlock);

    }
}
