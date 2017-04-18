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
import com.emagroup.imsdk.MsgBean;
import com.emagroup.imsdk.MsgHeartResponse;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EmaImSdk.getInstance().init(this, "a5fdfc18c72f4fc9602746ddec9f3b21"); //20007


        btLogin = (Button) findViewById(R.id.bt_login_server);
        btUpdateInfo = (Button) findViewById(R.id.bt_update_info);
        btheart = (Button) findViewById(R.id.bt_heart_beat);
        btSendMsg = (Button) findViewById(R.id.bt_send_msg);

        btLogin.setOnClickListener(this);
        btUpdateInfo.setOnClickListener(this);
        btheart.setOnClickListener(this);
        btSendMsg.setOnClickListener(this);



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
        EmaImSdk.getInstance().login(param);
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
        EmaImSdk.getInstance().updateInfo(param);
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

                break;
        }

    }

}
