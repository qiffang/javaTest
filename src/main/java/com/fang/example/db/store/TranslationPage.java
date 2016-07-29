package com.fang.example.db.store;

/**
 * Created by andy on 6/26/16.
 */
public class TranslationPage extends PageHeader {
    // offsets
    public static final short O_TRANS = PageHeader.SIZE; // short count
    public static final short ELEMS_PER_PAGE =
            (RecordFile.BLOCK_SIZE - O_TRANS) / PhysicalRowId.SIZE;

    // slots we returned.
    final PhysicalRowId[] slots = new PhysicalRowId[ELEMS_PER_PAGE];

    /**
     *  Constructs a data page view from the indicated block.
     */
    public TranslationPage(BlockIo block) {
        super(block);
    }

    /**
     *  Factory method to create or return a data page for the
     *  indicated block.
     */
    public static TranslationPage getTranslationPageView(BlockIo block) {
        BlockView view = block.getView();
        if (view != null && view instanceof TranslationPage)
            return (TranslationPage) view;
        else
            return new TranslationPage(block);
    }

    /** Returns the value of the indicated rowid on the page */
    public PhysicalRowId get(short offset) {
        int slot = (offset - O_TRANS) / PhysicalRowId.SIZE;
        if (slots[slot] == null)
            slots[slot] = new PhysicalRowId(block, offset);
        return slots[slot];
    }
}

