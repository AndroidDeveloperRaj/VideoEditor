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
import com.bsoft.core.AdmobNativeHelper;
import com.halilibo.bettervideoplayer.BetterVideoCallback;
import com.halilibo.bettervideoplayer.BetterVideoPlayer;

import java.io.File;

public class PlayVideoFragment extends AbsFragment {
    private BetterVideoPlayer bvp;

    public static PlayVideoFragment newInstance(Bundle bundle) {
        PlayVideoFragment fragment = new PlayVideoFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private VideoModel videoModel;

    private void loadAdsNative() {
        AdmobNativeHelper admobNativeHelper = new AdmobNativeHelper.Builder(getContext())
                .setParentView((FrameLayout) findViewById(R.id.fl_ad_native))
                .setLayoutAdNative(R.layout.layout_ad_native)
                .setAdNativeId(getString(R.string.admod_native_id))
                .build();

        admobNativeHelper.loadAd();

        admobNativeHelper.setNativeAdListener(new AdmobNativeHelper.OnNativeAdListener() {
            @Override
            public void onNativeAdLoaded() {

            }

            @Override
            public void onAdFailedToLoad(int i) {

            }
        });
    }

    @Override
    public void initViews() {

        Uri uri = Uri.fromFile(new File(videoModel.getPath()));

        bvp = (BetterVideoPlayer) findViewById(R.id.bvp);
        bvp.setAutoPlay(true);
        bvp.setSource(uri);
        bvp.setHideControlsOnPlay(true);
        bvp.enableSwipeGestures(getActivity().getWindow());

        bvp.setCallback(new BetterVideoCallback() {
            @Override
            public void onStop(BetterVideoPlayer player) {

            }

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

        loadAdsNative();
    }

    @Override
    public void initToolbar() {
        super.initToolbar();
        videoModel = getArguments().getParcelable(Statistic.VIDEO_MODEL);
        getToolbar().getMenu().clear();
        getToolbar().setTitle(videoModel.getNameAudio());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_play_video, container, false);
    }
}
