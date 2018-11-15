package com.bs.videoeditor.fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.bs.videoeditor.R;
import com.bs.videoeditor.model.VideoModel;
import com.bs.videoeditor.statistic.Statistic;
import com.bs.videoeditor.utils.FileUtil;
import com.bs.videoeditor.utils.Flog;
import com.bs.videoeditor.utils.Utils;
import com.bs.videoeditor.video.MyVideoView_Old;
import com.bs.videoeditor.video.VideoControllerView;
import com.bs.videoeditor.video.VideoTimelineView;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Hung on 11/15/2018.
 */

public class CutterFragment extends AbsFragment {
    private MyVideoView_Old videoView;
    private VideoTimelineView videoTimelineView;
    private VideoControllerView videoControllerView;
    private String pathOldFile = null, pathNewFile = null;
    private FFmpeg ffmpeg;
    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;
    private EditText edtNameFile;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
    private VideoModel videoModel;
    private ProgressDialog progressDialog;
    private Boolean isSuccessCut = false;

    public static CutterFragment newInstance(Bundle bundle) {
        CutterFragment fragment = new CutterFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void initViews() {

        videoModel = getArguments().getParcelable(Statistic.VIDEO_MODEL);
        pathOldFile = getArguments().getString(Statistic.PATH_VIDEO);
        if (pathOldFile == null) {
            getFragmentManager().popBackStack();
            return;
        }

        ffmpeg = FFmpeg.getInstance(getContext());
        videoTimelineView = (VideoTimelineView) findViewById(R.id.video_timeline);
        videoView = (MyVideoView_Old) findViewById(R.id.video_view);
        videoView.setDependentView(findViewById(R.id.foreground_video));
        videoControllerView = (VideoControllerView) findViewById(R.id.foreground_video);
        videoControllerView.setViewVideoView(videoView);

        initVideoView();
        initVideoTimeline();
        getToolbar().setTitle(getString(R.string.cutter));
        getToolbar().getMenu().findItem(R.id.item_save).setOnMenuItemClickListener(menuItem -> dialogSelectLocalSaveFile());
    }

    private void save() {
        String nameFile = null, extensionFile = null;
        int startTime = 0, endTime = 0, durationAudio = 0;

        nameFile = edtNameFile.getText().toString().trim();
        extensionFile = Utils.getFileExtension(pathOldFile);

        if (FileUtil.isEmpty(nameFile)) {
            Toast.makeText(getContext(), getString(R.string.name_file_can_not_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        if (Utils.isStringHasCharacterSpecial(nameFile)) {
            Toast.makeText(getContext(), getString(R.string.name_file_can_not_contain_character), Toast.LENGTH_SHORT).show();
            return;
        }

        pathNewFile = Environment.getExternalStorageDirectory().getAbsolutePath() + Statistic.DIR_APP + "/";

        if (!new File(pathNewFile).exists()) {
            new File(pathNewFile).mkdirs();
        }

        pathNewFile = pathNewFile + nameFile + extensionFile;

        startTime = Math.round(videoTimelineView.getLeftProgress() * videoTimelineView.getVideoLength() / 1000);
        endTime = Math.round(videoTimelineView.getRightProgress() * videoTimelineView.getVideoLength() / 1000);
        durationAudio = endTime - startTime;

        String command[] = new String[]{"-i", pathOldFile, "-ss", startTime + "", "-t", String.valueOf(durationAudio), "-c", "copy", pathNewFile};

        hideDialogSave();
        initDialogProgress();
        execFFmpegBinary(command, pathNewFile, nameFile);

    }

    private boolean dialogSelectLocalSaveFile() {

        pauseVideo();

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_save_file, null);

        createDialog(view);

        view.findViewById(R.id.btn_local_ok).setOnClickListener(v -> save());
        view.findViewById(R.id.btn_local_cancel).setOnClickListener(v -> alertDialog.dismiss());

        edtNameFile = view.findViewById(R.id.edt_name_file);
        edtNameFile.setText("VC_" + simpleDateFormat.format(System.currentTimeMillis()));
        edtNameFile.setSelection(edtNameFile.getText().length());
        return true;
    }

    private void createDialog(View view) {
        builder = new AlertDialog.Builder(getContext());
        builder.setView(view);
        alertDialog = builder.create();
        alertDialog.show();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cutter, container, false);
    }

    private void initVideoTimeline() {
        videoTimelineView.clearFrames();
        videoTimelineView.setVideoPath(pathOldFile);
        videoTimelineView.setOnProgressChangeListener((leftChange, currentMili) -> {
            if (videoView.isPlaying()) {
                videoView.pause();
                videoControllerView.goPauseMode();
            }

            videoView.seekTo((int) currentMili);

        });
    }

    private void initVideoView() {
        videoView.setVideoPath(pathOldFile);
        videoView.setMediaListener(new MyVideoView_Old.MediaListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onPause() {

            }

            @Override
            public void onSeek(long milisecond) {

            }
        });

        videoView.setOnPreparedListener(mediaPlayer -> {
            videoView.setHandleListener(false);
            videoView.start();
            videoView.pause();
            videoView.setHandleListener(true);
        });

        videoView.setOnErrorListener((mediaPlayer, i, i1) -> true);
    }

    private void initDialogProgress() {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle(getString(R.string.progress_dialog_saving));
        progressDialog.setProgress(0);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), (dialog, which) -> cancelCutter());
        progressDialog.show();
    }

    private void cancelCutter() {
        if (ffmpeg.isFFmpegCommandRunning()) {
            ffmpeg.killRunningProcesses();
        }

        if (pathNewFile != null) {
            new File(pathNewFile).delete();
        }

        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        hideDialogSave();

    }

    private void execFFmpegBinary(final String[] command, String path, String title) {
        Log.e("xxx", "cccccccccccccc");
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    Flog.e("Successs     "+ s);
                    isSuccessCut = false;
                }

                @Override
                public void onSuccess(String s) {
                    Flog.e("Failllllllll   "+ s);
                    isSuccessCut = true;
                }

                @Override
                public void onProgress(String s) {
                    Flog.e(s);
                    int durationFile = (int) Utils.getProgress(s, Long.parseLong(videoModel.getDuration()) / 1000);
                    float percent = durationFile / (Float.parseFloat(videoModel.getDuration()) / 1000);
                    if (progressDialog != null) {
                        progressDialog.setProgress((int) (percent * 100));
                    }
                }

                @Override
                public void onStart() {

                }

                @Override
                public void onFinish() {
                    if (isSuccessCut) {
                        FileUtil.addFileToContentProvider(getContext(), path, title);

                        progressDialog.setProgress(100);

                        Toast.makeText(getContext(), getString(R.string.create_file) + ": " + path, Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();

        }
    }

    private void pauseVideo() {
        if (videoView != null && null != videoControllerView) {
            videoView.pause();
            videoControllerView.goPauseMode();
        }
    }

    private void hideDialogSave() {
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        pauseVideo();
    }
}
