package com.emagroup.imsdk;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import static com.emagroup.imsdk.ImConstants.EMA_IM_HEART_OK;
import static com.emagroup.imsdk.ImConstants.EMA_IM_PUMP_MSG;
import static com.emagroup.imsdk.ImConstants.EMA_IM_UNION_MSG;
import static com.emagroup.imsdk.ImConstants.EMA_IM_WORLD_MSG;

/**
 * Created by Administrator on 2017/4/13.
 */

public class EmaImSdk {

    private static EmaImSdk instance;
    private String appKey;
    private Context mContext;
    private MsgHeartResponse mHeartResponse;
    private MsgQueue mUnionMsgQueue;
    private MsgQueue mWorldMsgQueue;
    private int mHeartDelay;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EMA_IM_HEART_OK:
                    Log.e("heart ok","ddddddd");
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

    public static EmaImSdk getInstance() {
        if (null == instance) {
            instance = new EmaImSdk();
        }
        return instance;
    }


    public void init(Context context, String key) {
        this.mContext = context;
        this.appKey = key;
        ImUrl.initUrl(context);
    }

    public void login(HashMap<String, String> param) {

        param.put(ImConstants.APP_ID, ConfigUtils.getAppId(mContext));
        param.put(ImConstants.TIME_STAMP, System.currentTimeMillis() + "");

        String sign = param.get(ImConstants.APP_ID) + param.get(ImConstants.SERVER_ID) + param.get(ImConstants.TIME_STAMP) + param.get(ImConstants.UID) + appKey;
        sign = ConfigUtils.MD5(sign);
        param.put(ImConstants.SIGN, sign);
        new HttpRequestor().doPostAsync(ImUrl.getLoginUrl(), param, new HttpRequestor.OnResponsetListener() {
            @Override
            public void OnResponse(String result) {
                try {

                    JSONObject jsonObject = new JSONObject(result);
                    JSONObject data = jsonObject.getJSONObject("data");
                    String serverHost = data.getString("host");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void updateInfo(HashMap<String, String> param) {

        param.put(ImConstants.APP_ID, ConfigUtils.getAppId(mContext));
        param.put(ImConstants.TIME_STAMP, System.currentTimeMillis() + "");

        String sign = param.get(ImConstants.APP_ID) + param.get(ImConstants.SERVER_ID) + param.get(ImConstants.TIME_STAMP) + param.get(ImConstants.UID) + appKey;
        sign = ConfigUtils.MD5(sign);
        param.put(ImConstants.SIGN, sign);
        new HttpRequestor().doPostAsync(ImUrl.getUpdateInfoUrl(), param, new HttpRequestor.OnResponsetListener() {
            @Override
            public void OnResponse(String result) {
                try {

                    JSONObject jsonObject = new JSONObject(result);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void msgHeart(final HashMap<String, String> param, final MsgHeartResponse response, final int delay) {

        this.mHeartResponse = response;
        this.mHeartDelay = delay;
        mUnionMsgQueue = new MsgQueue();
        mWorldMsgQueue = new MsgQueue();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                perHeart(param, mUnionMsgQueue, mWorldMsgQueue);  //应该搞个handler来说明网络心跳完了才能再执行下面的循环或者直接在下面进行

            }
        }, 0, delay * 1000);
    }

    private void pumpMsg(final int type, final MsgQueue msgQueue, final int delay) {

        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                long msgdelay = (long) ((delay / 10.0) * 2000);
                Log.e("pumpMsg", msgdelay + "");
                while (!msgQueue.QueueEmpty()) {
                    Message message = Message.obtain();
                    message.what = EMA_IM_PUMP_MSG;
                    message.arg1 = type;
                    message.obj = msgQueue.deQueue();
                    mHandler.sendMessageDelayed(message, msgdelay);
                }
            }
        });
    }

    public void perHeart(HashMap<String, String> param, final MsgQueue unionMsgQueue, final MsgQueue worldMsgQueue) {
        param.put(ImConstants.APP_ID, ConfigUtils.getAppId(mContext));
        param.put(ImConstants.TIME_STAMP, System.currentTimeMillis() + "");

        String sign = param.get(ImConstants.APP_ID) + param.get(ImConstants.SERVER_ID) + param.get(ImConstants.TIME_STAMP) + param.get(ImConstants.UID) + appKey;
        sign = ConfigUtils.MD5(sign);
        param.put(ImConstants.SIGN, sign);
        new HttpRequestor().doPostAsync(ImUrl.getHeartUrl(), param, new HttpRequestor.OnResponsetListener() {
            @Override
            public void OnResponse(String result) {
                try {

                    JSONObject jsonObject = new JSONObject(result);
                    JSONObject data = jsonObject.getJSONObject("data");

                    JSONArray unionMsg = data.getJSONArray("unionMsg");
                    JSONArray worldMsg = data.getJSONArray("worldMsg");

                    for (int i = 0; i < unionMsg.length(); i++) {
                        JSONObject obj = unionMsg.getJSONObject(i);
                        MsgBean unionMsgBean = getMsgBean(obj);
                        unionMsgQueue.enQueue(unionMsgBean);
                    }

                    for (int i = 0; i < worldMsg.length(); i++) {
                        JSONObject obj = worldMsg.getJSONObject(i);
                        MsgBean worldMsgBean = getMsgBean(obj);
                        worldMsgQueue.enQueue(worldMsgBean);
                    }

                    Message message = Message.obtain();
                    message.what = EMA_IM_HEART_OK;
                    mHandler.sendMessage(message);

                    Log.e("U+W size", unionMsgQueue.QueueLength() + "..." + worldMsgQueue.QueueLength());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public MsgBean getMsgBean(JSONObject obj) {
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
        return appKey;
    }
}
