package com.chicuong.audioplayer;

import static android.content.Context.MODE_PRIVATE;
import static com.chicuong.audioplayer.MainActivity.ARTIST_NAME;
import static com.chicuong.audioplayer.MainActivity.ARTIST_TO_FRAG;
import static com.chicuong.audioplayer.MainActivity.PATH_TO_FRAG;
import static com.chicuong.audioplayer.MainActivity.SHOW_MINI_PLAYER;
import static com.chicuong.audioplayer.MainActivity.SONG_FILE;
import static com.chicuong.audioplayer.MainActivity.SONG_LAST_PLAYED;
import static com.chicuong.audioplayer.MainActivity.SONG_NAME;
import static com.chicuong.audioplayer.MainActivity.SONG_NAME_TO_FRAG;
import static com.chicuong.audioplayer.MainActivity.musicFiles;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NowPlayingFragment extends Fragment implements ServiceConnection {

    private ImageView nextBtn, albumArt, prevBtn;
    private TextView artist, songName;
    private FloatingActionButton playPauseBtn;
    private View view;
    MusicService service;
    RelativeLayout layoutSongArtist;

    public NowPlayingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(view == null) {
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
                if(service != null && service.mediaPlayer != null) {
                    service.nextBtnClicked();
                    if(getActivity() != null) {
                        SharedPreferences.Editor editor = getActivity().getSharedPreferences(SONG_LAST_PLAYED, MODE_PRIVATE)
                                .edit();
                        editor.putString(SONG_FILE,
                                service.musicFiles.get(service.position).getPath());
                        editor.putString(ARTIST_NAME, musicFiles.get(service.position).getArtist());
                        editor.putString(SONG_NAME, musicFiles.get(service.position).getTitle());
                        editor.apply();
                        SharedPreferences preferences = getActivity().getSharedPreferences(SONG_LAST_PLAYED, MODE_PRIVATE);
                        String path = preferences.getString(SONG_FILE, null);
                        String song = preferences.getString(SONG_NAME, null);
                        String artistName = preferences.getString(ARTIST_NAME, null);
                        if(path != null) {
                            SHOW_MINI_PLAYER = true;
                            PATH_TO_FRAG = path;
                            ARTIST_TO_FRAG = artistName;
                            SONG_NAME_TO_FRAG = song;
                        }
                        else {
                            SHOW_MINI_PLAYER = false;
                            PATH_TO_FRAG = null;
                            ARTIST_TO_FRAG = null;
                            SONG_NAME_TO_FRAG = null;
                        }
                        if(SHOW_MINI_PLAYER) {
                            if(PATH_TO_FRAG != null) {
                                byte[] art = getAlbumArt(PATH_TO_FRAG);
                                if(art != null) {
                                    Glide.with(getContext())
                                            .load(art)
                                            .into(albumArt);
                                }
                                else {
                                    Glide.with(getContext())
                                            .load(R.drawable.music_icon)
                                            .into(albumArt);
                                }
                                songName.setText(SONG_NAME_TO_FRAG);
                                artist.setText(ARTIST_TO_FRAG);
                            }
                        }
                    }
                }
            }
        });

        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(service != null && service.mediaPlayer != null) {
                    service.prevBtnClicked();
                    if(getActivity() != null) {
                        SharedPreferences.Editor editor = getActivity().getSharedPreferences(SONG_LAST_PLAYED, MODE_PRIVATE)
                                .edit();
                        editor.putString(SONG_FILE,
                                service.musicFiles.get(service.position).getPath());
                        editor.putString(ARTIST_NAME, musicFiles.get(service.position).getArtist());
                        editor.putString(SONG_NAME, musicFiles.get(service.position).getTitle());
                        editor.apply();
                        SharedPreferences preferences = getActivity().getSharedPreferences(SONG_LAST_PLAYED, MODE_PRIVATE);
                        String path = preferences.getString(SONG_FILE, null);
                        String song = preferences.getString(SONG_NAME, null);
                        String artistName = preferences.getString(ARTIST_NAME, null);
                        if(path != null) {
                            SHOW_MINI_PLAYER = true;
                            PATH_TO_FRAG = path;
                            ARTIST_TO_FRAG = artistName;
                            SONG_NAME_TO_FRAG = song;
                        }
                        else {
                            SHOW_MINI_PLAYER = false;
                            PATH_TO_FRAG = null;
                            ARTIST_TO_FRAG = null;
                            SONG_NAME_TO_FRAG = null;
                        }
                        if(SHOW_MINI_PLAYER) {
                            if(PATH_TO_FRAG != null) {
                                byte[] art = getAlbumArt(PATH_TO_FRAG);
                                if(art != null) {
                                    Glide.with(getContext())
                                            .load(art)
                                            .into(albumArt);
                                }
                                else {
                                    Glide.with(getContext())
                                            .load(R.drawable.music_icon)
                                            .into(albumArt);
                                }
                                songName.setText(SONG_NAME_TO_FRAG);
                                artist.setText(ARTIST_TO_FRAG);
                            }
                        }
                    }
                }
            }
        });

        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(service != null && service.mediaPlayer != null) {
                    service.playPauseBtnClicked();
                    if(service.isPlaying()) {
                        playPauseBtn.setImageResource(R.drawable.ic_pause);
                    }
                    else {
                        playPauseBtn.setImageResource(R.drawable.ic_play);
                    }
                }
            }
        });

        layoutSongArtist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(service != null) {
                    Intent intent = new Intent(getContext(), PlayerActivity.class);
                    intent.putExtra("position", service.position);
                    intent.putExtra("continue", true);
                    startActivity(intent);
                }
            }
        });

        albumArt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(service != null) {
                    Intent intent = new Intent(getContext(), PlayerActivity.class);
                    intent.putExtra("position", service.position);
                    intent.putExtra("continue", true);
                    startActivity(intent);
                }
            }
        });


        if(service != null) {
            if(service.isPlaying()) {
                playPauseBtn.setImageResource(R.drawable.ic_pause);
            }
        }
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
        if(SHOW_MINI_PLAYER) {
            if(PATH_TO_FRAG != null) {
                byte[] art = getAlbumArt(PATH_TO_FRAG);
                if(art != null) {
                    Glide.with(getContext())
                            .load(art)
                            .into(albumArt);
                }
                else {
                    Glide.with(getContext())
                            .load(R.drawable.music_icon)
                            .into(albumArt);
                }
                songName.setText(SONG_NAME_TO_FRAG);
                artist.setText(ARTIST_TO_FRAG);

                //getContext(): get the context that the fragment is bound to
                Intent intent = new Intent(getContext(), MusicService.class);
                if(getContext() != null) {
                    getContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
                }
            }
        }
        if(service != null && service.mediaPlayer != null) {
            if(service.isPlaying()) {
                playPauseBtn.setImageResource(R.drawable.ic_pause);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(getContext() != null) {
            getContext().unbindService(this);
        }
    }


    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        MusicService.MyBinder binder = (MusicService.MyBinder) iBinder;
        service = binder.getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        service = null;
    }
}