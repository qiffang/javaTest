package com.fang.example.db.store;

import com.fang.example.db.constant.Magic;

/**
 * Created by andy on 6/26/16.
 */
public class FreePhysicalRowIdPage extends PageHeader {
    // offsets
    private static final short O_COUNT = PageHeader.SIZE; // short count
    static final short O_FREE = O_COUNT + Magic.SZ_SHORT;
    static final short ELEMS_PER_PAGE =
            (RecordFile.BLOCK_SIZE - O_FREE) / FreePhysicalRowId.SIZE;

    // slots we returned.
    FreePhysicalRowId[] slots = new FreePhysicalRowId[ELEMS_PER_PAGE];

    /**
     *  Constructs a data page view from the indicated block.
     */
    FreePhysicalRowIdPage(BlockIo block) {
        super(block);
    }

    /**
     *  Factory method to create or return a data page for the
     *  indicated block.
     */
    public static FreePhysicalRowIdPage getFreePhysicalRowIdPageView(BlockIo block) {
        BlockView view = block.getView();
        if (view != null && view instanceof FreePhysicalRowIdPage)
            return (FreePhysicalRowIdPage) view;
        else
            return new FreePhysicalRowIdPage(block);
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
        get(slot).setSize(0);
        setCount((short) (getCount() - 1));
    }

    /** Allocates a slot */
    public FreePhysicalRowId alloc(int slot) {
        setCount((short) (getCount() + 1));
        return get(slot);
    }

    /** Returns true if a slot is allocated */
    boolean isAllocated(int slot) {
        return get(slot).getSize() != 0;
    }

    /** Returns true if a slot is free */
    boolean isFree(int slot) {
        return !isAllocated(slot);
    }


    /** Returns the value of the indicated slot */
    public FreePhysicalRowId get(int slot) {
        if (slots[slot] == null)
            slots[slot] = new FreePhysicalRowId(block, slotToOffset(slot));;
        return slots[slot];
    }

    /** Converts slot to offset */
    short slotToOffset(int slot) {
        return (short) (O_FREE +
                (slot * FreePhysicalRowId.SIZE));
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
     *  Returns first slot with available size >= indicated size,
     *  or -1 if no slots are available.
     **/
    public int getFirstLargerThan(int size) {
        for (int i = 0; i < ELEMS_PER_PAGE; i++) {
            if (isAllocated(i) && get(i).getSize() >= size)
                return i;
        }
        return -1;
    }
}
