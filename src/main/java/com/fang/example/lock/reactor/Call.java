package com.fang.example.lock.reactor;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by andy on 5/15/16.
 */
public class Call {

    private int id;
    private String protocolName;
    private String methodName;
    private Object[] paras;
    private String[] paraTypes;
    private String ip;  //client address

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getProtocolName() {
        return protocolName;
    }
    public void setProtocolName(String protocolName) {
        this.protocolName = protocolName;
    }
    public String getMethodName() {
        return methodName;
    }
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    public Object[] getParas() {
        return paras;
    }
    public void setParas(Object[] paras) {
        this.paras = paras;
    }
    public String[] getParaTypes() {
        return paraTypes;
    }
    public void setParaTypes(String[] paraTypes) {
        this.paraTypes = paraTypes;
    }
    public String getIp() {
        return ip;
    }
    public void setIp(String ip) {
        this.ip = ip;
    }

    public static void main(String[]args) {
        Gson gson = new Gson();


        Type type = new TypeToken<Call>(){}.getType();
        String str = "{\"id\":0,\"protocolName\":\"LockProtocol\",\"methodName\":\"lock\",\"paras\":[\"key\",\"value\",5000],\"paraTypes\":[\"String\",\"String\",\"Long\"],\"ip\":\"localhost\"}";

        Call c = gson.fromJson(str, Call.class);

        for (int i = 0 ; i < c.getParas().length; i++) {
            int str1 = c.getParas()[2].toString().indexOf('.');
            System.out.println(c.getParas()[i] + "" + c.getParas()[2].toString().substring(0, str1));
        }

    }
}
