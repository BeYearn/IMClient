package com.emagroup.imsdk;

/**
 * Created by beyearn on 2017/6/20.
 */

public class SensBean {
    private boolean sensitive;
    private String resultStr;

    public SensBean(boolean sensitive, String resultStr) {
        this.sensitive = sensitive;
        this.resultStr = resultStr;
    }

    public boolean isSensitive() {
        return sensitive;
    }

    public String getResultStr() {
        return resultStr;
    }
}
