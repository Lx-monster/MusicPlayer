package com.example.musicplayer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.List;

//自定义适配器
public class MyAdapter extends ArrayAdapter<Music> {
    private Context context;
    private List<Music> musiclist;
    private Music music;
    private LayoutInflater inflater;

    public MyAdapter(Context context, int TVresource,List<Music> musiclist) {
        super(context, TVresource, musiclist);
        this.context = context;
        this.musiclist = musiclist;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        //显示内容 ListView中显示歌曲信息
        music=musiclist.get(i);
        View itemView=inflater.inflate(R.layout.music_item,null);
        ImageView iv_album=itemView.findViewById(R.id.iv_album);
        TextView tv_name=itemView.findViewById(R.id.tv_name);
        TextView tv_artist=itemView.findViewById(R.id.tv_artist);

        if (music.getAlbumPicture()!=null){
            iv_album.setImageBitmap(music.getAlbumPicture());
        }else {
            iv_album.setImageResource(R.drawable.p21);
        }
        tv_name.setText(music.getName());//歌名
        tv_artist.setText(music.getArtist());//歌曲信息

        return itemView;
    }
}
