package com.emagroup.imsdk;

/**
 * Created by Administrator on 2017/4/14.
 */

public class MsgBean {
    private String appId;
    private String fName;   //发送这个消息的人的名字
    private String fuid;     //发送这个消息的人的id
    private String handler;  //2私聊 3长频道 4短频道
    private String msg;      // 消息内容
    private String msgId;    // 消息时间
    private String tID;      // 接受者/频道 的id
    private String ext; //透传字段
    private String mark; //使用者不用管，用来在长连接传输时对应每条信息的

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public String getFuid() {
        return fuid;
    }

    public void setFuid(String fuid) {
        this.fuid = fuid;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String gettID() {
        return tID;
    }

    public void settID(String tID) {
        this.tID = tID;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }
}
