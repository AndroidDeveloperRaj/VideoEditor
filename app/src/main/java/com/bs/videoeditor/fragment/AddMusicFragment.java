package com.bs.videoeditor.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bs.videoeditor.R;
import com.bs.videoeditor.model.VideoModel;
import com.bs.videoeditor.statistic.Statistic;
import com.halilibo.bettervideoplayer.BetterVideoCallback;
import com.halilibo.bettervideoplayer.BetterVideoPlayer;

import java.io.File;

/**
 * Created by Hung on 11/15/2018.
 */

public class AddMusicFragment extends AbsFragment implements View.OnClickListener {
    private VideoModel videoModel;
    private BetterVideoPlayer bvp;

    public static AddMusicFragment newInstance(Bundle bundle) {
        AddMusicFragment fragment = new AddMusicFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

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
                //Log.i(TAG, "Started");
            }

            @Override
            public void onPaused(BetterVideoPlayer player) {
                //Log.i(TAG, "Paused");
            }

            @Override
            public void onPreparing(BetterVideoPlayer player) {
                //Log.i(TAG, "Preparing");
            }

            @Override
            public void onPrepared(BetterVideoPlayer player) {
                //Log.i(TAG, "Prepared");
            }

            @Override
            public void onBuffering(int percent) {
                //Log.i(TAG, "Buffering " + percent);
            }

            @Override
            public void onError(BetterVideoPlayer player, Exception e) {
                //Log.i(TAG, "Error " +e.getMessage());
            }

            @Override
            public void onCompletion(BetterVideoPlayer player) {
                //Log.i(TAG, "Completed");
            }

            @Override
            public void onToggleControls(BetterVideoPlayer player, boolean isShowing) {

            }
        });
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
}
