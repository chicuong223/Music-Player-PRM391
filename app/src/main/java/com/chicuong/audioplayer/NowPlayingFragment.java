package com.chicuong.audioplayer;

import static android.content.Context.MODE_PRIVATE;
import static com.chicuong.audioplayer.MainActivity.ARTIST_NAME;
import static com.chicuong.audioplayer.MainActivity.ARTIST_TO_FRAG;
import static com.chicuong.audioplayer.MainActivity.PATH_TO_FRAG;
import static com.chicuong.audioplayer.MainActivity.POSITION_TO_FRAG;
import static com.chicuong.audioplayer.MainActivity.SHOW_MINI_PLAYER;
import static com.chicuong.audioplayer.MainActivity.SONG_FILE;
import static com.chicuong.audioplayer.MainActivity.SONG_LAST_PLAYED;
import static com.chicuong.audioplayer.MainActivity.SONG_NAME;
import static com.chicuong.audioplayer.MainActivity.SONG_NAME_TO_FRAG;
import static com.chicuong.audioplayer.MainActivity.SONG_POSITION;
import static com.chicuong.audioplayer.MainActivity.musicFiles;
import static com.chicuong.audioplayer.MainActivity.repeatBoolean;
import static com.chicuong.audioplayer.MainActivity.shuffleBoolean;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Random;

public class NowPlayingFragment extends Fragment implements ServiceConnection, ActionPlaying {

    private ImageView nextBtn, albumArt, prevBtn;
    private TextView artist, songName;
    private FloatingActionButton playPauseBtn;
    private View view;
    MusicService service;
    RelativeLayout layoutSongArtist;
    private boolean serviceConnected = false;
    int position;

    public NowPlayingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_now_playing,
                    container, false);
        }

        artist = view.findViewById(R.id.artist_mini_player);
        songName = view.findViewById(R.id.song_name_mini_player);
        albumArt = view.findViewById(R.id.mini_player_album_art);
        nextBtn = view.findViewById(R.id.mini_imgNext);
        playPauseBtn = view.findViewById(R.id.mini_play_pause_btn);
        prevBtn = view.findViewById(R.id.mini_imgPrev);
        layoutSongArtist = view.findViewById(R.id.mini_song_artist_layout);
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

        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (service != null) {
                    SharedPreferences preferences = getActivity().getSharedPreferences(SONG_LAST_PLAYED, MODE_PRIVATE);
                    POSITION_TO_FRAG = preferences.getInt(SONG_POSITION, -1);
                    if (POSITION_TO_FRAG == -1) {
                        POSITION_TO_FRAG = 0;
                    }
                    position = POSITION_TO_FRAG;
                    if (service.mediaPlayer == null) {
                        Intent intent = new Intent(getContext(), MusicService.class);
                        intent.putExtra("servicePosition", POSITION_TO_FRAG);
                        service.position = POSITION_TO_FRAG;
                        getContext().startService(intent);
                        playPauseBtn.setImageResource(R.drawable.ic_pause);
                    } else {
                        if (service.isPlaying()) {
                            playPauseBtn.setImageResource(R.drawable.ic_pause);
                        } else {
                            playPauseBtn.setImageResource(R.drawable.ic_play);
                        }
                        service.playPauseBtnClicked();
                    }
                    service.showNotification(R.drawable.ic_pause, false);
                }
            }
        });

        layoutSongArtist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (service != null) {
                    SharedPreferences preferences = getActivity().getSharedPreferences(SONG_LAST_PLAYED, MODE_PRIVATE);
                    String path = preferences.getString(SONG_FILE, null);
                    int songPosition = preferences.getInt(SONG_POSITION, -1);
                    if (path != null) {
                        PATH_TO_FRAG = path;
                        POSITION_TO_FRAG = songPosition;
                    } else {
                        PATH_TO_FRAG = null;
                        POSITION_TO_FRAG = -1;
                    }
                    Intent intent = new Intent(getContext(), PlayerActivity.class);
                    intent.putExtra("position", POSITION_TO_FRAG);
                    intent.putExtra("continue", true);
                    startActivity(intent);
                }
            }
        });

        albumArt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (service != null) {
                    SharedPreferences preferences = getActivity().getSharedPreferences(SONG_LAST_PLAYED, MODE_PRIVATE);
                    String path = preferences.getString(SONG_FILE, null);
                    int songPosition = preferences.getInt(SONG_POSITION, -1);
                    if (path != null) {
                        PATH_TO_FRAG = path;
                        POSITION_TO_FRAG = songPosition;
                    } else {
                        PATH_TO_FRAG = null;
                        POSITION_TO_FRAG = -1;
                    }
                    Intent intent = new Intent(getContext(), PlayerActivity.class);
                    intent.putExtra("position", POSITION_TO_FRAG);
                    intent.putExtra("continue", true);
                    startActivity(intent);
                }
            }
        });

        return view;
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

    @Override
    public void onResume() {
        super.onResume();
        if (SHOW_MINI_PLAYER) {
            if (PATH_TO_FRAG != null) {
                byte[] art = getAlbumArt(PATH_TO_FRAG);
                if (art != null) {
                    Glide.with(getContext())
                            .load(art)
                            .into(albumArt);
                } else {
                    Glide.with(getContext())
                            .load(R.drawable.music_icon)
                            .into(albumArt);
                }
                songName.setText(SONG_NAME_TO_FRAG);
                artist.setText(ARTIST_TO_FRAG);
                //getContext(): get the context that the fragment is bound to
                Intent intent = new Intent(getContext(), MusicService.class);
                if (getContext() != null) {
                    getContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
                }
            }
        }
//        if (service != null && service.mediaPlayer != null) {
//            if (service.isPlaying()) {
//                playPauseBtn.setImageResource(R.drawable.ic_pause);
//            }
//        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getContext() != null) {
            if (serviceConnected) {
                getContext().unbindService(this);
            }
        }
    }


    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        MusicService.MyBinder binder = (MusicService.MyBinder) iBinder;
        service = binder.getService();
        service.setActionPlaying(this);
        serviceConnected = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        service = null;
        serviceConnected = false;
    }

    @Override
    public void playPauseBtnClicked() {
        if (service.isPlaying()) {
            playPauseBtn.setImageResource(R.drawable.ic_play);
            service.showNotification(R.drawable.ic_play, false);
            service.pause();
        } else {
            playPauseBtn.setImageResource(R.drawable.ic_pause);
            service.showNotification(R.drawable.ic_pause, false);
            service.start();
        }
    }

    @Override
    public void nextBtnClicked() {
        if (service != null) {
            if (getActivity() != null) {
                //get position from shared preference
                SharedPreferences preferences = getActivity().getSharedPreferences(SONG_LAST_PLAYED, MODE_PRIVATE);
                POSITION_TO_FRAG = preferences.getInt(SONG_POSITION, -1);
                if(POSITION_TO_FRAG == -1) POSITION_TO_FRAG = 0;
                position = POSITION_TO_FRAG;

                if(service.mediaPlayer != null) {
                    service.stop();
                    service.release();
                    if (shuffleBoolean) {
                        position = getRandom(musicFiles.size() - 1);
                    } else {
                        position = (position + 1) % musicFiles.size();
                    }
                    Uri uri = Uri.parse(service.serviceMusicFiles.get(position).getPath());
                    service.createMediaPlayer(position);
//                    metaData(uri);
                    byte[] art = getAlbumArt(uri.toString());
                    if (art != null) {
                        Glide.with(getContext())
                                .load(art)
                                .into(albumArt);
                    } else {
                        Glide.with(getContext())
                                .load(R.drawable.music_icon)
                                .into(albumArt);
                    }
                    service.setLooping(repeatBoolean);
                    service.onCompleted();
                    service.start();
                }
                else {
                    Intent intent = new Intent(getContext(), MusicService.class);
                    intent.putExtra("servicePosition", POSITION_TO_FRAG + 1);
                    service.position = POSITION_TO_FRAG + 1 ;
                    getContext().startService(intent);
                }
                service.showNotification(R.drawable.ic_pause, false);
                SharedPreferences.Editor editor = getActivity().getSharedPreferences(SONG_LAST_PLAYED, MODE_PRIVATE)
                        .edit();
                editor.putString(SONG_FILE,
                            musicFiles.get(service.position).getPath());
                editor.putString(ARTIST_NAME, musicFiles.get(service.position).getArtist());
                editor.putString(SONG_NAME, musicFiles.get(service.position).getTitle());
                editor.putInt(SONG_POSITION, service.position);
                editor.apply();

                preferences = getActivity().getSharedPreferences(SONG_LAST_PLAYED, MODE_PRIVATE);
                String path = preferences.getString(SONG_FILE, null);
                String song = preferences.getString(SONG_NAME, null);
                String artistName = preferences.getString(ARTIST_NAME, null);
                int songPosition = preferences.getInt(SONG_POSITION, -1);
                if (path != null) {
                    SHOW_MINI_PLAYER = true;
                    PATH_TO_FRAG = path;
                    ARTIST_TO_FRAG = artistName;
                    SONG_NAME_TO_FRAG = song;
                    POSITION_TO_FRAG = songPosition;
                } else {
                    SHOW_MINI_PLAYER = false;
                    PATH_TO_FRAG = null;
                    ARTIST_TO_FRAG = null;
                    SONG_NAME_TO_FRAG = null;
                    POSITION_TO_FRAG = -1;
                }
                if (SHOW_MINI_PLAYER) {
                    byte[] art = getAlbumArt(PATH_TO_FRAG);
                    if (art != null) {
                        Glide.with(getContext())
                                .load(art)
                                .into(albumArt);
                    } else {
                        Glide.with(getContext())
                                .load(R.drawable.music_icon)
                                .into(albumArt);
                    }
                    songName.setText(SONG_NAME_TO_FRAG);
                    artist.setText(ARTIST_TO_FRAG);
                    playPauseBtn.setImageResource(R.drawable.ic_pause);
                }
            }
        }
    }

    @Override
    public void prevBtnClicked() {
        if (service != null) {
            if (getActivity() != null) {
                SharedPreferences preferences = getActivity().getSharedPreferences(SONG_LAST_PLAYED, MODE_PRIVATE);
                POSITION_TO_FRAG = preferences.getInt(SONG_POSITION, -1);
                if(POSITION_TO_FRAG == -1) POSITION_TO_FRAG = 0;
                position = POSITION_TO_FRAG;
                if(service.mediaPlayer != null) {
                    service.stop();
                    service.release();
                    if (shuffleBoolean && !repeatBoolean) {
                        position = getRandom(service.serviceMusicFiles.size() - 1);
                    } else if (!shuffleBoolean && !repeatBoolean) {
                        //nếu position < 0
                        //play cái cuối cùng
                        //ko thì play cái trước nó
                        position = (position - 1) < 0 ? (service.serviceMusicFiles.size() - 1) : (position - 1);
                    }
                    //else position = position
                    Uri uri = Uri.parse(service.serviceMusicFiles.get(position).getPath());
//        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                    service.createMediaPlayer(position);
                    byte[] art = getAlbumArt(uri.toString());
                    if (art != null) {
                        Glide.with(getContext())
                                .load(art)
                                .into(albumArt);
                    } else {
                        Glide.with(getContext())
                                .load(R.drawable.music_icon)
                                .into(albumArt);
                    }
                    service.setLooping(repeatBoolean);
                    service.onCompleted();
                    service.start();
                }
                else {
                    Intent intent = new Intent(getContext(), MusicService.class);
                    intent.putExtra("servicePosition", POSITION_TO_FRAG - 1);
                    service.position = POSITION_TO_FRAG - 1 ;
                    getContext().startService(intent);
                }
                service.showNotification(R.drawable.ic_pause, false);

                //set new data to SharedPreference
                SharedPreferences.Editor editor = getActivity().getSharedPreferences(SONG_LAST_PLAYED, MODE_PRIVATE)
                        .edit();
                editor.putString(SONG_FILE,
                        musicFiles.get(position).getPath());
                editor.putString(ARTIST_NAME, musicFiles.get(service.position).getArtist());
                editor.putString(SONG_NAME, musicFiles.get(service.position).getTitle());
                editor.putInt(SONG_POSITION, POSITION_TO_FRAG - 1);
                editor.apply();

                //get new applied sharedreference to render views
                preferences = getActivity().getSharedPreferences(SONG_LAST_PLAYED, MODE_PRIVATE);
                String path = preferences.getString(SONG_FILE, null);
                String song = preferences.getString(SONG_NAME, null);
                String artistName = preferences.getString(ARTIST_NAME, null);
                int songPosition = preferences.getInt(SONG_POSITION, -1);
                if (path != null) {
                    SHOW_MINI_PLAYER = true;
                    PATH_TO_FRAG = path;
                    ARTIST_TO_FRAG = artistName;
                    SONG_NAME_TO_FRAG = song;
                    POSITION_TO_FRAG = songPosition;
                } else {
                    SHOW_MINI_PLAYER = false;
                    PATH_TO_FRAG = null;
                    ARTIST_TO_FRAG = null;
                    SONG_NAME_TO_FRAG = null;
                    POSITION_TO_FRAG = -1;
                }
                if (SHOW_MINI_PLAYER) {
                    byte[] art = getAlbumArt(PATH_TO_FRAG);
                    if (art != null) {
                        Glide.with(getContext())
                                .load(art)
                                .into(albumArt);
                    } else {
                        Glide.with(getContext())
                                .load(R.drawable.music_icon)
                                .into(albumArt);
                    }
                    songName.setText(SONG_NAME_TO_FRAG);
                    artist.setText(ARTIST_TO_FRAG);
                    playPauseBtn.setImageResource(R.drawable.ic_pause);
                }
            }
        }
    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i + 1);
    }
}