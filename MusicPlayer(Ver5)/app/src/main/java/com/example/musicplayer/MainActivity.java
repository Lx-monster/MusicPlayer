package com.example.musicplayer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
//    private static final String TAG_EXIT = "exit";

    private Button ok;
    private Button cancel;
    private Button tuichu_btn;

//    private long exitTime = 0;

    private TextView tv_pname;
    private TextView tv_partist;
    private ImageView iv_playing;
    private ImageButton btn_play;
    private SeekBar sb_play;
    private ListView lv_music;
    private static List<Music> musicList = new ArrayList<Music>();
    private RelativeLayout rl_play;
    private MyAdapter myAdapter;
    private Integer p;//当前歌曲id位置
    private boolean b=false;//切换界面时b为true,定断点

    //权限获取
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    private Intent intent;
    private MusicService musicService;
    private ServiceConnection conn=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            musicService=((MusicService.myBinder)iBinder).getService();//获取Service

            /*onServiceConnected在绑定成功时进行回调，但不保证在执行binService后立马回调，
            我们在onCreate方法中绑定后立马获取service实例，但此时不保证onServiceConnected已经被回调*/
            musicService.setSb(sb_play);
            musicService.setmName(tv_pname);
            musicService.setmArtist(tv_partist);
            musicService.setIv_album(iv_playing);
            musicService.setStart(btn_play);

            musicService.setMusicList(musicList);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            musicService=null;
        }
    };//Service连接

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("MainActivity","onCreate.....");

        intent=new Intent(MainActivity.this,MusicService.class);
        bindService(intent,conn,BIND_AUTO_CREATE);//绑定Service

        LayoutInflater inflater=getLayoutInflater();
        View exitView= inflater.inflate(R.layout.dialog_layout,null);
        ok = exitView.findViewById(R.id.tv_ok);
        cancel = exitView.findViewById(R.id.tv_cancel);
        tuichu_btn = findViewById(R.id.tuichu_btn);

        lv_music = findViewById(R.id.lv_music);
        rl_play = findViewById(R.id.rl_play);
        tv_pname = findViewById(R.id.tv_pname);
        tv_partist = findViewById(R.id.tv_partist);
        iv_playing = findViewById(R.id.iv_playing);
        sb_play = findViewById(R.id.sb_play);
        btn_play = findViewById(R.id.btn_play);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, REQUEST_EXTERNAL_STORAGE);
        }//检测是否获取读外存的权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_EXTERNAL_STORAGE);
        }//检测是否获取写外存的权限

        //获取歌曲列表
        final MusicFind musicFind = new MusicFind();
        musicList = musicFind.getMusic(MainActivity.this.getContentResolver(),MainActivity.this);
        //适配器 添加歌曲
        myAdapter = new MyAdapter(MainActivity.this, R.layout.music_item, musicList);
        lv_music.setAdapter(myAdapter);
    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.i("MainActivity","onStart.....");

        if (p!=null){//当前播放歌曲id不为空,则表示为从另一个界面跳转而至,重新连接Service
            conn=new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    musicService.setSb(sb_play);
                    musicService.setmName(tv_pname);
                    musicService.setmArtist(tv_partist);
                    musicService.setIv_album(iv_playing);
                    musicService.setStart(btn_play);
                    p=musicService.getPosition();
                    if (p>-1){
                        musicService.play(p);
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                    musicService=null;
                }
            };//Service连接
            bindService(intent,conn,BIND_AUTO_CREATE);//绑定Service
        }
        lv_music.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                p=i;
//                Log.i("Position11",p+"");
                musicService.play(i);
                musicService.setPosition(i);
                Toast.makeText(MainActivity.this, "" + musicList.get(i).getUrl(),
                        Toast.LENGTH_SHORT).show();//测试代码 点击显示歌曲路径
            }
        });

        rl_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //点击下方播放区域，跳转界面
                if (p==null){
                    Toast.makeText(MainActivity.this,"٩(๑> ₃ <)۶Music~",Toast.LENGTH_SHORT).show();
                    return;
                }
                b=true;
                musicService.setPosition(p);
                musicService.setProgress(b);//传递歌曲进度断点
                b=false;
                Intent intent = new Intent(MainActivity.this, PlayActivity.class);
                startActivity(intent);
            }
        });

        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                musicService.start();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(conn);
        Log.i("MainActivity","onStop.....");
    }

    // 弹窗退出
    public void tuichu(View view) {
        tuichu_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("退出提示");
                builder.setMessage("你真的要离开吗？");
                builder.setPositiveButton("是的", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onDestroy();
                       finish();
                    }
                });
                builder.setNegativeButton("不", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "兜兜转转还是我！", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.show();
            }
        });
    }

    @Override
    protected void onDestroy () {
            super.onDestroy();
            b=false;
//            unbindService(conn);//解绑Service
            Log.i("MainActivity","onDestroy......");
        }
}

