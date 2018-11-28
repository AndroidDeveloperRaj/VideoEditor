package com.bs.videoeditor.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bs.videoeditor.R;
import com.bs.videoeditor.adapter.MusicAdapter;
import com.bs.videoeditor.adapter.VideoAdapter;
import com.bs.videoeditor.model.MusicModel;
import com.bs.videoeditor.statistic.Statistic;
import com.bs.videoeditor.utils.Flog;
import com.bs.videoeditor.utils.SharedPrefs;
import com.bs.videoeditor.utils.Utils;
import com.bs.videoeditor.view.RangeSeekBar;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.google.android.gms.ads.AdView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hung on 11/19/2018.
 */

public class ChooseMusicFragment extends AbsFragment implements MusicAdapter.ItemSelected, View.OnClickListener {
    private List<MusicModel> musicModelList = new ArrayList<>();
    private MusicAdapter musicAdapter;
    private RecyclerView rvAudio;

    int durationAudio;
    private MusicAdapter adapter;
    private File mFile;
    private String mFilename = "record";
    private Handler mHandler;
    private boolean mLoadingKeepGoing;
    private long mLoadingLastUpdateTime;
    private RecyclerView musicList;
    private MediaPlayer mPlayer;
    private ProgressDialog mProgressDialog;
    private String mRecordingFilename;
    private Uri mRecordingUri;
    private TextView tvNameSong;
    private RangeSeekBar rangeSeekbar;
    private SearchView searchView;
    private MusicModel selectedSong;
    private Toolbar toolbar;
    private ImageView ivPrevious, ivNext, ivPlay;
    private MediaPlayer mediaPlayer = null;
    private int position = 0;
    private FFmpeg ffmpeg;
    private String outPath = null;
    private TextView tvAddMusic;
    private boolean isSuccess = false;
    private AdView mAdView;
    private boolean isOnPause = false;
    private Handler handler;
    private TextView tvNoAudio;
    private List<MusicModel> listAllSong = new ArrayList<>();
    private View viewPlay;

    public static ChooseMusicFragment newInstance() {
        Bundle args = new Bundle();
        ChooseMusicFragment fragment = new ChooseMusicFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void initViews() {

        ffmpeg = FFmpeg.getInstance(getContext());

        listAllSong.clear();
        listAllSong.addAll(Utils.getMusicFiles(getContext()));

        musicModelList.clear();
        musicModelList.addAll(listAllSong);
        musicAdapter = new MusicAdapter(getContext(), musicModelList, this);

        rvAudio = (RecyclerView) findViewById(R.id.rvMusicList);
        rvAudio.setHasFixedSize(true);
        rvAudio.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAudio.setAdapter(musicAdapter);

        tvNoAudio = (TextView) findViewById(R.id.tv_no_audio);
        ivNext = (ImageView) findViewById(R.id.iv_next);
        ivPrevious = (ImageView) findViewById(R.id.iv_previous);
        ivPlay = (ImageView) findViewById(R.id.iv_play);
        tvAddMusic = (TextView) findViewById(R.id.tv_add_music);
        tvNameSong = (TextView) findViewById(R.id.tv_name_song);
        rangeSeekbar = (RangeSeekBar) findViewById(R.id.rangeSeekbar);
        viewPlay = findViewById(R.id.lnView);

        ivNext.setOnClickListener(this);
        ivPlay.setOnClickListener(this);
        ivPrevious.setOnClickListener(this);

        hasAudio();

    }

    private void hasAudio() {
        if (musicModelList == null) {
            return;
        }

        if (musicModelList.size() > 0) {
            tvNoAudio.setVisibility(View.GONE);
        } else {
            tvNoAudio.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void initToolbar() {
        super.initToolbar();
        getToolbar().getMenu().clear();
        getToolbar().inflateMenu(R.menu.menu_select_multi_song);
        getToolbar().getMenu().findItem(R.id.item_done).setOnMenuItemClickListener(menuItem -> addMusic());
        searchAudio(getToolbar());
    }

    private void searchAudio(Toolbar toolbar) {
        MenuItem menuItem = toolbar.getMenu().findItem(R.id.item_search);
        searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                // actionSearch(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                actionSearch(s);
                return true;
            }
        });
    }

    private void actionSearch(String s) {
        musicModelList = Utils.filterSong(listAllSong, s);
        musicAdapter.setFilter(musicModelList);
    }

    private boolean addMusic() {
        if (isExistSong()) {
            onSave();
        } else {
            getActivity().onBackPressed();
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        isOnPause = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        pauseMediaPlayer();
    }


    private int STATE_STOP = 0;
    private int STATE_PLAYING = 1;
    private int STATE_PAUSE = 2;
    private int STATE_PLAY_MUSIC = STATE_STOP;

    public void playMusic(int index) {

        if (STATE_PLAY_MUSIC == STATE_PLAYING) {
            pauseMediaPlayer();

        } else if (STATE_PLAY_MUSIC == STATE_PAUSE) {
            startMediaPlayer();
        } else {
            position = index;
            if (position > musicModelList.size() - 1) {
                position = musicModelList.size() - 1;
            }

            selectedSong = musicModelList.get(position);

            mFilename = selectedSong.getFilePath();
            tvNameSong.setText(selectedSong.getTitle());

            rangeSeekbar.setRangeValues(0, Integer.parseInt(String.valueOf(selectedSong.getDuration())));
            rangeSeekbar.setSelectedMinValue(0);
            rangeSeekbar.setSelectedMaxValue(Integer.parseInt(String.valueOf(selectedSong.getDuration())));
            rangeSeekbar.setOnRangeSeekBarChangeListener((bar, minValue, maxValue) -> {
                isChangeSeekbar = true;
                pauseMediaPlayer();
            });

            stopMedia();
            playAudio();
        }
    }

    private boolean isChangeSeekbar = false;

    private void pauseMediaPlayer() {
        if (mediaPlayer == null) {
            return;
        }

        mediaPlayer.pause();

        ivPlay.setImageResource(R.drawable.ic_play_arrow_black_24dp);

        STATE_PLAY_MUSIC = STATE_PAUSE;

    }

    private void startMediaPlayer() {

        if (mediaPlayer == null) return;

        mediaPlayer.start();

        if (isChangeSeekbar) {
            mediaPlayer.seekTo((Integer) rangeSeekbar.getSelectedMinValue());
            isChangeSeekbar = false;
        } else {
            mediaPlayer.start();
        }

        if (isMaxRangerSeekbar) {
            isMaxRangerSeekbar = false;
            mediaPlayer.seekTo((Integer) rangeSeekbar.getSelectedMinValue());
        }

        ivPlay.setImageResource(R.drawable.ic_pause_black_24dp);

        STATE_PLAY_MUSIC = STATE_PLAYING;
    }

    private void playAudio() {
        Flog.e(" xxx  play");
        STATE_PLAY_MUSIC = STATE_PLAYING;

        int timeStart = Integer.parseInt(rangeSeekbar.getSelectedMinValue() + "") / 1000;
        int timeEnd = Integer.parseInt(rangeSeekbar.getSelectedMaxValue() + "") / 1000;
        int timePlay = timeEnd - timeStart;

        if (timePlay > 0) {

            try {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(selectedSong.getFilePath());
                mediaPlayer.prepare();
                mediaPlayer.setOnPreparedListener(mp -> {
                    mediaPlayer.start();
                    mediaPlayer.seekTo((Integer) rangeSeekbar.getSelectedMinValue());
                });

                mediaPlayer.setOnCompletionListener(mp -> {
                    //mediaPlayer.stop();
                    Flog.e(" onComplelteeeeeeeeee");
                    STATE_PLAY_MUSIC = STATE_STOP;
                });

                ivPlay.setImageResource(R.drawable.ic_pause_black_24dp);

            } catch (IOException e) {

                if (position < musicModelList.size() - 1) {
                    position++;
                }

                playMusic(position);
            }


        } else {
            Toast.makeText(getContext(), getString(R.string.time_fail), Toast.LENGTH_SHORT).show();
        }

        if (handler == null) {
            handler = new Handler();
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    if (mediaPlayer.getCurrentPosition() >= (Integer) rangeSeekbar.getSelectedMaxValue()) {
                        ivPlay.setImageResource(R.drawable.ic_play_arrow_black_24dp);
//                        mediaPlayer.stop();
//                        mediaPlayer.reset();
//                        mediaPlayer.release();
//                        mediaPlayer = null;
                        isMaxRangerSeekbar = true;
                        mediaPlayer.pause();
                        STATE_PLAY_MUSIC = STATE_PAUSE;
                    }

                }
                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    private boolean isMaxRangerSeekbar = false;

    private void stopMedia() {

        STATE_PLAY_MUSIC = STATE_STOP;

        ivPlay.setImageResource(R.drawable.ic_play_arrow_black_24dp);

        if (mediaPlayer == null) return;

        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_choose_music, container, false);
    }

    @Override
    public void onClick(int position) {

        STATE_PLAY_MUSIC = STATE_STOP;

        playMusic(position);

        if (viewPlay.getVisibility() == View.GONE) {
            viewPlay.setVisibility(View.VISIBLE);
        }
    }

    private boolean isExistSong() {
        if (musicModelList != null && musicModelList.size() > 0) {
            return true;
        }
        return false;
    }

    private void onSave() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            ivPlay.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        }
        saveRingtone("temp");
    }

    private String makeRingtoneFilename(CharSequence title, String extension) {
        Statistic.TEMP_DIRECTORY_AUDIO.mkdirs();
        File tempFile = new File(Statistic.TEMP_DIRECTORY_AUDIO, title + extension);
        if (tempFile.exists()) {
            Statistic.deleteFile(tempFile);
        }
        return tempFile.getAbsolutePath();
    }

    private void saveRingtone(String title) {
        outPath = makeRingtoneFilename(title + "1", ".mp3");

        int timeStart = Integer.parseInt(rangeSeekbar.getSelectedMinValue() + "");
        int timeEnd = Integer.parseInt(rangeSeekbar.getSelectedMaxValue() + "");

        timeStart = timeStart / 1000;
        timeEnd = timeEnd / 1000;

        durationAudio = (timeEnd - timeStart);
        if (durationAudio > 0) {
            String command[];
            if (musicModelList.get(position).getFilePath().contains(".aac") || musicModelList.get(position).getFilePath().contains(".wav") || musicModelList.get(position).getFilePath().contains(".m4a")) {
                command = new String[]{"-i", musicModelList.get(position).getFilePath(), "-ss", timeStart + "", "-t", String.valueOf(durationAudio), outPath};
            } else {
                command = new String[]{"-i", musicModelList.get(position).getFilePath(), "-ss", timeStart + "", "-t", String.valueOf(durationAudio), "-map", "0:a", "-c", "copy", outPath};
            }

            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setProgressStyle(0);
            mProgressDialog.setTitle(R.string.progress_dialog_saving);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();

            execFFmpegBinary(command);
        } else {
            Toast.makeText(getContext(), getString(R.string.time_fail), Toast.LENGTH_SHORT).show();
        }
    }

    private void execFFmpegBinary(final String[] command) {
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    Flog.d("FAILED with output: " + s);
                }

                @Override
                public void onSuccess(String s) {
                    isSuccess = true;
                    Flog.e("Success " + s);
                }

                @Override
                public void onProgress(String s) {
                    Flog.e(" onpro" + s);
                }


                @Override
                public void onFinish() {
                    Flog.e(" finishhhh  cut   ");
                    if (isSuccess) {
                        mProgressDialog.dismiss();
                        getContext().sendBroadcast(new Intent(Statistic.SEND_PATH_ADD_MUSIC).putExtra(Statistic.PATH_MUSIC, outPath));
                        getActivity().onBackPressed();
                    } else {
                        SharedPrefs.getInstance().put(Statistic.MUSIC, 0);
                        mProgressDialog.dismiss();
                        Toast.makeText(getContext(), getString(R.string.choose_other_file), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_play:
                if (isExistSong()) {
                    playMusic(position);
                }

                break;

            case R.id.iv_next:
                STATE_PLAY_MUSIC = STATE_STOP;
                if (isExistSong()) {
                    if (position < musicModelList.size() - 1) {
                        position++;
                    }
                    playMusic(position);
                }
                break;

            case R.id.iv_previous:
                STATE_PLAY_MUSIC = STATE_STOP;
                if (isExistSong()) {
                    if (position > 0) {
                        position--;
                    }
                    playMusic(position);
                }
                break;
        }
    }
}
