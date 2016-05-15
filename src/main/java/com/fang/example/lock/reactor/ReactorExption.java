package com.fang.example.lock.reactor;

/**
 * Created by andy on 5/15/16.
 */
public class ReactorExption extends RuntimeException {
    public ReactorExption(String msg) {
        super(msg);
    }
    public ReactorExption(String msg, Throwable clause) {
        super(msg, clause);
    }
}
