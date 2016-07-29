package com.fang.example.db.constant;

/**
 * Created by andy on 6/26/16.
 */
public interface Magic {
    /** Magic cookie at start of file */
    public short FILE_HEADER = 0x1350;

    /** Magic for blocks. They're offset by the block type magic codes. */
    public short BLOCK = 0x1351;

    /** Magics for blocks in certain lists. Offset by baseBlockMagic */
    short FREE_PAGE = 0;
    short USED_PAGE = 1;
    short TRANSLATION_PAGE = 2;
    short FREELOGIDS_PAGE = 3;
    short FREEPHYSIDS_PAGE = 4;

    /** Number of lists in a file */
    public short NLISTS = 5;

    /**
     *  Maximum number of blocks in a file, leaving room for a 16 bit
     *  offset encoded within a long.
     */
    long MAX_BLOCKS = 0x7FFFFFFFFFFFL;

    /** Magic for transaction file */
    short LOGFILE_HEADER = 0x1360;

    /** Size of an externalized byte */
    public short SZ_BYTE = 1;
    /** Size of an externalized short */
    public short SZ_SHORT = 2;
    /** Size of an externalized int */
    public short SZ_INT = 4;
    /** Size of an externalized long */
    public short SZ_LONG = 8;
}

