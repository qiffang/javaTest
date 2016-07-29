package com.fang.example.db.store;

import com.fang.example.db.constant.Magic;

/**
 * Created by andy on 6/26/16.
 */
public class PhysicalRowId {
    // offsets
    private static final short O_BLOCK = 0; // long block
    private static final short O_OFFSET = Magic.SZ_LONG; // short offset
    public static final int SIZE = O_OFFSET + Magic.SZ_SHORT;

    // my block and the position within the block
    BlockIo block;
    short pos;

    /**
     *  Constructs a physical rowid from the indicated data starting at
     *  the indicated position.
     */
    PhysicalRowId(BlockIo block, short pos) {
        this.block = block;
        this.pos = pos;
    }

    /** Returns the block number */
    long getBlock() {
        return block.readLong(pos + O_BLOCK);
    }

    /** Sets the block number */
    public void setBlock(long value) {
        block.writeLong(pos + O_BLOCK, value);
    }

    /** Returns the offset */
    short getOffset() {
        return block.readShort(pos + O_OFFSET);
    }

    /** Sets the offset */
    public void setOffset(short value) {
        block.writeShort(pos + O_OFFSET, value);
    }
}