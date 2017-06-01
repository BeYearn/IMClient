package com.emagroup.example;

import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.emagroup.imsdk.EmaImSdk;
import com.emagroup.imsdk.ImConstants;
import com.emagroup.imsdk.MsgBean;
import com.emagroup.imsdk.response.ChannelHandler;
import com.emagroup.imsdk.response.ImResponse;
import com.emagroup.imsdk.response.LCStateListener;
import com.emagroup.imsdk.response.SendResponse;
import com.emagroup.imsdk.util.ToastHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText metAppId;
    private EditText metSelfUid;
    private EditText metMsgLimit;
    private EditText metChannelId;
    private Button btRegist;
    private Button btClearAll;
    private Button btStopIm;
    private Button btSenfLongMsg;
    private Button btSenfshortMsg;
    private Button btSenfPriMsg;
    private Button btJoinSChannel;
    private Button btJoinLChannel;
    private Button btLeaveSChannel;
    private Button btLeaveLChannel;
    private RecyclerView recylerMsg;
    private ArrayList<String> mDataList;
    private MsgAdapter mMsgAdapter;
    private EditText etAppId;
    private EditText etSelfUid;
    private EditText etMsgLimit;
    //private EditText etChannelId;
    private EditText etChangeChannelId;
    private EditText etShortChannelId;
    private EditText etShortMsg;
    private EditText etLongChannelId;
    private EditText etLongMsg;
    private EditText etPriUid;
    private EditText etPriMsg;

    private Button btMenu;
    private EditText etChatLogUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btRegist = (Button) findViewById(R.id.bt_regist);
        //btIsLongLinked = (Button) findViewById(R.id.bt_long_islinked);
        //btLongReconec = (Button) findViewById(R.id.bt_long_reconnec);
        btClearAll = (Button) findViewById(R.id.bt_clear_all);
        btStopIm = (Button) findViewById(R.id.bt_stop_im);

        btSenfLongMsg = (Button) findViewById(R.id.bt_send_longmsg);
        btSenfshortMsg = (Button) findViewById(R.id.bt_send_shortmsg);
        btSenfPriMsg = (Button) findViewById(R.id.bt_send_primsg);

        btJoinSChannel = (Button) findViewById(R.id.bt_join_shortlink_channel);
        btJoinLChannel = (Button) findViewById(R.id.bt_join_longlink_channel);
        btLeaveSChannel = (Button) findViewById(R.id.bt_leave_shortid);
        btLeaveLChannel = (Button) findViewById(R.id.bt_leave_longid);

        etAppId = (EditText) findViewById(R.id.et_app_id);
        etSelfUid = (EditText) findViewById(R.id.et_self_id);
        etMsgLimit = (EditText) findViewById(R.id.et_msg_limit);
        //etChannelId = (EditText) findViewById(R.id.et_channel_id);

        etChangeChannelId = (EditText) findViewById(R.id.et_change_channel_id);

        etShortChannelId = (EditText) findViewById(R.id.et_short_channelid);
        etShortMsg = (EditText) findViewById(R.id.et_short_msg);
        etLongChannelId = (EditText) findViewById(R.id.et_long_channelid);
        etLongMsg = (EditText) findViewById(R.id.et_long_msg);
        etPriUid = (EditText) findViewById(R.id.et_pri_uid);
        etPriMsg = (EditText) findViewById(R.id.et_pri_msg);

        btMenu = (Button) findViewById(R.id.bt_menu);

        btRegist.setOnClickListener(this);
        //btIsLongLinked.setOnClickListener(this);
        //btLongReconec.setOnClickListener(this);
        btClearAll.setOnClickListener(this);
        btStopIm.setOnClickListener(this);
        btSenfLongMsg.setOnClickListener(this);
        btSenfshortMsg.setOnClickListener(this);
        btSenfPriMsg.setOnClickListener(this);
        btJoinSChannel.setOnClickListener(this);
        btJoinLChannel.setOnClickListener(this);
        btLeaveSChannel.setOnClickListener(this);
        btLeaveLChannel.setOnClickListener(this);
        btMenu.setOnClickListener(this);

        recylerMsg = (RecyclerView) findViewById(R.id.recycler_msg);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recylerMsg.setLayoutManager(linearLayoutManager);
        recylerMsg.setHasFixedSize(true);  // 如果每个item高度一定  这个设置可以提高性能
        mDataList = new ArrayList<>();
        mMsgAdapter = new MsgAdapter(mDataList);
        recylerMsg.setItemAnimator(new DefaultItemAnimator());
        recylerMsg.setAdapter(mMsgAdapter);


        EmaImSdk.getInstance().setLCStateListener(new LCStateListener() {
            @Override
            public void onState(int stateCode) {
                Log.e("LCState!!!!!","code"+stateCode);
            }
        });

        EmaImSdk.getInstance().setDebugable(false);
    }


    public void regist() {
        HashMap<String, String> registParams = new HashMap<>();
        registParams.put(ImConstants.APP_ID, "20007");
        registParams.put(ImConstants.APP_KEY, "a5fdfc18c72f4fc9602746ddec9f3b21");
        registParams.put(ImConstants.UID, etSelfUid.getText().toString());
        registParams.put(ImConstants.MSG_NUM_LIMIT, etMsgLimit.getText().toString()); // 该字段不传默认为10
        registParams.put(ImConstants.SERVER_URL, "http://118.178.230.138:8080/");  //注意格式

        registParams.put(ImConstants.SHORT_HEARTBEAT_DELAY, "10");
        registParams.put(ImConstants.LONG_HEARTBEAT_DELAY, "20");
        EmaImSdk.getInstance().regist(this, registParams, new ImResponse() {

            @Override
            public void onSuccessed() {
                ToastHelper.toast(MainActivity.this, "regist succ");
            }

            @Override
            public void onFailed(int code) {
                ToastHelper.toast(MainActivity.this, "regist onFailed"+code);
            }

            @Override
            public void onStoped() {
                ToastHelper.toast(MainActivity.this, "regist onStoped");
            }

            @Override
            public void onGetPriMsg(MsgBean msgBean) {
                //mDataList.add(msgBean.getFuid() + " : " + msgBean.getMsg());
                //mMsgAdapter.notifyDataSetChanged();
                mMsgAdapter.add(msgBean.getFuid() + " : " + msgBean.getMsg());
                recylerMsg.smoothScrollToPosition(mDataList.size() - 1);
            }
        });
    }

    public void joinShortChannel() {

        EmaImSdk.getInstance().joinShortLinkChannel(etChangeChannelId.getText().toString(), new ChannelHandler() {
            @Override
            public void onJoineSucc(String channelId) {
                ToastHelper.toast(MainActivity.this, "onJoineSucc succ");
            }

            @Override
            public void onJoinFail(int code) {
                ToastHelper.toast(MainActivity.this, "onJoinFail"+code);
            }

            @Override
            public void onGetMsg(MsgBean msgBean) {
                //mDataList.add(msgBean.getFuid() + " : " + msgBean.getMsg());
                //mMsgAdapter.notifyDataSetChanged();
                mMsgAdapter.add(msgBean.getFuid() + " : " + msgBean.getMsg());
                recylerMsg.smoothScrollToPosition(mDataList.size() - 1);
            }

            @Override
            public void onLeaveSucc(String channelId) {
                ToastHelper.toast(MainActivity.this, "onLeaveSucc succ");
            }

            @Override
            public void onLeaveFail(int code) {
                ToastHelper.toast(MainActivity.this, "onLeave short Fail"+code);
            }
        });
    }

    public void sendShortMsg() {
        EmaImSdk.getInstance().sendShortLinkMsg(etShortChannelId.getText().toString(), etSelfUid.getText().toString(), etShortMsg.getText().toString(), "ext", new SendResponse() {
            @Override
            public void onSendSucc() {
                ToastHelper.toast(MainActivity.this, "onSendSucc");
            }

            @Override
            public void onSendFail(int code) {
                ToastHelper.toast(MainActivity.this, "onSendFail"+code);
            }
        });
    }

    public void leaveShortChannel() {
        EmaImSdk.getInstance().leaveShortLinkChannel(etShortChannelId.getText().toString());
    }


    public void joinLongChannel() {
        EmaImSdk.getInstance().joinLongLinkChannel(etChangeChannelId.getText().toString(), new ChannelHandler() {
            @Override
            public void onJoineSucc(String channelId) {
                ToastHelper.toast(MainActivity.this, "joinLongChannel succ");
            }

            @Override
            public void onJoinFail(int code) {
                ToastHelper.toast(MainActivity.this, "onJoinFail"+code);
            }

            @Override
            public void onGetMsg(MsgBean msgBean) {
                //mDataList.add(msgBean.getFuid() + " : " + msgBean.getMsg());
                //mMsgAdapter.notifyDataSetChanged();
                mMsgAdapter.add(msgBean.getFuid() + " : " + msgBean.getMsg());
                recylerMsg.smoothScrollToPosition(mDataList.size() - 1);
            }

            @Override
            public void onLeaveSucc(String channelId) {
                ToastHelper.toast(MainActivity.this, "onLeaveSucc succ");
            }

            @Override
            public void onLeaveFail(int code) {
                ToastHelper.toast(MainActivity.this, "onLeaveFail"+code);
            }
        });
    }

    public void sendLongMsg() {
        EmaImSdk.getInstance().sendLongLinkMsg(etLongChannelId.getText().toString(), etSelfUid.getText().toString(), etLongMsg.getText().toString(), "ext", new SendResponse() {
            @Override
            public void onSendSucc() {
                ToastHelper.toast(MainActivity.this, "onSendSucc");
            }

            @Override
            public void onSendFail(int code) {
                ToastHelper.toast(MainActivity.this, "onSendFail"+code);
            }
        });
    }

    public void sendPriMsg() {
        EmaImSdk.getInstance().sendPriMsg(etPriUid.getText().toString(), etSelfUid.getText().toString(), etPriMsg.getText().toString(), "ext", new SendResponse() {
            @Override
            public void onSendSucc() {
                ToastHelper.toast(MainActivity.this, "onSendSucc");
            }

            @Override
            public void onSendFail(int code) {
                ToastHelper.toast(MainActivity.this, "onSendFail"+code);
            }
        });
    }

    public void leaveLongChannel() {
        EmaImSdk.getInstance().leaveLongLinkChannel(etLongChannelId.getText().toString());
    }

    private void doStopPriConnect() {
        EmaImSdk.getInstance().stop();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_regist:
                regist();
                break;
            case R.id.bt_long_islinked:
                isLongLinked();
                break;
            case R.id.bt_long_reconnec:
                longReconec();
                break;
            case R.id.bt_clear_all:
                mMsgAdapter.removeAll();
                break;
            case R.id.bt_stop_im:
                doStopPriConnect();
                break;

            case R.id.bt_join_shortlink_channel:
                joinShortChannel();
                break;
            case R.id.bt_send_shortmsg:
                sendShortMsg();
                break;
            case R.id.bt_leave_shortid:
                leaveShortChannel();
                break;
            case R.id.bt_join_longlink_channel:
                joinLongChannel();
                break;
            case R.id.bt_send_longmsg:
                sendLongMsg();
                break;
            case R.id.bt_leave_longid:
                leaveLongChannel();
                break;
            case R.id.bt_send_primsg:
                sendPriMsg();
                break;
            case R.id.bt_menu:
                showBottomMenu();
                break;
            case R.id.bt_show_chat_record:
                showChatLog();
                break;
        }

    }

    private void longReconec() {
        EmaImSdk.getInstance().longLinkReConnect();
    }

    private void isLongLinked() {
        if (EmaImSdk.getInstance().isNeedReConnect()) {
            ToastHelper.toast(MainActivity.this, "需要");
        } else {
            ToastHelper.toast(MainActivity.this, "不用");
        }
    }

    private void showBottomMenu() {
        BottomSheetDialog sheetDialog = new BottomSheetDialog(this);
        sheetDialog.setContentView(R.layout.ll_bottom_sheet);

        Button btIsLongLinked = (Button) sheetDialog.findViewById(R.id.bt_long_islinked);
        Button btLongReconec = (Button) sheetDialog.findViewById(R.id.bt_long_reconnec);
        etChatLogUid = (EditText) sheetDialog.findViewById(R.id.et_chat_record_uid);
        Button btShowChatLog =  (Button) sheetDialog.findViewById(R.id.bt_show_chat_record);

        btIsLongLinked.setOnClickListener(this);
        btLongReconec.setOnClickListener(this);
        btShowChatLog.setOnClickListener(this);

        sheetDialog.show();
    }

    private void showChatLog() {
        String tUid = etChatLogUid.getText().toString();

        ArrayList<MsgBean> msgBeenList = EmaImSdk.getInstance().queryPriMsgRecord(etSelfUid.getText().toString(), tUid, "10");

        for(MsgBean msgBean:msgBeenList){
            mMsgAdapter.add("聊天记录： "+msgBean.getMsg());
        }
    }


}
