package com.emagroup.imsdk.util;

import com.emagroup.imsdk.MsgBean;

import java.util.LinkedList;

/**
 * Created by beyearn on 2017/5/12.
 */

public class sendMsgQueue {

    private final Object synObj = new Object();
    private int maxSize = 5;

    private LinkedList<MsgBean> list = new LinkedList<>();

    public void add(MsgBean msgBean) {
        synchronized (synObj) {
            if (list.size() > maxSize) {
                list.removeFirst();
            }
            list.addLast(msgBean);
        }
    }


}
