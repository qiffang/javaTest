package com.fang.example.lock;

/**
 * Created by andy on 5/12/16.
 */
public class Pair<T1, T2> {
    private final T1 _first;
    private final T2 _second;

    public Pair(T1 first, T2 second) {
        this._first = first;
        this._second = second;
    }

    public T1 getFirst() {
        return this._first;
    }

    public T2 getSecond() {
        return this._second;
    }
}
