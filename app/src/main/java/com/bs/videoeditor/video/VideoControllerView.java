package com.bs.videoeditor.video;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.bs.videoeditor.R;


public class VideoControllerView extends FrameLayout implements View.OnClickListener, Runnable, MediaPlayer.OnCompletionListener, net.protyposis.android.mediaplayer.MediaPlayer.OnCompletionListener {
    public static final String TAG = VideoControllerView.class.getSimpleName();
    private static final int FADE_TIME = 1500;
    private MediaController.MediaPlayerControl mMediaController;
    private ImageView mButton;
    private Animation fadeIn, fadeOut;
    private boolean isShowForeground = true;
    private Handler hideForegroundHandler;
    private boolean isCompleted = false;

    public VideoControllerView(Context context) {
        super(context);
        init();
    }

    public VideoControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoControllerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VideoControllerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        Log.d("VideoControllerView", "init");
        int size = getResources().getDimensionPixelSize(R.dimen._60sdp);
        LayoutParams params = new LayoutParams(size, size);
        params.gravity = Gravity.CENTER;
        if (mButton == null) {
            mButton = new ImageView(getContext());
        }
        mButton.setLayoutParams(params);
        mButton.setImageResource(R.drawable.ic_play_video);
        mButton.invalidate();
        addView(mButton);


        initAnimation();
        this.setOnClickListener(this);
        mButton.setOnClickListener(this);

        hideForegroundHandler = new Handler();
    }

    private void initAnimation() {
        //fadeIn
        fadeIn = new AlphaAnimation(0.65f, 1f);
        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
        fadeIn.setDuration(500);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        //fadeOut
        if (fadeOut == null) {
            fadeOut = new AlphaAnimation(1, 0);
        }
        fadeOut.setInterpolator(new DecelerateInterpolator()); //add this
        fadeOut.setDuration(400);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mButton.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void fadeIn() {
        mButton.startAnimation(fadeIn);
    }

    private void fadeOut() {
        mButton.clearAnimation();
        mButton.startAnimation(fadeOut);
    }

    @Override
    public void onClick(View v) {
        if (v == this) {
            Log.d(TAG, "click foreground");
            if (mMediaController.isPlaying()) {
                if (!isShowForeground) {
                    showForeground();
                } else {
                    hideForeground();
                }
            }
        } else {
            Log.d(TAG, "click button");
            //click button
            if (isCompleted) {
                //replay video
                mButton.setImageResource(R.drawable.ic_play_video);

                mMediaController.start();
                isCompleted = false;
                hideForeground();
                return;
            }

            if (mMediaController.isPlaying()) {
                mButton.setImageResource(R.drawable.ic_play_video);
                fadeIn();
                showForeground();
                mMediaController.pause();
            } else {
                mButton.setImageResource(R.drawable.ic_pause_video);
                hideForeground();
                fadeOut();
                mMediaController.start();
            }
        }
    }

    public void showForeground() {
        setBackgroundColor(getResources().getColor(R.color.dim_color));
        mButton.setVisibility(VISIBLE);
        if (!isCompleted) {
            hideForegroundHandler.postDelayed(this, FADE_TIME);
        }
        isShowForeground = true;
    }

    public void goPauseMode() {
        mButton.setImageResource(R.drawable.ic_play_video);
        showForeground();
    }

    private void hideForeground() {
        setBackgroundColor(Color.TRANSPARENT);
        fadeOut();
        hideForegroundHandler.removeCallbacks(this);
        isShowForeground = false;
    }

    //callback from Handler
    @Override
    public void run() {
        if (mMediaController.isPlaying()) {
            hideForeground();
        }
    }

    public void setViewVideoView(MediaController.MediaPlayerControl viewVideoView) {
        this.mMediaController = viewVideoView;

        if (mMediaController instanceof VideoView) {
            VideoView delegate = (VideoView) mMediaController;
            delegate.setOnCompletionListener(this);
        } else {
            VideoView delegate = (VideoView) mMediaController;
            delegate.setOnCompletionListener(this);
        }
    }

    //callback form normal mediaplayer
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        isCompleted = true;
        showReplay();
    }

    //callback from advanced mediaplayer
    @Override
    public void onCompletion(net.protyposis.android.mediaplayer.MediaPlayer mp) {
        isCompleted = true;
        showReplay();
    }

    private void showReplay() {
        Log.d(TAG, "show Replay");
        mButton.setImageResource(R.drawable.ic_play_video);
        showForeground();
    }
}
