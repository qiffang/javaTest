package com.fang.example.thread;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.*;

/**
 * Created by andy on 3/21/16.
 */
public class FutureTest {
    public static void main(String[]args) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        final HashMap<String, Object> env = new HashMap<String, Object>();
        env.put("jmx.remote.x.request.waiting.timeout", Long.toString(100000));
        final BlockingDeque<Object> queue = new LinkedBlockingDeque<Object>();
        Callable c = new Callable<JMXConnector>() {
            public JMXConnector call() throws Exception {
                JMXConnector connector = null;
                try {
                    connector = JMXConnectorFactory.connect(new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:1099/testServer"), env);
                    queue.offer(connector);
                } catch (IOException e) {
                    queue.offer(e);
                } catch (Exception e) {
                    queue.offer(e);
                }
                return null;
            }
        };
        Future task = executor.submit(c);
//
//        try {
//            task.get(10, TimeUnit.SECONDS);
//        } catch (InterruptedException e) {
//
//        } catch (ExecutionException e) {
//
//        } catch (TimeoutException e) {
//
//        }
//        finally {
//            executor.shutdown();
//        }
//        System.out.println("no wait");


//        Object t = null;
//        queue.offer(t);
        try {
            Object o = queue.poll(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.out.println("e");
        }

        System.out.println("b");
    }
}
