package com.fang.example.spring.di.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by andy on 4/6/16.
 */
public abstract class Entry {
    public void addEntries(Collection<Entry> entries) {

    };
    public abstract String write(Map<String/*className*/, Entry> map) throws Exception;
}
