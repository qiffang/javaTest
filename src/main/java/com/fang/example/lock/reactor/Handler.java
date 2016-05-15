package com.fang.example.lock.reactor;
import com.fang.example.lock.protocol.LockProtocol;
import com.fang.example.lock.server.LockManager;
import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by andy on 5/15/16.
 */
public class Handler implements Runnable {

    public int id;
    public Reader[] readers;

    public Handler(Reader[] readers) {
        this.readers = readers;
    }

    @Override
    public void run() {
        while (readers[id].running) {
            try {
                Call call = readers[id].queue.take();
//                Class clazz = Protocols.protocolMap.get(call.getProtocolName());
//                Class[] types;
//
//                if (call.getParaTypes() != null) {
//                    types = new Class[call.getParaTypes().length];
//                    for (int j = 0; j < types.length; j++) {
//                        types[j] = Protocols.protocolMap.get(call
//                                .getParaTypes()[j]);
//                    }
//                } else {
//                    types = null;
//                }

//                Method method = clazz.getMethod(call.getMethodName(), types);
//                Object result = method.invoke(clazz.newInstance(),
//                        call.getParas());
                String result = "";
                if (call.getProtocolName().equals("LockProtocol")) {
                    switch (call.getMethodName()) {
                        case "lock":
                            int len = call.getParas()[2].toString().indexOf('.');
                            result =  LockManager.getInstance().lock(call.getParas()[0].toString(), call.getParas()[1].toString(), Long.valueOf(call.getParas()[2].toString().substring(0, len)));
                            break;
                        case "tryLock":
                            int len2 = call.getParas()[2].toString().indexOf('.');
                            result = LockManager.getInstance().tryLock(call.getParas()[0].toString(), call.getParas()[1].toString(), Long.valueOf(call.getParas()[2].toString().substring(0, len2)));
                            break;
                        case "unLock":
                            result = LockManager.getInstance().unLock(call.getParas()[0].toString(), call.getParas()[1].toString());
                            break;
                    }
                }

                Gson gson = new Gson();
                String res = gson.toJson(result);;

                Response response = new Response("OK", res, call.getId());
                sendResponse(response, Protocols.channelMap.get(call.getIp()));
            } catch (Exception e) {
               throw new ReactorExption("error" + e.getMessage(), e);
            }
        }
    }

    public void sendResponse(Response response, SelectionKey key)
            throws IOException {
        Gson gson = new Gson();
        String s = gson.toJson(response);
        int len = s.length();

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        byte[] inttobyte = new byte[4];
        inttobyte = Util.intToByteArray(len);
        buffer.clear();
        buffer.put(inttobyte);
        buffer.put(s.getBytes());
        buffer.flip();

        SocketChannel channel = (SocketChannel) key.channel();

        while (buffer.hasRemaining()) {
            int num = channel.write(buffer);
        }
        key.interestOps(SelectionKey.OP_READ);
    }

}
