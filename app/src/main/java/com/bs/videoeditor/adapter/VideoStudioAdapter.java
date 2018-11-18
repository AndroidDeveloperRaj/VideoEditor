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
import com.bs.videoeditor.fragment.StudioFragmentDetail;
import com.bs.videoeditor.model.VideoModel;
import com.bs.videoeditor.utils.Utils;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hung on 11/15/2018.
 */

public class VideoStudioAdapter extends RecyclerView.Adapter<VideoStudioAdapter.ViewHolder> {
    private List<VideoModel> videoModelList;
    private ItemSelectedStudio callback;
    private StudioFragmentDetail context;
    private boolean isStudio;

    public VideoStudioAdapter(List<VideoModel> videoModels, ItemSelectedStudio callback, StudioFragmentDetail context, boolean isStudio) {
        this.videoModelList = videoModels;
        this.callback = callback;
        this.context = context;
        this.isStudio = isStudio;
    }

    public void setFilter(List<VideoModel> list) {
        videoModelList = new ArrayList<>();
        videoModelList = list;
        notifyDataSetChanged();
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
        holder.checkBox.setChecked(videoModelList.get(position).isCheck());
        Glide.with(context).load(Uri.fromFile(new File(videoModel.getPath()))).into(holder.ivThumb);

        // action mode
        if (context.isActionMode) {
            if (context.isSelectAll) {
                holder.checkBox.setChecked(videoModelList.get(position).isCheck());
            }
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.ivMore.setVisibility(View.GONE);
        } else {
            holder.checkBox.setVisibility(View.GONE);
            holder.ivMore.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return videoModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvTime;
        private ImageView ivThumb, ivMore;
        private CheckBox checkBox;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvTime = itemView.findViewById(R.id.tv_time);
            ivThumb = itemView.findViewById(R.id.iv_thumb);
            ivMore = itemView.findViewById(R.id.iv_more);
            checkBox = itemView.findViewById(R.id.checkbox);

            itemView.setOnClickListener(v -> {
                if (context.isActionMode) {

                    VideoModel videoModel = videoModelList.get(getAdapterPosition());

                    context.isSelectAll = false;

                    if (videoModel.isCheck()) {
                        videoModel.setCheck(false);
                        checkBox.setChecked(videoModel.isCheck());
                    } else {
                        videoModel.setCheck(true);
                        checkBox.setChecked(videoModel.isCheck());
                    }

                    context.prepareSelection(checkBox, getAdapterPosition());

                } else {
                    callback.onClick(getAdapterPosition());
                }
                context.isSelectAll = false;
            });

            itemView.setOnLongClickListener(v -> callback.onLongClick(getAdapterPosition()));

            ivMore.setOnClickListener(v -> callback.onOptionClick(getAdapterPosition()));
        }
    }

    public interface ItemSelectedStudio {
        void onClick(int index);

        boolean onLongClick(int index);

        void onOptionClick(int index);
    }
}
