package com.emagroup.imsdk.response;

import com.emagroup.imsdk.MsgBean;

/**
 * Created by Administrator on 2017/4/14.
 */
public interface SysExMsgResponse {

    void onSysMsgGet(MsgBean sysMsgBean);

    void onExMsgGet(MsgBean exMsgBean);
}
