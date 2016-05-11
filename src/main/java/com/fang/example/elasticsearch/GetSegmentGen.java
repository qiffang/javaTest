package com.fang.example.elasticsearch;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andy on 4/27/16.
 */
public class GetSegmentGen {
    public static void main(String[] args) {
        List<Long> intervals =  new ArrayList<>();

        intervals.add(3l);
        intervals.add(1l);
//        intervals.add(2l);

//        qSort(0, intervals.size() - 1,intervals);


        Long[]list = new Long[intervals.size()];
        intervals.toArray(list);
        sort(list);
        for (Long l : list) {
            System.out.println(l);
        }


        System.out.println(5 /2);


    }

    private static void sort(Long[]list) {
        for (int i = 0; i < list.length; i++) {
            for (int j = i + 1; j < list.length; j++) {

                if (list[j - 1] > list[j] ) {
                    long tmp = list[j];
                    list[j] = list[j - 1];
                    list[j - 1] = tmp;
                }
            }
        }
    }

    private static int pivot(int low, int high, List<Long> intervals) {

        long m = intervals.get(low);

        while (low < high) {

            while (low < high && intervals.get(low) <= m)
                low++;
            if (low < high) {
                intervals.set(high, intervals.get(low));
            }
            while (low < high && intervals.get(high) >= m)
                high--;

            if (low < high) {
                intervals.set(low, intervals.get(high));
            }

        }

        intervals.set(low, m);
        return low;

    }

    private static void qSort(int low, int high, List<Long> intervals) {
       if (low < high) {
           int m = pivot(low, high, intervals);

           qSort(low, m - 1, intervals);
           qSort(m+1, high, intervals);
       }


    }



}


