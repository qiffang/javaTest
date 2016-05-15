package com.fang.example.lock.reactor;

import com.google.gson.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.LogManager;

import static java.lang.Thread.currentThread;


/**
 * Created by andy on 5/15/16.
 */
public class Reader implements Runnable {
    public BlockingQueue<Call> queue = new LinkedBlockingQueue<>();
    private volatile boolean _adding = false;
    private Selector _readSelector;
    public boolean running = true;

    public Reader(Selector readSelector) {
        _readSelector = readSelector;
    }
    @Override
    public void run() {
        SelectionKey key = null;
        while (this.running) {
            synchronized (this) {
                try {
                    _readSelector.select();
                    while (_adding) {
                        wait();
                    }
                } catch (Exception e) {
                    throw new ReactorExption("error" + e.getMessage(), e);
                }
                Iterator<SelectionKey> it = _readSelector.selectedKeys()
                        .iterator();

                while (it.hasNext()) {
                    key = it.next();
                    it.remove();

                    if (key.isValid()) {
                        if (key.isReadable()) {
                            try {
                                read(key);

                            } catch (Exception e) {
                                throw new ReactorExption("error" + e.getMessage(), e);
                            }
                            key.interestOps(SelectionKey.OP_WRITE);
                        }
                    }
                    // key.cancel();
                }
                key = null;
            }
        }

        synchronized (this) {
            try {
                _readSelector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void read(SelectionKey key) throws IOException, InterruptedException {
        SocketChannel channel = (SocketChannel) key.channel();
        InetSocketAddress address = (InetSocketAddress) channel
                .getRemoteAddress();
        if (!Protocols.channelMap.contains((InetSocketAddress) channel
                .getRemoteAddress())) {
            Protocols.channelMap.put(address.getHostName(), key);
        }
        ByteBuffer buffer = (ByteBuffer) key.attachment();


        int k = channel.read(buffer);
        buffer.flip();

        if (k > 0) {
            byte[] head = new byte[4];
            buffer.get(head, 0, 4);
            int length = Util.byteArrayToInt(head);

            byte[] call = new byte[length];
            buffer.get(call, 0, length);
            String s = new String(call);
            Gson gson = new Gson();
            Call c = gson.fromJson(s.toString(), Call.class);
            this.queue.put(c);
        }
        buffer.clear();

    }

    public synchronized SelectionKey addChannel(SocketChannel channel,
                                                int action) throws ClosedChannelException {

        return channel.register(_readSelector, action);
    }

    public synchronized void stopAdding() {
        _adding = false;
        this.notify();
    }

    public void setAdding(boolean adding) {
        _adding = adding;
    }

    public Selector getReadSelector() {
        return _readSelector;
    }

}
