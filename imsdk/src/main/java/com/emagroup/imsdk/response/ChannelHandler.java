package com.emagroup.imsdk.response;

import com.emagroup.imsdk.MsgBean;

/**
 * Created by Administrator on 2017/4/18.
 */

public interface ChannelHandler {

    void onJoined(String channelId);

    void onGetMsg(MsgBean msgBean);

    void onLeave(String channelId);
}
