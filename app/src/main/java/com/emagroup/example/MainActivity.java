package com.emagroup.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.emagroup.imsdk.EmaImSdk;
import com.emagroup.imsdk.ImConstants;
import com.emagroup.imsdk.response.ImResponse;
import com.emagroup.imsdk.MsgBean;
import com.emagroup.imsdk.response.MsgHeartResponse;
import com.emagroup.imsdk.util.ToastHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btLogin;
    private Button btUpdateInfo;
    private Button btheart;
    private Button btSendMsg;
    private RecyclerView recylerMsg;
    private MsgAdapter mMsgAdapter;
    private ArrayList<String> mDataList;
    private Button btClearAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EmaImSdk.getInstance().init(this, "a5fdfc18c72f4fc9602746ddec9f3b21"); //20007


        btLogin = (Button) findViewById(R.id.bt_login_server);
        btUpdateInfo = (Button) findViewById(R.id.bt_update_info);
        btheart = (Button) findViewById(R.id.bt_heart_beat);
        btSendMsg = (Button) findViewById(R.id.bt_send_msg);
        btClearAll = (Button) findViewById(R.id.bt_clear_all);

        btLogin.setOnClickListener(this);
        btUpdateInfo.setOnClickListener(this);
        btheart.setOnClickListener(this);
        btSendMsg.setOnClickListener(this);
        btClearAll.setOnClickListener(this);


        recylerMsg = (RecyclerView) findViewById(R.id.recycler_msg);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recylerMsg.setLayoutManager(linearLayoutManager);
        recylerMsg.setHasFixedSize(true);  // 如果每个item高度一定  这个设置可以提高性能
        mDataList = new ArrayList<>();
        mMsgAdapter = new MsgAdapter(mDataList);
        recylerMsg.setAdapter(mMsgAdapter);
    }


    private void doLogin() {
        HashMap<String, String> param = new HashMap<>();
        param.put(ImConstants.SERVER_ID, "01");
        param.put(ImConstants.UID, "6");
        param.put(ImConstants.TEAM_ID, "123");
        param.put(ImConstants.UNION_ID, "c工会");
        param.put(ImConstants.WORLD_ID, "a");
        param.put(ImConstants.WORLD_LIMIT, "10");
        param.put(ImConstants.UNION_LIMIT, "10");
        EmaImSdk.getInstance().login(param,new ImResponse(){
            @Override
            public void onSuccessResponse() {
                ToastHelper.toast(MainActivity.this,"login success");
            }
        });
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
        EmaImSdk.getInstance().updateInfo(param,new ImResponse(){
            @Override
            public void onSuccessResponse() {
                ToastHelper.toast(MainActivity.this,"updateInfo success");
            }
        });
    }

    private void doHeart() {
        HashMap<String, String> param = new HashMap<>();
        param.put(ImConstants.SERVER_ID, "01");
        param.put(ImConstants.UID, "6");
        param.put(ImConstants.TEAM_ID, "123");
        param.put(ImConstants.UNION_ID, "c工会");
        param.put(ImConstants.WORLD_ID, "a");

        EmaImSdk.getInstance().msgHeart(param, new MsgHeartResponse() {
            @Override
            public void onUnionMsgGet(MsgBean UnionMsgBean) {
                Log.e("UnionMsg", UnionMsgBean.getMsg());
                mDataList.add("公会："+UnionMsgBean.getMsg());
                mMsgAdapter.notifyDataSetChanged();
                //mMsgAdapter.notifyItemInserted(mDataList.size());

                recylerMsg.smoothScrollToPosition(mDataList.size()-1);
            }

            @Override
            public void onWorldMsgGet(MsgBean worldMsgBean) {
                Log.e("worldMsg", worldMsgBean.getMsg());
                mDataList.add("世界："+worldMsgBean.getMsg());
                mMsgAdapter.notifyDataSetChanged();

                recylerMsg.smoothScrollToPosition(mDataList.size()-1);
            }

        }, 5);
    }
    private void doSendMsg() {
        HashMap<String, String> param = new HashMap<>();
        param.put(ImConstants.SERVER_ID, "01");
        param.put(ImConstants.FUID, "6");
        param.put(ImConstants.FNAME,"beyearn");
        param.put(ImConstants.HANDLER, "4");    // 5世界 4工会
        param.put(ImConstants.TID, "c工会");
        param.put(ImConstants.MSG,"beyearnsmsg");
        EmaImSdk.getInstance().sendMsg(param, new ImResponse() {
            @Override
            public void onSuccessResponse() {
                ToastHelper.toast(MainActivity.this,"send msg success");
            }
        });
    }


    private void doSocket() {
        EmaImSdk.getInstance().exactMsg();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_login_server:
                doLogin();
                break;
            case R.id.bt_update_info:
                doUpdate();
                break;
            case R.id.bt_heart_beat:
                doHeart();
                break;
            case R.id.bt_send_msg:
                doSendMsg();
                break;
            case R.id.bt_socket:
                doSocket();
                break;
            case R.id.bt_clear_all:
                mDataList.clear();
                mMsgAdapter.notifyDataSetChanged();
                break;
        }

    }



}
