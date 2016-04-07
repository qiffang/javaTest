package com.fang.example.spring.di.util;

import org.apache.commons.codec.binary.Base64;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Created by andy on 4/6/16.
 * Entry Generator to get
 */
public class EntryGenerator {
    public static Map<String/*className*/, Entry/*beanId*/> map = new HashMap<>();
    /**
     * create bean entry,
     * id is the bean id
     * className is the className
     * if thereis not relation entry in beanEntry, set is null
     */
    public static void addEntry(String id, Class cls, Class<? extends Entry> entryCls, Class...classes) throws Exception  {
        Constructor constructor = entryCls.getDeclaredConstructor(String.class, String.class, List.class);
        constructor.setAccessible(true);
        List<String> relList = new ArrayList<>();
        for (Class relcls : classes) {
            relList.add(relcls.getName());
        }
        map.put(cls.getName(), (Entry) constructor.newInstance(id, cls.getName(), relList));
    }
    public static void addEntry(Class cls, Class<? extends Entry> entryCls, Class...classes) throws Exception {
        String id = encodeBase64(UUID.randomUUID());
        addEntry(id, cls, entryCls, classes);
    }

    public static String encodeBase64(UUID uuid) {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return Base64.encodeBase64URLSafeString(buffer.array());
    }

    private static String _createId() {
        int[] array = {0,1,2,3,4,5,6,7,8,9};
        Random rand = new Random();
        for (int i = 10; i > 1; i--) {
            int index = rand.nextInt(i);
            int tmp = array[index];
            array[index] = array[i - 1];
            array[i - 1] = tmp;
        }
        int result = 0;
        for(int i = 0; i < 6; i++)
            result = result * 10 + array[i];
        return String.valueOf(result);
    }


    public static void main(String[]args) throws Exception {
        Constructor constructor = BeanEntry.class.getDeclaredConstructor(String.class, String.class, List.class);
        constructor.setAccessible(true);
        BeanEntry entry = (BeanEntry) constructor.newInstance(new Object[]{"a", "b" ,null});
        System.out.println(entry.write(null));
    }
}
