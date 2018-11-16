package com.bs.videoeditor.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bs.videoeditor.R;
import com.bs.videoeditor.listener.OnGalleryFileSelectListener;
import com.bs.videoeditor.model.VideoModel;
import com.yalantis.multiselection.lib.adapter.BaseLeftAdapter;


public class LeftListAdapter extends BaseLeftAdapter<VideoModel, AudioFilesViewHolder> {

    private OnGalleryFileSelectListener mListener;
    private Context mContext;

    public LeftListAdapter(Context context,OnGalleryFileSelectListener mListener) {
        super(VideoModel.class);
        mContext = context;
        this.mListener = mListener;
    }

    @Override
    public AudioFilesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_gallery, parent, false);
        return new AudioFilesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final AudioFilesViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        AudioFilesViewHolder.bind(mContext, holder, getItemAt(position));

        holder.itemView.setOnClickListener(v -> {
            v.setPressed(true);

            v.postDelayed(() -> {
                v.setPressed(false);
                mListener.onGalleryFileSelectListener(holder.getAdapterPosition());
            }, 200);
        });
    }

    public void setOnGalleryFileSelectListener(OnGalleryFileSelectListener mListener) {
        this.mListener = mListener;
    }
}