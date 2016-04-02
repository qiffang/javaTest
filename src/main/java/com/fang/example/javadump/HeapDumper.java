package com.fang.example.javadump;

import com.sun.management.HotSpotDiagnosticMXBean;

import javax.management.MBeanServer;
import java.io.IOException;
import java.lang.management.ManagementFactory;

/**
 * Created by fang on 3/15/16.
 *
 * This class dump heap from application, and analyze those off line using jhat
 */
public class HeapDumper {

    // This is the name of the HotSpot Diagnostic MBean
    private static final String HOTSPOT_BEAN_NAME =
            "com.sun.management:type=HotSpotDiagnostic";
    private static volatile HotSpotDiagnosticMXBean _mBean;
    public static void dump(String fileName, boolean isLive) {
        if (_mBean == null) {
            synchronized (HeapDumper.class) {
                if (_mBean == null)
                    _mBean = getHotSpotMBean();
            }
        }
        try {
            _mBean.dumpHeap(fileName, isLive);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static HotSpotDiagnosticMXBean getHotSpotMBean() {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try {
            return ManagementFactory.newPlatformMXBeanProxy(server, HOTSPOT_BEAN_NAME, HotSpotDiagnosticMXBean.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
