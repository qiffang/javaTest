package com.fang.example.db.client;

import com.fang.example.db.manager.PageManager;

import java.io.IOException;

/**
 * Created by andy on 6/26/16.
 */
public class PageCursor {
    PageManager pageman;
    long current;
    short type;

    /**
     *  Constructs a page cursor that starts at the indicated block.
     */
    public PageCursor(PageManager pageman, long current) {
        this.pageman = pageman;
        this.current = current;
    }

    /**
     *  Constructs a page cursor that starts at the first block
     *  of the indicated list.
     */
    public PageCursor(PageManager pageman, short type) throws IOException {
        this.pageman = pageman;
        this.type = type;
    }

    /**
     *  Returns the current value of the cursor.
     */
    public long getCurrent() throws IOException {
        return current;
    }

    /**
     *  Returns the next value of the cursor
     */
    public long next() throws IOException {
        if (current == 0)
            current = pageman.getFirst(type);
        else
            current = pageman.getNext(current);
        return current;
    }

    /**
     *  Returns the previous value of the cursor
     */
    long prev() throws IOException {
        current = pageman.getPrev(current);
        return current;
    }
}

