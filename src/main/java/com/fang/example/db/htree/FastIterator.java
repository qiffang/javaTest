package com.fang.example.db.htree;

/**
 * Created by andy on 6/26/16.
 */
public abstract class FastIterator
{

    /**
     * Returns the next element in the interation.
     *
     * @return the next element in the iteration, or null if no more element.
     */
    public abstract Object next()
            throws Exception;

}
