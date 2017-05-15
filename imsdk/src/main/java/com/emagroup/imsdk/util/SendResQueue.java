package com.emagroup.imsdk.util;

import com.emagroup.imsdk.response.SendResponse;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Created by beyearn on 2017/5/12.
 */

public class SendResQueue {

    private final Object synObj = new Object();
    private int maxSize = 20;

    private LinkedHashMap<String, SendResponse> map = new LinkedHashMap<>();

    public void put(String mark, SendResponse response) {

        synchronized (synObj) {

            if (map.size() > maxSize) {
                Set<String> keySet = map.keySet();
                Iterator<String> iterator = keySet.iterator();
                String firstKey = "";
                if (iterator.hasNext()) {
                    firstKey = iterator.next();
                }
                if (!"".equals(firstKey)) {
                    map.remove(firstKey);
                }
            }

            map.put(mark, response);
        }
    }

    public SendResponse get(String mark) {
        return map.get(mark);
    }


}
