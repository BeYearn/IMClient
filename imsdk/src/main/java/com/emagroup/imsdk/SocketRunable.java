package com.emagroup.imsdk;

import android.os.SystemClock;
import android.util.Log;

import com.emagroup.imsdk.response.ImResponse;
import com.emagroup.imsdk.response.PrivateMsgResponse;
import com.emagroup.imsdk.util.ThreadUtil;

import org.json.JSONObject;

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
    private static SocketRunable mInstance;
    private PrivateMsgResponse mPrivateMsgResponse;
    private RunableWrite mRunableWrite;

    public static SocketRunable getInstance() {
        if (mInstance == null) {
            mInstance = new SocketRunable();
            Log.e("newSocketRunable", mInstance.toString());
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
        Socket socket = null;
        int i = 0;
        while (null == socket) {
            try {
                i++;
                //定义当前线程所处理的Socket
                socket = new Socket(mHost, mPort);

                //获取该socket对应的输入流
                ThreadUtil.runInSubThread(new RunableRead(socket.getInputStream(), mResponse, mPrivateMsgResponse));

                mRunableWrite = new RunableWrite(socket.getOutputStream());
                ThreadUtil.runInSubThread(mRunableWrite);

                //向服务器提交初始信息
                putStrIntoSocket(new JSONObject(mInfoParam).toString());

                // 第三步开始维持心跳保持连接
                connectHeart();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("socketRunable", "socket连接失败, 重试中...");
                if(i>5){
                    break;
                }
                SystemClock.sleep(1000);  //1秒后重连
            }
        }
    }

    public void putStrIntoSocket(String string) {
        if (null != mRunableWrite) {
            mRunableWrite.putStrIntoSocket(string);
        } else {
            Log.e("Socketrunable", "请先建立长连接");
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
                    putStrIntoSocket(heartMsg);
                    Log.e("socketHeartBeat", heartMsg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 20 * 1000);
    }

    public void setOnMsgResponce(PrivateMsgResponse onResponce) {
        this.mPrivateMsgResponse = onResponce;
    }
}
