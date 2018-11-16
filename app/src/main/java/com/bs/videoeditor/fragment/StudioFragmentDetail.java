package com.bs.videoeditor.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bs.videoeditor.R;
import com.bs.videoeditor.adapter.VideoAdapter;
import com.bs.videoeditor.model.VideoModel;
import com.bs.videoeditor.statistic.Statistic;
import com.bs.videoeditor.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hung on 11/15/2018.
 */

public class StudioFragmentDetail extends AbsFragment implements VideoAdapter.ItemSelected {
    private VideoAdapter videoAdapter;
    private List<VideoModel> videoModelList;
    private RecyclerView rvVideo;
    private TextView tvNoVideo;

    public static StudioFragmentDetail newInstance(Bundle bundle) {
        StudioFragmentDetail fragment = new StudioFragmentDetail();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_studio_detail, container, false);
    }

    private String checkCurrentFragment = null;

    @Override
    public void initViews() {

        checkCurrentFragment = getArguments().getString(Statistic.CHECK_STUDIO_FRAGMENT, null);
        if (checkCurrentFragment == null) {
            return;
        }

        videoModelList = new ArrayList<>();
        videoAdapter = new VideoAdapter(videoModelList, this, getContext(), false);

        tvNoVideo = (TextView) findViewById(R.id.tv_no_video);
        rvVideo = (RecyclerView) findViewById(R.id.recycle_view);
        rvVideo.setHasFixedSize(true);
        rvVideo.setLayoutManager(new LinearLayoutManager(getContext()));
        rvVideo.setAdapter(videoAdapter);

        loadVideo();
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
}
