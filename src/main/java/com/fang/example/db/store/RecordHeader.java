package com.fang.example.db.store;

import com.fang.example.db.constant.Magic;

/**
 * Created by andy on 6/26/16.
 */
public class RecordHeader {
    // offsets
    private static final short O_CURRENTSIZE = 0; // int currentSize
    private static final short O_AVAILABLESIZE = Magic.SZ_INT; // int availableSize
    public static final int SIZE = O_AVAILABLESIZE + Magic.SZ_INT;

    // my block and the position within the block
    private BlockIo block;
    private short pos;

    /**
     *  Constructs a record header from the indicated data starting at
     *  the indicated position.
     */
    public RecordHeader(BlockIo block, short pos) {
        this.block = block;
        this.pos = pos;
        if (pos > (RecordFile.BLOCK_SIZE - SIZE))
            throw new Error("Offset too large for record header ("
                    + block.getBlockId() + ":"
                    + pos + ")");
    }

    /** Returns the current size */
    public int getCurrentSize() {
        return block.readInt(pos + O_CURRENTSIZE);
    }

    /** Sets the current size */
    public void setCurrentSize(int value) {
        block.writeInt(pos + O_CURRENTSIZE, value);
    }

    /** Returns the available size */
    public int getAvailableSize() {
        return block.readInt(pos + O_AVAILABLESIZE);
    }

    /** Sets the available size */
    public void setAvailableSize(int value) {
        block.writeInt(pos + O_AVAILABLESIZE, value);
    }

    // overrides java.lang.Object
    public String toString() {
        return "RH(" + block.getBlockId() + ":" + pos
                + ", avl=" + getAvailableSize()
                + ", cur=" + getCurrentSize()
                + ")";
    }
}
