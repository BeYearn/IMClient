package com.emagroup.imsdk;

/**
 * Created by Administrator on 2017/4/13.
 */

public class ImUrl {

    private static String serverUrl = "http://118.178.230.138:8080/";
    private static String STAGING_SERVER_URL;
    private static String TESTING_SERVER_URL;
    private static String PRODU_CTION_SERVER_URL;


    public static String getCheckSensitiveUrl() {

        return serverUrl + "ema-im/register/checkSensitiveWord";
    }

    public static String getLoginUrl() {

        return serverUrl + "ema-im/register/login";
    }

    public static String getJoinChannelUrl() {
        return serverUrl + "ema-im/register/joinChannels";
    }


    public static String getUpdateInfoUrl() {

        return serverUrl + "ema-im/register/updateInfo";
    }


    public static String getHeartUrl() {
        return serverUrl + "ema-im/chat/heart";
    }


    public static String getSendMsgUrl() {
        return serverUrl + "ema-im/chat/sendMsg";
    }

    public static String getLeaveChannelUrl(){
        return serverUrl + "ema-im/register/leaveChannels";
    }

    /*public static void initUrl(Context context) {
        String emaEnvi = ConfigUtils.getEnvi(context);
        if ("staging".equals(emaEnvi)) {
            ImUrl.setServerUrl(ImUrl.STAGING_SERVER_URL);
        } else if ("testing".equals(emaEnvi)) {
            ImUrl.setServerUrl(ImUrl.TESTING_SERVER_URL);
        } else if ("production".equals(emaEnvi)) {
            ImUrl.setServerUrl(ImUrl.PRODU_CTION_SERVER_URL);
        }
    }*/

    public static String getServerUrl() {
        return serverUrl;
    }

    public static void setServerUrl(String serverUrl) {
        ImUrl.serverUrl = serverUrl;
    }

}
