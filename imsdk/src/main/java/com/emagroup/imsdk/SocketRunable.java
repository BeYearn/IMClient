package com.emagroup.imsdk;

import android.util.Log;

import com.emagroup.imsdk.response.ImResponse;

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

    public static SocketRunable getInstance(Map<String, String> param, String host, int port, ImResponse response){
        if(mInstance==null){
            try {
                mInstance = new SocketRunable(param,host,port,response);
            } catch (IOException e) {
                Log.e("SocketRunable","获取soket实例失败");
                e.printStackTrace();
            }
        }
        return mInstance;
    }

    private SocketRunable(Map<String, String> param, String host, int port, ImResponse response) throws IOException {
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

            String readMsg = null;

            //第一步连接成功后受到服务器信息
            readMsg = mSocketReader.readLine();
            JSONObject object1 = new JSONObject(readMsg);
            String handler1 = object1.getString("handler");

            if (!"0".equals(handler1)) {
                Log.e("longconnection", "1 failed");
                return;
            }

            //第二部向服务器提交信息
            mSocketWriter.write(new JSONObject(mInfoParam).toString());
            mSocketWriter.flush();    // 写如记得刷新！！

            readMsg = mSocketReader.readLine();
            JSONObject object2 = new JSONObject(readMsg);
            String handler2 = object2.getString("handler");

            if (!"96".equals(handler2)) {
                Log.e("longconnection", "2 failed");
                return;
            }

            // 第三步开始维持心跳保持连接
            connectHeart();

            //连接成功回调
            mResponse.onSuccessResponse();

            //循环不断从socket中读取数据
            while ((readMsg = mSocketReader.readLine()) != null) {
                Log.e("clientTheard", readMsg);   // {"appId":"20007","serverId":"01","fUid":"","fName":"","handler":"96","tId":"6","msg":"","msgId":"1492596604479"}
            }


        } catch (Exception e) {
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
                    Log.e("connectHeartBeat",heartMsg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 20 * 1000);
    }

}
