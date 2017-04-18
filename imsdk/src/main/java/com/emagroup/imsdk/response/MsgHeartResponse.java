package com.emagroup.imsdk.response;

import com.emagroup.imsdk.MsgBean;

/**
 * Created by Administrator on 2017/4/14.
 */
public interface MsgHeartResponse {

    void onUnionMsgGet(MsgBean unionMsgBean);

    void onWorldMsgGet(MsgBean worldMsgBean);
}
