package com.fang.example.log;

/**
 * Created by andy on 6/14/16.
 */
public class Logger2 {
    public void print() {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();

        for (int i = 1; i < elements.length; i++) {
            System.out.println(elements[i].getClassName());
        }


    }
}
