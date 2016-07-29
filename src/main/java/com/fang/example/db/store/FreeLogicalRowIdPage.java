package com.fang.example.db.store;

import com.fang.example.db.constant.Magic;

/**
 * Created by andy on 6/26/16.
 */
public class FreeLogicalRowIdPage extends PageHeader {
    // offsets
    private static final short O_COUNT = PageHeader.SIZE; // short count
    static final short O_FREE = (short)(O_COUNT + Magic.SZ_SHORT);
    static final short ELEMS_PER_PAGE = (short)
            ((RecordFile.BLOCK_SIZE - O_FREE) / PhysicalRowId.SIZE);

    // slots we returned.
    final PhysicalRowId[] slots = new PhysicalRowId[ELEMS_PER_PAGE];

    /**
     *  Constructs a data page view from the indicated block.
     */
    FreeLogicalRowIdPage(BlockIo block) {
        super(block);
    }

    /**
     *  Factory method to create or return a data page for the
     *  indicated block.
     */
    public static FreeLogicalRowIdPage getFreeLogicalRowIdPageView(BlockIo block) {

        BlockView view = block.getView();
        if (view != null && view instanceof FreeLogicalRowIdPage)
            return (FreeLogicalRowIdPage) view;
        else
            return new FreeLogicalRowIdPage(block);
    }

    /** Returns the number of free rowids */
    public short getCount() {
        return block.readShort(O_COUNT);
    }

    /** Sets the number of free rowids */
    private void setCount(short i) {
        block.writeShort(O_COUNT, i);
    }

    /** Frees a slot */
    public void free(int slot) {
        get(slot).setBlock(0);
        setCount((short) (getCount() - 1));
    }

    /** Allocates a slot */
    public PhysicalRowId alloc(int slot) {
        setCount((short) (getCount() + 1));
        get(slot).setBlock(-1);
        return get(slot);
    }

    /** Returns true if a slot is allocated */
    boolean isAllocated(int slot) {
        return get(slot).getBlock() > 0;
    }

    /** Returns true if a slot is free */
    boolean isFree(int slot) {
        return !isAllocated(slot);
    }


    /** Returns the value of the indicated slot */
    public PhysicalRowId get(int slot) {
        if (slots[slot] == null)
            slots[slot] = new PhysicalRowId(block, slotToOffset(slot));;
        return slots[slot];
    }

    /** Converts slot to offset */
    private short slotToOffset(int slot) {
        return (short) (O_FREE +
                (slot * PhysicalRowId.SIZE));
    }

    /**
     *  Returns first free slot, -1 if no slots are available
     */
    public int getFirstFree() {
        for (int i = 0; i < ELEMS_PER_PAGE; i++) {
            if (isFree(i))
                return i;
        }
        return -1;
    }

    /**
     *  Returns first allocated slot, -1 if no slots are available.
     */
    public int getFirstAllocated() {
        for (int i = 0; i < ELEMS_PER_PAGE; i++) {
            if (isAllocated(i))
                return i;
        }
        return -1;
    }
}

