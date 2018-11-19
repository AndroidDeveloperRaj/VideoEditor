package com.bs.videoeditor.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.bs.videoeditor.R;
import com.bs.videoeditor.model.VideoModel;
import com.bs.videoeditor.statistic.Statistic;
import com.bs.videoeditor.utils.Flog;
import com.halilibo.bettervideoplayer.BetterVideoCallback;
import com.halilibo.bettervideoplayer.BetterVideoPlayer;

import java.io.File;
import java.io.IOException;

/**
 * Created by Hung on 11/15/2018.
 */

public class AddMusicFragment extends AbsFragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
    private VideoModel videoModel;
    private BetterVideoPlayer bvp;
    private SeekBar sbVolumeVideo, sbVolumeMusic;
    private ImageView ivAddMusic;
    private String pathMusicAdd = null;

    public static AddMusicFragment newInstance(Bundle bundle) {
        AddMusicFragment fragment = new AddMusicFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                return;
            }

            switch (intent.getAction()) {
                case Statistic.SEND_PATH_ADD_MUSIC:
                    pathMusicAdd = intent.getStringExtra(Statistic.PATH_MUSIC);
                    Flog.e(" pathhhhh adddddd   " + pathMusicAdd);
                    break;
            }
        }
    };

    @Override
    public void initViews() {
        videoModel = getArguments().getParcelable(Statistic.VIDEO_MODEL);
        Uri uri = Uri.fromFile(new File(videoModel.getPath()));

        findViewById(R.id.view2).setOnClickListener(this);
        findViewById(R.id.view3).setOnClickListener(this);

        bvp = (BetterVideoPlayer) findViewById(R.id.bvp);
        bvp.setAutoPlay(false);
        bvp.setSource(uri);
        bvp.setHideControlsOnPlay(true);
        bvp.setBottomProgressBarVisibility(false);
        bvp.enableSwipeGestures(getActivity().getWindow());

        bvp.setCallback(new BetterVideoCallback() {
            @Override
            public void onStarted(BetterVideoPlayer player) {
                Flog.e("Started");
            }

            @Override
            public void onPaused(BetterVideoPlayer player) {
                Flog.e("Paused");
            }

            @Override
            public void onPreparing(BetterVideoPlayer player) {
                Flog.e("Preparing");
            }

            @Override
            public void onPrepared(BetterVideoPlayer player) {
                Flog.e("Prepared");
                if (pathMusicAdd == null) {
                    return;
                }

                playAudio();
            }

            @Override
            public void onBuffering(int percent) {
                Flog.e("Buffering " + percent);
            }

            @Override
            public void onError(BetterVideoPlayer player, Exception e) {
                Flog.e("Error " + e.getMessage());
            }

            @Override
            public void onCompletion(BetterVideoPlayer player) {
                //Log.i(TAG, "Completed");
            }

            @Override
            public void onToggleControls(BetterVideoPlayer player, boolean isShowing) {

            }
        });


        sbVolumeVideo = (SeekBar) findViewById(R.id.seekbar_volume);
        sbVolumeMusic = (SeekBar) findViewById(R.id.seekbar_music);
        sbVolumeMusic.setMax(100);
        sbVolumeVideo.setMax(100);
        sbVolumeVideo.setOnSeekBarChangeListener(this);
        sbVolumeMusic.setOnSeekBarChangeListener(this);

        ivAddMusic = (ImageView) findViewById(R.id.iv_add_music);
        ivAddMusic.setOnClickListener(v -> addMusic());

        initActions();

    }

    private MediaPlayer mediaPlayer;

    private void playAudio() {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(pathMusicAdd);
            mediaPlayer.prepare();
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void initActions() {
        IntentFilter it = new IntentFilter();
        it.addAction(Statistic.SEND_PATH_ADD_MUSIC);
        getContext().registerReceiver(receiver, it);
    }

    @Override
    public void onDestroy() {
        getContext().unregisterReceiver(receiver);
        super.onDestroy();
    }

    private void addMusic() {
        getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.animation_left_to_right
                        , R.anim.animation_right_to_left
                        , R.anim.animation_left_to_right
                        , R.anim.animation_right_to_left)
                .add(R.id.view_container, ChooseMusicFragment.newInstance())
                .addToBackStack(null)
                .commit();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_music, container, false);
    }

    @Override
    public void initToolbar() {
        super.initToolbar();
        getToolbar().setTitle(getString(R.string.add_music));
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        switch (seekBar.getId()) {
            case R.id.seekbar_volume:
                Flog.e(" prrrrrrrrrrrrr   " + seekBar.getProgress());
                bvp.setVolume(seekBar.getProgress(), seekBar.getProgress());

                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mediaPlayer.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
    }
}
