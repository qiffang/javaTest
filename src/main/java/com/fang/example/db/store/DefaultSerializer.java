package com.fang.example.db.store;

import java.io.IOException;

/**
 * Created by andy on 6/26/16.
 */
public class DefaultSerializer
        implements Serializer
{


    public static final DefaultSerializer INSTANCE = new DefaultSerializer();


    /**
     * Construct a DefaultSerializer.
     */
    public DefaultSerializer()
    {
        // no op
    }


    /**
     * Serialize the content of an object into a byte array.
     *
     * @param obj Object to serialize
     * @return a byte array representing the object's state
     */
    public byte[] serialize( Object obj )
            throws IOException
    {
        return Serialization.serialize( obj );
    }


    /**
     * Deserialize the content of an object from a byte array.
     *
     * @param serialized Byte array representation of the object
     * @return deserialized object
     */
    public Object deserialize( byte[] serialized )
            throws IOException
    {
        try {
            return Serialization.deserialize( serialized );
        } catch ( ClassNotFoundException except ) {
            throw new RuntimeException( except );
        }
    }

}
