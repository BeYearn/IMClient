package com.emagroup.imsdk;

import android.util.Log;

import com.emagroup.imsdk.response.ImResponse;
import com.emagroup.imsdk.response.PrivateMsgResponse;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2017/4/19.
 */

public class SocketRunable implements Runnable {

    private ImResponse mResponse;
    private Map<String, String> mInfoParam;
    private String mHost;
    private int mPort;
    private BufferedReader mSocketReader;
    private BufferedWriter mSocketWriter;
    private static SocketRunable mInstance;
    private PrivateMsgResponse mPrivateMsgResponse;

    public static SocketRunable getInstance() {
        if (mInstance == null) {
            mInstance = new SocketRunable();
            Log.e("newSocketRunable",mInstance.toString());
        }
        return mInstance;
    }

    private SocketRunable() {

    }

    public void setStartInfo(Map<String, String> param, String host, int port, ImResponse response) {
        this.mInfoParam = param;
        this.mHost = host;
        this.mPort = port;
        this.mResponse = response;
    }


    @Override
    public void run() {
        try {

            //定义当前线程所处理的Socket
            Socket socket = new Socket(mHost, mPort);
            //获取该socket对应的输入流
            mSocketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            mSocketWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            //向服务器提交初始信息
            putStrIntoSocket(new JSONObject(mInfoParam).toString());

            // 第三步开始维持心跳保持连接
            connectHeart();

            //循环不断从socket中读取数据
            String readMsg = null;
            while ((readMsg = mSocketReader.readLine()) != null) {
                Log.e("clientTheard", readMsg);   // {"appId":"20007","serverId":"01","fUid":"","fName":"","handler":"96","tId":"6","msg":"","msgId":"1492596604479"}

                JSONObject strFromSocket = new JSONObject(readMsg);

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

                        break;
                    case 96: //提交初始信息后服务器返会
                        //连接成功回调
                        mResponse.onSuccessResponse();
                        break;
                    case 1:  // 心跳的回应
                        Log.e("socketHeart", readMsg);
                        break;
                    case 2:  //1-1收到的消息
                        mPrivateMsgResponse.onPersonalMsgGet(msgBean);
                        break;
                    case 3:  //组队收到的消息
                        mPrivateMsgResponse.onTeamMsgGet(msgBean);
                        break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void putStrIntoSocket(String string) {
        try {
            mSocketWriter.write(string);
            mSocketWriter.flush();    // 写如记得刷新！！
        } catch (IOException e) {
            e.printStackTrace();
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
                    mSocketWriter.write(heartMsg);
                    mSocketWriter.flush();
                    Log.e("connectHeartBeat", heartMsg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 20 * 1000);
    }

    public void setOnMsgResponce(PrivateMsgResponse onResponce) {
        this.mPrivateMsgResponse = onResponce;
    }
}
