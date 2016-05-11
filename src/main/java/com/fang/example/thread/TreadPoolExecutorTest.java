package com.fang.example.thread;

import java.util.concurrent.*;

/**
 * Created by andy on 4/17/16.
 */
public class TreadPoolExecutorTest {
    public static void main(String[]args) {

        ExecutorService _executor = new MyPoolExecutor(1, 2, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1024),
                new ThreadFactory() {
                    int numThread = 0;
                    @Override
                    public Thread newThread(Runnable r) {
                        numThread++;
                       return new Thread(r);
                    }
                });

        Future future =  _executor.submit(new Task());
        future.cancel(true);
    }


   static class MyPoolExecutor extends ThreadPoolExecutor {
        private volatile int _taskNum = 0;
        private volatile int _exceptions = 0;
        public MyPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory factory) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, factory);
        }

        @Override
        protected void beforeExecute(Thread t, Runnable r) {

        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            if ( t != null )
                _exceptions++;
            super.afterExecute(r, t);
        }

        @Override
        protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {

            if (callable instanceof Task) {
                _taskNum++;
            }
            return new FutureTask<T>(callable);
        }


    }

    static class Task implements Callable<Void> {

        private BlockingQueue<String> _queue = new ArrayBlockingQueue<String>(1);

        @Override
        public Void call() throws Exception {
            while (!Thread.currentThread().isInterrupted()) {
                _queue.take();
            }
            return null;
        }
    }
}
