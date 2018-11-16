package com.bs.videoeditor.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bs.videoeditor.R;
import com.bs.videoeditor.model.VideoModel;
import com.bumptech.glide.Glide;

import static com.bs.videoeditor.utils.Utils.convertMillisecond;
import static com.bs.videoeditor.utils.Utils.getFileExtension;
import static com.bs.videoeditor.utils.Utils.getStringSizeLengthFile;

public class AudioFilesViewHolder extends RecyclerView.ViewHolder {
    TextView name, size, dateCreate;
    ImageView avatar;

    public AudioFilesViewHolder(View view) {
        super(view);
        name = view.findViewById(R.id.name);
        avatar = view.findViewById(R.id.avatar);
        size = view.findViewById(R.id.size);
        dateCreate = view.findViewById(R.id.dateCreate);
    }

    public static void bind(Context context, AudioFilesViewHolder viewHolder, VideoModel videoModel) {
        viewHolder.name.setText(videoModel.getNameAudio());
        viewHolder.size.setText(getStringSizeLengthFile(videoModel.getSize())
                + "  " + getFileExtension(videoModel.getPath()
                + "  " + convertMillisecond(Long.parseLong(videoModel.getDuration()))));
        Glide.with(context).load(videoModel.getPath()).into(viewHolder.avatar);
    }
}
