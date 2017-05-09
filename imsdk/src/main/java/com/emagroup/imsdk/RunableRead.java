package com.emagroup.imsdk;

import android.text.TextUtils;
import android.util.Log;

import com.emagroup.imsdk.response.ImResponse;
import com.emagroup.imsdk.response.PrivateMsgResponse;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Administrator on 2017/4/21.
 */
public class RunableRead implements Runnable {

    private final PrivateMsgResponse mPrivateMsgResponse;
    private final ImResponse mResponse;
    private BufferedReader mSocketReader;

    public RunableRead(InputStream is, ImResponse mResponse, PrivateMsgResponse mPrivateMsgResponse) {
        mSocketReader = new BufferedReader(new InputStreamReader(is));
        this.mResponse = mResponse;
        this.mPrivateMsgResponse = mPrivateMsgResponse;
    }

    public void run() {

        while (true) {
            try {
                String readMsg = mSocketReader.readLine();

                if(TextUtils.isEmpty(readMsg)){
                    continue;
                }
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
                        mResponse.onSuccessed();//连接成功回调
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

            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
