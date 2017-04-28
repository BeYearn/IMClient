package com.emagroup.imsdk;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.emagroup.imsdk.client.Client;
import com.emagroup.imsdk.client.Packet;
import com.emagroup.imsdk.response.ImResponse;
import com.emagroup.imsdk.response.PrivateMsgResponse;
import com.emagroup.imsdk.response.PublicMsgResponse;
import com.emagroup.imsdk.response.SysExMsgResponse;
import com.emagroup.imsdk.util.ConfigUtils;
import com.emagroup.imsdk.util.HttpRequestor;
import com.emagroup.imsdk.util.MsgQueue;
import com.emagroup.imsdk.util.ThreadUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import static com.emagroup.imsdk.ImConstants.EMA_IM_EX_MSG;
import static com.emagroup.imsdk.ImConstants.EMA_IM_PUMP_MSG;
import static com.emagroup.imsdk.ImConstants.EMA_IM_PUT_MSG_OK;
import static com.emagroup.imsdk.ImConstants.EMA_IM_SYS_MSG;
import static com.emagroup.imsdk.ImConstants.EMA_IM_UNION_MSG;
import static com.emagroup.imsdk.ImConstants.EMA_IM_WORLD_MSG;

/**
 * Created by Administrator on 2017/4/13.
 */

public class EmaImSdk {

    private static EmaImSdk instance;
    private String mServerHost;
    private Context mContext;
    private PublicMsgResponse mPublicMsgResponse;
    private SysExMsgResponse mSysMsgResponse;

    private MsgQueue mUnionMsgQueue;
    private MsgQueue mWorldMsgQueue;
    private MsgQueue mSysMsgQueue;
    private MsgQueue mExMsgQueue;

    private int mHeartDelay;

    private String mUid;
    private String mServerId;
    private static String mAppKey;
    private static String mAppId;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EMA_IM_PUT_MSG_OK:
                    pumpMsg(EMA_IM_UNION_MSG, mUnionMsgQueue, mHeartDelay);
                    pumpMsg(EMA_IM_WORLD_MSG, mWorldMsgQueue, mHeartDelay);
                    pumpMsg(EMA_IM_SYS_MSG, mSysMsgQueue, mHeartDelay);
                    pumpMsg(EMA_IM_EX_MSG, mExMsgQueue, mHeartDelay);
                    break;
                case EMA_IM_PUMP_MSG:
                    MsgBean msgBean = (MsgBean) msg.obj;
                    int type = msg.arg1;
                    if (null != msgBean) {
                        if (mPublicMsgResponse == null) {
                            return;
                        } else if (type == EMA_IM_UNION_MSG) {
                            mPublicMsgResponse.onUnionMsgGet(msgBean);
                        } else if (type == EMA_IM_WORLD_MSG) {
                            mPublicMsgResponse.onWorldMsgGet(msgBean);
                        }
                        if (mSysMsgResponse == null) {
                            return;
                        } else if (type == EMA_IM_SYS_MSG) {
                            mSysMsgResponse.onSysMsgGet(msgBean);
                        } else if (type == EMA_IM_EX_MSG) {
                            mSysMsgResponse.onExMsgGet(msgBean);
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
     * @param param
     * @param key
     */
    public void init(Context context, HashMap<String, String> param, String key) {
        this.mContext = context;
        mAppKey = key;
        mAppId = ConfigUtils.getAppId(mContext);
        mServerId = param.get(ImConstants.SERVER_ID);
        mUid = param.get(ImConstants.UID);
        ImUrl.initUrl(context);
    }

    /**
     * 登录服务器
     *
     * @param delay 心跳的频率  秒
     */
    public void buildPubConnect(final HashMap<String, String> param, int delay) {

        this.mHeartDelay = delay;

        param.put(ImConstants.SERVER_ID, mServerId);
        param.put(ImConstants.UID, mUid);
        param.put(ImConstants.APP_ID, mAppId);
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
                    JSONObject data = jsonObject.getJSONObject("data");
                    mServerHost = data.getString("host");

                    if (0 == status) {

                        HashMap<String, String> heartParam = new HashMap<>();
                        heartParam.put(ImConstants.SERVER_ID, param.get(ImConstants.SERVER_ID));
                        heartParam.put(ImConstants.UID, param.get(ImConstants.UID));
                        heartParam.put(ImConstants.UNION_ID, param.get(ImConstants.UNION_ID));
                        heartParam.put(ImConstants.WORLD_ID, param.get(ImConstants.WORLD_ID));

                        //开始心跳
                        msgHeart(heartParam);
                    }

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
    public void updatePubInfo(HashMap<String, String> param, final ImResponse response) {

        param.put(ImConstants.SERVER_ID, mServerId);
        param.put(ImConstants.UID, mUid);
        param.put(ImConstants.APP_ID, mAppId);
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
     * 获取世界、工会消息
     *
     * @param publicMsgResponse
     */
    public void getPubMsg(PublicMsgResponse publicMsgResponse) {
        this.mPublicMsgResponse = publicMsgResponse;
    }

    /**
     * 获取系统、扩展消息
     *
     * @param sysExMsgResponse
     */
    public void getSysExMsg(SysExMsgResponse sysExMsgResponse) {
        this.mSysMsgResponse = sysExMsgResponse;
    }

    /**
     * 发送世界或工会信息（同时获取聊天信息）
     *
     * @param param
     */
    public void sendPubMsg(final HashMap<String, String> param) {
        param.put(ImConstants.SERVER_ID, mServerId);
        param.put(ImConstants.FUID, mUid);
        param.put(ImConstants.APP_ID, mAppId);
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
    public void buildPriConnect(ImResponse response) {
        HashMap<String, String> param = new HashMap<>();
        param.put(ImConstants.SERVER_ID, mServerId);
        param.put(ImConstants.FUID, mUid);
        param.put(ImConstants.APP_ID, mAppId);
        param.put(ImConstants.MSG, "i am long connect info");
        param.put(ImConstants.HANDLER, "0");    // 0服务器  1心跳  2私聊 3队伍
        param.put(ImConstants.TID, "0");
        param.put(ImConstants.MSG_ID, System.currentTimeMillis() + "");

        Client client = Client.getInstance();
        client.setInitRe(mContext, response, param);
        client.open(mServerHost, 9999);

        //SocketRunable socketRunable = SocketRunable.getInstance();
        //socketRunable.setStartInfo(param, mServerHost, 9999, response);
        //ThreadUtil.runInSubThread(socketRunable);
    }

    /**
     * 发送私人或组队聊天（同时获取聊天信息）
     *
     * @param param
     */
    public void sendPriMsg(HashMap<String, String> param) {
        param.put(ImConstants.SERVER_ID, mServerId);
        param.put(ImConstants.FUID, mUid);
        param.put(ImConstants.APP_ID, mAppId);
        param.put(ImConstants.MSG_ID, System.currentTimeMillis() + "");

        Client client = Client.getInstance();
        Packet packet = new Packet();
        packet.setData(new JSONObject(param).toString());
        client.send(packet);

        //SocketRunable socketRunable = SocketRunable.getInstance();
        //socketRunable.putStrIntoSocket(new JSONObject(param).toString());
    }

    /**
     * 接收private信息
     *
     * @param privateMsgResponse
     */
    public void getPriMsg(PrivateMsgResponse privateMsgResponse) {

        Client client = Client.getInstance();
        client.setOnGetPriMsg(privateMsgResponse);
        //SocketRunable.getInstance().setOnMsgResponce(privateMsgResponse);
    }

    /**
     * 更新队伍信息 改变队伍id 或者退出队伍
     *
     * @param param
     */
    public void updateTeamInfo(HashMap<String, String> param) {

        param.put(ImConstants.SERVER_ID, mServerId);
        param.put(ImConstants.FUID, mUid);
        param.put(ImConstants.APP_ID, ConfigUtils.getAppId(mContext));
        param.put(ImConstants.MSG_ID, System.currentTimeMillis() + "");
        param.put(ImConstants.HANDLER, "98"); //退出或加入teamid
        param.put(ImConstants.TID, "0");  //固定 告诉服务器

        Client client = Client.getInstance();
        Packet packet = new Packet();
        packet.setData(new JSONObject(param).toString());
        client.send(packet);
        //SocketRunable socketRunable = SocketRunable.getInstance();
        //socketRunable.putStrIntoSocket(new JSONObject(param).toString());
    }

    /**
     * 停止长连接，退出服务器
     */
    public void stopPriConnect() {
        HashMap<String, String> param = new HashMap<>();
        param.put(ImConstants.TID, "0");  //固定 告诉服务器
        param.put(ImConstants.MSG, "");
        param.put(ImConstants.SERVER_ID, mServerId);
        param.put(ImConstants.FUID, mUid);
        param.put(ImConstants.APP_ID, mAppId);
        param.put(ImConstants.MSG_ID, System.currentTimeMillis() + "");
        param.put(ImConstants.HANDLER, "99"); //退出服务器

        Client client = Client.getInstance();
        Packet packet = new Packet();
        packet.setData(new JSONObject(param).toString());
        client.send(packet);

        client.close();
        //SocketRunable socketRunable = SocketRunable.getInstance();
        //socketRunable.putStrIntoSocket(new JSONObject(param).toString());
    }

    //---------------------------------------------------------------------------------------------

    /**
     * 心跳 获取工会和世界信息
     *
     * @param param
     */
    private void msgHeart(final HashMap<String, String> param) {

        mUnionMsgQueue = new MsgQueue();
        mWorldMsgQueue = new MsgQueue();
        mSysMsgQueue = new MsgQueue();
        mExMsgQueue = new MsgQueue();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                perHeart(param);  //应该搞个handler来说明网络心跳完了才能再执行下面的循环或者直接在下面进行

            }
        }, 0, mHeartDelay * 1000);
    }


    private void pumpMsg(final int type, final MsgQueue msgQueue, final int delay) {

        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                long msgdelay = (long) ((delay / 10.0) * 1000);

                //Log.e("pumpMsg", msgdelay + "");

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
        param.put(ImConstants.APP_ID, mAppId);
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
        JSONArray sysMsg = data.getJSONArray("sysMsg");
        JSONArray exMsg = data.getJSONArray("exMsg");


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

        for (int i = 0; i < sysMsg.length(); i++) {
            JSONObject obj = sysMsg.getJSONObject(i);
            MsgBean sysMsgBean = getMsgBean(obj);
            mWorldMsgQueue.enQueue(sysMsgBean);
        }

        for (int i = 0; i < exMsg.length(); i++) {
            JSONObject obj = exMsg.getJSONObject(i);
            MsgBean exMsgBean = getMsgBean(obj);
            mWorldMsgQueue.enQueue(exMsgBean);
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

}
