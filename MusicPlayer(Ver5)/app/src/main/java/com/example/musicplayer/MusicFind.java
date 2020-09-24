package com.example.musicplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

//获取本地歌曲列表
public class MusicFind {
    private Context context;
    private ContentResolver cr;

    public List<Music> getMusic(ContentResolver contentResolver,Context context){
        this.context=context;
        cr=contentResolver;
        Cursor cursor=contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,null,null,MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
/*      获取内存中的歌曲
        多媒体存储MediaStorage类 Android系统提供的多媒体数据库
        通过ContentResolver调用接口获取数据
        cursor数据指针
*/
        List<Music> musicList=new ArrayList<Music>();
        while (cursor.moveToNext()){
            Music music=new Music();
            String musicName=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));//歌名
            String musicArtist=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));//歌曲艺术家
            String musicAlbum=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));//歌曲专辑封面
            long musicTime=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));//歌曲时长
            long musicSize=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));//歌曲大小
            String musicUrl=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));//歌曲路径
            int isMusic=cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));//是否为music

            if (isMusic!=0&&musicTime/(1000*60)>=1){//歌曲时长>60s
                music.setName(musicName);
                music.setArtist(musicArtist);
                music.setAlbum(musicAlbum);
                music.setTime(musicTime);
                music.setDuration(formatTime(musicTime));
                music.setUrl(musicUrl);
                music.setSize(musicSize);
                if (getAlbumPicture(musicUrl)!=null){
                    music.setAlbumPicture(getAlbumPicture(musicUrl));
                }
                musicList.add(music);
            }
        }
        cursor.close();
        return musicList;
    }

    //格式化时间，将毫秒转换为分:秒格式
    public static String formatTime(long time){
        String min=time/(1000*60)+"";
        String sec=time%(1000*60)+"";
        if (min.length()<2){
            min="0"+min;
        }
        if (sec.length()==4){
            sec="0"+sec;
        }else if (sec.length()==3){
            sec="00"+sec;
        }else if (sec.length()==2){
            sec="000"+sec;
        }else if (sec.length()==1){
            sec="0000"+sec;
        }
        return min+":"+sec.trim().substring(0,2);
    }

    //获取专辑图
/*
    public static Bitmap getAlbumPicture(String path){
        MediaMetadataRetriever retriever=new MediaMetadataRetriever();
        retriever.setDataSource(path);
        byte[] picture=retriever.getEmbeddedPicture();
        Bitmap bitmap;
        if (picture!=null&&picture.length>0){
            bitmap=BitmapFactory.decodeByteArray(picture,0,picture.length);
        }else {
            bitmap=null;
        }
        return bitmap;
    }
*/

    /*BitmapFactory.Options的作用：
    *   1.防止内存溢出；
    *   2.节省内存开销；
    *   3.系统更流畅；
    *
    * */
//  获取专辑图(内存优化)
    public static Bitmap getAlbumPicture(String path){
        MediaMetadataRetriever retriever=new MediaMetadataRetriever();
        retriever.setDataSource(path);
        byte[] picture=retriever.getEmbeddedPicture();
        if (picture!=null){
            BitmapFactory.Options options=new BitmapFactory.Options();
            //长宽像素缩小倍数  >1缩小 值为2的幂(否则自动匹配最近的2的幂)
            options.inSampleSize=1;
            //为true则在解码时不返回bitmap,不占用的内存只解析图片 减少内存,解决OOM问题
            options.inJustDecodeBounds=true;
            if (picture!=null&&picture.length>0){
                BitmapFactory.decodeByteArray(picture,0,picture.length,options);
            }else {
                return null;
            }
            options.inSampleSize=fitSize(options,800);
            options.inJustDecodeBounds=false;
            //bitmap保存或编码格式(决定内存大小)
            options.inPreferredConfig= Bitmap.Config.ARGB_8888;
            //获取bitmap数据
            return BitmapFactory.decodeByteArray(picture,0,picture.length,options);
        }
        return null;
    }
    //缩放图片
    public static int fitSize(BitmapFactory.Options options,int size){
        int w=options.outWidth;
        int h=options.outHeight;
        int fitW=w/size;
        int fitH=h/size;
        int fit=Math.max(fitW,fitH);
        if (fit==0){
            return 1;
        }else if (fit>1){
            if ((w>size)&&(w/fit)<size){
                fit--;
            }
            if ((h>size)&&(h/fit)<size){
                fit--;
            }
        }
        return fit;
    }
}
