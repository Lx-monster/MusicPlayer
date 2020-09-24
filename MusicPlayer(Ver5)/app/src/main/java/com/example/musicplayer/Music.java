package com.example.musicplayer;

import android.graphics.Bitmap;

import java.text.DecimalFormat;

//歌曲Music类
public class Music {
    private String name;//歌名
    private String artist;//歌曲艺术家
    private String album;//专辑
    private Bitmap albumPicture;//专辑图
    private long size;//大小
    private long time;//时长
    private String duration;//m:s
    private String url;//路径

    public Music() {
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }
    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }
    public void setAlbum(String album) {
        this.album = album;
    }

    public Bitmap getAlbumPicture() {
        return albumPicture;
    }
    public void setAlbumPicture(Bitmap albumPicture) {
        this.albumPicture = albumPicture;
    }

    public String getSize() {
        return formatSize(size);
    }
    public void setSize(long size) {
        this.size = size;
    }

    public long getTime() {
        return time;
    }
    public void setTime(long time) {
        this.time = time;
    }

    public String getDuration() {
        return duration;
    }
    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    public String formatSize(long size){
        DecimalFormat df=new DecimalFormat("#.00");//将数字指定格式化
        String formatsize="";
        if (size==0){
            formatsize="0B";
        }else if (size<1024){
            formatsize=df.format((double)size)+"B";
        }else if (size<1048576){
            formatsize=df.format((double)size/1024)+"KB";
        }else if (size<1073741824){
            formatsize=df.format((double)size/1048576)+"MB";
        }else {
            formatsize=df.format((double)size/1073741824)+"MB";
        }
        return formatsize;
    }//转换歌曲文件大小
}
