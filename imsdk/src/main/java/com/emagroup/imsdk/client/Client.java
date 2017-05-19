package com.emagroup.imsdk.client;


import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.emagroup.imsdk.ErrorCode;
import com.emagroup.imsdk.ImConstants;
import com.emagroup.imsdk.MsgBean;
import com.emagroup.imsdk.response.ChannelHandler;
import com.emagroup.imsdk.response.ImResponse;
import com.emagroup.imsdk.response.LCStateListener;
import com.emagroup.imsdk.response.SendResponse;
import com.emagroup.imsdk.save.ChatLogDao;
import com.emagroup.imsdk.util.SendResQueue;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Administrator
 */
public class Client {

    private static Client mInstance;
    private final int STATE_OPEN = 1;//socket打开
    private final int STATE_CLOSE = 1 << 1;//socket关闭
    private final int STATE_CONNECT_START = 1 << 2;//开始连接server
    private final int STATE_CONNECT_SUCCESS = 1 << 3;//连接成功
    private final int STATE_CONNECT_FAILED = 1 << 4;//连接失败
    private final int STATE_CONNECT_WAIT = 1 << 5;//等待连接

    private String IP = "192.168.1.100";
    private int PORT = 60000;

    private int state = STATE_CONNECT_START;

    private Socket socket = null;

    private BufferedReader reader;
    private BufferedWriter writer;

    private Thread conn = null;

    private Thread send = null;
    private Thread rec = null;
    private Context context;
    private ImResponse respListener;
    private LinkedBlockingQueue<Packet> requestQueen = new LinkedBlockingQueue<>();
    private final Object lock = new Object();
    private final String TAG = "Client";

    private HashMap<String, String> mInitInfo;
    private Timer mHeartTimer;

    private HashMap<String, ChannelHandler> mHandlerMap;

    private final SendResQueue mSendResponseQueue;  // 用来对应发送的消息和其成功与否回调的： mark--对应操作的回调
    private int HbDelay;
    private String mUid;  //当前用户id
    private LCStateListener mStateListener = new LCStateListener() {  //当前长连接状态
        @Override
        public void onState(int stateCode) {

        }
    };


    public static Client getInstance() {
        if (mInstance == null) {
            mInstance = new Client();
            Log.e("newSocketRunable", mInstance.toString());
        }
        return mInstance;
    }

    private Client() {

        mHandlerMap = new HashMap<>();

        mSendResponseQueue = new SendResQueue();
    }


    public void setInitRe(Context mContext, ImResponse response, HashMap<String, String> param) {
        this.context = mContext;
        this.respListener = response;
        this.mInitInfo = param;

        this.mUid = mInitInfo.get(ImConstants.FUID);
    }

    public void setStateListener(LCStateListener stateListener) {
        this.mStateListener = stateListener;
    }

    public void joinChannel(String channelId, ChannelHandler handler) {

        mHandlerMap.put(channelId, handler);

        HashMap<String, String> joinParam = new HashMap<>();
        joinParam.put(ImConstants.APP_ID, mInitInfo.get(ImConstants.APP_ID));
        joinParam.put(ImConstants.FUID, mInitInfo.get(ImConstants.FUID));
        joinParam.put(ImConstants.HANDLER, "98");
        joinParam.put(ImConstants.TID, "0");
        joinParam.put(ImConstants.MSG, channelId);
        joinParam.put(ImConstants.MSG_ID, System.currentTimeMillis() + "");

        String joinMsg = new JSONObject(joinParam).toString();
        send(new Packet(joinMsg));
    }

    public void sendMsg(String channelId, String fName, String msg, String ext, SendResponse sendResponse, String handler) {

        String currentTime = System.currentTimeMillis() + "";
        String shortTime = currentTime.substring(8, 13);

        mSendResponseQueue.put(shortTime, sendResponse);

        HashMap<String, String> param = new HashMap<>();
        param.put(ImConstants.APP_ID, mInitInfo.get(ImConstants.APP_ID));
        param.put(ImConstants.FNAME, fName);
        param.put(ImConstants.FUID, mInitInfo.get(ImConstants.FUID));
        param.put(ImConstants.HANDLER, handler);
        param.put(ImConstants.TID, channelId);
        param.put(ImConstants.MSG, msg);
        param.put(ImConstants.EXT, ext);
        param.put(ImConstants.MSG_ID, currentTime);
        param.put(ImConstants.MARK, shortTime);

        String joinMsg = new JSONObject(param).toString();
        send(new Packet(joinMsg));
    }

    public void leaveChannel(String channelId) {

        HashMap<String, String> param = new HashMap<>();
        param.put(ImConstants.APP_ID, mInitInfo.get(ImConstants.APP_ID));
        param.put(ImConstants.FUID, mInitInfo.get(ImConstants.FUID));
        param.put(ImConstants.HANDLER, "97");
        param.put(ImConstants.TID, "0");
        param.put(ImConstants.MSG, channelId);
        param.put(ImConstants.MSG_ID, System.currentTimeMillis() + "");

        String leaveMsg = new JSONObject(param).toString();
        send(new Packet(leaveMsg));
    }


    public int send(Packet in) {
        requestQueen.add(in);
        synchronized (lock) {
            lock.notifyAll();
        }
        return in.getId();
    }

   /* public int send(Packet in, MsgBean selfMsgBean) {

        if (selfMsgBean.getHandler().equals("3")) {   //长连接频道
            ChannelHandler channelHandler = mHandlerMap.get(selfMsgBean.gettID());
            if(channelHandler!=null){
                channelHandler.onGetMsg(selfMsgBean);
            }
        } else if (selfMsgBean.getHandler().equals("2")) {   //长连接私人
            respListener.onGetPriMsg(selfMsgBean);
        }

        requestQueen.add(in);
        synchronized (lock) {
            lock.notifyAll();
        }
        return in.getId();
    }*/

    public void cancel(int reqId) {
        Iterator<Packet> mIterator = requestQueen.iterator();
        while (mIterator.hasNext()) {
            Packet packet = mIterator.next();
            if (packet.getId() == reqId) {
                mIterator.remove();
            }
        }
    }

    public boolean isNeedConn() {
        return !((state == STATE_CONNECT_SUCCESS) && (null != send && send.isAlive()) && (null != rec && rec.isAlive()));
    }

    public void open() {
        reconn();
    }

    public void open(String host, int port, int heartBeatDelay) {
        this.IP = host;
        this.PORT = port;
        this.HbDelay = heartBeatDelay;
        reconn();
    }

    private long lastConnTime = 0;

    public synchronized void reconn() {
        if (System.currentTimeMillis() - lastConnTime < 2000) {
            return;
        }
        lastConnTime = System.currentTimeMillis();

        close();
        state = STATE_OPEN;                                                                        //1
        conn = new Thread(new Conn());
        conn.start();
    }

    public synchronized void close() {
        try {
            if (state != STATE_CLOSE) {
                try {
                    if (null != socket) {
                        socket.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    socket = null;
                }

                try {
                    if (null != mHeartTimer) {     //心跳的停止
                        mHeartTimer.cancel();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    if (null != writer) {
                        writer.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    writer = null;
                }

                try {
                    if (null != reader) {
                        reader.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    reader = null;
                }

                try {
                    if (null != conn && conn.isAlive()) {
                        conn.interrupt();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    conn = null;
                }

                try {
                    if (null != send && send.isAlive()) {
                        send.interrupt();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    send = null;
                }

                try {
                    if (null != rec && rec.isAlive()) {
                        rec.interrupt();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    rec = null;
                }

                state = STATE_CLOSE;
            }
            requestQueen.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private class Conn implements Runnable {
        public void run() {
            Log.v(TAG, "Conn :Start");
            try {
                while (state != STATE_CLOSE) {
                    try {
                        state = STATE_CONNECT_START;                                                //2
                        socket = new Socket();
                        socket.connect(new InetSocketAddress(IP, PORT), 10 * 1000);
                        state = STATE_CONNECT_SUCCESS;                                              //3    正式连接成功
                    } catch (Exception e) {
                        e.printStackTrace();
                        state = STATE_CONNECT_FAILED;                                               //3'
                    }

                    if (state == STATE_CONNECT_SUCCESS) {
                        try {
                            OutputStream outStream = socket.getOutputStream();
                            InputStream inStream = socket.getInputStream();
                            writer = new BufferedWriter(new OutputStreamWriter(outStream));
                            reader = new BufferedReader(new InputStreamReader(inStream));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        send = new Thread(new Send());
                        rec = new Thread(new Receive());

                        send.start();
                        rec.start();
                        break;

                    } else {
                        state = STATE_CONNECT_WAIT;                                                 //4
                        //如果有网络没有连接上，则定时去连接（还处于while中），没有网络则直接退出
                        if (NetworkUtil.isNetworkAvailable(context)) {
                            try {
                                Thread.sleep(15 * 1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.v(TAG, "Conn :End");
        }
    }

    private class Send implements Runnable {
        public void run() {
            Log.v(TAG, "Send :Start");
            Packet tempData = new Packet();
            try {
                while (state != STATE_CLOSE && state == STATE_CONNECT_SUCCESS && null != writer) {
                    Packet item;
                    while (null != (item = requestQueen.poll())) {
                        tempData = item;
                        writer.write(item.getData());
                        writer.flush();
                        item = null;
                    }

                    Log.v(TAG, "Send :woken up AAAAAAAAA");
                    synchronized (lock) {
                        lock.wait();
                    }
                    Log.v(TAG, "Send :woken up BBBBBBBBBB");
                }
            } catch (Exception e1) {
                e1.printStackTrace();//发送的时候出现异常，说明socket被关闭了(服务器关闭)java.net.SocketException: sendto failed: EPIPE (Broken pipe)

                //reconn();

                mStateListener.onState(ErrorCode.CODE_SOCKET_BROKEN);

                try {
                    JSONObject jsonObject = new JSONObject(tempData.getData());
                    int handler = jsonObject.getInt("handler");
                    switch (handler) {
                        case 98:  //加入长频道
                            String msgJ = jsonObject.getString("msg");
                            ChannelHandler channelHandlerJ = mHandlerMap.get(msgJ);
                            channelHandlerJ.onJoinFail(ErrorCode.CODE_SOCKET_BROKEN);
                            break;
                        case 97:  //离开长频道
                            String msgL = jsonObject.getString("msg");
                            ChannelHandler channelHandlerL = mHandlerMap.get(msgL);
                            channelHandlerL.onLeaveFail(ErrorCode.CODE_SOCKET_BROKEN);
                            break;
                        case 3:  //发送场频道
                            String mark1 = jsonObject.getString("mark");
                            SendResponse Response1 = mSendResponseQueue.get(mark1);
                            Response1.onSendFail(ErrorCode.CODE_SOCKET_BROKEN);
                            break;
                        case 2:  //私人
                            String mark2 = jsonObject.getString("mark");
                            SendResponse Response2 = mSendResponseQueue.get(mark2);
                            Response2.onSendFail(ErrorCode.CODE_SOCKET_BROKEN);
                            break;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            Log.v(TAG, "Send ::End");
        }
    }

    private class Receive implements Runnable {
        public void run() {
            Log.v(TAG, "Receive :Start");

            try {
                while (state != STATE_CLOSE && state == STATE_CONNECT_SUCCESS && null != reader) {
                    Log.v(TAG, "Receive :---------");

                    String str = null;
                    final MsgBean msgBean = new MsgBean();

                    while ((str = reader.readLine()) != null) {    //误以为readLine()是读取到没有数据时就返回null(因为其它read方法当读到没有数据时返回-1)，而实际上readLine()是一个阻塞函数，当没有数据读取时，就一直会阻塞在那，而不是返回null；readLine()只有在数据流发生异常或者另一端被close()掉时，才会返回null值。
                        if (null != respListener) {

                            Log.e("socket_receive", str);

                            JSONObject strFromSocket = new JSONObject(str);

                            msgBean.setAppId(strFromSocket.getString("appId"));
                            msgBean.setfName(strFromSocket.getString("fName"));
                            msgBean.setFuid(strFromSocket.getString("fUid"));
                            msgBean.setHandler(strFromSocket.getString("handler"));
                            msgBean.setMsg(strFromSocket.getString("msg"));
                            msgBean.setExt(strFromSocket.getString("ext"));
                            msgBean.setMsgId(strFromSocket.getString("msgId"));
                            msgBean.setMark(strFromSocket.getString("mark"));
                            msgBean.settID(strFromSocket.getString("tId"));


                            switch (Integer.parseInt(msgBean.getHandler())) {

                                case 0: //socket建立成功后受到服务器信息

                                    //向服务器提交初始信息
                                    send(new Packet(new JSONObject(mInitInfo).toString()));

                                    break;
                                case 96: //提交初始信息后服务器返会

                                    respListener.onSuccessed();//连接成功回调

                                    // 第三步开始维持心跳保持连接
                                    connectHeart();

                                    break;
                                case 97:   //退出频道后会原样反回

                                    ChannelHandler channelHandlerL = mHandlerMap.get(msgBean.getMsg());
                                    channelHandlerL.onLeaveSucc(msgBean.getMsg()); //渠道号

                                    break;

                                case 98:   // 加入频道后也返回
                                    ChannelHandler channelHandlerJ = mHandlerMap.get(msgBean.getMsg());
                                    channelHandlerJ.onJoineSucc(msgBean.getMsg()); //渠道号

                                    break;

                                case 1:  // 心跳的回应
                                    //Log.e("socketHeartRe", str);
                                    break;

                                case 2:  //1-1收到的消息

                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            respListener.onGetPriMsg(msgBean);

                                            SendResponse responseP = mSendResponseQueue.get(msgBean.getMark());
                                            if (null != responseP && msgBean.getFuid().equals(mUid)) { //回应发送方
                                                responseP.onSendSucc();
                                            }
                                        }
                                    });

                                    //保存私人聊天
                                    savePriMsg(msgBean);

                                    break;
                                case 3:  //组队收到的消息
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ChannelHandler channelHandlerG = mHandlerMap.get(msgBean.gettID());
                                            channelHandlerG.onGetMsg(msgBean);

                                            SendResponse response = mSendResponseQueue.get(msgBean.getMark());
                                            if (null != response && msgBean.getFuid().equals(mUid)) {  //回应发送方
                                                response.onSendSucc();
                                            }
                                        }
                                    });
                                    break;
                                case 5:  //长连接组队(没加入队伍)或私聊（不在线）发送失败收到的信息

                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            SendResponse response = mSendResponseQueue.get(msgBean.getMark());
                                            String msg = msgBean.getMsg();
                                            if (msg.equals("70001")) {
                                                response.onSendFail(ErrorCode.CODE_USER_OFFLINE);
                                            } else if (msg.equals("70002")) {
                                                response.onSendFail(ErrorCode.CODE_NOT_IN_CHANNEL);
                                            }
                                        }

                                    });
                                    break;
                            }
                        }
                    }
                    //reconn();//走到这一步，说明服务器socket断了

                    mStateListener.onState(ErrorCode.CODE_SOCKET_BROKEN);

                    break;
                }
            } catch (SocketException e1) {
                e1.printStackTrace();//客户端主动socket.close()会调用这里 java.net.SocketException: Socket closed
            } catch (Exception e2) {
                Log.v(TAG, "Receive :Exception");
                e2.printStackTrace();
            }

            Log.v(TAG, "Receive :End");
        }
    }

    private void savePriMsg(MsgBean msgBean) {
        ChatLogDao chatLogDao = new ChatLogDao(context);
        chatLogDao.add(msgBean);

    }

    private void connectHeart() {
        mHeartTimer = new Timer();
        mHeartTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                HashMap<String, String> heartParam = new HashMap<>();
                heartParam.put(ImConstants.APP_ID, mInitInfo.get(ImConstants.APP_ID));
                heartParam.put(ImConstants.FUID, mInitInfo.get(ImConstants.FUID));
                heartParam.put(ImConstants.HANDLER, "1");
                heartParam.put(ImConstants.TID, "0"); // 固定
                heartParam.put(ImConstants.MSG, "heart beat");
                heartParam.put(ImConstants.MSG_ID, System.currentTimeMillis() + "");

                try {
                    String heartMsg = new JSONObject(heartParam).toString();
                    send(new Packet(heartMsg));
                    Log.e("socketHeartContent", heartMsg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, HbDelay * 1000);
    }
}
