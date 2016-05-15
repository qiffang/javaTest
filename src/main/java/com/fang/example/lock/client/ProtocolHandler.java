package com.fang.example.lock.client;
import com.fang.example.lock.reactor.Call;
import com.fang.example.lock.reactor.Response;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
/**
 * Created by andy on 5/15/16.
 */
public class ProtocolHandler implements InvocationHandler {
    private InetSocketAddress address; //Server address
    private int _timeout;

    public void setTimeOut(int timeout) {
        _timeout = timeout;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        Call call = new Call();
        call.setId(Client.getCounter());
        call.setMethodName(method.getName());
        call.setParas(args);
        call.setIp(address.getHostName());

        String fullProName = method.getDeclaringClass().getName();
        char[] tmp = fullProName.toCharArray();
        StringBuffer buffer = new StringBuffer();
        int index = 0;
        for(index = tmp.length - 1 ; index >= 0 ; index--){
            if(tmp[index] == '.')
                break;
        }
        for(index = index + 1 ; index < tmp.length ; index++){
            buffer.append(tmp[index]);
        }
        call.setProtocolName(buffer.toString());

        if (args != null) {
            String[] types = new String[args.length];
            for (int i = 0; i < args.length; i++) {
                String fullname = args[i].getClass().getName();
                tmp = fullname.toCharArray();
                index = 0;
                buffer.delete(0, buffer.capacity());
                for (index = tmp.length - 1; index >= 0; index--) {
                    if (tmp[index] == '.')
                        break;
                }
                for (index = index + 1; index < tmp.length; index++) {
                    buffer.append(tmp[index]);
                }
                types[i] = buffer.toString();
            }
            call.setParaTypes(types);
        }
        else {
            call.setParaTypes(null);
        }
        Connection conn = Client.getConnection(address);
        conn.sendCall(call);

        //int times = 0;
        Long startTime=System.currentTimeMillis();
        Long current=startTime;
        while((current-startTime)/1000 < _timeout){
            if(conn.getResultMap().containsKey(call.getId())){
                break;
            }
            current=System.currentTimeMillis();
        }

        if(conn.getResultMap().containsKey(call.getId())){
            Response res = conn.getResultMap().get(call.getId());
            conn.getResultMap().remove(call.getId());
            if(res.getStatus().equals("OK"))
                return res.getResult();
            else
                return "Execute Failed";
        }
        else {
            return "Time_OUT";
        }
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public void setAddress(InetSocketAddress address) {
        this.address = address;
    }
}
