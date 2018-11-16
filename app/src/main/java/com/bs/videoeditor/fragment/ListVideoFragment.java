package com.bs.videoeditor.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bs.videoeditor.R;
import com.bs.videoeditor.activity.MainActivity;
import com.bs.videoeditor.adapter.VideoAdapter;
import com.bs.videoeditor.model.VideoModel;
import com.bs.videoeditor.statistic.Statistic;
import com.bs.videoeditor.utils.Flog;
import com.bs.videoeditor.utils.Utils;

import java.util.ArrayList;
import java.util.List;


public class ListVideoFragment extends AbsFragment implements VideoAdapter.ItemSelected {
    private VideoAdapter videoAdapter;
    private List<VideoModel> videoModelList;
    private RecyclerView rvVideo;
    private TextView tvNoVideo;
    private String listTitle[];

    private int checkAction = 0;

    public static ListVideoFragment newInstance(Bundle bundle) {
        Bundle args = new Bundle();
        ListVideoFragment fragment = new ListVideoFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void initViews() {

        videoModelList = new ArrayList<>();
        videoAdapter = new VideoAdapter(videoModelList, this, getContext(),false);

        tvNoVideo = (TextView) findViewById(R.id.tv_no_video);
        rvVideo = (RecyclerView) findViewById(R.id.recycle_view);
        rvVideo.setHasFixedSize(true);
        rvVideo.setLayoutManager(new LinearLayoutManager(getContext()));
        rvVideo.setAdapter(videoAdapter);

        loadVideo();
    }

    @Override
    public void initToolbar() {
        super.initToolbar();

        checkAction = getArguments().getInt(Statistic.ACTION,0);

        listTitle = new String[]{getString(R.string.cutter),
                getString(R.string.speed),
                getString(R.string.merger),
                getString(R.string.add_music)};

        getToolbar().setTitle(listTitle[checkAction]);
        getToolbar().getMenu().clear();
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
                videoModelList.addAll(Utils.getVideos(getContext()));
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_video, container, false);
    }

    @Override
    public void onClick(int index) {
        Flog.e("index  " + index);
        addFragment(checkAction, index);
    }


    private void addFragment(int action, int indexVideo) {
        AbsFragment absFragment = null;
        Bundle bundle = new Bundle();
        bundle.putInt(Statistic.ACTION, action);
        bundle.putString(Statistic.PATH_VIDEO, videoModelList.get(indexVideo).getPath());
        bundle.putParcelable(Statistic.VIDEO_MODEL,videoModelList.get(indexVideo));
        Flog.e("pathhhh   " + videoModelList.get(indexVideo).getPath());

        switch (action) {
            case MainActivity.INDEX_CUTTER:
                absFragment = CutterFragment.newInstance(bundle);
                break;

            case MainActivity.INDEX_ADD_MUSIC:
                absFragment = AddMusicFragment.newInstance(bundle);
                break;

            case MainActivity.INDEX_MERGER:
                absFragment = MergerFragment.newInstance(bundle);
                break;

            case MainActivity.INDEX_SPEED:
                absFragment = SpeedFragment.newInstance(bundle);
                break;

        }

        getFragmentManager().beginTransaction()
                .add(R.id.view_container, absFragment)
                .addToBackStack(null)
                .commit();
    }
}

