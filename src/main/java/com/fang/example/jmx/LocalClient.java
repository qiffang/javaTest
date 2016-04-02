package com.fang.example.jmx;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import java.lang.management.ManagementFactory;

/**
 * Created by andy on 3/16/16.
 */
public class LocalClient {
    MBeanServer _server = null;
    private static LocalClient _client = new LocalClient();
    private LocalClient() {
        _server = ManagementFactory.getPlatformMBeanServer();
    }
    public static LocalClient getInstance() {
        return _client;
    }


}
