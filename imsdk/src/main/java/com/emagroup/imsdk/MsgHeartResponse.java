package com.emagroup.imsdk;

/**
 * Created by Administrator on 2017/4/14.
 */
public interface MsgHeartResponse {

    void onUnionMsgGet(MsgBean msgBean);

    void onWorldMsgGet(MsgBean msgBean);
}
