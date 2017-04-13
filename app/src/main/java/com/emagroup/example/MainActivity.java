package com.emagroup.example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.emagroup.imsdk.HttpRequestor;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new HttpRequestor().doGetAsync("https://www.baidu.com/", null, new HttpRequestor.OnResponsetListener() {
            @Override
            public void OnResponse(String result) {
                Log.e("....",result);
            }
        });
    }
}
