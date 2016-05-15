package com.fang.example.lock.client;
import com.fang.example.lock.reactor.Call;
import com.fang.example.lock.reactor.Response;
import com.fang.example.lock.reactor.Util;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
/**
 * Created by andy on 5/15/16.
 */
public class Connection implements Runnable {
    public boolean running;
    private InetSocketAddress address; //server address
    private SocketChannel channel;
    private Selector connSelector;
    private SelectionKey key;

    public Connection(InetSocketAddress address) throws IOException{
        this.running = true;
        this.connSelector = Selector.open();
        this.address = address;
        this.channel = SocketChannel.open();
        this.channel.configureBlocking(false);
        this.channel.socket().setTcpNoDelay(true);
        this.key = this.channel.register(connSelector, SelectionKey.OP_READ);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        this.key.attach(buffer);
        channel.connect(address);
        while(!channel.finishConnect())
            ;
    }

    private ConcurrentHashMap<Integer , Response> resultMap =
            new ConcurrentHashMap<Integer , Response>();

    @Override
    public void run() {
        // TODO Auto-generated method stub

        SelectionKey key = null;
        while(running){
            try {
                connSelector.select();
                Iterator it = connSelector.selectedKeys().iterator();

                while(it.hasNext()){
                    key = (SelectionKey) it.next();
                    it.remove();

                    if(key.isValid()){
                        if(key.isReadable()){
                            readResponse(key);
                        }
                    }
                    key.interestOps(SelectionKey.OP_WRITE);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            channel.close();
            connSelector.close();
            resultMap = null;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void readResponse(SelectionKey key) throws IOException{
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        int k = channel.read(buffer);
        buffer.flip();

        if(k > 0){
            byte[] head = new byte[4];
            buffer.get(head, 0, 4);
            int length = Util.byteArrayToInt(head);

            byte[] call = new byte[length];
            buffer.get(call, 0, length);
            String s = new String(call);
            Gson gson = new Gson();
            Response c = gson.fromJson(s.toString(), Response.class);
            this.resultMap.put(c.getId() , c);
        }
        buffer.clear();

    }

    public synchronized void sendCall(Call call) throws IOException{
        Gson gson = new Gson();
        String c = gson.toJson(call);
        int len = c.length();

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        byte[] inttobyte = new byte[4];
        inttobyte =Util.intToByteArray(len);
        buffer.put(inttobyte);
        buffer.put(c.getBytes());
        buffer.flip();
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
        buffer.clear();

        this.key.interestOps(SelectionKey.OP_READ);
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public void setAddress(InetSocketAddress address) {
        this.address = address;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public void setChannel(SocketChannel channel) {
        this.channel = channel;
    }

    public Selector getConnSelector() {
        return connSelector;
    }

    public void setConnSelector(Selector connSelector) {
        this.connSelector = connSelector;
    }

    public ConcurrentHashMap<Integer, Response> getResultMap() {
        return resultMap;
    }

    public void setResultMap(ConcurrentHashMap<Integer, Response> resultMap) {
        this.resultMap = resultMap;
    }
}
