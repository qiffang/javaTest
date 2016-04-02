package com.fang.example.java;

import com.fang.example.javadump.HeapDumper;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by fang on 4/1/16.
 * Note: ConcurrentLinkedQueue class.
 * When use the remove method, there is a problem.
 * remove a node in this queue and if the node is the last node, this node do not remove from the queue
 * just set the value is null.
 * So if you call remove method, the really size of the queue is more and more length,
 * because there are many invalide node.
 * Call the size of the ConcurrentLinkedQueue, it filter the item which equals null.
 * So it may be clause increasing of the cpu usage.
 */
public class ConcurrentLinkedQueueNotes {
    public static void main(String[]args) throws InterruptedException {
        ConcurrentLinkedQueue<Integer> queue = new ConcurrentLinkedQueue<>();
        queue.add(100000);
        for (int i = 0; i < 300000; i++) {
            queue.offer(i + 1);
            /**
             * This method do not remove the node from the queue, just set the item=null
             */
            queue.remove(i + 1);
            if (i == 8000) {
                System.out.println(queue.size());
                /**
                 * analyze the heap dump file, we can find there are 8002 ConcurrentLinkedQueueNote objects.
                 */
                HeapDumper.dump("heap.bin", true);
            }
        }
    }

}


