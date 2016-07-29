package com.fang.example.db.manager;

import com.fang.example.db.client.PageCursor;
import com.fang.example.db.constant.Magic;
import com.fang.example.db.store.*;

import java.io.IOException;

/**
 * Created by andy on 6/26/16.
 */
final class FreePhysicalRowIdPageManager
{
    // our record file
    protected RecordFile _file;

    // our page manager
    protected PageManager _pageman;

    /**
     *  Creates a new instance using the indicated record file and
     *  page manager.
     */
    FreePhysicalRowIdPageManager( RecordFile file, PageManager pageman )
            throws IOException
    {
        _file = file;
        _pageman = pageman;
    }


    /**
     *  Returns a free physical rowid of the indicated size, or
     *  null if nothing was found.
     */
    Location get( int size )
            throws IOException
    {
        // Loop through the free physical rowid list until we find
        // a rowid that's large enough.
        Location retval = null;
        PageCursor curs = new PageCursor( _pageman, Magic.FREEPHYSIDS_PAGE );

        while (curs.next() != 0) {
            FreePhysicalRowIdPage fp = FreePhysicalRowIdPage
                    .getFreePhysicalRowIdPageView( _file.get( curs.getCurrent() ) );
            int slot = fp.getFirstLargerThan( size );
            if ( slot != -1 ) {
                // got one!
                retval = new Location( fp.get( slot ) );

                int slotsize = fp.get( slot ).getSize();
                fp.free( slot );
                if ( fp.getCount() == 0 ) {
                    // page became empty - free it
                    _file.release( curs.getCurrent(), false );
                    _pageman.free( Magic.FREEPHYSIDS_PAGE, curs.getCurrent() );
                } else {
                    _file.release( curs.getCurrent(), true );
                }

                return retval;
            } else {
                // no luck, go to next page
                _file.release( curs.getCurrent(), false );
            }

        }
        return null;
    }

    /**
     *  Puts the indicated rowid on the free list
     */
    void put(Location rowid, int size)
            throws IOException {

        FreePhysicalRowId free = null;
        PageCursor curs = new PageCursor(_pageman, Magic.FREEPHYSIDS_PAGE);
        long freePage = 0;
        while (curs.next() != 0) {
            freePage = curs.getCurrent();
            BlockIo curBlock = _file.get(freePage);
            FreePhysicalRowIdPage fp = FreePhysicalRowIdPage
                    .getFreePhysicalRowIdPageView(curBlock);
            int slot = fp.getFirstFree();
            if (slot != -1) {
                free = fp.alloc(slot);
                break;
            }

            _file.release(curBlock);
        }
        if (free == null) {
            // No more space on the free list, add a page.
            freePage = _pageman.allocate(Magic.FREEPHYSIDS_PAGE);
            BlockIo curBlock = _file.get(freePage);
            FreePhysicalRowIdPage fp =
                    FreePhysicalRowIdPage.getFreePhysicalRowIdPageView(curBlock);
            free = fp.alloc(0);
        }

        free.setBlock(rowid.getBlock());
        free.setOffset(rowid.getOffset());
        free.setSize(size);
        _file.release(freePage, true);
    }

    public static void main(String[] args) throws IOException {
        RecordFile f = new RecordFile("testFreePhysicalRowIdPageManager");
        PageManager pm = new PageManager(f);
        FreePhysicalRowIdPageManager freeMgr =
                new FreePhysicalRowIdPageManager(f, pm);

        // allocate 10,000 bytes - should fail on an empty file.
        Location loc = freeMgr.get(10000);
        System.out.println("loc is null=" + loc == null);

        pm.close();
        f.close();
    }
}
