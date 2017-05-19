package com.emagroup.imsdk.util;

/**
 * Created by Administrator on 2016/7/29.
 */

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Map;


public class HttpRequestor {

    private final String TAG = "HttpRequestor";
    private String charset = "utf-8";
    private Integer connectTimeout = 5000;
    private Integer socketTimeout = null;
    private String proxyHost = null;
    private Integer proxyPort = null;
    private boolean isDebug = true;


    public void doPostAsync(String url, Map<String, String> params, OnResponsetListener listener) {

        final String _url = url;
        final Map<String, String> _params = params;
        final OnResponsetListener _listener = listener;

        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                try {
                    doPost(_url, _params, _listener);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void doGetAsync(String url, Map<String, String> params, OnResponsetListener listener) {

        final String _url = url;
        final Map<String, String> _params = params;
        final OnResponsetListener _listener = listener;

        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                try {
                    doGet(_url, _params, _listener);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * Do GET request
     *
     * @param url
     * @param listener
     * @return
     * @throws Exception
     * @throws IOException
     */
    private void doGet(String url, Map<String, String> params, OnResponsetListener listener) throws Exception {

        //返回值
        StringBuffer resultSb = new StringBuffer();
        // 拼凑get请求的URL字串，使用URLEncoder.encode对特殊和不可见字符进行编码
        url = buildUrl(url, params);
        Log.e(TAG, "url_:" + url);
        URL localURL = new URL(url);

        URLConnection connection = openConnection(localURL);
        HttpURLConnection httpURLConnection = (HttpURLConnection) connection;

        httpURLConnection.setRequestProperty("Accept-Charset", charset);
        httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        httpURLConnection.setConnectTimeout(connectTimeout);
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader reader = null;
        StringBuffer resultBuffer = new StringBuffer();
        String tempLine = null;

        if (httpURLConnection.getResponseCode() >= 300) {
            throw new Exception("HTTP Request is not success, Response code is " + httpURLConnection.getResponseCode());
        }

        try {
            inputStream = httpURLConnection.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream);
            reader = new BufferedReader(inputStreamReader);

            while ((tempLine = reader.readLine()) != null) {
                resultBuffer.append(tempLine);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }

            setOnResponse(listener,"{\"config\":{},\"data\":{},\"message\":\"网络连接超时\",\"status\":\"9\"}");
        }

        String result = resultBuffer.toString();
        setOnResponse(listener, result);
    }

    /**
     * Do POST request
     *
     * @param url
     * @param parameterMap
     * @param listener
     * @return
     * @throws Exception
     */
    private void doPost(String url, Map<String, String> parameterMap, OnResponsetListener listener) throws Exception {

        StringBuilder parameterBuffer = new StringBuilder();
        if (parameterMap != null) {
            Iterator iterator = parameterMap.keySet().iterator();
            String key = null;
            String value = null;
            while (iterator.hasNext()) {
                key = (String) iterator.next();
                if (parameterMap.get(key) != null) {
                    value = (String) parameterMap.get(key);
                } else {
                    value = "";
                }
                parameterBuffer.append(key).append("=").append(value);
                if (iterator.hasNext()) {
                    parameterBuffer.append("&");
                }
            }
        }

        if(isDebug){
            Log.e("postUrl", url + "?" + parameterBuffer.toString());
        }

        URL localURL = new URL(url);

        URLConnection connection = openConnection(localURL);
        HttpURLConnection httpURLConnection = (HttpURLConnection) connection;

        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Accept-Charset", charset);
        httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        //httpURLConnection.setRequestProperty("Content-Length", String.valueOf(parameterBuffer.length()));
        httpURLConnection.setConnectTimeout(connectTimeout);

        OutputStream outputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader reader = null;
        StringBuffer resultBuffer = new StringBuffer();
        String tempLine = null;

        try {
            outputStream = httpURLConnection.getOutputStream();
            outputStreamWriter = new OutputStreamWriter(outputStream);

            outputStreamWriter.write(parameterBuffer.toString());
            outputStreamWriter.flush();

            if (httpURLConnection.getResponseCode() >= 300) {
                throw new Exception("HTTP Request is not success, Response code is " + httpURLConnection.getResponseCode());
            }
            inputStream = httpURLConnection.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream);
            reader = new BufferedReader(inputStreamReader);

            while ((tempLine = reader.readLine()) != null) {
                resultBuffer.append(tempLine);
            }
        } finally {
            if (outputStreamWriter != null) {
                outputStreamWriter.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (reader != null) {
                reader.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }

            setOnResponse(listener,"{\"config\":{},\"data\":{},\"message\":\"网络连接超时\",\"status\":\"9\"}");
        }

        String result = resultBuffer.toString();
        setOnResponse(listener, result);
    }


    /**
     * 拼接url
     *
     * @param url
     * @param map
     * @return
     */
    private String buildUrl(String url, Map<String, String> map) {
        if (map == null || map.size() == 0) {
            return url;
        }
        StringBuffer sb = new StringBuffer();
        sb.append(url);
        int i = 0;
        for (String key : map.keySet()) {
            if (i == 0) {
                sb.append("?");
            } else {
                sb.append("&");
            }
            sb.append(key).append("=").append(map.get(key));
            i++;
        }
        return sb.toString();
    }


    public interface OnResponsetListener {
        public abstract void OnResponse(String result);
    }

    private void setOnResponse(OnResponsetListener listener, String result) {
        if (listener != null) {
            if (isDebug) {
                Log.e("httpResponse", result);
            }
            listener.OnResponse(result);
        } else {
            Log.e(TAG, "OnResponsetListener is null");
        }
    }


    private URLConnection openConnection(URL localURL) throws IOException {
        URLConnection connection;
        if (proxyHost != null && proxyPort != null) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            connection = localURL.openConnection(proxy);
        } else {
            connection = localURL.openConnection();
        }
        return connection;
    }


    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Integer getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(Integer socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

}