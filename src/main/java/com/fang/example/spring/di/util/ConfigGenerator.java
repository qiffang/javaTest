package com.fang.example.spring.di.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.HashMap;

/**
 * Created by andy on 4/3/16.
 */
public class ConfigGenerator {
    public static void createSpringFile(String path, String name) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n");
        sb.append("<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" +
                "       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "       xsi:schemaLocation=\"\n" +
                "    http://www.springframework.org/schema/beans\n" +
                "    http://www.springframework.org/schema/beans/spring-beans.xsd\"> \n");
        EntryGenerator.addEntry(ConfigGenerator.class, BeanEntry.class, File.class);
        EntryGenerator.addEntry(EntryGenerator.class, BeanEntry.class, HashMap.class);
        for (Entry entry : EntryGenerator.map.values()) {
            sb.append(entry.write(EntryGenerator.map) + "\n");
        }
        sb.append("</beans>");
        File file = new File(String.format("%s%s%s", path ,File.separator, name));
        if (file.exists())
            FileUtils.forceDelete(file);
        FileUtils.write(file, sb.toString());
    }

    public static void main(String[]args) throws Exception {
        ConfigGenerator conf = new ConfigGenerator();
        conf.createSpringFile(System.getProperty("user.dir"), "test.xml");
    }
}
