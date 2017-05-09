package com.emagroup.imsdk.response;

import com.emagroup.imsdk.MsgBean;

/**
 * Created by Administrator on 2017/4/18.
 */

public interface ImResponse {
    void onSuccessed();

    void onFailed();

    void onStoped();

    void onGetPriMsg(MsgBean msgBean);  // 收私人消息
}
