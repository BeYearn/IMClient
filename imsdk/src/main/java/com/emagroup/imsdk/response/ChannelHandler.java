package com.emagroup.imsdk.response;

import com.emagroup.imsdk.MsgBean;

/**
 * Created by Administrator on 2017/4/18.
 */

public interface ChannelHandler {

    void onJoineSucc(String channelId);

    void onJoinFail(int errorCode);

    void onGetMsg(MsgBean msgBean);

    void onLeaveSucc(String channelId);

    void onLeaveFail(int errorCode);
}
