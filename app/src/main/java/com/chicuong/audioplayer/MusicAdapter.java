package com.chicuong.audioplayer;

import static com.chicuong.audioplayer.MainActivity.SHOW_MINI_PLAYER;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import org.w3c.dom.Text;

import java.io.File;
import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MyViewHolder> {

    private Context _mContext;
    private List<MusicFiles> _mFiles;

    MusicAdapter(Context mContext, List<MusicFiles> mFiles) {
        this._mContext = mContext;
        this._mFiles = mFiles;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(_mContext).inflate(R.layout.music_item, parent, false);
        return new MyViewHolder(view);
    }

    //khi gắn customer layout vô view
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.fileName.setText(_mFiles.get(position).getTitle());
        Log.e("File Path", _mFiles.get(position).getPath());
        byte[] img = getAlbumArt(_mFiles.get(position).getPath());

        //load image into view holder
        if(img != null) {
            Glide.with(_mContext).asBitmap()
                    .load(img)
                    .into(holder.albumArt);
        }
        else {
            Glide.with(_mContext)
                    .load(R.drawable.music_icon)
                    .into(holder.albumArt);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(_mContext, PlayerActivity.class);
                intent.putExtra("position", position);
                _mContext.startActivity(intent);
                SHOW_MINI_PLAYER = true;
            }
        });
        holder.menuMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(_mContext, view);
                popupMenu.getMenuInflater().inflate(R.menu.popup, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener((item) -> {
                    switch (item.getItemId()) {
                        case R.id.delete:
                            Toast.makeText(_mContext, "Delete Clicked!", Toast.LENGTH_SHORT).show();
                            deleteFile(position, view);
                            break;
                    }
                    return true;
                });
            }
        });
    }

    private void deleteFile(int position, View view) {
        //Delete song from database
        _mContext.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                , "_id = '" + _mFiles.get(position).getId() + "'", null);

        boolean deleted = false;
        try {
            /* Remove file from storage */
            //get file path
            File file = new File(_mFiles.get(position).getPath());
            Log.e("Delete file", file.getPath());

            //delete file
            deleted = file.delete();
        }
        catch (Exception ex) {
            Log.e("Error Delete File", ex.getMessage());
        }
        if(deleted) {
            //remove songs from song list
            _mFiles.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, _mFiles.size());
            Snackbar.make(view, "File Deleted", Snackbar.LENGTH_LONG)
                    .show();
        }
        else {
            Snackbar.make(view, "Could not delete file", Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public int getItemCount() {
        return _mFiles.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView fileName;
        ImageView albumArt, menuMore;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            albumArt = itemView.findViewById(R.id.music_img);
            fileName = itemView.findViewById(R.id.txt_file_name);
            menuMore = itemView.findViewById(R.id.menu_more);
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
