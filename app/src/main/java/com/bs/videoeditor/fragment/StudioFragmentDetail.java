package com.bs.videoeditor.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.bs.videoeditor.BuildConfig;
import com.bs.videoeditor.R;
import com.bs.videoeditor.activity.MainActivity;
import com.bs.videoeditor.adapter.VideoAdapter;
import com.bs.videoeditor.adapter.VideoStudioAdapter;
import com.bs.videoeditor.listener.IInputNameFile;
import com.bs.videoeditor.model.VideoModel;
import com.bs.videoeditor.statistic.Statistic;
import com.bs.videoeditor.utils.FileUtil;
import com.bs.videoeditor.utils.Flog;
import com.bs.videoeditor.utils.Utils;


import net.protyposis.android.mediaplayer.VideoView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.bs.videoeditor.utils.FileUtil.deleteAudio;
import static com.bs.videoeditor.utils.FileUtil.renameContentProvider;
import static com.bs.videoeditor.utils.Utils.closeKeyboard;
import static com.bs.videoeditor.utils.Utils.getFileExtension;

/**
 * Created by Hung on 11/15/2018.
 */

public class StudioFragmentDetail extends AbsFragment implements VideoAdapter.ItemSelected, VideoStudioAdapter.ItemSelectedStudio, IInputNameFile {
    private VideoStudioAdapter videoAdapter;
    private List<VideoModel> videoModelList = new ArrayList<>(), listAllVideo = new ArrayList<>(), mListChecked = new ArrayList<>();
    private RecyclerView rvVideo;
    private TextView tvNoVideo;
    public boolean isActionMode = false, isSelectAll = false;
    public int countItemSelected = 0;
    private String checkCurrentFragment = null;
    private MainActivity mainActivity;
    private int indexOption = 0;
    private ActionMode actionMode = null;
    private BottomSheetDialog bottomSheetDialog;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                return;
            }

            switch (intent.getAction()) {

                case Statistic.CLEAR_ACTION_MODE:

                    if (actionMode == null) {
                        return;
                    }

                    actionMode.finish();
                    break;
            }

        }
    };

    public void beginSearch(String s) {
        videoModelList = Utils.filterVideoModel(listAllVideo, s);
        videoAdapter.setFilter(videoModelList);
    }


    public static StudioFragmentDetail newInstance(Bundle bundle) {
        StudioFragmentDetail fragment = new StudioFragmentDetail();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mainActivity = (MainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_studio_detail, container, false);
    }

    public void createAction() {
        mainActivity.startSupportActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                isActionMode = true;
                for (VideoModel audioEntity : videoModelList) {
                    audioEntity.setCheck(false);
                }
                videoAdapter.notifyDataSetChanged();
                actionMode = mode;
                actionMode.setTitle("0");
                mainActivity.getMenuInflater().inflate(R.menu.setting_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item_delete:
                        actionModeDelete(mode);
                        break;
                    case R.id.item_check_all:
                        selectItem();
                        break;
                }
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                isActionMode = false;
                isSelectAll = false;
                countItemSelected = 0;
                if (mListChecked != null) {
                    mListChecked.clear();
                }
                for (VideoModel videoModel : videoModelList) {
                    videoModel.setCheck(false);
                }
                videoAdapter.notifyDataSetChanged();
                actionMode.finish();
                actionMode = null;
            }
        });
    }

    private void selectItem() {
        if (!isSelectAll) {
            countItemSelected = videoModelList.size();
            isSelectAll = true;
            for (VideoModel videoModel : videoModelList) {
                videoModel.setCheck(true);
            }
            videoAdapter.notifyDataSetChanged();
            actionMode.setTitle(countItemSelected + "");
            mListChecked.clear();
            mListChecked.addAll(videoModelList);
        } else {
            isSelectAll = false;
            for (VideoModel videoModel : videoModelList) {
                videoModel.setCheck(false);
            }
            mListChecked.clear();
            countItemSelected = 0;
            actionMode.setTitle(countItemSelected + " ");
            actionMode.finish();
        }
    }

    private void settingDeleteRecord() {
        List<VideoModel> videoModels = new ArrayList<>();
        for (VideoModel videoModel : videoModelList) {
            if (videoModel.getPath().toLowerCase().equals(videoModel.getPath().toLowerCase())) {
                videoModels.add(videoModelList.get(indexOption));
            }
        }


        if (mListChecked.size() != 0) {
            for (VideoModel videoModel : mListChecked) {
                countItemSelected = countItemSelected - 1;
                new File(videoModel.getPath()).delete();
                deleteAudio(getContext(), videoModel.getPath());
            }

            updateList();

            updateCountItemSelected();

            notifiAdapter();

            getContext().sendBroadcast(new Intent(Statistic.UPDATE_DELETE_RECORD));
        }
    }

    private void notifiAdapter() {
        videoAdapter.notifyDataSetChanged();
    }

    private void updateList() {
        listAllVideo.clear();
        listAllVideo.addAll(Utils.getStudioVideos(getContext(), checkCurrentFragment));
        videoModelList.clear();
        videoModelList.addAll(listAllVideo);
        videoAdapter.notifyDataSetChanged();

        notifiAdapter();
        hideBottomSheetDialog();
        closeKeyboard(getActivity());
        checkStateFile();
    }

    private void checkStateFile() {
        if (videoModelList.size() > 0) {
            tvNoVideo.setVisibility(View.GONE);
        } else {
            tvNoVideo.setVisibility(View.VISIBLE);
        }
    }


    private void actionModeDelete(final ActionMode mode) {
        if (mListChecked.size() != 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(getResources().getString(R.string.delete_this_record));
            builder.setNegativeButton(android.R.string.no, (dialog, id) -> dialog.dismiss());
            builder.setPositiveButton(android.R.string.yes, (dialog, id) -> {
                settingDeleteRecord();
                isActionMode = false;
                isSelectAll = false;
                videoAdapter.notifyDataSetChanged();
                mode.finish();
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        }
    }


    public void updateCountItemSelected() {
        if (countItemSelected == 0) {
            actionMode.setTitle("0");
            return;
        }

        actionMode.setTitle(countItemSelected + "");
    }

    public void prepareSelection(View view, int i) {
        if (((CheckBox) view).isChecked()) {
            if (!mListChecked.contains(videoModelList.get(i))) {
                mListChecked.add(videoModelList.get(i));
                countItemSelected = countItemSelected + 1;
                updateCountItemSelected();
            }
        } else {
            if (mListChecked.contains(videoModelList.get(i))) {
                mListChecked.remove(videoModelList.get(i));
                countItemSelected = countItemSelected - 1;
                updateCountItemSelected();
            }
        }
    }

    @Override
    public void initViews() {

        initActions();

        checkCurrentFragment = getArguments().getString(Statistic.CHECK_STUDIO_FRAGMENT, null);
        if (checkCurrentFragment == null) {
            return;
        }

        listAllVideo.clear();
        listAllVideo.addAll(Utils.getStudioVideos(getContext(), checkCurrentFragment));

        videoModelList.clear();
        videoModelList.addAll(listAllVideo);

        videoAdapter = new VideoStudioAdapter(videoModelList, this, this, true);

        tvNoVideo = (TextView) findViewById(R.id.tv_no_video);
        rvVideo = (RecyclerView) findViewById(R.id.recycle_view);
        rvVideo.setHasFixedSize(true);
        rvVideo.setLayoutManager(new LinearLayoutManager(getContext()));
        rvVideo.setAdapter(videoAdapter);

        checkStateFile();

    }

    private void initActions() {
        IntentFilter it = new IntentFilter();
        it.addAction(Statistic.CLEAR_ACTION_MODE);
        getContext().registerReceiver(receiver, it);
    }

    @Override
    public void onDestroy() {
        getContext().unregisterReceiver(receiver);
        super.onDestroy();
    }

    private void isHasVideo() {
        if (videoModelList.size() == 0) {
            tvNoVideo.setVisibility(View.VISIBLE);
            return;
        }

        tvNoVideo.setVisibility(View.GONE);
    }

    @Override
    public void onClick(int index) {
        indexOption = index;
        openVideoWith();
    }

    @Override
    public boolean onLongClick(int index) {
        createAction();
        videoModelList.get(index).setCheck(true);
        videoAdapter.notifyDataSetChanged();
        return true;
    }

    @Override
    public void onOptionClick(int index) {
        indexOption = index;
        showBottomSheet(indexOption);
    }

    private void showBottomSheet(int index) {

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_option_bottom, null);

        TextView tvTitle = view.findViewById(R.id.btn_title);

        tvTitle.setText(videoModelList.get(index).getNameAudio());

        view.findViewById(R.id.btn_share).setOnClickListener(v -> shareVideo());
        view.findViewById(R.id.btn_delete).setOnClickListener(v -> deleteVideo());
        view.findViewById(R.id.btn_detail).setOnClickListener(v -> detailVideo());
        view.findViewById(R.id.btn_rename).setOnClickListener(v -> renameVideo());
        view.findViewById(R.id.btn_open_file).setOnClickListener(v -> openVideoWith());

        bottomSheetDialog = new BottomSheetDialog(getContext());
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }

    private void playVideo() {

        Bundle bundle = new Bundle();

        bundle.putParcelable(Statistic.VIDEO_MODEL, videoModelList.get(indexOption));

        getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.animation_left_to_right
                        , R.anim.animation_right_to_left
                        , R.anim.animation_left_to_right
                        , R.anim.animation_right_to_left)
                .add(R.id.view_container1, PlayVideoFragment.newInstance(bundle))
                .addToBackStack(null)
                .commit();
    }

    private void openVideoWith() {
        Uri uri;

        VideoModel videoModel = videoModelList.get(indexOption);

        Intent intent = new Intent();

        intent.setAction(Intent.ACTION_VIEW);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".provider", new File(videoModel.getPath()));
        } else {
            uri = Uri.fromFile(new File(videoModel.getPath()));
        }

        intent.setDataAndType((uri), "video/*");

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(intent);

        hideBottomSheetDialog();
    }

    private void hideBottomSheetDialog() {
        if (bottomSheetDialog != null) {
            bottomSheetDialog.dismiss();
        }
    }

    private DialogInputName dialogInputName;

    private void renameVideo() {
        dialogInputName = new DialogInputName(getContext(), this, "",getString(R.string.rename));
        dialogInputName.initDialog();
        hideBottomSheetDialog();
    }

    private void detailVideo() {
        VideoModel videoModel = videoModelList.get(indexOption);
        TextView tvTitle, tvFilePath, tvDuration, tvSize, tvDateAdded;
        DialogDetail dialogDetail = new DialogDetail(getContext());
        dialogDetail.setOnClickBtnOk(v -> dialogDetail.hideDialog());

        View view = dialogDetail.getView();
        tvSize = view.findViewById(R.id.tvSize);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvFilePath = view.findViewById(R.id.tvFilePath);
        tvDuration = view.findViewById(R.id.tvDuaration);
        tvDateAdded = view.findViewById(R.id.tvDateTime);
        tvTitle.setText(getResources().getString(R.string.title_audio) + ": " + videoModel.getNameAudio());
        tvSize.setText(getString(R.string.size) + ": " + Utils.getStringSizeLengthFile(videoModel.getSize()));
        tvFilePath.setText(getResources().getString(R.string.path) + ": " + videoModel.getPath());
        tvDuration.setText(getResources().getString(R.string.duration) + ": " + Utils.convertMillisecond(Long.parseLong(videoModel.getDuration())));
        tvDateAdded.setText(getString(R.string.date_time) + ": " + Utils.convertDate(String.valueOf(videoModel.getDateModifier()), "dd/MM/yyyy HH:mm:ss"));
        hideBottomSheetDialog();

    }

    private void deleteVideo() {
        VideoModel videoModel = videoModelList.get(indexOption);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getResources().getString(R.string.delete_this_record));
        builder.setNegativeButton(android.R.string.no, (dialog, id) -> dialog.dismiss());
        builder.setPositiveButton(getResources().getString(R.string.yes), (dialog, id) -> {
            new File(videoModel.getPath()).delete();
            deleteAudio(getContext(), videoModel.getPath());
            updateList();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        hideBottomSheetDialog();
    }

    private void shareVideo() {
        Uri uri;
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".provider", new File(videoModelList.get(indexOption).getPath()));
        } else {
            uri = Uri.fromFile(new File(videoModelList.get(indexOption).getPath()));
        }
        intent.setType("video/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(intent, getString(R.string.share_file)));
        hideBottomSheetDialog();
    }

    @Override
    public void onApplySelect(String nameFile) {
        applyRenameVideo(nameFile);
    }

    private void applyRenameVideo(String nameFile) {
        File currentFile, newFile;

        VideoModel videoModel = videoModelList.get(indexOption);

        currentFile = new File(videoModel.getPath());

        newFile = new File(videoModel.getPath().replace(videoModel.getNameAudio() + Statistic.FORMAT_MP4, "") + nameFile + getFileExtension(videoModel.getPath()));

        if (newFile.exists()) {
            Toast.makeText(getContext(), getString(R.string.name_file_exist), Toast.LENGTH_SHORT).show();

        } else {
            rename(currentFile, newFile);
            renameContentProvider(nameFile, getFileExtension(videoModel.getPath()), videoModel, getContext());
            updateList();
            onHideDialogInputNameFile();
        }
    }

    private void onHideDialogInputNameFile() {
        if (dialogInputName != null) {
            dialogInputName.hideDialog();
        }
    }

    private boolean rename(File from, File to) {
        return from.getParentFile().exists() && from.exists() && from.renameTo(to);
    }

    @Override
    public void onCancelSelect() {
        onHideDialogInputNameFile();
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
