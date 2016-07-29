package com.fang.example.snmp;

/**
 * Created by andy on 6/13/16.
 */
public class SendThread implements Runnable {
    private long _lastSendEpochInSec = System.currentTimeMillis() / 1000;
    @Override
    public void run() {

        if (!_canSendNextBatch(_lastSendEpochInSec, System.currentTimeMillis() / 1000))
            return;


    }

    private boolean _canSendNextBatch(long lastSendEpochInSec, long currentEpochInSec) {
        /**
         * 32 seconds as slot
         */
        long epoch = currentEpochInSec - (currentEpochInSec % 32);

        if (lastSendEpochInSec == epoch)
            return false;
        return true;

    }
}
