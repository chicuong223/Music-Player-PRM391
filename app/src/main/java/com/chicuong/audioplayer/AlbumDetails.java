package com.chicuong.audioplayer;

import static com.chicuong.audioplayer.MainActivity.musicFiles;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class AlbumDetails extends AppCompatActivity {
    RecyclerView recyclerView;
    ImageView albumPhoto;
    String albumName;
    ArrayList<MusicFiles> albumSongs = new ArrayList<>();
    AlbumDetailsAdapter albumDetailsAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_details);
        recyclerView = findViewById(R.id.recycle_view);
        albumPhoto = findViewById(R.id.albumPhoto);
        albumName = getIntent().getStringExtra("albumName");
        int j = 0;
        for (int i = 0; i < musicFiles.size(); i++) {
            if(albumName.equals(musicFiles.get(i).getAlbum())) {
                albumSongs.add(j, musicFiles.get(i));
                j++;
            }
        }
        byte[] image = getAlbumArt(albumSongs.get(0).getPath());
        if(image != null) {
            Glide.with(this).load(image).into(albumPhoto);
        } else {
            Glide.with(this).load(R.drawable.music_icon).into(albumPhoto);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if((albumSongs.size()) >= 1) {
            albumDetailsAdapter = new AlbumDetailsAdapter(this, albumSongs);
            recyclerView.setAdapter(albumDetailsAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        }
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