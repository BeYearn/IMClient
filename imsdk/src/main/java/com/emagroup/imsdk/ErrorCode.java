package com.emagroup.imsdk;

/**
 * Created by beyearn on 2017/5/18.
 */

public class ErrorCode {


    public static final int CODE_PARAMS_INCOMPLETE = 1001;   //参数不全
    public static final int CODE_PARAMS_ERROR = 1002;        //参数错误
    public static final int CODE_NOT_REGIST = 1003;        //还未注册


    public static final int CODE_NET_ERROR = 2001;           //网络错误
    public static final int CODE_SOCKET_BROKEN = 2002;        // 长连接断开
    public static final int CODE_USER_OFFLINE = 2003;         // 用户不在线
    public static final int CODE_NOT_IN_CHANNEL = 2004;       //不在该频道中
}
