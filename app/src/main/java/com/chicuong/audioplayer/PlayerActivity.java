package com.chicuong.audioplayer;

import static com.chicuong.audioplayer.ApplicationClass.ACTION_NEXT;
import static com.chicuong.audioplayer.ApplicationClass.ACTION_PLAY;
import static com.chicuong.audioplayer.ApplicationClass.ACTION_PREVIOUS;
import static com.chicuong.audioplayer.ApplicationClass.CHANNEL_ID_2;
import static com.chicuong.audioplayer.MainActivity.SHOW_MINI_PLAYER;
import static com.chicuong.audioplayer.MainActivity.musicFiles;
import static com.chicuong.audioplayer.MainActivity.repeatBoolean;
import static com.chicuong.audioplayer.MainActivity.shuffleBoolean;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentTransaction;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class PlayerActivity extends AppCompatActivity
        implements ActionPlaying, ServiceConnection {

    TextView songName, artistName, durationPlayed, durationTotal;
    ImageView coverArt, nextBtn, backBtn, shuffleBtn, repeatBtn, prevBtn;
    FloatingActionButton playPauseBtn;
    SeekBar seekBar;
    int position = -1;
    static List<MusicFiles> listSong = new ArrayList<>();
    static Uri uri;
    //    static MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Thread playThread, prevThread, nextThread;
    MusicService musicService;
    boolean isContinue = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_player);
        Objects.requireNonNull(getSupportActionBar()).hide();
        initViews();
        isContinue = getIntent().getBooleanExtra("continue", false);
        getIntentMethod();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (musicService != null && b) {
                    //skip media player to (position of seek bar * 1000) seconds
                    musicService.seekTo(i * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //create a new thread to run media player
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (musicService != null) {
                    int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                    durationPlayed.setText(formattedTime(mCurrentPosition));
                }
                handler.postDelayed(this, 250);
            }
        });

        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                repeatBoolean = false;
                musicService.setLooping(false);
                if (shuffleBoolean) {
                    shuffleBoolean = false;
                    shuffleBtn.setImageResource(R.drawable.ic_shuffle_off);
                } else {
                    shuffleBoolean = true;
                    repeatBtn.setImageResource(R.drawable.ic_repeat_off);
                    shuffleBtn.setImageResource(R.drawable.ic_shuffle_on);
                }
            }
        });
        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (repeatBoolean) {
                    repeatBoolean = false;
                    musicService.setLooping(false);
                    repeatBtn.setImageResource(R.drawable.ic_repeat_off);
                } else {
                    repeatBoolean = true;
                    shuffleBoolean = false;
                    shuffleBtn.setImageResource(R.drawable.ic_shuffle_off);
                    musicService.setLooping(true);
                    repeatBtn.setImageResource(R.drawable.ic_repeat_on);
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private String formattedTime(int mCurrentPosition) {
        String totalOut = "";
        String totalNew = "";
        String seconds = String.valueOf(mCurrentPosition % 60);
        String minutes = String.valueOf(mCurrentPosition / 60);
        totalOut = minutes + ":" + seconds;
        totalNew = minutes + ":" + "0" + seconds;
        if (seconds.length() == 1) {
            return totalNew;
        } else {
            return totalOut;
        }
    }

    private void getIntentMethod() {
        position = getIntent().getIntExtra("position", -1);
        listSong = musicFiles;
        if (listSong != null) {
            playPauseBtn.setImageResource(R.drawable.ic_pause);
            uri = Uri.parse(listSong.get(position).getPath());
        }
//        //create a media player in the context
//        //pass the uri into the method to assign file to play
//        musicService.createMediaPlayer(position);
//        musicService.start();
        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra("servicePosition", position);
        if(isContinue) {
            intent.putExtra("isContinue", true);
        }
        startService(intent);
    }

    private void initViews() {
        songName = findViewById(R.id.song_name);
        artistName = findViewById(R.id.song_artist);
        durationPlayed = findViewById(R.id.duration_played);
        durationTotal = findViewById(R.id.duration_total);
        coverArt = findViewById(R.id.img_cover_art);
        nextBtn = findViewById(R.id.img_next);
        backBtn = findViewById(R.id.btn_back);
        shuffleBtn = findViewById(R.id.img_shuffle);
        repeatBtn = findViewById(R.id.img_repeat);
        playPauseBtn = findViewById(R.id.play_pause);
        seekBar = findViewById(R.id.seekbar);
        prevBtn = findViewById(R.id.img_previous);

        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPauseBtnClicked();
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextBtnClicked();
            }
        });

        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prevBtnClicked();
            }
        });
    }

    //method to set meta data
    //duration and cover art
    //into views
    private void metaData(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        int songDuration = Integer.parseInt(listSong.get(position).getDuration()) / 1000;
        durationTotal.setText(formattedTime(songDuration));
        byte[] art = retriever.getEmbeddedPicture();

        Bitmap bitmap;

        if (art != null) {
            if(!isDestroyed()) {
                Glide.with(this)
                        .asBitmap()
                        .load(art)
                        .into(coverArt);
            }
        } else {
            if(!isDestroyed()) {
                Glide.with(this)
                        .load(R.drawable.music_icon)
                        .into(coverArt);
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, MusicService.class);

        bindService(intent, this, BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }

    @Override
    public void playPauseBtnClicked() {
        if (musicService.isPlaying()) {
            playPauseBtn.setImageResource(R.drawable.ic_play);
            musicService.showNotification(R.drawable.ic_play);
            musicService.pause();
            seekBar.setMax(musicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 500);
                }
            });
        } else {
            playPauseBtn.setImageResource(R.drawable.ic_pause);
            musicService.showNotification(R.drawable.ic_pause);
            musicService.start();
            seekBar.setMax(musicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 500);
                }
            });
        }
    }

    @Override
    public void nextBtnClicked() {
        musicService.stop();
        musicService.release();
        if (shuffleBoolean) {
            position = getRandom(listSong.size() - 1);
        } else {
            position = (position + 1) % listSong.size();
        }
        uri = Uri.parse(listSong.get(position).getPath());
        musicService.createMediaPlayer(position);
        metaData(uri);
        songName.setText(listSong.get(position).getTitle());
        artistName.setText(listSong.get(position).getArtist());

        seekBar.setMax(musicService.getDuration() / 1000);
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (musicService != null) {
                    int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                }
                handler.postDelayed(this, 500);
            }
        });
        musicService.setLooping(repeatBoolean);
        musicService.onCompleted();
        musicService.showNotification(R.drawable.ic_pause);
        playPauseBtn.setImageResource(R.drawable.ic_pause);
        musicService.start();
    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i + 1);
    }

    @Override
    public void prevBtnClicked() {
        musicService.stop();
        musicService.release();
        if (shuffleBoolean && !repeatBoolean) {
            position = getRandom(listSong.size() - 1);
        } else if (!shuffleBoolean && !repeatBoolean) {
            //nếu position < 0
            //play cái cuối cùng
            //ko thì play cái trước nó
            position = (position - 1) < 0 ? (listSong.size() - 1) : (position - 1);
        }
        //else position = position
        uri = Uri.parse(listSong.get(position).getPath());
//        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        musicService.createMediaPlayer(position);
        metaData(uri);
        songName.setText(listSong.get(position).getTitle());
        artistName.setText(listSong.get(position).getArtist());

        seekBar.setMax(musicService.getDuration() / 1000);
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (musicService != null) {
                    int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                }
                handler.postDelayed(this, 500);
            }
        });
        musicService.setLooping(repeatBoolean);
        playPauseBtn.setImageResource(R.drawable.ic_pause);
        musicService.onCompleted();
        musicService.showNotification(R.drawable.ic_pause);
        musicService.start();
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        MusicService.MyBinder binder = (MusicService.MyBinder) iBinder;
        musicService = binder.getService();
        musicService.setActionPlaying(this);
//        Toast.makeText(this, "Service Connected: " + musicService, Toast.LENGTH_SHORT).show();
        seekBar.setMax(musicService.getDuration() / 1000);
        metaData(uri);
        songName.setText(listSong.get(position).getTitle());
        artistName.setText(listSong.get(position).getArtist());
        musicService.onCompleted();
        musicService.showNotification(R.drawable.ic_pause);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        musicService = null;
    }




}