package com.emagroup.imsdk;

import java.util.LinkedList;

public class MsgQueue {

    private LinkedList<MsgBean> list = new LinkedList<>();    // 考虑是否需要改成线程安全的？  因为每次心跳（一个新的线程）都往里面加 所以确实需要！！  待改

    public void clear()//销毁队列
    {
        list.clear();
    }

    public boolean QueueEmpty()//判断队列是否为空
    {
        return list.isEmpty();
    }

    public void enQueue(MsgBean msgBean)//进队
    {
        list.addLast(msgBean);
    }

    public MsgBean deQueue()//出队
    {
        if (!list.isEmpty()) {
            return list.removeFirst();
        }
        return null;
    }

    public int QueueLength()//获取队列长度
    {
        return list.size();
    }

    public Object QueuePeek()//查看队首元素
    {
        return list.getFirst();
    }
}