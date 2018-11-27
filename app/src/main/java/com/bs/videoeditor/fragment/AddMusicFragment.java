package com.bs.videoeditor.fragment;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
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
import com.halilibo.bettervideoplayer.BetterVideoProgressCallback;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static com.bs.videoeditor.utils.Utils.getFileExtension;

/**
 * Created by Hung on 11/15/2018.
 */

public class AddMusicFragment extends AbsFragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, IInputNameFile {
    private VideoModel videoModel;
    private BetterVideoPlayer bvp;
    private SeekBar sbVolumeVideo, sbVolumeMusic;
    private ImageView ivAddMusic;
    private String pathMusicAdd = null;
    private MediaPlayer mediaPlayer;
    private Uri uriVideo;
    private static final int MAX_VOLUME = 100;
    private FFmpeg ffmpeg;
    private float volumeVideo = 1.0f, volumeMusic = 1.0f;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
    private DialogInputName dialogInputName;
    private TextView tvProgressVideo, tvProgressMusic;
    private boolean isAddMusicSuccess = false;
    private ProgressDialog progressDialog;

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
                    bvp.stop();
                    bvp.reset();
                    if (!bvp.isPrepared()) {
                        bvp.prepare();
                    }
                    initMusic();
                    break;

            }
        }
    };

    private void initMusic() {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(pathMusicAdd);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void releaseVideo() {
        if (bvp == null) return;
        bvp.stop();
    }

    private boolean isCompleteVideo = false;

    private void initVideo(boolean isReset) {

        bvp.setAutoPlay(false);
        bvp.setSource(uriVideo);
        bvp.setHideControlsOnPlay(true);
        bvp.setBottomProgressBarVisibility(false);
        bvp.enableSwipeGestures(getActivity().getWindow());

        bvp.setCallback(new BetterVideoCallback() {
            @Override
            public void onStop(BetterVideoPlayer player) {
                Flog.e(" Stop video ");
                if (mediaPlayer == null) {
                    return;
                }
                mediaPlayer.stop();
            }

            @Override
            public void onStarted(BetterVideoPlayer player) {
                Flog.e("Started");
                if (mediaPlayer == null) {
                    return;
                }

                try {

                    mediaPlayer.start();

                    if (isCompleteVideo) {
                        mediaPlayer.seekTo(0);
                    }

                } catch (IllegalStateException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onPaused(BetterVideoPlayer player) {
                Flog.e("Paused");
                if (mediaPlayer == null) {
                    return;
                }

                try {
                    mediaPlayer.pause();
                } catch (IllegalStateException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onPreparing(BetterVideoPlayer player) {
                Flog.e("Preparing");
            }

            @Override
            public void onPrepared(BetterVideoPlayer player) {
                Flog.e("Prepared");

                try {

                    if (mediaPlayer == null) return;

                    mediaPlayer.prepare();

                } catch (IOException e) {
                    e.printStackTrace();
                }
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

                isCompleteVideo = true;

                if (mediaPlayer == null) {
                    return;
                }
                try {
                    mediaPlayer.pause();
                } catch (IllegalStateException ex) {
                    ex.printStackTrace();
                }

            }

            @Override
            public void onToggleControls(BetterVideoPlayer player, boolean isShowing) {

            }

            @Override
            public void onSeekbarProgressChanged(int position) {
                if (mediaPlayer == null) return;

                if (position < mediaPlayer.getDuration()) {
                    mediaPlayer.seekTo(position);
                }
            }
        });
//        bvp.setProgressCallback(new BetterVideoProgressCallback() {
//            @Override
//            public void onVideoProgressUpdate(int position, int duration) {
//                Flog.e(" preeeeeeeeeeee       " + position + "___" + duration);
//            }
//        });
    }

    @Override
    public void initViews() {
        ffmpeg = FFmpeg.getInstance(getContext());
        bvp = (BetterVideoPlayer) findViewById(R.id.bvp);
        tvProgressMusic = (TextView) findViewById(R.id.tv_music_pecent);
        tvProgressVideo = (TextView) findViewById(R.id.tv_video_pecent);
        sbVolumeVideo = (SeekBar) findViewById(R.id.seekbar_volume);
        sbVolumeMusic = (SeekBar) findViewById(R.id.seekbar_music);

        ivAddMusic = (ImageView) findViewById(R.id.iv_add_music);
        ivAddMusic.setOnClickListener(v -> addMusic());

        sbVolumeMusic.setMax(MAX_VOLUME);
        sbVolumeVideo.setMax(MAX_VOLUME);

        sbVolumeVideo.setProgress(MAX_VOLUME);
        sbVolumeMusic.setProgress(MAX_VOLUME);

        sbVolumeVideo.setOnSeekBarChangeListener(this);
        sbVolumeMusic.setOnSeekBarChangeListener(this);

        findViewById(R.id.view2).setOnClickListener(this);
        findViewById(R.id.view3).setOnClickListener(this);

        videoModel = getArguments().getParcelable(Statistic.VIDEO_MODEL);
        uriVideo = Uri.fromFile(new File(videoModel.getPath()));

        initVideo(false);
        initActions();


    }

    private void initActions() {
        IntentFilter it = new IntentFilter();
        it.addAction(Statistic.SEND_PATH_ADD_MUSIC);
        getContext().registerReceiver(receiver, it);
    }

    @Override
    public void onDestroy() {
        getContext().unregisterReceiver(receiver);
        if (bvp != null) {
            bvp.stop();
            bvp.reset();
            bvp.release();
        }

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
        }
        super.onDestroy();
    }

    private void stopVideoAudio() {
        if (bvp != null) {
            bvp.stop();
            bvp.reset();
        }
    }

    private void addMusic() {

        bvp.pause();

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
        getToolbar().getMenu().findItem(R.id.item_save).setOnMenuItemClickListener(menuItem -> {
            if (bvp == null || pathMusicAdd == null) {
                Toast.makeText(getContext(), getString(R.string.you_not_add_music), Toast.LENGTH_SHORT).show();
                return true;
            }

            bvp.pause();

            initDialogSaveFile();

            return true;
        });
    }

    private void initDialogSaveFile() {
        String defaultName = "VA_" + simpleDateFormat.format(System.currentTimeMillis());
        dialogInputName = new DialogInputName(getContext(), this, defaultName, getString(R.string.save));
        dialogInputName.initDialog();
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        switch (seekBar.getId()) {

            case R.id.seekbar_volume:

                volumeVideo = (float) sbVolumeVideo.getProgress() / 100;
                tvProgressVideo.setText(sbVolumeVideo.getProgress() + "%");

                if (bvp == null) {
                    return;
                }

                bvp.setVolume(volumeVideo, volumeVideo);
                break;

            case R.id.seekbar_music:

                volumeMusic = (float) sbVolumeMusic.getProgress() / 100;
                tvProgressMusic.setText(sbVolumeMusic.getProgress() + "%");

                if (mediaPlayer == null) {
                    return;
                }

                mediaPlayer.setVolume(volumeMusic, volumeMusic);
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
    public void onStop() {
        super.onStop();
        bvp.pause();
    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        Flog.e(" prepare  media  ");
        bvp.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mediaPlayer.pause();
    }

    @Override
    public void onApplySelect(String nameFile) {
        saveFileAddMusic(nameFile);
    }

    String pathNewFile = null;

    private void saveFileAddMusic(String nameFile) {
        String pathAudio = null;
        pathNewFile = Environment.getExternalStorageDirectory().getAbsolutePath() + Statistic.DIR_APP + Statistic.DIR_ADD_MUSIC + "/";

        if (!new File(pathNewFile).exists()) {
            new File(pathNewFile).mkdirs();
        }

        pathNewFile = pathNewFile + nameFile + getFileExtension(videoModel.getPath());

        File f = new File(pathNewFile);
        if (f.exists()) {
            Toast.makeText(getContext(), getString(R.string.name_file_exist), Toast.LENGTH_SHORT).show();
            return;
        }

        String command[] = new String[]{"-i", videoModel.getPath(), "-i",
                pathMusicAdd, "-filter_complex", "[0:a]volume=" + volumeVideo + "[a0];[1:a]volume=" + volumeMusic + "[a1];[a0][a1]amix=inputs=2[a]",
                "-map", "0:v", "-map", "[a]", "-c:v", "copy", "-c:a", "aac", pathNewFile};

        initDialogProgress();

        execFFmpegBinary(command, pathNewFile, nameFile);

    }

    private void execFFmpegBinary(final String[] command, String path, String title) {
        Log.e("xxx", "cccccccccccccc");
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    Flog.e("Failllllllll   " + s);
                    Toast.makeText(getContext(), getString(R.string.can_not_create_file), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(String s) {
                    Flog.e("Successs     " + s);

                    progressDialog.setProgress(100);
                    progressDialog.dismiss();

                    FileUtil.addFileToContentProvider(getContext(), path, title);

                    Toast.makeText(getContext(), getString(R.string.create_file) + ": " + path, Toast.LENGTH_SHORT).show();

                    if (isPauseFragment()) {
                        return;
                    }

                    Utils.clearFragment(getFragmentManager());

                    getContext().sendBroadcast(new Intent(Statistic.OPEN_ADD_MUSIC_STUDIO));

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

    private void initDialogProgress() {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle(getString(R.string.progress_dialog_saving));
        progressDialog.setProgress(0);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), (dialog, which) -> cancelAddMusic());
        progressDialog.show();
    }

    private void cancelAddMusic() {
        if (ffmpeg.isFFmpegCommandRunning()) {
            ffmpeg.killRunningProcesses();
        }

        if (pathNewFile != null) {
            new File(pathNewFile).delete();
        }

        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        dialogInputName.hideDialog();
    }


    @Override
    public void onCancelSelect() {
        if (dialogInputName != null) {
            dialogInputName.hideDialog();
        }
    }

    @Override
    public void onFileNameEmpty() {
        Toast.makeText(getContext(), getString(R.string.name_file_can_not_empty), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFileNameHasSpecialCharacter() {
        Toast.makeText(getContext(), getString(R.string.name_file_can_not_contain_character), Toast.LENGTH_SHORT).show();
    }
}
