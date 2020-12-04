package com.yuukidach.ucount;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import cn.bmob.v3.Bmob;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //初始化 bmob包
        Bmob.initialize(this, "8723278d4f502f2b279e675df353cb8b");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean login = (boolean) SPUtils.get(SplashActivity.this, "login", false);
                if (login){
                    startActivity(new Intent(SplashActivity.this,HomeActivity.class));
                }else {
                    startActivity(new Intent(SplashActivity.this,LoginActivity.class));

                }
                finish();
            }
        },3000);
    }
}