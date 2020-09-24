package com.example.musicplayer;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MusicService extends Service {
    private List<Music> musicList=new ArrayList<>();
    private Music music;
    private int position;//当前播放歌曲ID
    private MediaPlayer mediaPlayer = new MediaPlayer();//媒体播放对象
    private Thread thread;
    private boolean flag=false;//播放与否
    private int progress;//当前进度
    private int i=0;
    private int[] playType={R.drawable.list,R.drawable.sj,R.drawable.xunhuan};//播放方式

    //统一设置
    private SeekBar sb;//进度条
    private TextView mName;//歌名
    private TextView mArtist;//艺术家
    private ImageView iv_album;//专辑图
    private ImageButton start;//播放/暂停按钮
    private ImageButton ib_order;//播放方式

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            sb.setProgress(msg.what);
            //进度
        }
    };

    public MusicService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("IBinder","onBind......");
        return new myBinder();
    }

    class myBinder extends Binder{
        public MusicService getService(){
            return MusicService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

//获取组件 进行统一设置
    public List<Music> getMusicList() {
        return musicList;
    }
    public void setMusicList(List<Music> musicList) {
        this.musicList = musicList;
    }

    public Music getMusic() {
        return music;
    }

    public int getPosition() {
        if (progress>-1){
            return position;
        }
        return -1;
    }
    public void setPosition(int position) {
        this.position = position;
    }

    public void setProgress(boolean b) {
        if (b){
            progress = mediaPlayer.getCurrentPosition();
        }else {
            progress=0;
        }
    }

    public void setSb(SeekBar sb) {
        this.sb = sb;
    }

    public void setmName(TextView mName) {
        this.mName = mName;
    }

    public void setmArtist(TextView mArtist) {
        this.mArtist = mArtist;
    }

    public void setIv_album(ImageView iv_album) {
        this.iv_album = iv_album;
    }

    public void setStart(ImageButton start) {
        this.start = start;
        if (flag)
            start.setImageResource(R.drawable.play);
        else
            start.setImageResource(R.drawable.pause);
    }

    public void getIb_order() {
        i++;
        if (i>2){
            i=0;
        }
        ib_order.setImageResource(playType[i]);

    }
    public void setIb_order(ImageButton ib_order) {
        this.ib_order = ib_order;
        ib_order.setImageResource(playType[i]);
    }

    //功能
    public void play(final int position){
        this.position=position;
        playMusic();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                //播放完下一首
                progress=0;
                next();
            }
        });
    }

    public void playMusic(){
        music = musicList.get(position);
        if (music.getAlbumPicture()!=null){
            iv_album.setImageBitmap(music.getAlbumPicture());
        }else {
            iv_album.setImageResource(R.drawable.p21);
        }
        mName.setText(music.getName());
        mArtist.setText(music.getArtist());
        start.setImageResource(R.drawable.play);
        flag = true;
        String musicUri = music.getUrl();//歌曲路径

        //播放歌曲
        if (mediaPlayer==null){
            try {
                mediaPlayer.setDataSource(musicUri);
                mediaPlayer.prepare();
                if (progress>0&&progress<music.getTime()){
                    mediaPlayer.seekTo(progress);//实现断点播放
                }
                mediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            mediaPlayer.reset();//重设播放
            try {
                mediaPlayer.setDataSource(musicUri);
                mediaPlayer.prepare();
                if (progress>0&&progress<music.getTime()){
                    mediaPlayer.seekTo(progress);
                }
                mediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        BarSet();
        progress=0;
    }//播放歌曲

    public void BarSet(){
        //监听滚动条时间
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(android.widget.SeekBar seekBar, int i, boolean b) {
                if (b){//用户手动更改进度条
                    mediaPlayer.seekTo(i);//跳转到指定帧 i(ms)
                }
            }

            @Override
            public void onStartTrackingTouch(android.widget.SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(android.widget.SeekBar seekBar) {

            }
        });
        sb.setMax(mediaPlayer.getDuration());
        //通过线程更改进度条进度
        thread=new Thread(new MusicService.MusicThread());
        thread.start();
    }
    public class MusicThread extends Thread {
        @Override
        public void run() {
            while (mediaPlayer!=null&&flag){//播放中
                try {
                    Thread.sleep(100);//100ms变更一次位置
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(mediaPlayer.getCurrentPosition());
            }
        }
    }

    public void orderPlay(){
        position++;
        if (position>musicList.size()-1){
            position=0;
        }
        playMusic();//播放歌曲

    }//顺序播放
    public void RandomPlay(){
        int mposition=position;
        while (position>-1){
            mposition=new Random().nextInt(musicList.size());
            if (mposition!=position)
                break;//随机数不等于当前数
        }
        position=mposition;
        playMusic();//播放歌曲
    }//随机播放
    public void SelfPlay(){
//        final int selfp=position;
        this.position=position;
        playMusic();
    }//单曲循环
    public void next(){
        switch (i){
            case 1:
                RandomPlay();//随机
                break;
            case 2:
                SelfPlay();
                break;
                default:
                    orderPlay();//顺序
        }
    }//播放类型

    public void start(){
        if (flag){//播放中
            start.setImageResource(R.drawable.pause);
            flag=false;
            if (mediaPlayer.isPlaying()){
                mediaPlayer.pause();//暂停
            }
        } else {//暂停中
            start.setImageResource(R.drawable.play);
            flag=true;
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();//播放
            }
        }
    }//开始/暂停播放

}
