package com.emagroup.imsdk.client;


import android.content.Context;
import android.util.Log;

import com.emagroup.imsdk.ImConstants;
import com.emagroup.imsdk.MsgBean;
import com.emagroup.imsdk.response.ImResponse;
import com.emagroup.imsdk.response.PrivateMsgResponse;

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
    //private OutputStream outStream = null;
    //private InputStream inStream = null;

    private BufferedReader reader;
    private BufferedWriter writer;

    private Thread conn = null;

    private Thread send = null;
    private Thread rec = null;
    private Context context;
    private ImResponse respListener;
    private LinkedBlockingQueue<Packet> requestQueen = new LinkedBlockingQueue<Packet>();
    private final Object lock = new Object();
    private final String TAG = "Client";
    private PrivateMsgResponse onGetPriMsg;
    private HashMap<String, String> mInfoParam;


    public static Client getInstance() {
        if (mInstance == null) {
            mInstance = new Client();
            Log.e("newSocketRunable", mInstance.toString());
        }
        return mInstance;
    }

    private Client() {
    }


    public void setInitRe(Context mContext, ImResponse response, HashMap<String, String> param) {
        this.context = mContext;
        this.respListener = response;
        this.mInfoParam = param;
    }

    public void setOnGetPriMsg(PrivateMsgResponse onGetPriMsg) {
        this.onGetPriMsg = onGetPriMsg;
    }


    public int send(Packet in) {
        requestQueen.add(in);
        synchronized (lock) {
            lock.notifyAll();
        }
        return in.getId();
    }

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

    public void open(String host, int port) {
        this.IP = host;
        this.PORT = port;
        reconn();
    }

    private long lastConnTime = 0;

    public synchronized void reconn() {
        if (System.currentTimeMillis() - lastConnTime < 2000) {
            return;
        }
        lastConnTime = System.currentTimeMillis();

        close();
        state = STATE_OPEN;
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
                        state = STATE_CONNECT_START;
                        socket = new Socket();
                        socket.connect(new InetSocketAddress(IP, PORT), 15 * 1000);
                        state = STATE_CONNECT_SUCCESS;
                    } catch (Exception e) {
                        e.printStackTrace();
                        state = STATE_CONNECT_FAILED;
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
                        rec = new Thread(new Rec());
                        send.start();
                        rec.start();
                        break;
                    } else {
                        state = STATE_CONNECT_WAIT;
                        //如果有网络没有连接上，则定时取连接，没有网络则直接退出
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
            try {
                while (state != STATE_CLOSE && state == STATE_CONNECT_SUCCESS && null != writer) {
                    Packet item;
                    while (null != (item = requestQueen.poll())) {
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
            } catch (SocketException e1) {
                e1.printStackTrace();//发送的时候出现异常，说明socket被关闭了(服务器关闭)java.net.SocketException: sendto failed: EPIPE (Broken pipe)
                reconn();
            } catch (Exception e) {
                Log.v(TAG, "Send ::Exception");
                e.printStackTrace();
            }

            Log.v(TAG, "Send ::End");
        }
    }

    private class Rec implements Runnable {
        public void run() {
            Log.v(TAG, "Rec :Start");

            try {
                while (state != STATE_CLOSE && state == STATE_CONNECT_SUCCESS && null != reader) {
                    Log.v(TAG, "Rec :---------");
                    String str = null;
                    while ((str = reader.readLine()) != null) {    //误以为readLine()是读取到没有数据时就返回null(因为其它read方法当读到没有数据时返回-1)，而实际上readLine()是一个阻塞函数，当没有数据读取时，就一直会阻塞在那，而不是返回null；readLine()只有在数据流发生异常或者另一端被close()掉时，才会返回null值。
                        if (null != respListener) {

                            JSONObject strFromSocket = new JSONObject(str);

                            MsgBean msgBean = new MsgBean();
                            msgBean.setAppId(strFromSocket.getString("appId"));
                            msgBean.setfName(strFromSocket.getString("fName"));
                            msgBean.setFuid(strFromSocket.getString("fUid"));
                            msgBean.setHandler(strFromSocket.getString("handler"));
                            msgBean.setMsg(strFromSocket.getString("msg"));
                            msgBean.setMsgId(strFromSocket.getString("msgId"));
                            msgBean.setServerId(strFromSocket.getString("serverId"));
                            msgBean.settID(strFromSocket.getString("tId"));

                            switch (Integer.parseInt(msgBean.getHandler())) {

                                case 0: //socket建立成功后受到服务器信息

                                    //向服务器提交初始信息
                                    send(new Packet(new JSONObject(mInfoParam).toString()));

                                    break;
                                case 96: //提交初始信息后服务器返会

                                    respListener.onSuccessResponse();//连接成功回调

                                    // 第三步开始维持心跳保持连接
                                    connectHeart();

                                    break;
                                case 1:  // 心跳的回应
                                    Log.e("socketHeart", str);
                                    break;
                                case 2:  //1-1收到的消息
                                    onGetPriMsg.onPersonalMsgGet(msgBean);
                                    break;
                                case 3:  //组队收到的消息
                                    onGetPriMsg.onTeamMsgGet(msgBean);
                                    break;
                            }

                            // respListener.onSocketResponse(str);
                        }
                    }

                    reconn();//走到这一步，说明服务器socket断了
                    break;
                }
            } catch (SocketException e1) {
                e1.printStackTrace();//客户端主动socket.close()会调用这里 java.net.SocketException: Socket closed
            } catch (Exception e2) {
                Log.v(TAG, "Rec :Exception");
                e2.printStackTrace();
            }

            Log.v(TAG, "Rec :End");
        }
    }

    private void connectHeart() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                HashMap<String, String> heartParam = new HashMap<>();
                heartParam.put(ImConstants.APP_ID, mInfoParam.get(ImConstants.APP_ID));
                heartParam.put(ImConstants.SERVER_ID, mInfoParam.get(ImConstants.SERVER_ID));
                heartParam.put(ImConstants.FUID, mInfoParam.get(ImConstants.FUID));
                heartParam.put(ImConstants.HANDLER, "1");
                heartParam.put(ImConstants.TID, "0"); // 固定
                heartParam.put(ImConstants.MSG, "heart beat");
                heartParam.put(ImConstants.MSG_ID, System.currentTimeMillis() + "");

                try {
                    String heartMsg = new JSONObject(heartParam).toString();
                    send(new Packet(heartMsg));
                    Log.e("socketHeartBeat", heartMsg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 20 * 1000);
    }
}
