package com.bs.videoeditor.fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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
import com.bs.videoeditor.listener.IInputNameFile;
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

import static com.bs.videoeditor.utils.Utils.getFileExtension;

/**
 * Created by Hung on 11/15/2018.
 */

public class CutterFragment extends AbsFragment implements IInputNameFile, VideoControllerView.ICallBackComplete {
    private MyVideoView_Old videoView;
    private VideoTimelineView videoTimelineView;
    private VideoControllerView videoControllerView;
    private String pathOldFile = null, pathNewFile = null;
    private FFmpeg ffmpeg;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
    private VideoModel videoModel;
    private ProgressDialog progressDialog;
    private boolean isSuccessCut = false;
    private Handler handler;

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
        videoControllerView.setListener(this);
        videoControllerView.setViewVideoView(videoView);

        initVideoView();
        initVideoTimeline();
        getToolbar().setTitle(getString(R.string.cutter));
        getToolbar().getMenu().findItem(R.id.item_save).setOnMenuItemClickListener(menuItem -> dialogSelectLocalSaveFile());
    }

    private void save(String fileName) {

        isCancelCut = false;

        int startTime = 0, endTime = 0, durationAudio = 0;

        pathNewFile = Environment.getExternalStorageDirectory().getAbsolutePath() + Statistic.DIR_APP + Statistic.DIR_CUTTER + "/";

        if (!new File(pathNewFile).exists()) {
            new File(pathNewFile).mkdirs();
        }

        pathNewFile = pathNewFile + fileName + getFileExtension(videoModel.getPath());

        File f = new File(pathNewFile);
        if (f.exists()) {
            Toast.makeText(getContext(), getString(R.string.name_file_exist), Toast.LENGTH_SHORT).show();
            return;
        }

        startTime = Math.round(videoTimelineView.getLeftProgress() * videoTimelineView.getVideoLength() / 1000);

        endTime = Math.round(videoTimelineView.getRightProgress() * videoTimelineView.getVideoLength() / 1000);

        durationAudio = endTime - startTime;

        if (durationAudio < 1) {
            Toast.makeText(getContext(), getString(R.string.time_fail), Toast.LENGTH_SHORT).show();
            return;
        }

        String command[] = new String[]{"-i", videoModel.getPath(), "-ss", startTime + "", "-t", String.valueOf(durationAudio), "-c", "copy", pathNewFile};

        initDialogProgress();

        execFFmpegBinary(command, pathNewFile, fileName);

    }

    private DialogInputName dialogInputName;

    private boolean dialogSelectLocalSaveFile() {
        String defaultName = "VC_" + simpleDateFormat.format(System.currentTimeMillis());
        dialogInputName = new DialogInputName(getContext(), this, defaultName, getString(R.string.save));
        dialogInputName.initDialog();
        pauseVideo();
        return true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cutter, container, false);
    }

    private boolean isChangeVideoTimeline = false;

    private void initVideoTimeline() {
        videoTimelineView.clearFrames();
        videoTimelineView.setVideoPath(pathOldFile);
        videoTimelineView.setOnProgressChangeListener((leftChange, currentMili) -> {
            isPlayToEnd = true;
            if (videoView.isPlaying()) {
                videoView.pause();
                videoControllerView.goPauseMode();
            }
            videoView.seekTo((int) currentMili);
        });

        if (true) {
            Flog.e("durrrrr        " + videoTimelineView.getVideoLength() + "___" + videoView.getDuration());
        }

        if (videoTimelineView.getVideoLength() <= 0) {
            Toast.makeText(getContext(), getString(R.string.not_support_this_file), Toast.LENGTH_SHORT).show();
            getActivity().onBackPressed();
        }
    }

    private boolean isPlayToEnd = false;
    private boolean isFirstTime = false;

    private void updateProgress() {
        if (handler == null) {
            handler = new Handler();
        }

        handler.removeCallbacksAndMessages(null);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                Flog.e(" posssss     " + videoView.getCurrentPosition() + "___" + (videoTimelineView.getRightProgress() * 100) * videoView.getDuration() / 100);
                if (videoView.getCurrentPosition() >= (videoTimelineView.getRightProgress() * 100) * videoView.getDuration() / 100) {
                    Flog.e("paissssssssssssssss  ");
                    isPlayToEnd = true;

                    pauseVideo();

                    handler.removeCallbacksAndMessages(null);

                } else {
                    handler.postDelayed(this, 500);
                }
            }
        }, 500);
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

    private boolean isCancelCut = false;

    private void cancelCutter() {
        isCancelCut = true;

        if (ffmpeg.isFFmpegCommandRunning()) {
            ffmpeg.killRunningProcesses();
        }

        if (pathNewFile != null) {
            new File(pathNewFile).delete();
        }

        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void execFFmpegBinary(final String[] command, String path, String title) {
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    Flog.e("Successs     " + s);
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), getString(R.string.can_not_create_file), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(String s) {

                    if (isSuccessCut) return;

                    progressDialog.setProgress(100);
                    progressDialog.dismiss();

                    FileUtil.addFileToContentProvider(getContext(), path, title);

                    Toast.makeText(getContext(), getString(R.string.create_file) + ": " + path, Toast.LENGTH_SHORT).show();

                    if (isPauseFragment()) return;

                    Utils.clearFragment(getFragmentManager());

                    getContext().sendBroadcast(new Intent(Statistic.OPEN_CUTTER_STUDIO));
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

    @Override
    public void onPause() {
        super.onPause();
        pauseVideo();

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onApplySelect(String nameFile) {
        save(nameFile);
    }

    @Override
    public void onCancelSelect() {
        if (dialogInputName == null)
            return;

        dialogInputName.hideDialog();
    }

    @Override
    public void onFileNameEmpty() {
        Toast.makeText(getContext(), getString(R.string.name_file_can_not_empty), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFileNameHasSpecialCharacter() {
        Toast.makeText(getContext(), getString(R.string.name_file_can_not_contain_character), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCompleteVideo() {
        isPlayToEnd = false;
    }

    @Override
    public void onStartVideo() {
        if (videoView != null && null != videoControllerView) {
            videoView.start();
            videoControllerView.startVideo();
            if (isPlayToEnd) {
                Flog.e(" xxx              " + (int) ((videoTimelineView.getLeftProgress() * 100) * videoView.getDuration() / 100));
                videoView.seekTo((int) ((videoTimelineView.getLeftProgress() * 100) * videoView.getDuration() / 100));
                isPlayToEnd = false;
            }
        }
        updateProgress();
    }

    @Override
    public void onPauseVideo() {
        if (videoView != null && null != videoControllerView) {
            videoView.pause();
            videoControllerView.goPauseMode();
        }
    }
}
