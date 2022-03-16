package com.chicuong.audioplayer;

import static com.chicuong.audioplayer.AlbumDetailsAdapter.albumFiles;
import static com.chicuong.audioplayer.ApplicationClass.ACTION_NEXT;
import static com.chicuong.audioplayer.ApplicationClass.ACTION_PLAY;
import static com.chicuong.audioplayer.ApplicationClass.ACTION_PREVIOUS;
import static com.chicuong.audioplayer.ApplicationClass.CHANNEL_ID_2;
import static com.chicuong.audioplayer.MainActivity.ARTIST_NAME;
import static com.chicuong.audioplayer.MainActivity.SONG_FILE;
import static com.chicuong.audioplayer.MainActivity.SONG_LAST_PLAYED;
import static com.chicuong.audioplayer.MainActivity.SONG_NAME;
import static com.chicuong.audioplayer.MainActivity.SONG_POSITION;
import static com.chicuong.audioplayer.MainActivity.musicFiles;
import static com.chicuong.audioplayer.PlayerActivity.listSong;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {
    IBinder binder = new MyBinder();
    MediaPlayer mediaPlayer;
    List<MusicFiles> serviceMusicFiles = new ArrayList<>();
    Uri uri;
    int position = -1;
    ActionPlaying actionPlaying;
    MediaSessionCompat mediaSessionCompat;
    boolean close = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaSessionCompat = new MediaSessionCompat(getBaseContext(), "My Audio");

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
        boolean isContinue = intent.getBooleanExtra("isContinue", false);
        close = intent.getBooleanExtra("Close", false);
        String actionName = intent.getStringExtra("ActionName");
        boolean album = intent.getBooleanExtra("album", false);
        if (myPosition != -1 && !isContinue) {
            playMedia(myPosition, album);
        }
        if(close) {
            if(mediaPlayer != null) {
//                mediaPlayer.pause();
                mediaPlayer.stop();
                mediaPlayer.release();
            }
            stopForeground(true);
            stopSelf();
        }
        if (actionName != null) {
            switch (actionName) {
                case "playPause":
                    playPauseBtnClicked();
                    break;
                case "next":
                    nextBtnClicked();
                    break;
                case "previous":
                    prevBtnClicked();
                    break;
            }
        }
        return START_STICKY;
    }

    private void playMedia(int startPosition, boolean album) {
        if(album) {
            serviceMusicFiles = albumFiles;
        }
        else {
            serviceMusicFiles = musicFiles;
        }
        position = startPosition;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            if (serviceMusicFiles != null) {
                createMediaPlayer(position);
            }
        } else {
            createMediaPlayer(position);
        }
        mediaPlayer.start();
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
        uri = Uri.parse(serviceMusicFiles.get(position).getPath());
        SharedPreferences.Editor editor = getSharedPreferences(SONG_LAST_PLAYED, MODE_PRIVATE)
                .edit();
        editor.putString(SONG_FILE, uri.toString());
        editor.putString(ARTIST_NAME, serviceMusicFiles.get(position).getArtist());
        editor.putString(SONG_NAME, serviceMusicFiles.get(position).getTitle());
        editor.putInt(SONG_POSITION, position);
        editor.apply();
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
        if (actionPlaying != null) {
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

    void showNotification(int playPauseBtn, boolean album) {
        Intent intent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(intent);
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0
//                , intent, 0);
        PendingIntent resultPending =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

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

        Intent closeIntent = new Intent(this, NotificationReceiver.class)
                .setAction("Close")
                .putExtra("Close", true);
        PendingIntent closePending = PendingIntent
                .getBroadcast(this, 0, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        List<MusicFiles> lst = new ArrayList<>();
        if(album) {
            lst = albumFiles;
        }
        else {
            lst = musicFiles;
        }
        //album picture to display on notification
        byte[] picture = null;
        picture = getAlbumArt(lst.get(position).getPath());
        Bitmap thumb = null;
        if (picture != null) {
            thumb = BitmapFactory.decodeByteArray(picture, 0, picture.length);
        } else {
            thumb = BitmapFactory.decodeResource(getResources(), R.drawable.music_icon);
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_2)
                .setSmallIcon(playPauseBtn)
                .setLargeIcon(thumb)
                .setContentTitle(lst.get(position).getTitle())
                .setContentText(lst.get(position).getArtist())
                .addAction(R.drawable.ic_previous, "Previous", prevPending)
                .addAction(playPauseBtn, "Pause", pausePending)
                .addAction(R.drawable.ic_next, "Next", nextPending)
                .addAction(R.drawable.ic_close, "Close", closePending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(resultPending)
                .build();

        /* Send notification */
//        NotificationManager notificationManager =
//                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        notificationManager.notify(0, notification);

        //Instead of showing miniplayer as a notification
        //run it as a foreground service
        //to do that, add permission FOREGROUND_SERVICE in AndroidManifest first
        /* ONLY ID = 1 WORKS */
        startForeground(1, notification);
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

    void nextBtnClicked() {
        if (actionPlaying != null) {
            actionPlaying.nextBtnClicked();
        }
    }

    void playPauseBtnClicked() {
        if (actionPlaying != null) {
            actionPlaying.playPauseBtnClicked();
        }
    }

    void prevBtnClicked() {
        if (actionPlaying != null) {
            actionPlaying.prevBtnClicked();
        }
    }
}
