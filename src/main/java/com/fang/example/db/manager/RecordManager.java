package com.fang.example.db.manager;

import com.fang.example.db.store.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by andy on 6/25/16.
 */
public final class RecordManager
{

    /**
     * Underlying record file.
     */
    private RecordFile _file;


    /**
     * Physical row identifier manager.
     */
    private PhysicalRowIdManager _physMgr;


    /**
     * Logigal to Physical row identifier manager.
     */
    private LogicalRowIdManager _logMgr;


    /**
     * Page manager.
     */
    private PageManager _pageman;


    /**
     * Reserved slot for name directory.
     */
    public static final int NAME_DIRECTORY_ROOT = 0;


    /**
     * Directory of named JDBMHashtables.  This directory is a persistent
     * directory, stored as a Hashtable.  It can be retrived by using
     * the NAME_DIRECTORY_ROOT.
     */
    private Map _nameDirectory;


    /**
     *  Creates a record manager for the indicated file
     *
     *  @throws IOException when the file cannot be opened or is not
     *          a valid file content-wise.
     */
    public RecordManager( String filename )
            throws IOException
    {
        _file = new RecordFile( filename );
        _pageman = new PageManager( _file );
        _physMgr = new PhysicalRowIdManager( _file, _pageman );
        _logMgr = new LogicalRowIdManager( _file, _pageman );
    }


    /**
     *  Switches off transactioning for the record manager. This means
     *  that a) a transaction log is not kept, and b) writes aren't
     *  synch'ed after every update. This is useful when batch inserting
     *  into a new database.
     *  <p>
     *  Only call this method directly after opening the file, otherwise
     *  the results will be undefined.
     */
    public synchronized void disableTransactions()
    {
        checkIfClosed();

        _file.disableTransactions();
    }


    /**
     *  Closes the record manager.
     *
     *  @throws IOException when one of the underlying I/O operations fails.
     */
    public synchronized void close()
            throws IOException
    {
        checkIfClosed();

        _pageman.close();
        _pageman = null;

        _file.close();
        _file = null;
    }


    /**
     *  Inserts a new record using standard java object serialization.
     *
     *  @param obj the object for the new record.
     *  @return the rowid for the new record.
     *  @throws IOException when one of the underlying I/O operations fails.
     */
    public long insert( Object obj )
            throws IOException
    {
        return insert( obj, DefaultSerializer.INSTANCE );
    }


    /**
     *  Inserts a new record using a custom serializer.
     *
     *  @param obj the object for the new record.
     *  @param serializer a custom serializer
     *  @return the rowid for the new record.
     *  @throws IOException when one of the underlying I/O operations fails.
     */
    public synchronized long insert( Object obj, Serializer serializer )
            throws IOException
    {
        byte[]    data;
        long      recid;
        Location physRowId;

        checkIfClosed();

        data = serializer.serialize( obj );
        physRowId = _physMgr.insert( data, 0, data.length );
        recid = _logMgr.insert( physRowId ).toLong();
        return recid;
    }

    /**
     *  Deletes a record.
     *
     *  @param recid the rowid for the record that should be deleted.
     *  @throws IOException when one of the underlying I/O operations fails.
     */
    public synchronized void delete( long recid )
            throws IOException
    {
        checkIfClosed();
        if ( recid <= 0 ) {
            throw new IllegalArgumentException( "Argument 'recid' is invalid: "
                    + recid );
        }

        Location logRowId = new Location( recid );
        Location physRowId = _logMgr.fetch( logRowId );
        _physMgr.delete( physRowId );
        _logMgr.delete( logRowId );
    }


    /**
     *  Updates a record using standard java object serialization.
     *
     *  @param recid the recid for the record that is to be updated.
     *  @param obj the new object for the record.
     *  @throws IOException when one of the underlying I/O operations fails.
     */
    public void update( long recid, Object obj )
            throws IOException
    {
        update( recid, obj, DefaultSerializer.INSTANCE );
    }


    /**
     *  Updates a record using a custom serializer.
     *
     *  @param recid the recid for the record that is to be updated.
     *  @param obj the new object for the record.
     *  @param serializer a custom serializer
     *  @throws IOException when one of the underlying I/O operations fails.
     */
    public synchronized void update( long recid, Object obj, Serializer serializer )
            throws IOException
    {
        checkIfClosed();
        if ( recid <= 0 ) {
            throw new IllegalArgumentException( "Argument 'recid' is invalid: "
                    + recid );
        }

        Location logRecid = new Location( recid );
        Location physRecid = _logMgr.fetch( logRecid );

        byte[] data = serializer.serialize( obj );

        Location newRecid = _physMgr.update( physRecid, data, 0, data.length );
        if ( ! newRecid.equals( physRecid ) ) {
            _logMgr.update( logRecid, newRecid );
        }
    }


    /**
     *  Fetches a record using standard java object serialization.
     *
     *  @param recid the recid for the record that must be fetched.
     *  @return the object contained in the record.
     *  @throws IOException when one of the underlying I/O operations fails.
     */
    public Object fetch( long recid )
            throws IOException
    {
        return fetch( recid, DefaultSerializer.INSTANCE );
    }


    /**
     *  Fetches a record using a custom serializer.
     *
     *  @param recid the recid for the record that must be fetched.
     *  @param serializer a custom serializer
     *  @return the object contained in the record.
     *  @throws IOException when one of the underlying I/O operations fails.
     */
    public synchronized Object fetch( long recid, Serializer serializer )
            throws IOException
    {
        byte[] data;

        checkIfClosed();
        if ( recid <= 0 ) {
            throw new IllegalArgumentException( "Argument 'recid' is invalid: "
                    + recid );
        }
        data = _physMgr.fetch( _logMgr.fetch( new Location( recid ) ) );

        return serializer.deserialize( data );
    }


    /**
     *  Returns the number of slots available for "root" rowids. These slots
     *  can be used to store special rowids, like rowids that point to
     *  other rowids. Root rowids are useful for bootstrapping access to
     *  a set of data.
     */
    public int getRootCount()
    {
        return FileHeader.NROOTS;
    }

    /**
     *  Returns the indicated root rowid.
     *
     *  @see #getRootCount
     */
    public synchronized long getRoot( int id )
            throws IOException
    {
        checkIfClosed();

        return _pageman.getFileHeader().getRoot( id );
    }


    /**
     *  Sets the indicated root rowid.
     *
     *  @see #getRootCount
     */
    public synchronized void setRoot( int id, long rowid )
            throws IOException
    {
        checkIfClosed();

        _pageman.getFileHeader().setRoot( id, rowid );
    }


    /**
     * Obtain the record id of a named object. Returns 0 if named object
     * doesn't exist.
     */
    public long getNamedObject( String name )
            throws IOException
    {
        checkIfClosed();

        Map nameDirectory = getNameDirectory();
        Long recid = (Long) nameDirectory.get( name );
        if ( recid == null ) {
            return 0;
        }
        return recid.longValue();
    }

    /**
     * Set the record id of a named object.
     */
    public void setNamedObject( String name, long recid )
            throws IOException
    {
        checkIfClosed();

        Map nameDirectory = getNameDirectory();
        if ( recid == 0 ) {
            // remove from hashtable
            nameDirectory.remove( name );
        } else {
            nameDirectory.put( name, new Long( recid ) );
        }
        saveNameDirectory( nameDirectory );
    }


    /**
     * Commit (make persistent) all changes since beginning of transaction.
     */
    public synchronized void commit()
            throws IOException
    {
        checkIfClosed();

        _pageman.commit();
    }


    /**
     * Rollback (cancel) all changes since beginning of transaction.
     */
    public synchronized void rollback()
            throws IOException
    {
        checkIfClosed();

        _pageman.rollback();
    }


    /**
     * Load name directory
     */
    private Map getNameDirectory()
            throws IOException
    {
        // retrieve directory of named hashtable
        long nameDirectory_recid = getRoot( NAME_DIRECTORY_ROOT );
        if ( nameDirectory_recid == 0 ) {
            _nameDirectory = new HashMap();
            nameDirectory_recid = insert( _nameDirectory );
            setRoot( NAME_DIRECTORY_ROOT, nameDirectory_recid );
        } else {
            _nameDirectory = (Map) fetch( nameDirectory_recid );
        }
        return _nameDirectory;
    }


    private void saveNameDirectory( Map directory )
            throws IOException
    {
        long recid = getRoot( NAME_DIRECTORY_ROOT );
        if ( recid == 0 ) {
            throw new IOException( "Name directory must exist" );
        }
        update( recid, _nameDirectory );
    }


    /**
     * Check if RecordManager has been closed.  If so, throw an
     * IllegalStateException.
     */
    private void checkIfClosed()
            throws IllegalStateException
    {
        if ( _file == null ) {
            throw new IllegalStateException( "RecordManager has been closed" );
        }
    }
}

