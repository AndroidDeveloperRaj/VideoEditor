package com.bs.videoeditor.fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Hung on 11/15/2018.
 */

public class SpeedFragment extends AbsFragment implements IInputNameFile {
    private DialogInputName dialogInputName;
    private FFmpeg ffmpeg;
    private VideoModel videoModel;
    private ProgressDialog progressDialog;
    private boolean isSuccessCreate = false;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);

    @Override
    public void initViews() {

        String[] complexCommand = {"-y", "-i", oldPath, "-filter_complex", "[0:v]setpts=2.0*PTS[v];[0:a]atempo=0.5[a]", "-map", "[v]", "-map", "[a]", "-b:v", "2097k", "-r", "60", "-vcodec", "mpeg4", newPath};

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
//        String[] complexCommand = {"-y", "-i", yourRealPath, "-filter_complex", "[0:v]setpts=0.5*PTS[v];[0:a]atempo=2.0[a]", "-map", "[v]", "-map", "[a]", "-b:v", "2097k", "-r", "60", "-vcodec", "mpeg4", filePath};
        String[] complexCommand = {"-i", videoModel.getPath(), "-filter_complex", "[0:v]setpts=0.75*PTS[v];[0:a]atempo=1.5[a]", "-map", "[v]", "-map", "[a]", "-b:v", "2097k", "-r", "60", "-vcodec", "mpeg4", newPath};
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
                    int durationFile = (int) Utils.getProgress(s, Long.parseLong(videoModel.getDuration()) / 1000);
                    float percent = durationFile / (Float.parseFloat(videoModel.getDuration()) / 1000);
                    if (progressDialog != null) {
                        if ((int) percent * 100 > 0) {
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
}
