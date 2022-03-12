package com.chicuong.audioplayer;

import static com.chicuong.audioplayer.ApplicationClass.ACTION_NEXT;
import static com.chicuong.audioplayer.ApplicationClass.ACTION_PLAY;
import static com.chicuong.audioplayer.ApplicationClass.ACTION_PREVIOUS;
import static com.chicuong.audioplayer.ApplicationClass.CHANNEL_ID_2;
import static com.chicuong.audioplayer.MainActivity.musicFiles;
import static com.chicuong.audioplayer.MainActivity.repeatBoolean;
import static com.chicuong.audioplayer.MainActivity.shuffleBoolean;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

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
    MediaSessionCompat mediaSessionCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_player);
        Objects.requireNonNull(getSupportActionBar()).hide();
        mediaSessionCompat = new MediaSessionCompat(getBaseContext(), "My Audio");
        initViews();
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
                handler.postDelayed(this, 500);
            }
        });

        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (shuffleBoolean) {
                    shuffleBoolean = false;
                    shuffleBtn.setImageResource(R.drawable.ic_shuffle_off);
                } else {
                    shuffleBoolean = true;
                    repeatBoolean = false;
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
        showNotification(R.drawable.ic_pause);
        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra("servicePosition", position);
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
            Glide.with(this)
                    .asBitmap()
                    .load(art)
                    .into(coverArt);
//            bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
//            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
//                @Override
//                public void onGenerated(@Nullable Palette palette) {
//                    Palette.Swatch swatch =
//                }
//            });
        } else {
            Glide.with(this)
                    .load(R.drawable.music_icon)
                    .into(coverArt);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
//        playThreadBtn();
//        nextThreadBtn();
//        prevThreadBtn();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }

    //    private void playThreadBtn() {
//        playThread = new Thread() {
//            @Override
//            public void run() {
//                super.run();
//                playPauseBtn.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        playPauseBtnClicked();
//                    }
//                });
//            }
//        };
//        playThread.start();
//    }

    @Override
    public void playPauseBtnClicked() {
        if (musicService.isPlaying()) {
            playPauseBtn.setImageResource(R.drawable.ic_play);
            showNotification(R.drawable.ic_play);
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
            showNotification(R.drawable.ic_pause);
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

//    private void nextThreadBtn() {
//        nextThread = new Thread() {
//            @Override
//            public void run() {
//                super.run();
//                nextBtn.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        nextBtnClicked();
//                    }
//                });
//            }
//        };
//        nextThread.start();
//    }

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
        musicService.onCompleted();
        showNotification(R.drawable.ic_pause);
        playPauseBtn.setImageResource(R.drawable.ic_pause);
        musicService.start();
    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i + 1);
    }

//    private void prevThreadBtn() {
//        prevThread = new Thread() {
//            @Override
//            public void run() {
//                super.run();
//                prevBtn.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        prevBtnClicked();
//                    }
//                });
//            }
//        };
//        prevThread.start();
//    }

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
        playPauseBtn.setImageResource(R.drawable.ic_pause);
        musicService.onCompleted();
        showNotification(R.drawable.ic_pause);
        musicService.start();
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        MusicService.MyBinder binder = (MusicService.MyBinder) iBinder;
        musicService = binder.getService();
        musicService.setActionPlaying(this);
        Toast.makeText(this, "Service Connected: " + musicService, Toast.LENGTH_SHORT).show();
        seekBar.setMax(musicService.getDuration() / 1000);
        metaData(uri);
        songName.setText(listSong.get(position).getTitle());
        artistName.setText(listSong.get(position).getArtist());
        musicService.onCompleted();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        musicService = null;
    }

    void showNotification(int playPauseBtn) {
        Intent intent = new Intent(this, PlayerActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0
                , intent, 0);

        Intent prevIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_PREVIOUS);
        PendingIntent prevPending = PendingIntent
                .getBroadcast(this, 0, prevIntent
                        , PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_PLAY);
        PendingIntent pausePending = PendingIntent
                .getBroadcast(this, 0, pauseIntent
                        , PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_NEXT);
        PendingIntent nextPending = PendingIntent
                .getBroadcast(this, 0, nextIntent
                        , PendingIntent.FLAG_UPDATE_CURRENT);

        //album picture to display on notification
        byte[] picture = null;
        picture = getAlbumArt(listSong.get(position).getPath());
        Bitmap thumb = null;
        if(picture != null) {
            thumb = BitmapFactory.decodeByteArray(picture, 0, picture.length);
        }
        else {
            thumb = BitmapFactory.decodeResource(getResources(), R.drawable.music_icon);
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_2)
                .setSmallIcon(playPauseBtn)
                .setLargeIcon(thumb)
                .setContentTitle(listSong.get(position).getTitle())
                .setContentText(listSong.get(position).getArtist())
                .addAction(R.drawable.ic_previous, "Previous", prevPending)
                .addAction(playPauseBtn, "Pause", pausePending)
                .addAction(R.drawable.ic_next, "Next", nextPending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .build();
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }

    private byte[] getAlbumArt(String uri) {
        //MediaMetadataRetriever: object to get metadata of a file
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        //retrieve metadata from file uri
        retriever.setDataSource(uri);

        //get picture from metadata
        byte[] art = retriever.getEmbeddedPicture();

        //after using retriever, we must release it
        retriever.release();
        return art;
    }
}