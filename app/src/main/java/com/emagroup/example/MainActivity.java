package com.emagroup.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.emagroup.imsdk.EmaImSdk;
import com.emagroup.imsdk.ImConstants;
import com.emagroup.imsdk.response.ImResponse;
import com.emagroup.imsdk.MsgBean;
import com.emagroup.imsdk.response.PrivateMsgResponse;
import com.emagroup.imsdk.response.PublicMsgResponse;
import com.emagroup.imsdk.util.ToastHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btLogin;
    private Button btUpdateInfo;
    private Button btSendPubMsg;
    private RecyclerView recylerMsg;
    private MsgAdapter mMsgAdapter;
    private ArrayList<String> mDataList;
    private Button btClearAll;
    private Button btLongConnect;
    private EditText etPriMsg;
    private View etPubMsg;
    private Button btSenfPriMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EmaImSdk.getInstance().init(this, "a5fdfc18c72f4fc9602746ddec9f3b21"); //20007


        btLogin = (Button) findViewById(R.id.bt_init);
        btUpdateInfo = (Button) findViewById(R.id.bt_update_info);
        btSendPubMsg = (Button) findViewById(R.id.bt_send_pub_msg);
        btSenfPriMsg = (Button) findViewById(R.id.bt_send_pri_msg);
        btClearAll = (Button) findViewById(R.id.bt_clear_all);
        btLongConnect = (Button) findViewById(R.id.bt_socket_build);
        etPriMsg = (EditText) findViewById(R.id.et_pri_msg);
        etPubMsg = findViewById(R.id.et_pub_msg);

        btLogin.setOnClickListener(this);
        btUpdateInfo.setOnClickListener(this);
        btSendPubMsg.setOnClickListener(this);
        btSenfPriMsg.setOnClickListener(this);
        btClearAll.setOnClickListener(this);
        btLongConnect.setOnClickListener(this);

        recylerMsg = (RecyclerView) findViewById(R.id.recycler_msg);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recylerMsg.setLayoutManager(linearLayoutManager);
        recylerMsg.setHasFixedSize(true);  // 如果每个item高度一定  这个设置可以提高性能
        mDataList = new ArrayList<>();
        mMsgAdapter = new MsgAdapter(mDataList);
        recylerMsg.setAdapter(mMsgAdapter);


        doGetPublicMsg();
        doGetPrivateMsg();
    }


    private void doInit() {
        HashMap<String, String> param = new HashMap<>();
        param.put(ImConstants.SERVER_ID, "01");
        param.put(ImConstants.UID, "6");
        param.put(ImConstants.TEAM_ID, "123");
        param.put(ImConstants.UNION_ID, "c工会");
        param.put(ImConstants.WORLD_ID, "a");
        param.put(ImConstants.WORLD_LIMIT, "10");
        param.put(ImConstants.UNION_LIMIT, "10");
        EmaImSdk.getInstance().init(param);
    }


    private void doUpdate() {
        HashMap<String, String> param = new HashMap<>();
        param.put(ImConstants.SERVER_ID, "01");
        param.put(ImConstants.UID, "6");
        param.put(ImConstants.TEAM_ID, "123");
        param.put(ImConstants.UNION_ID, "c工会");
        param.put(ImConstants.WORLD_ID, "a");
        param.put(ImConstants.WORLD_LIMIT, "10");
        param.put(ImConstants.UNION_LIMIT, "10");
        EmaImSdk.getInstance().updateInfo(param, new ImResponse() {
            @Override
            public void onSuccessResponse() {
                ToastHelper.toast(MainActivity.this, "updateInfo success");
            }
        });
    }

    private void doGetPublicMsg() {
        EmaImSdk.getInstance().getPublicMsg(new PublicMsgResponse() {
            @Override
            public void onUnionMsgGet(MsgBean UnionMsgBean) {
                Log.e("UnionMsg", UnionMsgBean.getMsg());
                mDataList.add("公会：" + UnionMsgBean.getMsg());
                mMsgAdapter.notifyDataSetChanged();
                //mMsgAdapter.notifyItemInserted(mDataList.size());

                recylerMsg.smoothScrollToPosition(mDataList.size() - 1);
            }

            @Override
            public void onWorldMsgGet(MsgBean worldMsgBean) {
                Log.e("worldMsg", worldMsgBean.getMsg());
                mDataList.add("世界：" + worldMsgBean.getMsg());
                mMsgAdapter.notifyDataSetChanged();

                recylerMsg.smoothScrollToPosition(mDataList.size() - 1);
            }

        }, 5);

    }

    private void doSendPublicMsg() {
        HashMap<String, String> param = new HashMap<>();
        param.put(ImConstants.SERVER_ID, "01");
        param.put(ImConstants.FUID, "6");
        param.put(ImConstants.FNAME, "beyearn6");
        param.put(ImConstants.HANDLER, "4");    // 5世界 4工会
        param.put(ImConstants.TID, "c工会");
        param.put(ImConstants.MSG, "beyearnsmsg");
        EmaImSdk.getInstance().sendPublicMsg(param, new ImResponse() {
            @Override
            public void onSuccessResponse() {
                ToastHelper.toast(MainActivity.this, "send msg success");
            }
        });
    }


    /**
     * 队伍私聊前建立长连接
     */
    private void dobuildConnect() {
        HashMap<String, String> param = new HashMap<>();
        param.put(ImConstants.SERVER_ID, "01");
        param.put(ImConstants.FUID, "6");
        param.put(ImConstants.HANDLER, "0");    // 0服务器  1心跳  2私聊 3队伍
        param.put(ImConstants.TID, "0");
        EmaImSdk.getInstance().buildLongConnect(param, new ImResponse() {
            @Override
            public void onSuccessResponse() {
                ToastHelper.toast(MainActivity.this, "send buildLongConnect success");
            }
        });
    }


    /**
     * 发送私人组队消息
     */
    private void doSendPrivateMsg() {
        HashMap<String, String> param = new HashMap<>();
        param.put(ImConstants.SERVER_ID, "01");
        param.put(ImConstants.FUID, "6");
        param.put(ImConstants.FNAME, "beyearn6");
        param.put(ImConstants.HANDLER, "2");    // 2私聊 3队伍
        param.put(ImConstants.TID, "8");
        param.put(ImConstants.MSG, "PrivateMsg");
        EmaImSdk.getInstance().sendPrivateMsg(param);
    }

    private void doGetPrivateMsg() {
        EmaImSdk.getInstance().getPrivateMsg(new PrivateMsgResponse() {
            @Override
            public void onPersonalMsgGet(MsgBean MsgBean) {
                Log.e("Personal", MsgBean.getMsg());
                mDataList.add("私聊：" + MsgBean.getMsg());
                mMsgAdapter.notifyDataSetChanged();
                //mMsgAdapter.notifyItemInserted(mDataList.size());

                recylerMsg.smoothScrollToPosition(mDataList.size() - 1);
            }

            @Override
            public void onTeamMsgGet(MsgBean MsgBean) {
                Log.e("Team", MsgBean.getMsg());
                mDataList.add("世界：" + MsgBean.getMsg());
                mMsgAdapter.notifyDataSetChanged();

                recylerMsg.smoothScrollToPosition(mDataList.size() - 1);
            }

        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_init:
                doInit();
                break;
            case R.id.bt_update_info:
                doUpdate();
                break;
            case R.id.bt_send_pub_msg:
                doSendPublicMsg();
                break;
            case R.id.bt_socket_build:
                dobuildConnect();
                break;
            case R.id.bt_send_pri_msg:
                doSendPrivateMsg();
                break;
            case R.id.bt_clear_all:
                mDataList.clear();
                mMsgAdapter.notifyDataSetChanged();
                break;
        }

    }


}
