package com.emagroup.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.emagroup.imsdk.EmaImSdk;
import com.emagroup.imsdk.ImConstants;
import com.emagroup.imsdk.MsgBean;
import com.emagroup.imsdk.response.ChannelHandler;
import com.emagroup.imsdk.response.ImResponse;
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
    private EditText etUid;
    private EditText etMsgLimit;
    private EditText etChannelId;
    private EditText etChangeChannelId;
    private EditText etShortChannelId;
    private EditText etShortMsg;
    private EditText etLongChannelId;
    private EditText etLongMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btRegist = (Button) findViewById(R.id.bt_regist);
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
        etUid = (EditText) findViewById(R.id.et_self_id);
        etMsgLimit = (EditText) findViewById(R.id.et_msg_limit);
        etChannelId = (EditText) findViewById(R.id.et_channel_id);

        etChangeChannelId = (EditText) findViewById(R.id.et_change_channel_id);

        etShortChannelId = (EditText) findViewById(R.id.et_short_channelid);
        etShortMsg = (EditText) findViewById(R.id.et_short_msg);
        etLongChannelId = (EditText) findViewById(R.id.et_long_channelid);
        etLongMsg = (EditText) findViewById(R.id.et_long_msg);

        btRegist.setOnClickListener(this);
        btClearAll.setOnClickListener(this);
        btStopIm.setOnClickListener(this);
        btSenfLongMsg.setOnClickListener(this);
        btSenfshortMsg.setOnClickListener(this);
        btSenfPriMsg.setOnClickListener(this);
        btJoinSChannel.setOnClickListener(this);
        btJoinLChannel.setOnClickListener(this);
        btLeaveSChannel.setOnClickListener(this);
        btLeaveLChannel.setOnClickListener(this);

        recylerMsg = (RecyclerView) findViewById(R.id.recycler_msg);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recylerMsg.setLayoutManager(linearLayoutManager);
        recylerMsg.setHasFixedSize(true);  // 如果每个item高度一定  这个设置可以提高性能
        mDataList = new ArrayList<>();
        mMsgAdapter = new MsgAdapter(mDataList);
        recylerMsg.setAdapter(mMsgAdapter);

    }


    public void regist() {
        HashMap<String, String> registParams = new HashMap<>();
        registParams.put(ImConstants.APP_ID, "20007");
        registParams.put(ImConstants.APP_KEY, "a5fdfc18c72f4fc9602746ddec9f3b21");
        registParams.put(ImConstants.UID, "88");
        registParams.put(ImConstants.MSG_NUM_LIMIT, "8"); // 该字段不传默认为10
        registParams.put(ImConstants.SERVER_URL, "http://118.178.230.138:8080/");  //注意格式
        EmaImSdk.getInstance().regist(registParams, new ImResponse() {
            @Override
            public void onSuccessed() {

            }

            @Override
            public void onFailed() {

            }

            @Override
            public void onStoped() {

            }

            @Override
            public void onGetPriMsg() {

            }
        });
    }

    public void joinShortChannel() {

        EmaImSdk.getInstance().joinShortLinkChannel("room01", new ChannelHandler() {
            @Override
            public void onJoined(String channelId) {
                ToastHelper.toast(MainActivity.this, "onJoined succ");
            }

            @Override
            public void onGetMsg(MsgBean msgBean) {
                mDataList.add(msgBean.getMsg());
                mMsgAdapter.notifyDataSetChanged();
            }

            @Override
            public void onStop(String channelId) {
                ToastHelper.toast(MainActivity.this, "onStop succ");
            }
        });
    }

    public void sendShortMsg() {
        EmaImSdk.getInstance().sendShortLinkMsg("room01", "大哥大", "woshidageda");
    }

    public void leaveShortChannel() {
        EmaImSdk.getInstance().leaveShortLinkChannel("room01");
    }


    private void doStopPriConnect() {
        EmaImSdk.getInstance().stopPriConnect();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_regist:
                regist();
                break;
            case R.id.bt_clear_all:
                mDataList.clear();
                mMsgAdapter.notifyDataSetChanged();
                break;
            case R.id.bt_stop_im:

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
        }

    }


}
