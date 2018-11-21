package com.bs.videoeditor.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bs.videoeditor.R;
import com.bs.videoeditor.model.VideoModel;
import com.bs.videoeditor.statistic.Statistic;
import com.bs.videoeditor.utils.FileUtil;
import com.bs.videoeditor.utils.Flog;
import com.bs.videoeditor.utils.Utils;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.jlmd.animatedcircleloadingview.AnimatedCircleLoadingView;

import me.itangqi.waveloadingview.WaveLoadingView;

/**
 * Created by ADMIN on 11/21/2018.
 */

public class ProgressSavingFragment extends AbsFragment {
    private FFmpeg ffmpeg;
    private AnimatedCircleLoadingView animatedCircleLoadingView;
    private VideoModel videoModel;
    private String tilte, path;
    private boolean isSuccessCreate = false;
    private String command[];

    public static ProgressSavingFragment newInstance(Bundle bundle) {
        ProgressSavingFragment fragment = new ProgressSavingFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    WaveLoadingView mWaveLoadingView;

    @Override
    public void initViews() {
        ffmpeg = FFmpeg.getInstance(getContext());

        videoModel = getArguments().getParcelable(Statistic.VIDEO_MODEL);
        tilte = getArguments().getString(Statistic.TITLE_VIDEO);
        path = getArguments().getString(Statistic.PATH_VIDEO);
        command = getArguments().getStringArray(Statistic.ARRAY_COMMAND);
        //animatedCircleLoadingView = (AnimatedCircleLoadingView) findViewById(R.id.circle_loading_view);

        // startLoading();
        execFFmpegBinary(command, path, tilte);

        mWaveLoadingView = (WaveLoadingView) findViewById(R.id.waveLoadingView);

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_progress_saving, container, false);
    }

    private void execFFmpegBinary(final String[] command, String path, String title) {
        Log.e("xxx", "cccccccccccccc");
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    Flog.e("Successs     " + s);
                    isSuccessCreate = false;
                }

                @Override
                public void onSuccess(String s) {
                    Flog.e("Failllllllll   " + s);
                    isSuccessCreate = true;
                }

                @Override
                public void onProgress(String s) {
                    Flog.e(s);
                    double durationFile = (int) Utils.getProgress(s, Long.parseLong(videoModel.getDuration()) / 1000) * 1.0;
                    double percent = durationFile / (Double.parseDouble(videoModel.getDuration()) / 1000);
                    Log.e("xxx", " durrrrrr  " + durationFile + "___" + percent * 100);

//                    if (animatedCircleLoadingView != null && percent > 0) {
//                        animatedCircleLoadingView.setPercent((int) percent);
//                    }

                    if (mWaveLoadingView != null && (int) (percent * 100) > 0) {
                        Flog.e("xxx ", "ccccccc       " + (int) percent * 100);
                        if ((int) (percent * 100) >= 100) {
                            mWaveLoadingView.setProgressValue(100);
                            mWaveLoadingView.setCenterTitle("100%");
                        } else {
                            mWaveLoadingView.setProgressValue((int) (percent * 100));
                            mWaveLoadingView.setCenterTitle((int) (percent * 100) + "%");
                        }
                    }

                }

                @Override
                public void onStart() {

                }

                @Override
                public void onFinish() {
                    if (isSuccessCreate) {

                        mWaveLoadingView.setProgressValue(100);
                        mWaveLoadingView.setCenterTitle("100%");

                        FileUtil.addFileToContentProvider(getContext(), path, title);

                        Toast.makeText(getContext(), getString(R.string.create_file) + ": " + path, Toast.LENGTH_SHORT).show();

                        if (isPauseFragment()) {
                            return;
                        }

                        Utils.clearFragment(getFragmentManager());

                        getContext().sendBroadcast(new Intent(Statistic.OPEN_SPEED_STUDIO));
                    } else {
                        animatedCircleLoadingView.resetLoading();
                    }
                }
            });

        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }
}
