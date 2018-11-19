package com.bs.videoeditor.fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bs.videoeditor.R;
import com.bs.videoeditor.listener.IInputNameFile;
import com.bs.videoeditor.model.VideoModel;
import com.bs.videoeditor.statistic.Statistic;
import com.bs.videoeditor.utils.FileUtil;
import com.bs.videoeditor.utils.Flog;
import com.bs.videoeditor.utils.Utils;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.halilibo.bettervideoplayer.BetterVideoCallback;
import com.halilibo.bettervideoplayer.BetterVideoPlayer;
import com.xw.repo.BubbleSeekBar;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Hung on 11/15/2018.
 */

public class SpeedFragment extends AbsFragment implements IInputNameFile, com.bs.videoeditor.custom.BubbleSeekBar.OnProgressChangedListener {
    private DialogInputName dialogInputName;
    private FFmpeg ffmpeg;
    private VideoModel videoModel;
    private ProgressDialog progressDialog;
    private TextView tvShowSpeed;
    private boolean isSuccessCreate = false;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
    private BetterVideoPlayer bvp;
    private com.bs.videoeditor.custom.BubbleSeekBar seekBar;
    private float tempoVideo = 1.0f, ptsVideo = 1.0f;
    private float listTempoAudio[] = new float[]{ 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f};
    private float listPtsVideo[] = new float[]{ 2.0f, 4 / 3f, 1.0f, 4 / 5f, 4 / 6f, 4 / 7f, 0.5f};

    @Override
    public void initViews() {
        tvShowSpeed = (TextView) findViewById(R.id.tv_show_speed);
        seekBar = (com.bs.videoeditor.custom.BubbleSeekBar) findViewById(R.id.demo_3_seek_bar_1);
        seekBar.setOnProgressChangedListener(this);
        seekBar.getConfigBuilder()
                .min(1)
                .max(7)
                .progress(3)
                .sectionCount(6)
                .trackColor(Color.WHITE)
                .secondTrackColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
                .thumbColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
                .bubbleTextColor(ContextCompat.getColor(getContext(), android.R.color.transparent))
                .sectionTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary))
                .sectionTextSize(18)
                .bubbleColor(ContextCompat.getColor(getContext(), android.R.color.transparent))
                .bubbleTextSize(18)
                .showSectionMark()
                .seekStepSection()
                .touchToSeek()
                .build();

        videoModel = getArguments().getParcelable(Statistic.VIDEO_MODEL);
        Uri uri = Uri.fromFile(new File(videoModel.getPath()));

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
//        String[] complexCommand = {"-y", "-i", oldPath, "-filter_complex", "[0:v]setpts=2.0*PTS[v];[0:a]atempo=0.5[a]", "-map", "[v]", "-map", "[a]", "-b:v", "2097k", "-r", "60", "-vcodec", "mpeg4", newPath};

    }

    private boolean dialogLocalSave() {
        String defaultName = "VS_" + simpleDateFormat.format(System.currentTimeMillis());
        dialogInputName = new DialogInputName(getContext(), this, defaultName);
        dialogInputName.initDialog();
        return true;
    }

    public static SpeedFragment newInstance(Bundle bundle) {
        SpeedFragment fragment = new SpeedFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private String newPath = null, oldPath = null;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_speed, container, false);

    }

    @Override
    public void initToolbar() {
        super.initToolbar();
        ffmpeg = FFmpeg.getInstance(getContext());
        videoModel = getArguments().getParcelable(Statistic.VIDEO_MODEL);
        getToolbar().setTitle(getString(R.string.speed));
        getToolbar().getMenu().findItem(R.id.item_save).setOnMenuItemClickListener(menuItem -> dialogLocalSave());
    }

    @Override
    public void onApplySelect(String nameFile) {
        saveFile(nameFile);
    }

    private void saveFile(String nameFile) {
        String extensionFile = null;

        if (FileUtil.isEmpty(nameFile)) {
            Toast.makeText(getContext(), getString(R.string.name_file_can_not_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        if (Utils.isStringHasCharacterSpecial(nameFile)) {
            Toast.makeText(getContext(), getString(R.string.name_file_can_not_contain_character), Toast.LENGTH_SHORT).show();
            return;
        }

        newPath = Environment.getExternalStorageDirectory().getAbsolutePath() + Statistic.DIR_APP + Statistic.DIR_SPEED + "/";

        if (!new File(newPath).exists()) {
            new File(newPath).mkdirs();
        }

        extensionFile = Utils.getFileExtension(videoModel.getPath());

        newPath = newPath + nameFile + extensionFile;

        Flog.e(" ppppppppppp   " + newPath);

        if (new File(newPath).exists()) {
            dialogInputName.hideDialog();
            Toast.makeText(getContext(), getString(R.string.name_file_exist), Toast.LENGTH_SHORT).show();
            return;
        }
//
//        if (true) {
//
//            Log.e("xxx ", " pts     " + ptsVideo + "___" + tempoVideo);
////            return;
//        }

        String sSpeed = "[0:v]setpts=" + ptsVideo + "*PTS[v];[0:a]atempo=" + tempoVideo + "[a]";
//        String[] complexCommand = {"-y", "-i", yourRealPath, "-filter_complex", "[0:v]setpts=0.5*PTS[v];[0:a]atempo=2.0[a]", "-map", "[v]", "-map", "[a]", "-b:v", "2097k", "-r", "60", "-vcodec", "mpeg4", filePath};
        String[] complexCommand = {"-i", videoModel.getPath(), "-filter_complex", sSpeed, "-map", "[v]", "-map", "[a]", "-b:v", "2097k", "-r", "60", "-vcodec", "mpeg4", newPath};
        initDialogProgress();
        execFFmpegBinary(complexCommand, newPath, nameFile);
    }

    private void initDialogProgress() {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle(getString(R.string.progress_dialog_saving));
        progressDialog.setProgress(0);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), (dialog, which) -> cancelCreateFile());
        progressDialog.show();
    }

    private void cancelCreateFile() {
        if (ffmpeg.isFFmpegCommandRunning()) {
            ffmpeg.killRunningProcesses();
        }

        if (newPath != null) {
            new File(newPath).delete();
        }

        if (progressDialog != null) {
            progressDialog.dismiss();
        }
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
                    double durationFile = (int) Utils.getProgress(s, Long.parseLong(videoModel.getDuration()) / 1000) * tempoVideo;
                    double percent = durationFile / (Double.parseDouble(videoModel.getDuration()) / 1000);
                    Log.e("xxx", " durrrrrr  " + durationFile + "___" + percent * 100);
                    if (progressDialog != null) {
                        if ((int)(percent * 100) > 0) {
                            progressDialog.setProgress((int) (percent * 100));
                        }
                    }
                }

                @Override
                public void onStart() {

                }

                @Override
                public void onFinish() {
                    if (isSuccessCreate) {
                        progressDialog.setProgress(100);
                        progressDialog.dismiss();

                        FileUtil.addFileToContentProvider(getContext(), path, title);

                        Toast.makeText(getContext(), getString(R.string.create_file) + ": " + path, Toast.LENGTH_SHORT).show();

                        if (isPauseFragment()) {
                            return;
                        }

                        Utils.clearFragment(getFragmentManager());

                        getContext().sendBroadcast(new Intent(Statistic.OPEN_SPEED_STUDIO));
                    }
                }
            });

        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onCancelSelect() {
        if (dialogInputName != null) {
            dialogInputName.hideDialog();
        }
    }

    @Override
    public void onProgressChanged(com.bs.videoeditor.custom.BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
        tvShowSpeed.setText(listTempoAudio[progress - 1] + "x");
        tempoVideo = listTempoAudio[progress - 1];
        ptsVideo = listPtsVideo[progress - 1];
    }

    @Override
    public void getProgressOnActionUp(com.bs.videoeditor.custom.BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

    }

    @Override
    public void getProgressOnFinally(com.bs.videoeditor.custom.BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {

    }
}
