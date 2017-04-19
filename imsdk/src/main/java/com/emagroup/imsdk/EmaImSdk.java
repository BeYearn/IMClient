package com.emagroup.imsdk;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.emagroup.imsdk.response.ImResponse;
import com.emagroup.imsdk.response.MsgHeartResponse;
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
import static com.emagroup.imsdk.ImConstants.EMA_IM_UNION_MSG;
import static com.emagroup.imsdk.ImConstants.EMA_IM_WORLD_MSG;

/**
 * Created by Administrator on 2017/4/13.
 */

public class EmaImSdk {

    private static EmaImSdk instance;
    public String mServerHost;
    private Context mContext;
    private MsgHeartResponse mHeartResponse;
    private MsgQueue mUnionMsgQueue;
    private MsgQueue mWorldMsgQueue;
    private int mHeartDelay;


    private static String mAppKey;
    private static String mAppId;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EMA_IM_PUT_MSG_OK:
                    Log.e("heart ok", "ddddddd");
                    pumpMsg(EMA_IM_UNION_MSG, mUnionMsgQueue, mHeartDelay);
                    pumpMsg(EMA_IM_WORLD_MSG, mWorldMsgQueue, mHeartDelay);
                    break;
                case EMA_IM_PUMP_MSG:
                    MsgBean msgBean = (MsgBean) msg.obj;
                    int type = msg.arg1;
                    if (null != msgBean) {
                        if (type == EMA_IM_UNION_MSG) {
                            mHeartResponse.onUnionMsgGet(msgBean);
                        } else if (type == EMA_IM_WORLD_MSG) {
                            mHeartResponse.onWorldMsgGet(msgBean);
                        }
                    }

                    break;
            }
        }
    };

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
     * 初始化
     *
     * @param context
     * @param key
     */
    public void init(Context context, String key) {
        this.mContext = context;
        this.mAppKey = key;
        this.mAppId = ConfigUtils.getAppId(mContext);
        ImUrl.initUrl(context);
    }

    /**
     * 登录服务器
     *
     * @param param
     */
    public void login(HashMap<String, String> param, final ImResponse response) {

        param.put(ImConstants.APP_ID, getAppId());
        param.put(ImConstants.TIME_STAMP, System.currentTimeMillis() + "");

        String sign = param.get(ImConstants.APP_ID) + param.get(ImConstants.SERVER_ID) + param.get(ImConstants.TIME_STAMP) + param.get(ImConstants.UID) + mAppKey;
        sign = ConfigUtils.MD5(sign);
        param.put(ImConstants.SIGN, sign);
        new HttpRequestor().doPostAsync(ImUrl.getLoginUrl(), param, new HttpRequestor.OnResponsetListener() {
            @Override
            public void OnResponse(String result) {
                try {

                    JSONObject jsonObject = new JSONObject(result);
                    int status = jsonObject.getInt("status");
                    if (0 == status) {
                        response.onSuccessResponse();
                    }
                    JSONObject data = jsonObject.getJSONObject("data");
                    String serverHost = data.getString("host");
                    mServerHost = serverHost;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 更新服务器信息
     *
     * @param param
     */
    public void updateInfo(HashMap<String, String> param, final ImResponse response) {

        param.put(ImConstants.APP_ID, getAppId());
        param.put(ImConstants.TIME_STAMP, System.currentTimeMillis() + "");

        String sign = param.get(ImConstants.APP_ID) + param.get(ImConstants.SERVER_ID) + param.get(ImConstants.TIME_STAMP) + param.get(ImConstants.UID) + mAppKey;
        sign = ConfigUtils.MD5(sign);
        param.put(ImConstants.SIGN, sign);
        new HttpRequestor().doPostAsync(ImUrl.getUpdateInfoUrl(), param, new HttpRequestor.OnResponsetListener() {
            @Override
            public void OnResponse(String result) {
                try {

                    JSONObject jsonObject = new JSONObject(result);
                    int status = jsonObject.getInt("status");
                    if (0 == status) {
                        response.onSuccessResponse();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * 心跳 获取工会和世界信息
     *
     * @param param
     * @param response
     * @param delay
     */
    public void msgHeart(final HashMap<String, String> param, final MsgHeartResponse response, final int delay) {

        this.mHeartResponse = response;
        this.mHeartDelay = delay;
        mUnionMsgQueue = new MsgQueue();
        mWorldMsgQueue = new MsgQueue();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                perHeart(param);  //应该搞个handler来说明网络心跳完了才能再执行下面的循环或者直接在下面进行

            }
        }, 0, delay * 1000);
    }

    /**
     * 发送信息（同时获取聊天信息）
     *
     * @param param
     * @param response
     */
    public void sendMsg(final HashMap<String, String> param, final ImResponse response) {
        param.put(ImConstants.APP_ID, ConfigUtils.getAppId(mContext));
        param.put(ImConstants.MSG_ID, System.currentTimeMillis() + "");
        String sign = param.get(ImConstants.APP_ID) + param.get(ImConstants.FNAME) + param.get(ImConstants.FUID) + param.get(ImConstants.HANDLER) + param.get(ImConstants.MSG) + param.get(ImConstants.MSG_ID) + param.get(ImConstants.SERVER_ID) + param.get(ImConstants.TID) + mAppKey;
        sign = ConfigUtils.MD5(sign);
        param.put(ImConstants.SIGN, sign);
        new HttpRequestor().doPostAsync(ImUrl.getSendMdgUrl(), param, new HttpRequestor.OnResponsetListener() {
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
     * 建立长连接
     */
    public void buildLongConnect(Map<String, String> param,ImResponse response) {
            param.put(ImConstants.APP_ID,getAppId());
            param.put(ImConstants.MSG,"i am long connect info");
            param.put(ImConstants.MSG_ID,System.currentTimeMillis()+"");

            SocketRunable socketRunable = SocketRunable.getInstance(param,mServerHost, 9999,response);
            ThreadUtil.runInSubThread(socketRunable);
    }

    //---------------------------------------------------------------------------------------------

    private void pumpMsg(final int type, final MsgQueue msgQueue, final int delay) {

        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                long msgdelay = (long) ((delay / 10.0) * 1000);

                Log.e("pumpMsg", msgdelay + "");

                MsgBean msgBean = msgQueue.deQueue();
                while (null != msgBean) {
                    Message message = Message.obtain();
                    message.what = EMA_IM_PUMP_MSG;
                    message.arg1 = type;
                    message.obj = msgBean;
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
        param.put(ImConstants.APP_ID, getAppId());
        param.put(ImConstants.TIME_STAMP, System.currentTimeMillis() + "");

        String sign = param.get(ImConstants.APP_ID) + param.get(ImConstants.SERVER_ID) + param.get(ImConstants.TIME_STAMP) + param.get(ImConstants.UID) + mAppKey;
        sign = ConfigUtils.MD5(sign);
        param.put(ImConstants.SIGN, sign);
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

        JSONArray unionMsg = data.getJSONArray("unionMsg");
        JSONArray worldMsg = data.getJSONArray("worldMsg");

        for (int i = 0; i < unionMsg.length(); i++) {
            JSONObject obj = unionMsg.getJSONObject(i);
            MsgBean unionMsgBean = getMsgBean(obj);
            mUnionMsgQueue.enQueue(unionMsgBean);
        }

        for (int i = 0; i < worldMsg.length(); i++) {
            JSONObject obj = worldMsg.getJSONObject(i);
            MsgBean worldMsgBean = getMsgBean(obj);
            mWorldMsgQueue.enQueue(worldMsgBean);
        }

        Message message = Message.obtain();
        message.what = EMA_IM_PUT_MSG_OK;
        mHandler.sendMessage(message);

        Log.e("U+W size", mUnionMsgQueue.QueueLength() + "..." + mWorldMsgQueue.QueueLength());
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
            msgBean.setServerId(obj.getString("serverId"));
            msgBean.settID(obj.getString("tId"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msgBean;
    }


    public String getAppKey() {
        return mAppKey;
    }

    public String getAppId() {
        return mAppId;
    }

    public String getServerHost() {
        return mServerHost;
    }

}
