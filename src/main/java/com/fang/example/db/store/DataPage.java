package com.fang.example.db.store;

import com.fang.example.db.constant.Magic;

/**
 * Created by andy on 6/25/16.
 */
public class DataPage extends PageHeader {
    // offsets
    private static final short O_FIRST = PageHeader.SIZE; // short firstrowid
    public static final short O_DATA = (short)(O_FIRST + Magic.SZ_SHORT);
    public static final short DATA_PER_PAGE = (short)(RecordFile.BLOCK_SIZE - O_DATA);

    /**
     *  Constructs a data page view from the indicated block.
     */
    DataPage(BlockIo block) {
        super(block);
    }

    /**
     *  Factory method to create or return a data page for the
     *  indicated block.
     */
    public static DataPage getDataPageView(BlockIo block) {
        BlockView view = block.getView();
        if (view != null && view instanceof DataPage)
            return (DataPage) view;
        else
            return new DataPage(block);
    }

    /** Returns the first rowid's offset */
    public short getFirst() {
        return block.readShort(O_FIRST);
    }

    /** Sets the first rowid's offset */
    public void setFirst(short value) {
        paranoiaMagicOk();
        if (value > 0 && value < O_DATA)
            throw new Error("DataPage.setFirst: offset " + value
                    + " too small");
        block.writeShort(O_FIRST, value);
    }
}
