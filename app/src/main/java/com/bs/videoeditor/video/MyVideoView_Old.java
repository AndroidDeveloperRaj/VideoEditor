package com.bs.videoeditor.video;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

public class MyVideoView_Old extends VideoView {

    View dependentView;
    int counter = 0;
    boolean handleListener = true;
    private MediaListener mediaListener;

    public MyVideoView_Old(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setMediaListener(MediaListener mediaListener) {
        this.mediaListener = mediaListener;
    }

    public void setHandleListener(boolean handle) {
        this.handleListener = handle;
    }

    @Override
    public void setOnErrorListener(MediaPlayer.OnErrorListener l) {
        super.setOnErrorListener(l);
        Log.e("xxx", "errrrrrrrrrrrrr");
    }

    @Override
    public void start() {
        if (mediaListener != null && handleListener) {
            mediaListener.onStart();
        }
        super.start();
    }

    public void getProgressVideo(){

    }

    @Override
    public void pause() {
        if (mediaListener != null && handleListener) {
            mediaListener.onPause();
        }
        super.pause();

    }

    @Override
    public void seekTo(int msec) {
        if (mediaListener != null && handleListener) {
            mediaListener.onSeek(msec);
        }
        super.seekTo(msec);

    }

    public void setDependentView(View v) {
        this.dependentView = v;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        counter++;
        if (counter >= 2) {
            if (dependentView == null || w == 0 || h == 0) return;
            ViewGroup.LayoutParams params = dependentView.getLayoutParams();
            params.width = w;
            params.height = h;

            dependentView.setLayoutParams(params);
        }
    }

    public interface MediaListener {
        void onStart();

        void onPause();

        void onSeek(long milisecond);
    }

}
