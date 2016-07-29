package com.fang.example.db.store;

import java.io.*;

/**
 * Created by andy on 6/25/16.
 */
public class BlockIo implements java.io.Externalizable {

    public final static long serialVersionUID = 2L;

    private long blockId;

    private transient byte[] data; // work area
    private transient BlockView view = null;
    private transient boolean dirty = false;
    private transient int transactionCount = 0;

    /**
     * Default constructor for serialization
     */
    public BlockIo() {
        // empty
    }

    /**
     *  Constructs a new BlockIo instance working on the indicated
     *  buffer.
     */
    BlockIo(long blockId, byte[] data) {
        // removeme for production version
        if (blockId > 10000000000L)
            throw new Error("bogus block id " + blockId);
        this.blockId = blockId;
        this.data = data;
    }

    /**
     *  Returns the underlying array
     */
    public byte[] getData() {
        return data;
    }

    /**
     *  Sets the block number. Should only be called by RecordFile.
     */
    void setBlockId(long id) {
        if (isInTransaction())
            throw new Error("BlockId assigned for transaction block");
        // removeme for production version
        if (id > 10000000000L)
            throw new Error("bogus block id " + id);
        blockId = id;
    }

    /**
     *  Returns the block number.
     */
    public long getBlockId() {
        return blockId;
    }

    /**
     *  Returns the current view of the block.
     */
    public BlockView getView() {
        return view;
    }

    /**
     *  Sets the current view of the block.
     */
    public void setView(BlockView view) {
        this.view = view;
    }

    /**
     *  Sets the dirty flag
     */
    void setDirty() {
        dirty = true;
    }

    /**
     *  Clears the dirty flag
     */
    public void setClean() {
        dirty = false;
    }

    /**
     *  Returns true if the dirty flag is set.
     */
    boolean isDirty() {
        return dirty;
    }

    /**
     *  Returns true if the block is still dirty with respect to the
     *  transaction log.
     */
    public boolean isInTransaction() {
        return transactionCount != 0;
    }

    /**
     *  Increments transaction count for this block, to signal that this
     *  block is in the log but not yet in the data file. The method also
     *  takes a snapshot so that the data may be modified in new transactions.
     */
    public synchronized void incrementTransactionCount() {
        transactionCount++;
        // @fixme(alex)
        setClean();
    }

    /**
     *  Decrements transaction count for this block, to signal that this
     *  block has been written from the log to the data file.
     */
    public synchronized void decrementTransactionCount() {
        transactionCount--;
        if (transactionCount < 0)
            throw new Error("transaction count on block "
                    + getBlockId() + " below zero!");

    }

    /**
     *  Reads a byte from the indicated position
     */
    public byte readByte(int pos) {
        return data[pos];
    }

    /**
     *  Writes a byte to the indicated position
     */
    public void writeByte(int pos, byte value) {
        data[pos] = value;
        setDirty();
    }

    /**
     *  Reads a short from the indicated position
     */
    public short readShort(int pos) {
        return (short)
                (((short) (data[pos+0] & 0xff) << 8) |
                        ((short) (data[pos+1] & 0xff) << 0));
    }

    /**
     *  Writes a short to the indicated position
     */
    public void writeShort(int pos, short value) {
        data[pos+0] = (byte)(0xff & (value >> 8));
        data[pos+1] = (byte)(0xff & (value >> 0));
        setDirty();
    }

    /**
     *  Reads an int from the indicated position
     */
    public int readInt(int pos) {
        return
                (((int)(data[pos+0] & 0xff) << 24) |
                        ((int)(data[pos+1] & 0xff) << 16) |
                        ((int)(data[pos+2] & 0xff) <<  8) |
                        ((int)(data[pos+3] & 0xff) <<  0));
    }

    /**
     *  Writes an int to the indicated position
     */
    public void writeInt(int pos, int value) {
        data[pos+0] = (byte)(0xff & (value >> 24));
        data[pos+1] = (byte)(0xff & (value >> 16));
        data[pos+2] = (byte)(0xff & (value >>  8));
        data[pos+3] = (byte)(0xff & (value >>  0));
        setDirty();
    }

    /**
     *  Reads a long from the indicated position
     */
    public long readLong( int pos )
    {
        // Contributed by Erwin Bolwidt <ejb@klomp.org>
        // Gives about 15% performance improvement
        return
                ( (long)( ((data[pos+0] & 0xff) << 24) |
                        ((data[pos+1] & 0xff) << 16) |
                        ((data[pos+2] & 0xff) <<  8) |
                        ((data[pos+3] & 0xff)      ) ) << 32 ) |
                        ( (long)( ((data[pos+4] & 0xff) << 24) |
                                ((data[pos+5] & 0xff) << 16) |
                                ((data[pos+6] & 0xff) <<  8) |
                                ((data[pos+7] & 0xff)      ) ) & 0xffffffff );
        /* Original version by Alex Boisvert.  Might be faster on 64-bit JVMs.
        return
            (((long)(data[pos+0] & 0xff) << 56) |
             ((long)(data[pos+1] & 0xff) << 48) |
             ((long)(data[pos+2] & 0xff) << 40) |
             ((long)(data[pos+3] & 0xff) << 32) |
             ((long)(data[pos+4] & 0xff) << 24) |
             ((long)(data[pos+5] & 0xff) << 16) |
             ((long)(data[pos+6] & 0xff) <<  8) |
             ((long)(data[pos+7] & 0xff) <<  0));
        */
    }

    /**
     *  Writes a long to the indicated position
     */
    public void writeLong(int pos, long value) {
        data[pos+0] = (byte)(0xff & (value >> 56));
        data[pos+1] = (byte)(0xff & (value >> 48));
        data[pos+2] = (byte)(0xff & (value >> 40));
        data[pos+3] = (byte)(0xff & (value >> 32));
        data[pos+4] = (byte)(0xff & (value >> 24));
        data[pos+5] = (byte)(0xff & (value >> 16));
        data[pos+6] = (byte)(0xff & (value >>  8));
        data[pos+7] = (byte)(0xff & (value >>  0));
        setDirty();
    }

    // overrides java.lang.Object

    public String toString() {
        return "BlockIO("
                + blockId + ","
                + dirty + ","
                + view + ")";
    }

    // implement externalizable interface
    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        blockId = in.readLong();
        int length = in.readInt();
        data = new byte[length];
        in.readFully(data);
    }

    // implement externalizable interface
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(blockId);
        out.writeInt(data.length);
        out.write(data);
    }

}

