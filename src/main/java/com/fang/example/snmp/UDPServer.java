package com.fang.example.snmp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Created by andy on 6/13/16.
 */
public class UDPServer {
    public static void main(String[] args) throws IOException {
        DatagramSocket socket = new DatagramSocket(5678);
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, 100);
        socket.receive(packet);

        String content = new String(buffer);
        System.out.println(content);

    }
}
