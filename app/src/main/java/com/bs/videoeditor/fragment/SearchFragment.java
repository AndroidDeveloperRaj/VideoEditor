package com.bs.videoeditor.fragment;

import android.content.Intent;
import android.os.Bundle;
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

import com.bs.videoeditor.R;
import com.bs.videoeditor.adapter.VideoAdapter;
import com.bs.videoeditor.model.VideoModel;
import com.bs.videoeditor.statistic.Statistic;
import com.bs.videoeditor.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends AbsFragment implements VideoAdapter.ItemSelected {
    private SearchView searchView;
    private RecyclerView rvAudio;
    private List<VideoModel> searchList = new ArrayList<>();
    private VideoAdapter audioAdapter;

    public static SearchFragment newInstance(Bundle bundle) {
        SearchFragment fragment = new SearchFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private List<VideoModel> videoModelList = new ArrayList<>();

    @Override
    public void initViews() {
        videoModelList.clear();
        videoModelList.addAll(getArguments().getParcelableArrayList(Statistic.LIST_VIDEO));

        audioAdapter = new VideoAdapter(videoModelList, this, getContext(), false);
        rvAudio = (RecyclerView) findViewById(R.id.rv_audio);
        rvAudio.setHasFixedSize(true);
        rvAudio.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAudio.setAdapter(audioAdapter);

    }

    public void initToolbar() {
        super.initToolbar();
        getToolbar().setTitle(R.string.select_file);
        getToolbar().getMenu().clear();
        getToolbar().inflateMenu(R.menu.menu_search);
        searchAudio(getToolbar());
    }


    private void backFragment() {
        if (searchView != null) {
            searchView.clearFocus();
        }
        getFragmentManager().popBackStack();
    }

    private void searchAudio(Toolbar toolbar) {
        MenuItem menuItem = toolbar.getMenu().findItem(R.id.item_search);
        searchView = (SearchView) menuItem.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.onActionViewExpanded();
        searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                actionSearch(s);
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
        searchList = Utils.filterVideoModel(videoModelList, s);
        audioAdapter.setFilter(searchList);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onClick(int index) {
        VideoModel videoModel;

        if (searchList.size() == 0) {
            videoModel = videoModelList.get(index);

        } else {

            videoModel = searchList.get(index);
        }

        if (searchView != null) {
            searchView.clearFocus();
        }

        Log.e("xxx", "vxcvx " + videoModel.getPath());

        getContext().sendBroadcast(new Intent(Statistic.UPDATE_CHOOSE_VIDEO).putExtra(Statistic.MODEL, videoModel.getPath()));

        getFragmentManager().popBackStack();

    }


    @Override
    public void onDestroy() {
        if (searchView != null) {
            searchView.clearFocus();
        }
        super.onDestroy();
    }
}
