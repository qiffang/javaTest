package com.fang.example.jmx;

import com.google.common.base.Strings;
import com.sun.media.jfxmedia.logging.Logger;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.PlatformManagedObject;
import java.lang.management.ThreadMXBean;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by fang on 3/20/16.
 * A management application can access platform MXBeans in three different ways.
 *
 * 1. Direct access, via the ManagementFactory class, for exmaple:
 * RuntimeMXBean mxbean = ManagementFactory.getRuntimeMXBean();
 * String vendor = mxbean.getVmVendor();
 *
 * 2. Direct access, via an MXBean proxy, for exmaple:
 * MBeanServerConnection mbs;
 * ...
 * Get a MBean proxy for RuntimeMXBean interface
 * RuntimeMXBean proxy =
 * ManagementFactory.newPlatformMXBeanProxy(mbs,
 * ManagementFactory.RUNTIME_MXBEAN_NAME,
 * RuntimeMXBean.class);
 * Get standard attribute "VmVendor"
 * String vendor = proxy.getVmVendor();
 *
 * 3. Indirect access, via the MBeanServerConnection class.
 * MBeanServerConnection mbs;
 * ...
 * try {
 * ObjectName oname = new ObjectName(ManagementFactory.RUNTIME_MXBEAN_NAME);
 * // Get standard attribute "VmVendor"
 * String vendor = (String) mbs.getAttribute(oname, "VmVendor");
 * } catch (....) {
 * Catch the exceptions thrown by ObjectName constructor
 * and MBeanServer.getAttribute method
 * ...
 * }
 */
public class Client {
    private MBeanServerConnection _connection;
    private JMXConnector _connector;
    public Client(String sUrl, String username, String pwd, int timeOut) throws Exception {
        JMXServiceURL url = new JMXServiceURL(sUrl);
        HashMap<String, Object> map = new HashMap<String, Object>();
        if (timeOut > 0) {
            map.put("jmx.remote.x.request.waiting.timeout", timeOut);
        }
        if (!Strings.isNullOrEmpty(username) && !Strings.isNullOrEmpty(pwd)) {
            map.put("jmx.remote.credentials", new String[]{username, pwd});
        }
        _connector = connect(url, map, timeOut);
        if (_connector == null)
            throw new Exception("Init jmxConnector failed");
        _connection = _connector.getMBeanServerConnection();
    }

    public void close() throws IOException {
        if (_connector != null)
            _connector.close();
    }

    private JMXConnector connect(final JMXServiceURL url, final Map parameter, int timeOut) throws Exception {
        ExecutorService executors = Executors.newSingleThreadExecutor();
        Future task = executors.submit(new Callable<JMXConnector>() {
            public JMXConnector call() {
                try {
                    return JMXConnectorFactory.connect(url, parameter);
                } catch (IOException e) {
                    System.out.println("init jmxConnector failed, erromsg=" + e);
                    return null;
                }
            }
        });
        return (JMXConnector) task.get(timeOut, TimeUnit.SECONDS);
    }

    public PlatformManagedObject getMBean(String beanName, Class<? extends PlatformManagedObject> cls) throws IOException {
        return ManagementFactory.newPlatformMXBeanProxy(_connection, beanName, cls);
    }

    public MBeanAttributeInfo[] getAttributes(String objectName) throws MalformedObjectNameException, IntrospectionException, ReflectionException, InstanceNotFoundException, IOException {
        ObjectName o = new ObjectName(objectName);
        return _connection.getMBeanInfo(o).getAttributes();
    }
}
