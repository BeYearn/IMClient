package com.emagroup.imsdk.client;

/**
 * @author Administrator
 */
public class Packet {

    private int id = AtomicIntegerUtil.getIncrementID();

    private String data;


    public Packet() {
    }

    public Packet(String data) {
        this.data = data;
    }

    public int getId() {
        return id;
    }

    public void setData(String txt) {
        this.data = txt;
    }

    public String getData() {
        return data;
    }
}
