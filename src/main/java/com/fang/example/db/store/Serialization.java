package com.fang.example.db.store;

import java.io.*;

/**
 * Created by andy on 6/26/16.
 */
public final class Serialization
{

    /**
     * Serialize the object into a byte array.
     */
    public static byte[] serialize( Object obj )
            throws IOException
    {
        ByteArrayOutputStream baos;
        ObjectOutputStream oos;

        baos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream( baos );
        oos.writeObject( obj );
        oos.close();

        return baos.toByteArray();
    }


    /**
     * Deserialize an object from a byte array
     */
    public static Object deserialize( byte[] buf )
            throws ClassNotFoundException, IOException
    {
        ByteArrayInputStream bais;
        ObjectInputStream ois;

        bais = new ByteArrayInputStream( buf );
        ois = new ObjectInputStream( bais );
        return ois.readObject();
    }

}

