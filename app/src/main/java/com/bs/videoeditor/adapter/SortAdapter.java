package com.bs.videoeditor.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bs.videoeditor.R;
import com.bs.videoeditor.listener.IItemTouchHelperAdapter;
import com.bs.videoeditor.listener.IListSongChanged;
import com.bs.videoeditor.model.VideoModel;

import java.util.Collections;
import java.util.List;

import static com.bs.videoeditor.utils.Utils.convertMillisecond;


public class SortAdapter extends RecyclerView.Adapter<SortAdapter.ViewHolder> implements IItemTouchHelperAdapter {
    private List<VideoModel> videoModelList;
    private Context context;
    private OnStartDragListener callback;
    private IListSongChanged iListSongChanged;

    public SortAdapter(List<VideoModel> songList, Context context, OnStartDragListener callback, IListSongChanged listener) {
        this.videoModelList = songList;
        this.context = context;
        this.callback = callback;
        this.iListSongChanged = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SortAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sort_audio, null));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        VideoModel videoModel = videoModelList.get(position);

        holder.tvName.setText(videoModel.getNameAudio());
        holder.tvArtist.setText(videoModel.getNameArtist());
        holder.tvDuration.setText(convertMillisecond(Long.parseLong(videoModel.getDuration())));

        holder.ivSort.setOnTouchListener((view, motionEvent) -> {
            if (MotionEventCompat.getActionMasked(motionEvent) == MotionEvent.ACTION_DOWN) {
                callback.onStartDrag(holder);
            }
            return false;
        });
    }


    public interface OnStartDragListener {
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    @Override
    public int getItemCount() {
        return videoModelList.size();
    }

    @Override
    public void onItemDismiss(int position) {
        videoModelList.remove(position);
        iListSongChanged.onNoteListChanged(videoModelList);
        notifyItemRemoved(position);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(videoModelList, fromPosition, toPosition);
        iListSongChanged.onNoteListChanged(videoModelList);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivSort;
        private TextView tvName, tvArtist, tvDuration;

        public ViewHolder(View itemView) {
            super(itemView);
            ivSort = itemView.findViewById(R.id.iv_sort);
            tvName = itemView.findViewById(R.id.name_song);
            tvArtist = itemView.findViewById(R.id.name_artist);
            tvDuration = itemView.findViewById(R.id.duration);
        }
    }
}
