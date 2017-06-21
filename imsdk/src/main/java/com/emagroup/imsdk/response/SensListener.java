package com.emagroup.imsdk.response;

import com.emagroup.imsdk.SensBean;

/**
 * Created by beyearn on 2017/6/20.
 */

public interface SensListener {
    void querySucc(SensBean sensBean);
    void queryFail();
}
