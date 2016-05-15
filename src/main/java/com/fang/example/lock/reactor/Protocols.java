package com.fang.example.lock.reactor;

import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by andy on 5/15/16.
 */
public class Protocols {
    public static HashMap<String , Class> protocolMap =
            new HashMap<String , Class>();

    public static ConcurrentHashMap<String , SelectionKey> channelMap=
            new ConcurrentHashMap<String , SelectionKey>();

    static {
        Protocols.protocolMap.put("String", String.class);
        Protocols.protocolMap.put("Double", Double.class);
        Protocols.protocolMap.put("int" , int.class);
    }
}
