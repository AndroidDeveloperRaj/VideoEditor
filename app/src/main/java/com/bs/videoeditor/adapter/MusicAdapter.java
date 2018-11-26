package com.bs.videoeditor.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bs.videoeditor.R;
import com.bs.videoeditor.model.MusicModel;
import com.bs.videoeditor.model.VideoModel;
import com.bs.videoeditor.utils.Utils;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hung on 11/19/2018.
 */

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ViewHolder> {
    private List<MusicModel> musicModelList;
    private Context context;
    private ItemSelected callback;

    public MusicAdapter(Context context, List<MusicModel> musicModels, ItemSelected callback) {
        this.context = context;
        this.musicModelList = musicModels;
        this.callback = callback;
    }

    public void setFilter(List<MusicModel> list) {
        musicModelList = new ArrayList<>();
        musicModelList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_music, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MusicModel musicModel = musicModelList.get(position);
        holder.tvName.setText(musicModel.getTitle());
        holder.tvTime.setText(Utils.convertMillisecond(musicModel.getDuration()));
        Glide.with(context).load(Uri.fromFile(new File(musicModel.getFilePath()))).into(holder.ivThumb);
    }

    @Override
    public int getItemCount() {
        return musicModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvTime;
        private ImageView ivThumb;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvTime = itemView.findViewById(R.id.tv_time);
            ivThumb = itemView.findViewById(R.id.iv_thumb);
            itemView.setOnClickListener(v -> callback.onClick(getAdapterPosition()));
        }
    }

    public interface ItemSelected {
        void onClick(int index);
    }

}
