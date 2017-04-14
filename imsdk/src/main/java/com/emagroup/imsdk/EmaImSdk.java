package com.emagroup.imsdk;

import android.content.Context;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Administrator on 2017/4/13.
 */

public class EmaImSdk {

    private static EmaImSdk instance;
    private String appKey;
    private Context mContext;

    private EmaImSdk() {
    }

    public static EmaImSdk getInstance() {
        if (null == instance) {
            instance = new EmaImSdk();
        }
        return instance;
    }


    public void init(Context context, String key) {
        this.mContext = context;
        this.appKey = key;
        ImUrl.initUrl(context);
    }

    public void login(HashMap<String, String> param) {

        param.put(ImConstants.APP_ID, ConfigUtils.getAppId(mContext));
        param.put(ImConstants.TIME_STAMP, System.currentTimeMillis() + "");

        String sign = param.get(ImConstants.APP_ID) + param.get(ImConstants.SERVER_ID) + param.get(ImConstants.TIME_STAMP) + param.get(ImConstants.UID) + appKey;
        sign = ConfigUtils.MD5(sign);
        param.put(ImConstants.SIGN, sign);
        new HttpRequestor().doPostAsync(ImUrl.getLoginUrl(), param, new HttpRequestor.OnResponsetListener() {
            @Override
            public void OnResponse(String result) {
                try {

                    JSONObject jsonObject = new JSONObject(result);
                    JSONObject data = jsonObject.getJSONObject("data");
                    String serverHost = data.getString("host");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public String getAppKey() {
        return appKey;
    }
}
