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
import com.emagroup.imsdk.MsgBean;
import com.emagroup.imsdk.response.ImResponse;
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
    private EditText etPubMsg;
    private Button btSenfPriMsg;
    private String mServerId;
    private String mUid;
    private String mTeamId;
    private String mUnionId;
    private String mWorldId;
    private EditText etPubid;
    private EditText etPubhanler;
    private EditText etPriid;
    private EditText etPrihandler;
    private EditText etSelfId;
    private EditText etWorldId;
    private EditText etUnionId;
    private EditText etTeamId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btLogin = (Button) findViewById(R.id.bt_init);
        btUpdateInfo = (Button) findViewById(R.id.bt_update_info);
        btSendPubMsg = (Button) findViewById(R.id.bt_send_pub_msg);
        btSenfPriMsg = (Button) findViewById(R.id.bt_send_pri_msg);
        btClearAll = (Button) findViewById(R.id.bt_clear_all);
        btLongConnect = (Button) findViewById(R.id.bt_socket_build);

        etSelfId= (EditText) findViewById(R.id.et_self_id);
        etWorldId= (EditText) findViewById(R.id.et_world_id);
        etUnionId= (EditText) findViewById(R.id.et_union_id);
        etTeamId = (EditText) findViewById(R.id.et_team_id);

        etPubMsg = (EditText) findViewById(R.id.et_pub_msg);
        etPubid = (EditText) findViewById(R.id.et_pub_id);
        etPubhanler = (EditText) findViewById(R.id.et_pub_handler);

        etPriMsg = (EditText) findViewById(R.id.et_pri_msg);
        etPriid= (EditText) findViewById(R.id.et_pri_id);
        etPrihandler= (EditText) findViewById(R.id.et_pri_handler);

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


    }



    private void doInitAndbuildPubConnect() {


        mServerId= "01";
        mUid=etSelfId.getText().toString();
        mTeamId= etTeamId.getText().toString();
        mUnionId=etUnionId.getText().toString();
        mWorldId = etWorldId.getText().toString();


        //初始化
        HashMap<String, String> paramInit = new HashMap<>();
        paramInit.put(ImConstants.SERVER_ID, mServerId);
        paramInit.put(ImConstants.UID, mUid);
        EmaImSdk.getInstance().init(this, paramInit,"a5fdfc18c72f4fc9602746ddec9f3b21"); //20007


        //建立工会、世界的连接
        HashMap<String, String> param = new HashMap<>();
        param.put(ImConstants.UNION_ID, mUnionId);   // 没有就传 ""
        param.put(ImConstants.WORLD_ID, mWorldId);
        param.put(ImConstants.WORLD_LIMIT, "10");
        param.put(ImConstants.UNION_LIMIT, "10");
        EmaImSdk.getInstance().buildPubConnect(param, 10);
    }


    private void doUpdate() {

        mUid=etSelfId.getText().toString();
        mTeamId= etTeamId.getText().toString();
        mUnionId=etUnionId.getText().toString();
        mWorldId = etWorldId.getText().toString();

        HashMap<String, String> param = new HashMap<>();
        param.put(ImConstants.UNION_ID, mUnionId);   // 改哪个传那个，短链改这两个
        param.put(ImConstants.WORLD_ID, mWorldId);
        param.put(ImConstants.WORLD_LIMIT, "10");
        param.put(ImConstants.UNION_LIMIT, "10");
        EmaImSdk.getInstance().updatePubInfo(param, new ImResponse() {
            @Override
            public void onSuccessResponse() {
                ToastHelper.toast(MainActivity.this, "updateInfo success");
            }
        });
    }

    private void doGetPublicMsg() {
        EmaImSdk.getInstance().getPubMsg(new PublicMsgResponse() {
            @Override
            public void onUnionMsgGet(MsgBean UnionMsgBean) {

                mDataList.add("公会：" + UnionMsgBean.getMsg());
                mMsgAdapter.notifyDataSetChanged();

                recylerMsg.smoothScrollToPosition(mDataList.size() - 1);
            }

            @Override
            public void onWorldMsgGet(MsgBean worldMsgBean) {

                mDataList.add("世界：" + worldMsgBean.getMsg());
                mMsgAdapter.notifyDataSetChanged();

                recylerMsg.smoothScrollToPosition(mDataList.size() - 1);
            }

        });

    }

    private void doSendPublicMsg() {

        HashMap<String, String> param = new HashMap<>();
        param.put(ImConstants.FNAME, mUid);
        param.put(ImConstants.HANDLER,etPubhanler.getText().toString());    // 5世界 4工会
        param.put(ImConstants.TID, etPubid.getText().toString());
        param.put(ImConstants.MSG, etPubMsg.getText().toString());
        EmaImSdk.getInstance().sendPubMsg(param);
    }


    /**
     * 队伍私聊前建立长连接
     */
    private void dobuildConnect() {
        EmaImSdk.getInstance().buildPriConnect(new ImResponse() {
            @Override
            public void onSuccessResponse() {
                ToastHelper.toast(MainActivity.this, "buildLongConnect success");
            }
        });
    }


    /**
     * 发送私人组队消息
     */
    private void doSendPrivateMsg() {
        String s = etPriMsg.getText().toString();
        String[] split = s.split(",");

        HashMap<String, String> param = new HashMap<>();
        param.put(ImConstants.FNAME, mUid);
        param.put(ImConstants.HANDLER, etPrihandler.getText().toString());    // 2私聊 3队伍
        param.put(ImConstants.TID, etPriid.getText().toString());
        param.put(ImConstants.MSG, etPriMsg.getText().toString());
        EmaImSdk.getInstance().sendPriMsg(param);
    }

    private void doGetPrivateMsg() {
        EmaImSdk.getInstance().getPriMsg(new PrivateMsgResponse() {
            @Override
            public void onPersonalMsgGet(final MsgBean MsgBean) {
                Log.e("Personal", MsgBean.getMsg());
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDataList.add("私聊：" + MsgBean.getMsg());
                        mMsgAdapter.notifyDataSetChanged();
                        recylerMsg.smoothScrollToPosition(mDataList.size() - 1);
                    }
                });
            }

            @Override
            public void onTeamMsgGet(final MsgBean MsgBean) {
                Log.e("Team", MsgBean.getMsg());
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDataList.add("世界：" + MsgBean.getMsg());
                        mMsgAdapter.notifyDataSetChanged();
                        recylerMsg.smoothScrollToPosition(mDataList.size() - 1);
                    }
                });
            }

        });
    }

    private void doupdateTeamInfo(){

        HashMap<String, String> param = new HashMap<>();
        param.put(ImConstants.MSG, "队伍id");

        EmaImSdk.getInstance().updateTeamInfo(param);
    }

    private void doStopPriConnect(){
        EmaImSdk.getInstance().stopPriConnect();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_init:
                doInitAndbuildPubConnect();
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
                //mDataList.clear();
                //mMsgAdapter.notifyDataSetChanged();

                //doStopPriConnect();

                doGetPublicMsg();
                doGetPrivateMsg();
                break;
        }

    }


}
