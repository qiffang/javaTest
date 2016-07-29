package com.fang.example.db.store;

/**
 * Created by andy on 6/26/16.
 */
public class Location {
    private long block;
    private short offset;

    /**
     * Creates a location from a (block, offset) tuple.
     */
    public Location(long block, short offset) {
        this.block = block;
        this.offset = offset;
    }

    /**
     * Creates a location from a combined block/offset long, as
     * used in the external representation of logical rowids.
     *
     * @see #toLong()
     */
    public Location(long blockOffset) {
        this.offset = (short) (blockOffset & 0xffff);
        this.block = blockOffset >> 16;
    }

    /**
     * Creates a location based on the data of the physical rowid.
     */
    public Location(PhysicalRowId src) {
        block = src.getBlock();
        offset = src.getOffset();
    }



    /**
     * Returns the file block of the location
     */
    public long getBlock() {
        return block;
    }

    /**
     * Returns the offset within the block of the location
     */
    public short getOffset() {
        return offset;
    }

    /**
     * Returns the external representation of a location when used
     * as a logical rowid, which combines the block and the offset
     * in a single long.
     */
    public long toLong() {
        return (block << 16) + (long) offset;
    }

    // overrides of java.lang.Object

    public boolean equals(Object o) {
        if (o == null || !(o instanceof Location))
            return false;
        Location ol = (Location) o;
        return ol.block == block && ol.offset == offset;
    }

    public String toString() {
        return "PL(" + block + ":" + offset + ")";
    }
}
