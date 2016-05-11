package com.fang.example.java;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Created by fang on 3/27/16.
 */
public class LRU {
    public static void main(String[]args) throws UnknownHostException {

        String a = "";
        String[]test = a.split(",");
        for (int i = 0;i < test.length; i++) {
            System.out.println(test[i] + "-");
        }

        System.out.println(InetAddress.getLocalHost().getHostName());

        System.out.println(InetAddress.getLocalHost().getCanonicalHostName());


        List<String> testList = new LinkedList<>();
        testList.add("a");
        testList.add("b");
        Iterator<String> iterator = testList.iterator();
        while (iterator.hasNext()) {
            String str = iterator.next();
            if (str.equalsIgnoreCase("b"))
                iterator.remove();
        }

        testList.add("c");
        System.out.println(testList.size());

        HashMap<String, String> testHashMap = new HashMap<>();
        testHashMap.put("a", "b");

        Collection<String> collection = testHashMap.values();

        testHashMap.put("d", "c");

        testHashMap.clear();

        for (String str : collection) {
            System.out.print(str);
        }
        System.out.println(collection.size());


        HashMap<String, List<String>> _match = new HashMap<>();

        for (String tm : _match.get("a")) {

        }

    }
}
