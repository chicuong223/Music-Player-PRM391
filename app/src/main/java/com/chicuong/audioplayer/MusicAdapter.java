package com.chicuong.audioplayer;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

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
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.fileName.setText(_mFiles.get(position).getTitle());
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
            }
        });
    }

    @Override
    public int getItemCount() {
        return _mFiles.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView fileName;
        ImageView albumArt;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            albumArt = itemView.findViewById(R.id.music_img);
            fileName = itemView.findViewById(R.id.txt_file_name);
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
