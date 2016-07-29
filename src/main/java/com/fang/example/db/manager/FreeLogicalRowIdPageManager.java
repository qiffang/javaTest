package com.fang.example.db.manager;

import com.fang.example.db.client.PageCursor;
import com.fang.example.db.constant.Magic;
import com.fang.example.db.store.*;

import java.io.IOException;

/**
 * Created by andy on 6/26/16.
 */
final class FreeLogicalRowIdPageManager {
    // our record file
    private RecordFile file;
    // our page manager
    private PageManager pageman;

    /**
     *  Creates a new instance using the indicated record file and
     *  page manager.
     */
    FreeLogicalRowIdPageManager(RecordFile file,
                                PageManager pageman) throws IOException {
        this.file = file;
        this.pageman = pageman;
    }

    /**
     *  Returns a free Logical rowid, or
     *  null if nothing was found.
     */
    Location get() throws IOException {

        // Loop through the free Logical rowid list until we find
        // the first rowid.
        Location retval = null;
        PageCursor curs = new PageCursor(pageman, Magic.FREELOGIDS_PAGE);
        while (curs.next() != 0) {
            FreeLogicalRowIdPage fp = FreeLogicalRowIdPage
                    .getFreeLogicalRowIdPageView(file.get(curs.getCurrent()));
            int slot = fp.getFirstAllocated();
            if (slot != -1) {
                // got one!
                retval =
                        new Location(fp.get(slot));
                fp.free(slot);
                if (fp.getCount() == 0) {
                    // page became empty - free it
                    file.release(curs.getCurrent(), false);
                    pageman.free(Magic.FREELOGIDS_PAGE, curs.getCurrent());
                }
                else
                    file.release(curs.getCurrent(), true);

                return retval;
            }
            else {
                // no luck, go to next page
                file.release(curs.getCurrent(), false);
            }
        }
        return null;
    }

    /**
     *  Puts the indicated rowid on the free list
     */
    void put(Location rowid)
            throws IOException {

        PhysicalRowId free = null;
        PageCursor curs = new PageCursor(pageman, Magic.FREELOGIDS_PAGE);
        long freePage = 0;
        while (curs.next() != 0) {
            freePage = curs.getCurrent();
            BlockIo curBlock = file.get(freePage);
            FreeLogicalRowIdPage fp = FreeLogicalRowIdPage
                    .getFreeLogicalRowIdPageView(curBlock);
            int slot = fp.getFirstFree();
            if (slot != -1) {
                free = fp.alloc(slot);
                break;
            }

            file.release(curBlock);
        }
        if (free == null) {
            // No more space on the free list, add a page.
            freePage = pageman.allocate(Magic.FREELOGIDS_PAGE);
            BlockIo curBlock = file.get(freePage);
            FreeLogicalRowIdPage fp =
                    FreeLogicalRowIdPage.getFreeLogicalRowIdPageView(curBlock);
            free = fp.alloc(0);
        }
        free.setBlock(rowid.getBlock());
        free.setOffset(rowid.getOffset());
        file.release(freePage, true);
    }
}
