package com.chicuong.audioplayer;

import static com.chicuong.audioplayer.PlayerActivity.listSong;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {
    IBinder binder = new MyBinder();
    MediaPlayer mediaPlayer;
    List<MusicFiles> musicFiles = new ArrayList<>();
    Uri uri;
    int position = -1;
    ActionPlaying actionPlaying;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("Bind", "Method");
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int myPosition = intent.getIntExtra("servicePosition", -1);
        String actionName = intent.getStringExtra("ActionName");
        if (myPosition != -1) {
            playMedia(myPosition);
        }
        if(actionName != null) {
            switch (actionName) {
                case "playPause":
                    Toast.makeText(this, "PlayPause"
                            , Toast.LENGTH_SHORT).show();
                    if(actionPlaying != null) {
                        actionPlaying.playPauseBtnClicked();
                    }
                    break;
                case "next":
                    Toast.makeText(this, "Next"
                            , Toast.LENGTH_SHORT).show();
                    if(actionPlaying != null) {
                        actionPlaying.nextBtnClicked();
                    }
                    break;
                case "previous":
                    Toast.makeText(this, "Previous"
                            , Toast.LENGTH_SHORT).show();
                    if(actionPlaying != null) {
                        actionPlaying.prevBtnClicked();
                    }
                    break;
            }
        }
        return START_STICKY;
    }

    private void playMedia(int startPosition) {
        musicFiles = listSong;
        position = startPosition;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            if (musicFiles != null) {
                createMediaPlayer(position);
            }
            mediaPlayer.start();
        } else {
            createMediaPlayer(position);
            mediaPlayer.start();
        }
    }


    public class MyBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    void start() {
        mediaPlayer.start();
    }

    boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    void stop() {
        mediaPlayer.stop();
    }

    void release() {
        mediaPlayer.release();
    }

    void pause() {
        mediaPlayer.pause();
    }

    int getDuration() {
        return mediaPlayer.getDuration();
    }

    void seekTo(int position) {
        mediaPlayer.seekTo(position);
    }

    void createMediaPlayer(int positionInner) {
        position = positionInner;
        uri = Uri.parse(musicFiles.get(position).getPath());

        mediaPlayer = MediaPlayer.create(getBaseContext(), uri);
    }

    int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    void setLooping(boolean isLooping) {
        mediaPlayer.setLooping(isLooping);
    }

    void onCompleted() {
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if(actionPlaying != null) {
            actionPlaying.nextBtnClicked();
//            if(mediaPlayer != null) {
//                createMediaPlayer(position);
//                mediaPlayer.start();
//                onCompleted();
//            }
        }
    }

    //actionPlaying is null by default
    //so we have to set it to a class that implements it
    //in order for it to work
    public void setActionPlaying(ActionPlaying actionPlaying) {
        this.actionPlaying = actionPlaying;
    }
}
