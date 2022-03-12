package com.chicuong.audioplayer;

import static com.chicuong.audioplayer.MainActivity.musicFiles;
import static com.chicuong.audioplayer.MainActivity.repeatBoolean;
import static com.chicuong.audioplayer.MainActivity.shuffleBoolean;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlayerActivity extends AppCompatActivity {

    TextView songName, artistName, durationPlayed, durationTotal;
    ImageView coverArt, nextBtn, backBtn, shuffleBtn, repeatBtn, prevBtn;
    FloatingActionButton playPauseBtn;
    SeekBar seekBar;
    int position = -1;
    List<MusicFiles> listSong = new ArrayList<>();
    static Uri uri;
    static MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Thread playThread, prevThread, nextThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        initViews();
        getIntentMethod();
        songName.setText(listSong.get(position).getTitle());
        artistName.setText(listSong.get(position).getArtist());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (mediaPlayer != null && b) {
                    //skip media player to (position of seek bar * 1000) seconds
                    mediaPlayer.seekTo(i * 1000);
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
                if (mediaPlayer != null) {
                    int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                    durationPlayed.setText(formattedTime(mCurrentPosition));
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            playPauseBtn.setImageResource(R.drawable.ic_play);
                            nextBtnClicked();
                        }
                    });
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
                    shuffleBtn.setImageResource(R.drawable.ic_shuffle_on);
                }
            }
        });
        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (repeatBoolean) {
                    repeatBoolean = false;
                    mediaPlayer.setLooping(false);
                    repeatBtn.setImageResource(R.drawable.ic_repeat_off);
                } else {
                    repeatBoolean = true;
                    mediaPlayer.setLooping(true);
                    repeatBtn.setImageResource(R.drawable.ic_repeat_on);
                }
            }
        });
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
        if (mediaPlayer != null) {
            //if media player is playing
            //stop and play the new file
            mediaPlayer.stop();
            mediaPlayer.release();

            //create a media player in the context
            //pass the uri into the method to assign file to play
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            mediaPlayer.start();
        } else {
            //if media player is not playing
            //just play the file
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            mediaPlayer.start();
        }
        seekBar.setMax(mediaPlayer.getDuration() / 1000);
        metaData(uri);
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

//    @Override
//    protected void onResume() {
//        super.onResume();
//        playThreadBtn();
//        nextThreadBtn();
//        prevThreadBtn();
//    }

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

    private void playPauseBtnClicked() {
        if (mediaPlayer.isPlaying()) {
            playPauseBtn.setImageResource(R.drawable.ic_play);
            mediaPlayer.pause();
            seekBar.setMax(mediaPlayer.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 500);
                }
            });
        } else {
            playPauseBtn.setImageResource(R.drawable.ic_pause);
            mediaPlayer.start();
            seekBar.setMax(mediaPlayer.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
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

    private void nextBtnClicked() {
//        if(mediaPlayer.isPlaying()) {
        mediaPlayer.stop();
        mediaPlayer.release();
//            if(shuffleBoolean && !repeatBoolean) {
//                position = getRandom(listSong.size() - 1);
//            }
//            else if(!shuffleBoolean && !repeatBoolean) {
//                position = (position + 1) % listSong.size();
//            }
        if (shuffleBoolean) {
            position = getRandom(listSong.size() - 1);
        } else {
            position = (position + 1) % listSong.size();
        }
        //else position = position (repeat)
        uri = Uri.parse(listSong.get(position).getPath());
        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        metaData(uri);
        songName.setText(listSong.get(position).getTitle());
        artistName.setText(listSong.get(position).getArtist());

        seekBar.setMax(mediaPlayer.getDuration() / 1000);
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                }
                handler.postDelayed(this, 500);
            }
        });
        playPauseBtn.setImageResource(R.drawable.ic_pause);
        mediaPlayer.start();
//        }
//        else {
//            mediaPlayer.stop();
//            mediaPlayer.release();
//            position = (position + 1) % listSong.size();
//            uri = Uri.parse(listSong.get(position).getPath());
//            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
//            metaData(uri);
//            songName.setText(listSong.get(position).getTitle());
//            artistName.setText(listSong.get(position).getArtist());
//
//            seekBar.setMax(mediaPlayer.getDuration() / 1000);
//            PlayerActivity.this.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    if(mediaPlayer != null) {
//                        int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
//                        seekBar.setProgress(mCurrentPosition);
//                    }
//                    handler.postDelayed(this, 500);
//                }
//            });
//            playPauseBtn.setImageResource(R.drawable.ic_play);
//        }
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

    private void prevBtnClicked() {
        mediaPlayer.stop();
        mediaPlayer.release();

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
        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        metaData(uri);
        songName.setText(listSong.get(position).getTitle());
        artistName.setText(listSong.get(position).getArtist());

        seekBar.setMax(mediaPlayer.getDuration() / 1000);
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                }
                handler.postDelayed(this, 500);
            }
        });
        playPauseBtn.setImageResource(R.drawable.ic_pause);
        mediaPlayer.start();
    }
}