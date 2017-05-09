package com.emagroup.imsdk.response;

/**
 * Created by Administrator on 2017/4/18.
 */

public interface ImResponse {
    void onSuccessed();

    void onFailed();

    void onStoped();

    void onGetPriMsg();  // 收私人消息
}
