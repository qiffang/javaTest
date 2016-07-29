package com.fang.example.db.manager;


import java.io.IOException;
import java.util.Properties;

/**
 * Created by andy on 6/25/16.
 */
public final class RecordManagerFactory {
    /**
     * Create a record manager.
     *
     * @param name Name of the record file.
     * @throws java.io.IOException if an I/O related exception occurs while creating
     *                    or opening the record manager.
     * @throws UnsupportedOperationException if some options are not supported by the
     *                                      implementation.
     * @throws IllegalArgumentException if some options are invalid.
     */
    public static RecordManager createRecordManager( String name )
            throws IOException
    {
        RecordManager  recman;

        recman = new RecordManager( name );


        return recman;
    }
}
