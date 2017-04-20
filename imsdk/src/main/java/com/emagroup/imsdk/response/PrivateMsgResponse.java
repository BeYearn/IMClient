package com.emagroup.imsdk.response;

import com.emagroup.imsdk.MsgBean;

/**
 * Created by Administrator on 2017/4/20.
 */

public interface PrivateMsgResponse {
    void onPersonalMsgGet(MsgBean unionMsgBean);

    void onTeamMsgGet(MsgBean worldMsgBean);
}
