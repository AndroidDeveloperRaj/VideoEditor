package com.bs.videoeditor.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.bs.videoeditor.R;
import com.bs.videoeditor.activity.MainActivity;
import com.bs.videoeditor.adapter.VideoAdapter;
import com.bs.videoeditor.adapter.VideoStudioAdapter;
import com.bs.videoeditor.model.VideoModel;
import com.bs.videoeditor.statistic.Statistic;
import com.bs.videoeditor.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Hung on 11/15/2018.
 */

public class StudioFragmentDetail extends AbsFragment implements VideoAdapter.ItemSelected, VideoStudioAdapter.ItemSelectedStudio {
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
                mode.finish();
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
        List<VideoModel> audios = new ArrayList<>();
        for (VideoModel audioEntity : videoModelList) {
            if (audioEntity.getPath().toLowerCase().equals(audioEntity.getPath().toLowerCase())) {
                audios.add(videoModelList.get(indexOption));
            }
        }


        if (mListChecked.size() != 0) {

            for (VideoModel videoModel : mListChecked) {
                File file = new File(videoModel.getPath());
                file.delete();

                Utils.deleteAudio(getContext(), videoModel.getPath());

                countItemSelected = countItemSelected - 1;

            }

            updateList();

            updateCountItemSelected();

            videoAdapter.notifyDataSetChanged();
            getContext().sendBroadcast(new Intent(Statistic.UPDATE_DELETE_RECORD));
        }
    }

    private void updateList() {

        listAllVideo.clear();
        listAllVideo.addAll(Utils.getStudioVideos(getContext(), checkCurrentFragment));

        videoModelList.clear();
        videoModelList.addAll(listAllVideo);

        //Collections.reverse(videoModelList);

        videoAdapter.notifyDataSetChanged();

        Utils.closeKeyboard(getActivity());

        if (bottomSheetDialog != null) {
            bottomSheetDialog.dismiss();
        }
    }


    private void actionModeDelete(final ActionMode mode) {
        if (mListChecked.size() != 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AppCompatAlertDialogStyle);
            builder.setTitle(getResources().getString(R.string.delete_this_record));
            builder.setPositiveButton(android.R.string.yes, (dialog, id) -> {

                settingDeleteRecord();
                isActionMode = false;
                isSelectAll = false;
                videoAdapter.notifyDataSetChanged();
                mode.finish();
            });
            builder.setNegativeButton(android.R.string.no, (dialog, id) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }


    public void updateCountItemSelected() {
        if (countItemSelected == 0)
            actionMode.setTitle("0");
        else
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

        checkCurrentFragment = getArguments().getString(Statistic.CHECK_STUDIO_FRAGMENT, null);
        if (checkCurrentFragment == null) {
            return;
        }

        listAllVideo.clear();
        listAllVideo.addAll(Utils.getStudioVideos(getContext(), checkCurrentFragment));
        videoModelList.clear();
        videoModelList.addAll(listAllVideo);

        //Collections.reverse(videoModelList);

        videoAdapter = new VideoStudioAdapter(videoModelList, this, this, true);

        tvNoVideo = (TextView) findViewById(R.id.tv_no_video);
        rvVideo = (RecyclerView) findViewById(R.id.recycle_view);
        rvVideo.setHasFixedSize(true);
        rvVideo.setLayoutManager(new LinearLayoutManager(getContext()));
        rvVideo.setAdapter(videoAdapter);

        //loadVideo();
    }

    private void isHasVideo() {
        if (videoModelList.size() == 0) {
            tvNoVideo.setVisibility(View.VISIBLE);
        }

        tvNoVideo.setVisibility(View.GONE);
    }

    private void loadVideo() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                videoModelList.clear();
                videoModelList.addAll(Utils.getStudioVideos(getContext(), checkCurrentFragment));
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                isHasVideo();
                videoAdapter.notifyDataSetChanged();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onClick(int index) {

    }

    @Override
    public boolean onLongClick(int index) {
        createAction();
        return true;
    }

    @Override
    public void onOptionClick(int index) {
        indexOption = index;

        showBottomSheet(indexOption);

    }

    private BottomSheetDialog bottomSheetDialog;

    private void showBottomSheet(int index) {

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_option_bottom, null);

        TextView tvTitle = view.findViewById(R.id.btn_title);

        tvTitle.setText(videoModelList.get(index).getNameAudio());

        view.findViewById(R.id.btn_share).setOnClickListener(v -> shareVideo());
        view.findViewById(R.id.btn_delete).setOnClickListener(v -> deleteVideo());
        view.findViewById(R.id.btn_detail).setOnClickListener(v -> detailVideo());
        view.findViewById(R.id.btn_rename).setOnClickListener(v -> renameVideo());
        view.findViewById(R.id.btn_open_file).setOnClickListener(v -> openVideo());

        bottomSheetDialog = new BottomSheetDialog(getContext());
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }

    private void openVideo() {
    }

    private void renameVideo() {
    }

    private void detailVideo() {
    }

    private void deleteVideo() {
    }

    private void shareVideo() {
    }

}
