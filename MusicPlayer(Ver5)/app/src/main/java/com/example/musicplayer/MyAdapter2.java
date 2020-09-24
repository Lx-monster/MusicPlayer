package com.example.musicplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

//自定义适配器
public class MyAdapter2 extends ArrayAdapter<Music> {
    private Context context;
    private List<Music> musiclist;
    private Music music;
    private LayoutInflater inflater;

    public MyAdapter2(Context context, int TVresource, List<Music> musiclist) {
        super(context, TVresource, musiclist);
        this.context = context;
        this.musiclist = musiclist;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        //显示内容 ListView中显示歌曲信息
        music=musiclist.get(i);
        View itemView=inflater.inflate(R.layout.list_item_layout,null);
        TextView tv_item=itemView.findViewById(R.id.tv_list_name);
        TextView tv_item_artist=itemView.findViewById(R.id.tv_list_artist);

        tv_item.setText(music.getName());
        tv_item_artist.setText(" - "+music.getArtist());
        return itemView;
    }
}
