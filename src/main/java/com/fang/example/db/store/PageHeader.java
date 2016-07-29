package com.fang.example.db.store;

import com.fang.example.db.constant.Magic;

/**
 * Created by andy on 6/25/16.
 */
public class PageHeader implements BlockView {
    // offsets
    private static final short O_MAGIC = 0; // short magic
    private static final short O_NEXT = Magic.SZ_SHORT;  // long next
    private static final short O_PREV = O_NEXT + Magic.SZ_LONG; // long prev
    protected static final short SIZE = O_PREV + Magic.SZ_LONG;

    // my block
    protected BlockIo block;

    /**
     *  Constructs a PageHeader object from a block
     *
     *  @param block The block that contains the file header
     *  @throws java.io.IOException if the block is too short to keep the file
     *          header.
     */
    protected PageHeader(BlockIo block) {
        initialize(block);
        if (!magicOk())
            throw new Error("CRITICAL: page header magic for block "
                    + block.getBlockId() + " not OK "
                    + getMagic());
    }

    /**
     *  Constructs a new PageHeader of the indicated type. Used for newly
     *  created pages.
     */
    public PageHeader(BlockIo block, short type) {
        initialize(block);
        setType(type);
    }

    /**
     *  Factory method to create or return a page header for the
     *  indicated block.
     */
    public static PageHeader getView(BlockIo block) {
        BlockView view = block.getView();
        if (view != null && view instanceof PageHeader)
            return (PageHeader) view;
        else
            return new PageHeader(block);
    }

    private void initialize(BlockIo block) {
        this.block = block;
        block.setView(this);
    }

    /**
     *  Returns true if the magic corresponds with the fileHeader magic.
     */
    private boolean magicOk() {
        int magic = getMagic();
        return magic >= Magic.BLOCK
                && magic <= (Magic.BLOCK + Magic.FREEPHYSIDS_PAGE);
    }

    /**
     *  For paranoia mode
     */
    protected void paranoiaMagicOk() {
        if (!magicOk())
            throw new Error("CRITICAL: page header magic not OK "
                    + getMagic());
    }

    /** Returns the magic code */
    short getMagic() {
        return block.readShort(O_MAGIC);
    }

    /** Returns the next block. */
    public long getNext() {
        paranoiaMagicOk();
        return block.readLong(O_NEXT);
    }

    /** Sets the next block. */
    public void setNext(long next) {
        paranoiaMagicOk();
        block.writeLong(O_NEXT, next);
    }

    /** Returns the previous block. */
    public long getPrev() {
        paranoiaMagicOk();
        return block.readLong(O_PREV);
    }

    /** Sets the previous block. */
    public void setPrev(long prev) {
        paranoiaMagicOk();
        block.writeLong(O_PREV, prev);
    }

    /** Sets the type of the page header */
    public void setType(short type) {
        block.writeShort(O_MAGIC, (short) (Magic.BLOCK + type));
    }
}

