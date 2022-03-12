package com.chicuong.audioplayer;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NowPlayingFragment extends Fragment {

    private ImageView nextBtn, albumArt;
    private TextView artist, songName;
    private FloatingActionButton playPauseBtn;
    private View view;

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
        return view;
    }
}