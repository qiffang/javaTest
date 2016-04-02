package com.fang.example.jmx;

import org.junit.Test;

import javax.management.MBeanAttributeInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by andy on 3/27/16.
 */
public class TestClient {
    public static int MAX_DEPTH = 30;
    @Test
    public void test1() throws Exception {
        Client client = new Client("service:jmx:rmi:///jndi/rmi://127.0.0.1:1098/jmxrmi", "", "", 200);
        ThreadMXBean threadMXBean = (ThreadMXBean) client.getMBean("java.lang:type=Threading", ThreadMXBean.class);
        ThreadInfo[] infos = threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds(), MAX_DEPTH);
        System.out.println("thread sum - " + threadMXBean.getThreadCount());
        ArrayList<ThreadDetail> threadDetails = new ArrayList<>();
        for (ThreadInfo info : infos) {
            StringBuilder sb = new StringBuilder();
            sb.append(info.getThreadName() + "\n");
            sb.append("java.lang.Thread.State:" + info.getThreadState() );
            StackTraceElement[] elements = info.getStackTrace();
            for (StackTraceElement ele : elements) {
                if (ele != null){
                    sb.append("\n    at");
                    sb.append(ele);
                }
            }
            threadDetails.add(new ThreadDetail(info.getThreadId(), info.getThreadName(), threadMXBean.getThreadCpuTime(info.getThreadId()), sb.toString()));
        }
        Collections.sort(threadDetails, new Comparator<ThreadDetail>() {
            @Override
            public int compare(ThreadDetail o1, ThreadDetail o2) {
                return (int)(o1._cpuUsage - o2._cpuUsage);
            }
        });
        for (ThreadDetail detail : threadDetails) {
            System.out.println(detail);
        }

        MBeanAttributeInfo[] attributeInfos = client.getAttributes("java.lang:type=Threading");
        for (MBeanAttributeInfo info : attributeInfos) {
            System.out.println(info.getName());
        }
    }
    @Test
    public void test2() {
        ThreadDetail t1 = new ThreadDetail(1l, "1", 1l, "1l");
        ThreadDetail t2 = new ThreadDetail(2l, "2", 2l, "2l");
        t1 = t2;
        System.out.println(t1 != (t1 = t2));
    }

    /**
     * ThreadDetail include id, name ,cpuusage and stack.
     */
    static class ThreadDetail {
        long _threadId;
        String _threadName;
        long _cpuUsage;
        String _stackInfos;
        public ThreadDetail(long threadId, String threadName, long cpuUsage, String stackInfos) {
            _threadId = threadId;
            _threadName = _threadName;
            _cpuUsage = cpuUsage;
            _stackInfos = stackInfos;
        }
        @Override
        public String toString() {
            return String.format("ThreadInfo: Id=%d, Name=%s, cpuUsage=%d, statckInfo=%s", _threadId, _threadName, _cpuUsage, _stackInfos);
        }

    }

}
