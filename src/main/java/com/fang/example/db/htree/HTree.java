package com.fang.example.db.htree;

import com.fang.example.db.manager.RecordManager;

import java.io.IOException;

/**
 * Created by andy on 6/26/16.
 */
public class HTree
{

    /**
     * Root hash directory.
     */
    private HashDirectory _root;


    /**
     * Private constructor
     *
     * @param root Root hash directory.
     */
    private HTree( HashDirectory root ) {
        _root = root;
    }


    /**
     * Create a persistent hashtable.
     *
     * @param recman Record manager used for persistence.
     */
    public static HTree createInstance( RecordManager recman )
            throws IOException
    {
        HashDirectory  root;
        long           recid;

        root = new HashDirectory( (byte) 0 );
        recid = recman.insert( root );
        root.setPersistenceContext( recman, recid );

        return new HTree( root );
    }


    /**
     * Load a persistent hashtable
     *
     * @param recman RecordManager used to store the persistent hashtable
     * @param root_recid Record id of the root directory of the HTree
     */
    public static HTree load( RecordManager recman, long root_recid )
            throws IOException
    {
        HTree tree;
        HashDirectory root;

        root = (HashDirectory) recman.fetch( root_recid );
        root.setPersistenceContext( recman, root_recid );
        tree = new HTree( root );
        return tree;
    }


    /**
     * Associates the specified value with the specified key.
     *
     * @param key key with which the specified value is to be assocated.
     * @param value value to be associated with the specified key.
     */
    public synchronized void put(Object key, Object value)
            throws IOException
    {
        _root.put(key, value);
    }


    /**
     * Returns the value which is associated with the given key. Returns
     * <code>null</code> if there is not association for this key.
     *
     * @param key key whose associated value is to be returned
     */
    public synchronized Object get(Object key)
            throws IOException
    {
        return _root.get(key);
    }


    /**
     * Remove the value which is associated with the given key.  If the
     * key does not exist, this method simply ignores the operation.
     *
     * @param key key whose associated value is to be removed
     */
    public synchronized void remove(Object key)
            throws IOException
    {
        _root.remove(key);
    }


    /**
     * Returns an enumeration of the keys contained in this
     */
    public synchronized FastIterator keys()
            throws IOException
    {
        return _root.keys();
    }


    /**
     * Returns an enumeration of the values contained in this
     */
    public synchronized FastIterator values()
            throws IOException
    {
        return _root.values();
    }


    /**
     * Get the record identifier used to load this hashtable.
     */
    public long getRecid()
    {
        return _root.getRecid();
    }

}


