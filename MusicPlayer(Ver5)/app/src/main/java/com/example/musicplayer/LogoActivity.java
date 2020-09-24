package com.example.musicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

public class LogoActivity extends AppCompatActivity {
    Handler handler=new Handler();
    private boolean isFirstUse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);//创建布局

        SharedPreferences preferences = getSharedPreferences("isFirstUse",MODE_PRIVATE);
        isFirstUse = preferences.getBoolean("isFirstUse", true);

        if (isFirstUse=true){//如果Logo界面在本次程序运行期间(即后台未关闭)已经创建过一次，就直接跳转到Main界面
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //实现从LogoActivity跳转到MainActivity
                    Intent intent=new Intent(LogoActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();//防止退出时返回logo界面
                }
            },3000);//Handler有一个方法叫postDelay里有两个参数：1、Runnable是它要运行的一个线程，2、delayMillis 是延迟的时间。1000就是1s.
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isFirstUse", false);
            editor.commit();
    }
      else{
            setContentView(R.layout.activity_main);
        }
}
}
