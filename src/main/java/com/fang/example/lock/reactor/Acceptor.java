package com.fang.example.lock.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by andy on 5/15/16.
 */
public class Acceptor implements Runnable{
    public final static int THREAD_POOL_NUM = 4;
    private Reader[] _readers = new Reader[THREAD_POOL_NUM];
    private ExecutorService _pool = Executors.newFixedThreadPool(THREAD_POOL_NUM);
    private int _counter = 0;
    private boolean _running = true;
    private ServerSocketChannel _acceptChannel;
    private Selector _selector;

    public Acceptor(int port) throws IOException {
        _acceptChannel = ServerSocketChannel.open();
        _acceptChannel.configureBlocking(false);
        _selector = Selector.open();
        _acceptChannel.bind(new InetSocketAddress(port));

        for(int i = 0 ; i < THREAD_POOL_NUM ; i++){
            Selector readSelector = Selector.open();
            Reader reader = new Reader(readSelector);
            _readers[i] = reader;
            _pool.execute(reader);
        }

        _acceptChannel.register(_selector, SelectionKey.OP_ACCEPT);
    }

    @Override
    public void run() {
        while(_running){
            SelectionKey key = null;
            try {
                _selector.select();
                Iterator<SelectionKey> it = _selector.selectedKeys().iterator();
                while(it.hasNext()){
                    key = it.next();
                    it.remove();
                    if(key.isValid()){
                        if(key.isAcceptable()){
                            accept(key);
                        }
                    }
                }
                key = null;
            } catch (IOException e) {
                throw new ReactorExption("error" + e.getMessage(), e);
            }
        }

        synchronized(this){
            try {
                _acceptChannel.close();
                _selector.close();
                _pool.shutdown();
            } catch (IOException e) {

                e.printStackTrace();
            }
            _selector = null;
            _acceptChannel = null;
            _pool = null;
        }

        System.out.println("Accepter stoped");
    }

    /**
     * register channel to reader selector
     */
    public void accept(SelectionKey key) throws IOException{
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel channel;
        Reader reader;
        SelectionKey rkey = null;
        while((channel = server.accept()) != null){
            channel.configureBlocking(false);
            channel.socket().setTcpNoDelay(true);
            if(_counter > 1000000)
                _counter = 0;
            reader = _readers[_counter % THREAD_POOL_NUM];
            try {
                reader.setAdding(true);
                reader.getReadSelector().wakeup();
                rkey = reader.addChannel(channel , SelectionKey.OP_READ);
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                rkey.attach(readBuffer);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                reader.stopAdding();
            }
            _counter++;
        }
    }
    public Reader[] getReaders() {
        return _readers;
    }
    public void setRunning(boolean running) {
        _running = running;
    }

}
