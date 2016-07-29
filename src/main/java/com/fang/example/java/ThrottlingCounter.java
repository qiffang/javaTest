package com.fang.example.java;

import java.util.LinkedList;

/**
 * Created by andy on 6/17/16.
 */
public class ThrottlingCounter {
    public static final int BUCKET_NUM = 10;
    /**
     * Ratelimit minutes
     */
    private int _periodInMin;
    private int _num;
    private LinkedList<Node> _bins = null;
    public ThrottlingCounter(int perild, int num) {

    }


    static private class Node {
        public Node(long ord) {
            this.ord = ord;
            this.tokens = 0;
        }

        public long ord;
        public int tokens = 0;
    }

    private long _getCurrent() {
        int secPerBin = _periodInMin * 60 / 10;

        return (System.currentTimeMillis() / 1000) / secPerBin;
    }

}
