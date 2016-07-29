package com.fang.example.db.store;

import com.fang.example.db.constant.Magic;

/**
 * Created by andy on 6/26/16.
 */
public class FreePhysicalRowId extends PhysicalRowId{
    // offsets
    private static final short O_SIZE = PhysicalRowId.SIZE; // int size
    static final short SIZE = O_SIZE + Magic.SZ_INT;

    /**
     *  Constructs a physical rowid from the indicated data starting at
     *  the indicated position.
     */
    FreePhysicalRowId(BlockIo block, short pos) {
        super(block, pos);
    }

    /** Returns the size */
    public int getSize() {
        return block.readInt(pos + O_SIZE);
    }

    /** Sets the size */
    public void setSize(int value) {
        block.writeInt(pos + O_SIZE, value);
    }
}
