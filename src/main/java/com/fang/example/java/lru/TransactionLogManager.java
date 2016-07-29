package com.fang.example.java.lru;

import com.fang.example.db.constant.Magic;
import com.fang.example.db.store.BlockIo;
import com.fang.example.db.store.RecordFile;
import com.fang.example.db.store.Serialization;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * Created by andy on 7/26/16.
 */
public final class TransactionLogManager {
    private RecordFile owner;

    // streams for transaction log.
    private FileOutputStream fos;
    private ObjectOutputStream oos;

    /**
     * By default, we keep 10 transactions in the log file before
     * synchronizing it with the main database file.
     */
    static final int DEFAULT_TXNS_IN_LOG = 10;

    /**
     * Maximum number of transactions before the log file is
     * synchronized with the main database file.
     */
    private int _maxTxns = DEFAULT_TXNS_IN_LOG;

    /**
     * In-core copy of transactions. We could read everything back from
     * the log file, but the RecordFile needs to keep the dirty blocks in
     * core anyway, so we might as well point to them and spare us a lot
     * of hassle.
     */
    private ArrayList[] txns = new ArrayList[DEFAULT_TXNS_IN_LOG];
    private int curTxn = -1;

    /** Extension of a log file. */
    static final String extension = ".lg";

    /**
     *  Instantiates a transaction manager instance. If recovery
     *  needs to be performed, it is done.
     *
     *  @param owner the RecordFile instance that owns this transaction mgr.
     */
    public TransactionLogManager(RecordFile owner) throws IOException {
        this.owner = owner;
        recover();
        open();
    }


    /**
     * Synchronize log file data with the main database file.
     * <p>
     * After this call, the main database file is guaranteed to be
     * consistent and guaranteed to be the only file needed for
     * backup purposes.
     */
    public void synchronizeLog()
            throws IOException
    {
        synchronizeLogFromMemory();
    }


    /**
     * Set the maximum number of transactions to record in
     * the log (and keep in memory) before the log is
     * synchronized with the main database file.
     * <p>
     * This method must be called while there are no
     * pending transactions in the log.
     */
    public void setMaximumTransactionsInLog( int maxTxns )
            throws IOException
    {
        if ( maxTxns <= 0 ) {
            throw new IllegalArgumentException(
                    "Argument 'maxTxns' must be greater than 0." );
        }
        if ( curTxn != -1 ) {
            throw new IllegalStateException(
                    "Cannot change setting while transactions are pending in the log" );
        }
        _maxTxns = maxTxns;
        txns = new ArrayList[ maxTxns ];
    }


    /** Builds logfile name  */
    private String makeLogName() {
        return owner.getFileName() + extension;
    }


    /** Synchs in-core transactions to data file and opens a fresh log */
    private void synchronizeLogFromMemory() throws IOException {
        close();

        TreeSet blockList = new TreeSet( new BlockIoComparator() );

        int numBlocks = 0;
        int writtenBlocks = 0;
        for (int i = 0; i < _maxTxns; i++) {
            if (txns[i] == null)
                continue;
            // Add each block to the blockList, replacing the old copy of this
            // block if necessary, thus avoiding writing the same block twice
            for (Iterator k = txns[i].iterator(); k.hasNext(); ) {
                BlockIo block = (BlockIo)k.next();
                if ( blockList.contains( block ) ) {
                    block.decrementTransactionCount();
                }
                else {
                    writtenBlocks++;
                    boolean result = blockList.add( block );
                }
                numBlocks++;
            }

            txns[i] = null;
        }
        // Write the blocks from the blockList to disk
        synchronizeBlocks(blockList.iterator(), true);

        owner.sync();
        open();
    }


    /** Opens the log file */
    private void open() throws IOException {
        fos = new FileOutputStream(makeLogName());
        oos = new ObjectOutputStream(fos);
        oos.writeShort(Magic.LOGFILE_HEADER);
        oos.flush();
        curTxn = -1;
    }

    /** Startup recovery on all files */
    private void recover() throws IOException {
        String logName = makeLogName();
        File logFile = new File(logName);
        if (!logFile.exists())
            return;
        if (logFile.length() == 0) {
            logFile.delete();
            return;
        }

        FileInputStream fis = new FileInputStream(logFile);
        ObjectInputStream ois = new ObjectInputStream(fis);

        try {
            if (ois.readShort() != Magic.LOGFILE_HEADER)
                throw new Error("Bad magic on log file");
        } catch (IOException e) {
            // corrupted/empty logfile
            logFile.delete();
            return;
        }

        while (true) {
            ArrayList blocks = null;
            try {
                blocks = (ArrayList) ois.readObject();
            } catch (ClassNotFoundException e) {
                throw new Error("Unexcepted exception: " + e);
            } catch (IOException e) {
                // corrupted logfile, ignore rest of transactions
                break;
            }
            synchronizeBlocks(blocks.iterator(), false);

            // ObjectInputStream must match exactly each
            // ObjectOutputStream created during writes
            try {
                ois = new ObjectInputStream(fis);
            } catch (IOException e) {
                // corrupted logfile, ignore rest of transactions
                break;
            }
        }
        owner.sync();
        logFile.delete();
    }

    /** Synchronizes the indicated blocks with the owner. */
    private void synchronizeBlocks(Iterator blockIterator, boolean fromCore)
            throws IOException {
        // write block vector elements to the data file.
        while ( blockIterator.hasNext() ) {
            BlockIo cur = (BlockIo)blockIterator.next();
            owner.synch(cur);
            if (fromCore) {
                cur.decrementTransactionCount();
                if (!cur.isInTransaction()) {
                    owner.releaseFromTransaction(cur, true);
                }
            }
        }
    }


    /** Set clean flag on the blocks. */
    private void setClean(ArrayList blocks)
            throws IOException {
        for (Iterator k = blocks.iterator(); k.hasNext(); ) {
            BlockIo cur = (BlockIo) k.next();
            cur.setClean();
        }
    }

    /** Discards the indicated blocks and notify the owner. */
    private void discardBlocks(ArrayList blocks)
            throws IOException {
        for (Iterator k = blocks.iterator(); k.hasNext(); ) {
            BlockIo cur = (BlockIo) k.next();
            cur.decrementTransactionCount();
            if (!cur.isInTransaction()) {
                owner.releaseFromTransaction(cur, false);
            }
        }
    }

    /**
     *  Starts a transaction. This can block if all slots have been filled
     *  with full transactions, waiting for the synchronization thread to
     *  clean out slots.
     */
    public void start() throws IOException {
        curTxn++;
        if (curTxn == _maxTxns) {
            synchronizeLogFromMemory();
            curTxn = 0;
        }
        txns[curTxn] = new ArrayList();
    }

    /**
     *  Indicates the block is part of the transaction.
     */
    public void add(BlockIo block) throws IOException {
        block.incrementTransactionCount();
        txns[curTxn].add(block);
    }

    /**
     *  Commits the transaction to the log file.
     */
    public void commit() throws IOException {
        oos.writeObject(txns[curTxn]);
        sync();

        // set clean flag to indicate blocks have been written to log
        setClean(txns[curTxn]);

        // open a new ObjectOutputStream in order to store
        // newer states of BlockIo
        oos = new ObjectOutputStream(fos);
    }

    /** Flushes and syncs */
    private void sync() throws IOException {
        oos.flush();
        fos.flush();
        fos.getFD().sync();
    }

    /**
     *  Shutdowns the transaction manager. Resynchronizes outstanding
     *  logs.
     */
    public void shutdown() throws IOException {
        synchronizeLogFromMemory();
        close();
    }

    /**
     *  Closes open files.
     */
    private void close() throws IOException {
        sync();
        oos.close();
        fos.close();
        oos = null;
        fos = null;
    }

    /**
     * Force closing the file without synchronizing pending transaction data.
     * Used for testing purposes only.
     */
    public void forceClose() throws IOException {
        oos.close();
        fos.close();
        oos = null;
        fos = null;
    }

    /**
     * Use the disk-based transaction log to synchronize the data file.
     * Outstanding memory logs are discarded because they are believed
     * to be inconsistent.
     */
    public void synchronizeLogFromDisk() throws IOException {
        close();

        for ( int i=0; i < _maxTxns; i++ ) {
            if (txns[i] == null)
                continue;
            discardBlocks(txns[i]);
            txns[i] = null;
        }

        recover();
        open();
    }


    /** INNER CLASS.
     *  Comparator class for use by the tree set used to store the blocks
     *  to write for this transaction.  The BlockIo objects are ordered by
     *  their blockIds.
     */
    public static class BlockIoComparator
            implements Comparator
    {

        public int compare( Object o1, Object o2 ) {
            BlockIo block1 = (BlockIo)o1;
            BlockIo block2 = (BlockIo)o2;
            int result = 0;
            if ( block1.getBlockId() == block2.getBlockId() ) {
                result = 0;
            }
            else if ( block1.getBlockId() < block2.getBlockId() ) {
                result = -1;
            }
            else {
                result = 1;
            }
            return result;
        }

        public boolean equals(Object obj) {
            return super.equals(obj);
        }
    } // class BlockIOComparator

}
