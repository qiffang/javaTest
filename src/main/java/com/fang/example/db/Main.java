package com.fang.example.db;

import com.fang.example.db.htree.FastIterator;
import com.fang.example.db.htree.HTree;
import com.fang.example.db.manager.RecordManager;
import com.fang.example.db.manager.RecordManagerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by andy on 6/26/16.
 */
public class Main {
    static RecordManager _recman;



    public static void queryAll(HTree hashtable)
            throws Exception
    {
        // Display content
        System.out.println( "contents: " );
        FastIterator iter = hashtable.keys();
        String name = (String) iter.next();
        while ( name != null ) {
            System.out.println(" " + name);
            name = (String) iter.next();
        }
        System.out.println();
    }


    public static void insert(HTree hashtable)
            throws Exception
    {
        // insert keys and values
        System.out.println();

        hashtable.put( "k1", "v1" );
        hashtable.put( "k2", "v2" );
        hashtable.put( "k3", "v3" );

        queryAll(hashtable);



        System.out.println();
        String key = (String) hashtable.get( "k1" );
        System.out.println( "key are " + key );

        _recman.commit();


        System.out.println();
        System.out.print( "Remove" );
        hashtable.remove( "k2" );
        _recman.commit();
        System.out.println( " done." );

       queryAll(hashtable);

        hashtable.remove("k1");
        _recman.rollback();

        queryAll(hashtable);

        // cleanup
        _recman.close();
    }





    public static void main( String[] args ) throws Exception {
        // create or open fruits record manager
        _recman = RecordManagerFactory.createRecordManager("db");


        long recid = _recman.getNamedObject( "test2" );
        HTree hashtable;
        if ( recid != 0 ) {
            System.out.println( "load Db." );
            hashtable = HTree.load( _recman, recid );
            queryAll(hashtable);
        } else {
            System.out.println( "Create DB." );
            hashtable = HTree.createInstance( _recman );
            _recman.setNamedObject( "test2", hashtable.getRecid() );
        }
        try {
            insert(hashtable);
        } catch ( IOException ioe ) {
            ioe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
