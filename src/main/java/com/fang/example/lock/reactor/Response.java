package com.fang.example.lock.reactor;

/**
 * Created by andy on 5/15/16.
 */
public class Response {

    private String status;
    private String result;
    private int id;

    public Response(){}

    public Response(String status , String result , int id){
        this.status = status;
        this.result = result;
        this.setId(id);
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getResult() {
        return result;
    }
    public void setResult(String result) {
        this.result = result;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


}