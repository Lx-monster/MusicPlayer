package com.example.musicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class PlayActivity extends AppCompatActivity {
    private ImageButton ib_order;//播放方式
    private ImageView iv;
    private TextView tv_mName;
    private TextView tv_mArtist;
    private SeekBar sb_playing;
    private ImageButton ib_ss;//开始/暂停键
    private ImageButton ib_back;//返回键

    private PopupWindow popup;

    private int position;
    private boolean b=false;

    private Intent intent;
    private MusicService musicService;
    private ServiceConnection conn=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            musicService=((MusicService.myBinder)iBinder).getService();//获取Service

            musicService.setSb(sb_playing);
            musicService.setmName(tv_mName);
            musicService.setmArtist(tv_mArtist);
            musicService.setIv_album(iv);
            musicService.setStart(ib_ss);
            musicService.setIb_order(ib_order);

            position=musicService.getPosition();
            musicService.play(position);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            musicService=null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        Log.i("PlayActivity","onCreate.....");

        intent=new Intent(PlayActivity.this,MusicService.class);
        bindService(intent,conn,BIND_AUTO_CREATE);//绑定Service

        ib_order=findViewById(R.id.ib_order);
        iv=findViewById(R.id.iv);
        tv_mName=findViewById(R.id.tv_mName);
        tv_mArtist=findViewById(R.id.tv_mArtist);
        sb_playing=findViewById(R.id.sb_playing);
        ib_ss=findViewById(R.id.ib_ss);
        ib_back=findViewById(R.id.ib_back);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("PlayActivity","onStart......");

        ib_ss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                musicService.start();
            }
        });

        ib_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                musicService.getIb_order();
            }
        });

        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                back();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        back();
    }//重写系统返回键方法

    public void back(){
        b=true;
        musicService.setProgress(b);//传递歌曲进度断点
        Intent intent=new Intent(PlayActivity.this,MainActivity.class);
        startActivity(intent);
    }//退回上一界面

    public void pre(View view){
        position--;
        if (position<0){
            position=musicService.getMusicList().size()-1;
        }
        musicService.play(position);
    }//上一首

    public void startplay(View view){
        musicService.start();
    }//开始/暂停播放

    public void next(View view){
        position++;
        if (position>musicService.getMusicList().size()-1){
            position=0;
        }
        musicService.play(position);
    }//下一首

    public void list(View view){
        View content= LayoutInflater.from(this).inflate(R.layout.list_layout,null);
        popup=new PopupWindow();
        List<Music> musicList=musicService.getMusicList();
        MyAdapter2 adapter=new MyAdapter2(PlayActivity.this,R.layout.list_item_layout,musicList);
        ListView lv_list=content.findViewById(R.id.lv_list);
        lv_list.setAdapter(adapter);
        lv_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                musicService.play(i);
                musicService.setPosition(i);
                popup.dismiss();
                popup=null;
            }
        });
        popup.setContentView(content);
        popup.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popup.setOutsideTouchable(true);
        popup.setFocusable(true);
        popup.setBackgroundDrawable(new BitmapDrawable());
        popup.showAtLocation(view, Gravity.START|Gravity.BOTTOM,0,0);
    }//歌单列表

    public void info(View view){
        View content= LayoutInflater.from(this).inflate(R.layout.info_layout,null);
        popup=new PopupWindow();
        Music music=musicService.getMusic();
        TextView tv_Info_name=content.findViewById(R.id.tv_Info_name);
        TextView tv_Info_artist=content.findViewById(R.id.tv_Info_artist);
        TextView tv_Info_album=content.findViewById(R.id.tv_Info_album);
        TextView tv_Info_size=content.findViewById(R.id.tv_Info_size);
        TextView tv_Info_path=content.findViewById(R.id.tv_Info_path);

        tv_Info_name.setText("歌名 : "+music.getName());
        tv_Info_artist.setText("歌手 : "+music.getArtist());
        tv_Info_album.setText("专辑 : "+music.getAlbum());
        tv_Info_size.setText("大小 : "+music.getSize());
        tv_Info_path.setText("路径 : "+music.getUrl());

        popup.setContentView(content);
        popup.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popup.setOutsideTouchable(true);
        popup.setFocusable(true);
        popup.setBackgroundDrawable(new BitmapDrawable());
        popup.showAtLocation(view, Gravity.LEFT|Gravity.BOTTOM,0,0);

    }//歌曲信息

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        if (popup!=null&&popup.isShowing()){
            popup.dismiss();
            popup=null;
        }
        return super.onTouchEvent(event);
    }//点击事件 实现popupWindow外部点击消失

    @Override
    protected void onDestroy() {
        super.onDestroy();
        b=false;
        Log.i("PlayActivity","onDestroy.....");
        unbindService(conn);
    }
}
