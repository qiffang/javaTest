package com.fang.example.log;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by andy on 6/14/16.
 */
public class StackTraceElementTest {
    public static void main(String[] args) {

        Logger2 logger2 = new Logger2();
        LogMsg logMsg = new LogMsg();
        print();



        long startEpochInSec = System.currentTimeMillis() - 10*24*3600*1000;
        long endEpochInSec = System.currentTimeMillis();


        Calendar startCal = Calendar.getInstance();
        startCal.setTimeInMillis(startEpochInSec);

        Calendar endCal = Calendar.getInstance();
        endCal.setTimeInMillis(endEpochInSec);

        while (!startCal.equals(endCal)) {

            if (startCal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
                System.out.println(startCal.getTime());
            }

            startCal.add(Calendar.DATE, 1);

        }

        DateTimeZone timeZone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));

        System.out.println(timeZone.getID());

        DateTime startTime = new DateTime(startEpochInSec, timeZone);
        DateTime endTime = new DateTime(endEpochInSec, timeZone);
        while (startTime.isBefore(endTime)) {
            if ( startTime.getDayOfWeek() == DateTimeConstants.MONDAY ){
                System.out.println("joda="+startTime.toDate());
            }
            startTime = startTime.plusDays(1);
        }

        String[] ids = TimeZone.getAvailableIDs();
        for (String id : ids) {
            System.out.println("id=" + id);
        }


    }

    public static void print() {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();

        for (int i = 1; i < elements.length; i++) {
            System.out.println(elements[i].getClassName());
        }


    }
}
