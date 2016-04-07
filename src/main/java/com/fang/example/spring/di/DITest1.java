package com.fang.example.spring.di;

import com.fang.example.spring.di.util.BeanEntry;
import com.fang.example.spring.di.util.ConfigGenerator;
import com.fang.example.spring.di.util.EntryGenerator;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Created by andy on 4/2/16.
 */
public class DITest1 {
    public static void main(String[]args) throws Exception {
        String classPath = System.getProperty("java.class.path");
        System.out.println("classpath=" + classPath);
        EntryGenerator.addEntry(ConfigGenerator.class, BeanEntry.class);
        EntryGenerator.addEntry("quest", RecuseDamselQuest.class, BeanEntry.class);
        EntryGenerator.addEntry("knight", BraveKnight.class, BeanEntry.class, RecuseDamselQuest.class);
        ConfigGenerator.createSpringFile(System.getProperty("user.dir"), "knight.xml");
        FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext("knight.xml");
        Knight knight = (Knight) context.getBean("knight");
        knight.embarkOnquest();
    }
}
