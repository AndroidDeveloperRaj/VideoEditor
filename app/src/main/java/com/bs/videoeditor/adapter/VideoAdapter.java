package com.bs.videoeditor.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bs.videoeditor.R;
import com.bs.videoeditor.model.VideoModel;
import com.bs.videoeditor.utils.Utils;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

/**
 * Created by Hung on 11/15/2018.
 */

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {
    private List<VideoModel> videoModelList;
    private ItemSelected callback;
    private Context context;

    public VideoAdapter(List<VideoModel> videoModels, ItemSelected callback, Context context) {
        this.videoModelList = videoModels;
        this.callback = callback;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_video, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VideoModel videoModel = videoModelList.get(position);
        holder.tvName.setText(videoModel.getNameAudio());
        holder.tvTime.setText(Utils.convertMillisecond(Long.parseLong(videoModel.getDuration())));
        Glide.with(context).load(Uri.fromFile(new File(videoModel.getPath()))).into(holder.ivThumb);
    }

    @Override
    public int getItemCount() {
        return videoModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvTime;
        private ImageView ivThumb;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvTime = itemView.findViewById(R.id.tv_time);
            ivThumb = itemView.findViewById(R.id.iv_thumb);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callback.onClick(getAdapterPosition());
                }
            });
        }
    }

    public interface ItemSelected {
        void onClick(int index);
    }
}
