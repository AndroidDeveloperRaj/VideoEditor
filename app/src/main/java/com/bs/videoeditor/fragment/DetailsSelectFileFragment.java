package com.bs.videoeditor.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bs.videoeditor.R;
import com.bs.videoeditor.adapter.LeftListAdapter;
import com.bs.videoeditor.adapter.RightListAdapter;
import com.bs.videoeditor.model.VideoModel;
import com.bs.videoeditor.statistic.Statistic;
import com.bs.videoeditor.utils.Utils;
import com.yalantis.multiselection.lib.MultiSelect;
import com.yalantis.multiselection.lib.MultiSelectBuilder;


import java.util.ArrayList;
import java.util.List;

public class DetailsSelectFileFragment extends AbsFragment {
    private MultiSelect<VideoModel> mMultiSelect;
    private LeftListAdapter leftAdapter;
    private RightListAdapter rightAdapter;
    private Toolbar toolbar;
    private View viewChooseFile;
    private TextView tvNoAudioFile;

    public static DetailsSelectFileFragment newInstance() {
        Bundle args = new Bundle();
        DetailsSelectFileFragment fragment = new DetailsSelectFileFragment();
        fragment.setArguments(args);
        return fragment;
    }


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                switch (intent.getAction()) {
                    case Statistic.UPDATE_CHOOSE_VIDEO:

                        String path = intent.getExtras().getString(Statistic.MODEL);

                        for (int i = 0; i < leftAdapter.getItems().size(); i++) {

                            if (leftAdapter.getItems().get(i).getPath().equals(path)) {

                                VideoModel videoModel = leftAdapter.getItemAt(i);

                                leftAdapter.removeItemAt(i);

                                rightAdapter.add(videoModel, false);

                            }
                        }
                        break;
                }
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details_select_folder, container, false);
        return view;
    }

    @Override
    public void initToolbar() {
        super.initToolbar();
        getToolbar().setTitle(getString(R.string.select_file));
        getToolbar().getMenu().clear();
        getToolbar().inflateMenu(R.menu.menu_details_gallery_folder);
        getToolbar().setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_select_file:

                    checkSelectFile();

                    if (videoModelListChecked.size() < 2) {

                        Toast.makeText(getContext(), getString(R.string.you_need_to_have), Toast.LENGTH_SHORT).show();

                    } else {

                        long duration = 0;

                        for (VideoModel videoModel : videoModelListChecked) {
                            duration += Long.parseLong(videoModel.getDuration());
                        }

                        Bundle bundle = new Bundle();
                        bundle.putParcelableArrayList(Statistic.LIST_VIDEO, (ArrayList<? extends Parcelable>) videoModelListChecked);
                        bundle.putLong(Statistic.DURATION, duration);

                        getFragmentManager().beginTransaction()
                                .add(R.id.view_container, SortMergerFragment.newInstance(bundle))
                                .addToBackStack(null)
                                .commit();
                    }
                    break;

                case R.id.item_search:

                    List<VideoModel> audioEntities = Utils.getVideos(getContext());

                    if (mMultiSelect.getSelectedItems().size() > 0) {

                        videoModelListChecked = mMultiSelect.getSelectedItems();

                        for (VideoModel videoModel : videoModelListChecked) {
                            for (VideoModel videoModel1 : audioEntities) {
                                if (videoModel.getPath().equals(videoModel1.getPath())) {
                                    audioEntities.remove(videoModel1);
                                    break;
                                }
                            }
                        }
                    }

                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList(Statistic.LIST_VIDEO, (ArrayList<? extends Parcelable>) audioEntities);

                    getFragmentManager().beginTransaction()
                            .add(R.id.view_container, SearchFragment.newInstance(bundle))
                            .addToBackStack(null)
                            .commit();

                    break;
            }

            return true;
        });
    }

    private List<VideoModel> videoModelListChecked = new ArrayList<>();

    public boolean checkSelectFile() {
        videoModelListChecked = mMultiSelect.getSelectedItems();
        return false;
    }

    private List<VideoModel> leftListAudio = new ArrayList<>();

    private void setUpAdapters(MultiSelectBuilder<VideoModel> builder) {

        leftListAudio = Utils.getVideos(getContext());
        leftAdapter = new LeftListAdapter(getContext(), position -> mMultiSelect.select(position));
        rightAdapter = new RightListAdapter(getContext(), position -> mMultiSelect.deselect(position));
        leftAdapter.addAll(leftListAudio);

        builder.withLeftAdapter(leftAdapter)
                .withRightAdapter(rightAdapter);

        if (leftListAudio == null) {
            return;
        }

        if (leftListAudio.size() == 0) {
            tvNoAudioFile.setVisibility(View.VISIBLE);
            viewChooseFile.setVisibility(View.INVISIBLE);

        } else {
            tvNoAudioFile.setVisibility(View.INVISIBLE);
            viewChooseFile.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void initViews() {

        tvNoAudioFile = (TextView) findViewById(R.id.tv_no_audio);
        viewChooseFile = findViewById(R.id.view_choose_file);

        MultiSelectBuilder<VideoModel> builder = new MultiSelectBuilder<>(VideoModel.class)
                .withContext(getActivity())
                .mountOn((ViewGroup) findViewById(R.id.viewDetailsSelectFolder))
                .withSidebarWidth(46 + 8 * 2); // ImageView width with paddings

        setUpAdapters(builder);

        mMultiSelect = builder.build();

        IntentFilter it = new IntentFilter();
        it.addAction(Statistic.UPDATE_CHOOSE_VIDEO);
        getContext().registerReceiver(receiver, it);
    }

    @Override
    public void onDestroy() {


        getContext().unregisterReceiver(receiver);
        super.onDestroy();
    }
}
