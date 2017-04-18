package com.emagroup.imsdk.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import java.security.MessageDigest;

/**
 * Created by Administrator on 2017/4/13.
 */

public class ConfigUtils {
    private static final String TAG = "ConfigUtils";


    public static String getEnvi(Context context){
        return getStringFromMetaData(context, "EMA_WHICH_ENVI");
    }

    public static String getAppId(Context context){
        return getStringFromMetaData(context,"EMA_APP_ID").substring(1);
    }

    public static String getChannelId(Context context){
        return getStringFromMetaData(context,"EMA_CHANNEL_ID").substring(1);
    }







    /**
     * 返回md5加密后的字符串
     *
     * @param str
     * @return
     */
    public static String MD5(String str) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(str.getBytes("UTF-8"));
            byte bytes[] = messageDigest.digest();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < bytes.length; i++)
                if (Integer.toHexString(0xff & bytes[i]).length() == 1)
                    sb.append("0").append(Integer.toHexString(0xff & bytes[i]));
                else
                    sb.append(Integer.toHexString(0xff & bytes[i]));
            return sb.toString().toUpperCase();
        } catch (Exception e) {
        }
        return "";
    }


    /**
     * 根据key获取metaData的string类型的数据
     *
     * @param context
     * @param key
     * @return
     */
    private static String getStringFromMetaData(Context context, String key) {
        ApplicationInfo ai;
        String value = null;
        try {
            ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            value = bundle.getString(key);
        } catch (Exception e) {
            Log.e(TAG, "参数设置错误, 请检查！");
            e.printStackTrace();
        }
        if (null == value) {
            value = "nNNN/AAA";
        }
        return value;
    }
}
