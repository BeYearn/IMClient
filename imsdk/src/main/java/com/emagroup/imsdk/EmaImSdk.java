package com.emagroup.imsdk;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.emagroup.imsdk.client.Client;
import com.emagroup.imsdk.client.Packet;
import com.emagroup.imsdk.response.ChannelHandler;
import com.emagroup.imsdk.response.ImResponse;
import com.emagroup.imsdk.util.ConfigUtils;
import com.emagroup.imsdk.util.HttpRequestor;
import com.emagroup.imsdk.util.MsgQueue;
import com.emagroup.imsdk.util.ThreadUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.emagroup.imsdk.ImConstants.EMA_IM_PUMP_MSG;
import static com.emagroup.imsdk.ImConstants.EMA_IM_PUT_MSG_OK;

/**
 * Created by Administrator on 2017/4/13.
 */

public class EmaImSdk {

    private static EmaImSdk instance;
    private String mServerHost;
    private ImResponse registResponse;

    private int mShortHeartDelay;
    private int mLongHeartDelay;

    private String mMsgLimit;
    private String mUid;
    private static String mAppKey;
    private static String mAppId;

    private String mInitChannelID;

    private boolean mFirstJoin = true; //第一加入频道才心跳

    private HashMap<String, ChannelHandler> mHandlerMap; // 保存各个频道的回调
    private HashMap<String, MsgQueue> mMsgQueueMap;   // 保存各个频道的消息队列

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EMA_IM_PUT_MSG_OK:                              //在此处遍历queueMap然后pump

                    for (Map.Entry<String, MsgQueue> entry : mMsgQueueMap.entrySet()) {
                        pumpMsg(entry.getKey(), entry.getValue());
                    }

                    break;
                case EMA_IM_PUMP_MSG:
                    MsgBean msgBean = (MsgBean) msg.obj;
                    Bundle bundle = msg.getData();
                    String channelId = (String) bundle.get("channelId");

                    ChannelHandler channelHandler = mHandlerMap.get(channelId);

                    if (null != msgBean) {
                        channelHandler.onGetMsg(msgBean);
                    }

                    break;
            }
        }
    };
    private Timer mHeartTimer;
    private Activity mActivity;

    private EmaImSdk() {
    }

    /**
     * 获得单例
     *
     * @return
     */
    public static EmaImSdk getInstance() {
        if (null == instance) {
            instance = new EmaImSdk();
        }
        return instance;
    }


    /**
     * 注册 建立起两个连接
     *
     * @param params
     * @param response
     */
    public void regist(Activity activity, Map<String, String> params, ImResponse response) {

        this.mActivity= activity;

        mHandlerMap = new HashMap<>();
        mMsgQueueMap = new HashMap<>();

        mAppId = params.get(ImConstants.APP_ID);
        mUid = params.get(ImConstants.UID);
        mMsgLimit = params.get(ImConstants.MSG_NUM_LIMIT);
        mInitChannelID = params.get(ImConstants.CHANNEL_ID); //这个字段先不往外暴露

        mAppKey = params.get(ImConstants.APP_KEY);
        String url = params.get(ImConstants.SERVER_URL);

        if (mAppId == null || mAppKey == null || mUid == null || url == null) {
            response.onFailed();
            Log.e("imregist", "parameters error");
            return;
        } else {
            ImUrl.setServerUrl(url);
            this.registResponse = response;
        }

        try {
            String shortDelay = params.get(ImConstants.SHORT_HEARTBEAT_DELAY);
            String longDelay = params.get(ImConstants.LONG_HEARTBEAT_DELAY);
            mShortHeartDelay = Integer.parseInt(shortDelay == null ? "10" : shortDelay);
            mLongHeartDelay = Integer.parseInt(longDelay == null ? "20" : longDelay);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("imregist", "heartbeat delay time error");
            response.onFailed();
            return;
        }
        //短链接（短链心跳也开始 不行，因为这时候还没消息回调handler） 里面进一步长连接
        shortLinkConnect();


    }

    /**
     * 加入频道
     *
     * @param channelId
     * @param handler   加入成功，停止成功，接收消息
     */
    public void joinShortLinkChannel(String channelId, final ChannelHandler handler) {

        mHandlerMap.put(channelId, handler);
        mMsgQueueMap.put(channelId, new MsgQueue());

        //加入的接口调用
        final HashMap<String, String> param = new HashMap<>();
        param.put(ImConstants.APP_ID, mAppId);
        param.put(ImConstants.UID, mUid);
        param.put(ImConstants.CHANNEL_ID, channelId);
        param.put(ImConstants.TIME_STAMP, System.currentTimeMillis() + "");

        String sign = param.get(ImConstants.APP_ID) + param.get(ImConstants.TIME_STAMP) + param.get(ImConstants.UID) + mAppKey;
        sign = ConfigUtils.MD5(sign);
        param.put(ImConstants.SIGN, sign);

        new HttpRequestor().doPostAsync(ImUrl.getJoinChannelUrl(), param, new HttpRequestor.OnResponsetListener() {
            @Override
            public void OnResponse(String result) {
                try {

                    JSONObject jsonObject = new JSONObject(result);

                    int status = jsonObject.getInt("status");

                    JSONObject data = jsonObject.getJSONObject("data");
                    JSONArray channelIdArr = data.getJSONArray("channelId");
                    String strid = (String) channelIdArr.get(0);  //现在加入离开都只是一个个的，所以取去第一个好了

                    if (0 == status) {
                        ChannelHandler channelHandler = mHandlerMap.get(strid);
                        channelHandler.onJoined(strid);
                    }

                    if (mFirstJoin) {
                        mFirstJoin = false;
                        //开始心跳
                        msgHeart();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 发送短链消息
     *
     * @param channelId
     * @param msg
     */
    public void sendShortLinkMsg(String channelId, String fName, String msg) {

        HashMap<String, String> param = new HashMap<>();
        param.put(ImConstants.APP_ID, mAppId);
        param.put(ImConstants.FUID, mUid);
        param.put(ImConstants.FNAME, fName);
        param.put(ImConstants.HANDLER, ImConstants.HANDLER_SHORT_LINK);
        param.put(ImConstants.TID, channelId);
        param.put(ImConstants.MSG, msg);
        param.put(ImConstants.MSG_ID, System.currentTimeMillis() + "");

        String sign = param.get(ImConstants.APP_ID) + param.get(ImConstants.FNAME) + param.get(ImConstants.FUID) + param.get(ImConstants.HANDLER) + param.get(ImConstants.MSG) + param.get(ImConstants.MSG_ID) + param.get(ImConstants.TID) + mAppKey;
        sign = ConfigUtils.MD5(sign);
        param.put(ImConstants.SIGN, sign);
        new HttpRequestor().doPostAsync(ImUrl.getSendMsgUrl(), param, new HttpRequestor.OnResponsetListener() {
            @Override
            public void OnResponse(String result) {
                try {

                    handleMsgResult(result);   //发送信息（同时获取聊天信息）  发的越快收的越快

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 离开某短频道
     *
     * @param channelId
     */
    public void leaveShortLinkChannel(String channelId) {
        HashMap<String, String> param = new HashMap<>();
        param.put(ImConstants.APP_ID, mAppId);
        param.put(ImConstants.UID, mUid);
        param.put(ImConstants.CHANNEL_ID, channelId);
        param.put(ImConstants.TIME_STAMP, System.currentTimeMillis() + "");

        String sign = param.get(ImConstants.APP_ID) + param.get(ImConstants.TIME_STAMP) + param.get(ImConstants.UID) + mAppKey;
        sign = ConfigUtils.MD5(sign);
        param.put(ImConstants.SIGN, sign);

        new HttpRequestor().doPostAsync(ImUrl.getLeaveChannelUrl(), param, new HttpRequestor.OnResponsetListener() {
            @Override
            public void OnResponse(String result) {
                try {

                    JSONObject jsonObject = new JSONObject(result);

                    int status = jsonObject.getInt("status");

                    JSONObject data = jsonObject.getJSONObject("data");
                    JSONArray channelIdArr = data.getJSONArray("channelId");
                    String strid = (String) channelIdArr.get(0);  //现在加入离开都只是一个个的，所以取去第一个好了

                    if (0 == status) {
                        ChannelHandler channelHandler = mHandlerMap.get(strid);
                        channelHandler.onLeave(strid);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * 加入长链频道
     *
     * @param channelId
     * @param handler
     */
    public void joinLongLinkChannel(String channelId, ChannelHandler handler) {

        Client client = Client.getInstance();
        client.joinChannel(channelId, handler);
    }

    /**
     * 发送长链信息
     *
     * @param channelId
     * @param msg
     */
    public void sendLongLinkMsg(String channelId, String fName, String msg) {

        HashMap<String, String> param = new HashMap<>();
        param.put(ImConstants.APP_ID, mAppId);
        param.put(ImConstants.FNAME, fName);
        param.put(ImConstants.FUID, mUid);
        param.put(ImConstants.HANDLER, "3");
        param.put(ImConstants.TID, channelId);
        param.put(ImConstants.MSG, msg);
        param.put(ImConstants.MSG_ID, System.currentTimeMillis() + "");


        //把自己发的消息也回调出来   世界工会信息服务器是返回的
        String nName = param.get(ImConstants.FNAME);
        String nMsg = param.get(ImConstants.MSG);
        String nHandler = param.get(ImConstants.HANDLER);
        String nTId = param.get(ImConstants.TID);
        MsgBean msgBean = new MsgBean();
        msgBean.setAppId(mAppId);
        msgBean.setfName(nName);
        msgBean.setFuid(mUid);
        msgBean.setHandler(nHandler);
        msgBean.setMsg(nMsg);
        msgBean.setMsgId(System.currentTimeMillis() + "");
        msgBean.settID(nTId);


        Client client = Client.getInstance();
        Packet packet = new Packet();
        packet.setData(new JSONObject(param).toString());
        client.send(packet,msgBean);
    }

    /**
     * 发送私人消息
     */
    public void sendPriMsg(String uid, String fName, String msg) {

        HashMap<String, String> param = new HashMap<>();
        param.put(ImConstants.APP_ID, mAppId);
        param.put(ImConstants.FNAME, fName);
        param.put(ImConstants.FUID, mUid);
        param.put(ImConstants.HANDLER, "2");
        param.put(ImConstants.TID, uid);
        param.put(ImConstants.MSG, msg);
        param.put(ImConstants.MSG_ID, System.currentTimeMillis() + "");


        //把自己发的消息也回调出来   世界工会信息服务器是返回的
        String nName = param.get(ImConstants.FNAME);
        String nMsg = param.get(ImConstants.MSG);
        String nHandler = param.get(ImConstants.HANDLER);
        String nTId = param.get(ImConstants.TID);
        MsgBean msgBean = new MsgBean();
        msgBean.setAppId(mAppId);
        msgBean.setfName(nName);
        msgBean.setFuid(mUid);
        msgBean.setHandler(nHandler);
        msgBean.setMsg(nMsg);
        msgBean.setMsgId(System.currentTimeMillis() + "");
        msgBean.settID(nTId);

        Client client = Client.getInstance();
        Packet packet = new Packet();
        packet.setData(new JSONObject(param).toString());
        client.send(packet,msgBean);
    }


    /**
     * 离开
     *
     * @param channelId
     */
    public void leaveLongLinkChannel(String channelId) {

        HashMap<String, String> param = new HashMap<>();
        param.put(ImConstants.APP_ID, mAppId);
        param.put(ImConstants.FUID, mUid);
        param.put(ImConstants.HANDLER, "97");
        param.put(ImConstants.TID, "0");
        param.put(ImConstants.MSG, channelId);
        param.put(ImConstants.MSG_ID, System.currentTimeMillis() + "");

        Client client = Client.getInstance();
        Packet packet = new Packet();
        packet.setData(new JSONObject(param).toString());
        client.send(packet);
    }

    /**
     * 判断长连接是否连接成功
     */
    public boolean isNeedReConnect(){
        Client client = Client.getInstance();
        return client.isNeedConn();
    }

    /**
     * 长连接重新连接
     */
    public void longLinkReConnect(){
        Client client = Client.getInstance();
        client.reconn();
    }

    /**
     * 停止im
     */
    public void stop() {
        //长连接的停止
        HashMap<String, String> param = new HashMap<>();
        param.put(ImConstants.APP_ID, mAppId);
        param.put(ImConstants.FUID, mUid);
        param.put(ImConstants.HANDLER, "99"); //退出服务器
        param.put(ImConstants.TID, "0");  //固定 告诉服务器
        param.put(ImConstants.MSG, "");
        param.put(ImConstants.MSG_ID, System.currentTimeMillis() + "");

        Client client = Client.getInstance();
        Packet packet = new Packet();
        packet.setData(new JSONObject(param).toString());
        client.send(packet);

        client.close();

        //短链心跳的停止
        if(mHeartTimer!=null){
            mHeartTimer.cancel();
        }
        if(registResponse!=null){
            registResponse.onStoped();
        }
    }


    /**
     * 登录服务器
     */
    public void shortLinkConnect() {

        final HashMap<String, String> param = new HashMap<>();

        param.put(ImConstants.APP_ID, mAppId);
        param.put(ImConstants.UID, mUid);
        if (null != mMsgLimit) {
            param.put(ImConstants.MSG_NUM_LIMIT, mMsgLimit);
        }
        if (null != mInitChannelID) {
            param.put(ImConstants.CHANNEL_ID, mInitChannelID);
        }
        param.put(ImConstants.TIME_STAMP, System.currentTimeMillis() + "");

        String sign = param.get(ImConstants.APP_ID) + param.get(ImConstants.TIME_STAMP) + param.get(ImConstants.UID) + mAppKey;
        sign = ConfigUtils.MD5(sign);
        param.put(ImConstants.SIGN, sign);

        new HttpRequestor().doPostAsync(ImUrl.getLoginUrl(), param, new HttpRequestor.OnResponsetListener() {
            @Override
            public void OnResponse(String result) {
                try {

                    JSONObject jsonObject = new JSONObject(result);
                    int status = jsonObject.getInt("status");
                    JSONObject data = jsonObject.getJSONObject("data");
                    mServerHost = data.getString("host");

                    if (0 == status) {

                        longLinkConnect(registResponse);    //在这里面某个时机 onsuccess   因为长连接更不太可靠些
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * 建立长连接
     */
    public void longLinkConnect(ImResponse response) {

        HashMap<String, String> param = new HashMap<>();
        param.put(ImConstants.APP_ID, mAppId);
        param.put(ImConstants.FUID, mUid);
        param.put(ImConstants.MSG, "i am long connect info");
        param.put(ImConstants.HANDLER, "0");    // 0服务器  1心跳  2私聊 3队伍
        param.put(ImConstants.TID, "0");
        param.put(ImConstants.MSG_ID, System.currentTimeMillis() + "");

        Client client = Client.getInstance();
        client.setInitRe(mActivity, response, param);
        client.open(mServerHost, 9999, mLongHeartDelay);
    }

    //---------------------------------------------------------------------------------------------

    /**
     * 心跳 获取工会和世界信息
     */
    private void msgHeart() {

        final HashMap<String, String> heartParam = new HashMap<>();
        heartParam.put(ImConstants.APP_ID, mAppId);
        heartParam.put(ImConstants.UID, mUid);
        heartParam.put(ImConstants.TIME_STAMP, System.currentTimeMillis() + "");
        String sign = heartParam.get(ImConstants.APP_ID) + heartParam.get(ImConstants.TIME_STAMP) + heartParam.get(ImConstants.UID) + mAppKey;
        sign = ConfigUtils.MD5(sign);
        heartParam.put(ImConstants.SIGN, sign);

        mHeartTimer = new Timer();
        mHeartTimer.schedule(new TimerTask() {
            @Override
            public void run() {

                perHeart(heartParam);

            }
        }, 0, mShortHeartDelay * 1000);
    }


    private void pumpMsg(final String channelId, final MsgQueue msgQueue) {

        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                long msgdelay = (long) ((mShortHeartDelay / 10.0) * 1000);

                //Log.e("pumpMsg", msgdelay + "");

                MsgBean msgBean = msgQueue.deQueue();
                while (null != msgBean) {
                    Bundle bundle = new Bundle();
                    bundle.putString("channelId", channelId);

                    Message message = Message.obtain();
                    message.what = EMA_IM_PUMP_MSG;
                    message.obj = msgBean;
                    message.setData(bundle);

                    mHandler.sendMessage(message);
                    try {
                        Thread.sleep(msgdelay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    msgBean = msgQueue.deQueue();
                }
            }
        });
    }

    private void perHeart(HashMap<String, String> param) {

        new HttpRequestor().doPostAsync(ImUrl.getHeartUrl(), param, new HttpRequestor.OnResponsetListener() {
            @Override
            public void OnResponse(String result) {
                try {

                    handleMsgResult(result);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void handleMsgResult(String result) throws JSONException {

        JSONObject jsonObject = new JSONObject(result);
        JSONObject data = jsonObject.getJSONObject("data");

        for (Map.Entry<String, MsgQueue> entry : mMsgQueueMap.entrySet()) {

            try { //避免某时某个频道没消息时没有该字段
                JSONArray channelMsgArray = data.getJSONArray(entry.getKey());
                for (int i = 0; i < channelMsgArray.length(); i++) {       //遍历每个消息 然后取出channelid对应的队列 入队
                    JSONObject obj = channelMsgArray.getJSONObject(i);
                    MsgBean msgBean = getMsgBean(obj);
                    entry.getValue().enQueue(msgBean);
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }

        Message message = Message.obtain();
        message.what = EMA_IM_PUT_MSG_OK;
        mHandler.sendMessage(message);
    }

    private MsgBean getMsgBean(JSONObject obj) {
        MsgBean msgBean = new MsgBean();
        try {
            msgBean.setAppId(obj.getString("appId"));
            msgBean.setfName(obj.getString("fName"));
            msgBean.setFuid(obj.getString("fUid"));
            msgBean.setHandler(obj.getString("handler"));
            msgBean.setMsg(obj.getString("msg"));
            msgBean.setMsgId(obj.getString("msgId"));
            msgBean.settID(obj.getString("tId"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msgBean;
    }

}
