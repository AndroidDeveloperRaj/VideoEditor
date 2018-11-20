package com.bs.videoeditor.fragment;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.bs.videoeditor.R;
import com.bs.videoeditor.adapter.SimpleItemTouchHelperCallback;
import com.bs.videoeditor.adapter.SortAdapter;
import com.bs.videoeditor.listener.IInputNameFile;
import com.bs.videoeditor.listener.IListSongChanged;
import com.bs.videoeditor.model.VideoModel;
import com.bs.videoeditor.statistic.Statistic;
import com.bs.videoeditor.utils.FileUtil;
import com.bs.videoeditor.utils.Flog;
import com.bs.videoeditor.utils.Keys;
import com.bs.videoeditor.utils.Utils;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.bs.videoeditor.statistic.Statistic.EXTENSION_MP4;


public class SortMergerFragment extends AbsFragment implements IListSongChanged, SortAdapter.OnStartDragListener, IInputNameFile {
    private List<VideoModel> videoModelList = new ArrayList<>();
    private RecyclerView rvAudio;
    private SortAdapter audioAdapter;
    private Toolbar toolbar;
    private ItemTouchHelper.Callback callback;
    private ItemTouchHelper itemTouchHelper;
    private FFmpeg ffmpeg;
    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;
    private EditText edtNameFile;
    private long duration;
    private ProgressDialog progressDialog;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
    private String path;
    private boolean isCancelSaveFile = false;
    private boolean isSuccess = false;

    public static SortMergerFragment newInstance(Bundle bundle) {
        SortMergerFragment fragment = new SortMergerFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onDestroy() {
        Utils.closeKeyboard(getActivity());
        super.onDestroy();
    }

    @Override
    public void initViews() {

        ffmpeg = FFmpeg.getInstance(getContext());

        duration = getArguments().getLong(Statistic.DURATION);
        videoModelList.clear();
        videoModelList.addAll(getArguments().getParcelableArrayList(Statistic.LIST_VIDEO));
        audioAdapter = new SortAdapter(videoModelList, getContext(), this, this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        rvAudio = (RecyclerView) findViewById(R.id.rv_audio);
        rvAudio.setHasFixedSize(true);
        rvAudio.setLayoutManager(linearLayoutManager);
        rvAudio.setAdapter(audioAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvAudio.getContext(),
                linearLayoutManager.getOrientation());

        rvAudio.addItemDecoration(dividerItemDecoration);

        callback = new SimpleItemTouchHelperCallback(audioAdapter);

        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(rvAudio);

        initToolbar();
        loadFFMpegBinary();

    }

    private void loadFFMpegBinary() {
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {

                }
            });
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }

        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

            });
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }
    }


    public void initToolbar() {
        super.initToolbar();
        getToolbar().setTitle(R.string.sort);
        getToolbar().getMenu().clear();
        getToolbar().inflateMenu(R.menu.menu_merger);
        getToolbar().getMenu().findItem(R.id.item_ads).setOnMenuItemClickListener(item -> dialogSelectLocalSaveFile());
    }

    public static void appendVideoLog(String text) {
        if (!Keys.TEMP_DIRECTORY.exists()) {
            Keys.TEMP_DIRECTORY.mkdirs();
        }
        File logFile = new File(Keys.TEMP_DIRECTORY, "video.txt");
        //Log.d("FFMPEG", "File append " + text);
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }


    private void initDialogProgress() {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle(getString(R.string.progress_dialog_saving));
        progressDialog.setProgress(0);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), (dialog, which) -> cancelMerger());
        progressDialog.show();
    }

    private void cancelMerger() {
        isCancelSaveFile = true;
        ffmpeg.killRunningProcesses();
        new File(path).delete();
    }

    public static final String[] listSpecialCharacter = new String[]{"%", "/", "#", "^", ":", "?", ","};

    private boolean mergerAudio(String nameFile) {

        boolean isAbleMerger = true;

        for (VideoModel videoModel : videoModelList) {
            Flog.e("exxxxxxxxxxxxxxx   " + Utils.getFileExtension(videoModel.getPath()));
            if (!Utils.getFileExtension(videoModel.getPath()).equals(EXTENSION_MP4)) {
                isAbleMerger = false;
            }
        }

        if (isAbleMerger) {

            if (!nameFile.isEmpty()) {

                path = Environment.getExternalStorageDirectory().getAbsolutePath() + Statistic.DIR_APP + Statistic.DIR_MERGER + "/";

                File f = new File(path);
                if (!f.exists()) {
                    f.mkdirs();
                }

                path = path + nameFile + EXTENSION_MP4;

                if (new File(path).exists()) {
                    Toast.makeText(getContext(), getString(R.string.file), Toast.LENGTH_SHORT).show();

                } else {

                    isCancelSaveFile = false;

                    initDialogProgress();

                    // delete last list path file
                    new File(Keys.TEMP_DIRECTORY, "video.txt").delete();

                    // add list path file to file txt
                    for (VideoModel videoModel : videoModelList) {
                        appendVideoLog(String.format("file '%s'", videoModel.getPath()));
                    }

                    File listFile = new File(Keys.TEMP_DIRECTORY, "video.txt");

                    String command[] = new String[]{"-f", "concat", "-safe", "0", "-i", listFile.getAbsolutePath(), "-c", "copy", path};

                    execFFmpegBinary(command, path, nameFile);

                }
            } else {
                Toast.makeText(getContext(), getString(R.string.name_file_can_not_empty), Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(getContext(), getString(R.string.support_mp4), Toast.LENGTH_SHORT).show();
            getFragmentManager().popBackStack();
        }

        if (alertDialog != null) {
            alertDialog.dismiss();
        }

        return true;

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sort, container, false);
    }


    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    private void createDialog(View view) {
        builder = new AlertDialog.Builder(getContext());
        builder.setView(view);
        alertDialog = builder.create();
        alertDialog.show();
    }


    private DialogInputName dialogInputName;

    private boolean dialogSelectLocalSaveFile() {


        if (videoModelList.size() >= 2) {

            String nameDefault = "VM_" + simpleDateFormat.format(System.currentTimeMillis());
            dialogInputName = new DialogInputName(getContext(), this, nameDefault);
            dialogInputName.initDialog();

        } else {

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AppCompatAlertDialogStyle);
            builder.setTitle(getResources().getString(R.string.error));
            builder.setMessage(getString(R.string.you_need_to_have));
            builder.setPositiveButton(getResources().getString(R.string.lib_crs_yes), (dialog, id) -> {
                dialog.dismiss();
                getFragmentManager().popBackStack();
            });
            builder.setNegativeButton(getResources().getString(R.string.lib_crs_no), (dialog, id) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();

        }

        return true;
    }

    private boolean isError = false;

    private void execFFmpegBinary(final String[] command, String path, String title) {
        Flog.e("xxx", "cccccccccccccc");
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    isSuccess = false;
                    Flog.e("xxx", "FAILED with output: " + s);
                }


                @Override
                public void onSuccess(String s) {
                    isSuccess = true;
                    Flog.e("Success " + s);

                }

                @Override
                public void onProgress(String s) {
                    Flog.e(s);

                    try {
                        int durationFile = (int) Utils.getProgress(s, duration / 1000);

                        float percent = durationFile / (float) (duration / 1000);

                        if (progressDialog != null) {
                            if (percent * 100 > 0) {
                                progressDialog.setProgress((int) (percent * 100));
                            }
                        }
                    } catch (ArithmeticException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onStart() {

                }

                @Override
                public void onFinish() {
                    Flog.e("Success finish ");
                    if (isCancelSaveFile) {
                        return;
                    }

                    if (isError) {
                        return;
                    }

                    if (isSuccess) {
                        isSuccess = false;

                        FileUtil.addFileToContentProvider(getContext(), path, title);

                        Toast.makeText(getContext(), getString(R.string.create_file) + path, Toast.LENGTH_SHORT).show();

                        progressDialog.setProgress(100);

                        new Handler().postDelayed(() -> progressDialog.dismiss(), 500);

                        if (isPauseFragment()) {
                            return;
                        }

                        if (getFragmentManager() == null) {
                            return;
                        }

                        Utils.clearFragment(getFragmentManager());

                        getContext().sendBroadcast(new Intent(Statistic.OPEN_MERGER_STUDIO));

                    } else {

                        new File(path).delete();

                        Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();

                        if (getFragmentManager() != null) {
                            getFragmentManager().popBackStack();
                        }

                        if (progressDialog != null) {
                            progressDialog.dismiss();
                        }
                    }
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNoteListChanged(List<VideoModel> videoModelList) {

    }

    @Override
    public void onApplySelect(String nameFile) {
        mergerAudio(nameFile);
    }

    @Override
    public void onCancelSelect() {
        if (dialogInputName == null) {
            return;
        }

        dialogInputName.hideDialog();
    }

    @Override
    public void onFileNameEmpty() {
        Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFileNameHasSpecialCharacter() {
        Toast.makeText(getContext(), getString(R.string.name_file_can_not_contain_character), Toast.LENGTH_SHORT).show();
    }
}
