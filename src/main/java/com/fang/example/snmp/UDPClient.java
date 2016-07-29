package com.fang.example.snmp;

import java.io.IOException;
import java.net.*;

/**
 * Created by andy on 6/13/16.
 */
public class UDPClient {
    public static void main(String[] args) throws IOException {

        InetAddress address = InetAddress.getByName("localhost");
        DatagramSocket socket = new DatagramSocket();
        String content = "content";
        byte[] buf = content.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 5678);
        socket.send(packet);

        socket.close();

    }
}
