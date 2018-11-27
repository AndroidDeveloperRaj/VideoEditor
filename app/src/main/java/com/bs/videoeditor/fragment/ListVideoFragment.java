package com.bs.videoeditor.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bs.videoeditor.R;
import com.bs.videoeditor.activity.MainActivity;
import com.bs.videoeditor.adapter.VideoAdapter;
import com.bs.videoeditor.model.VideoModel;
import com.bs.videoeditor.statistic.Statistic;
import com.bs.videoeditor.utils.Flog;
import com.bs.videoeditor.utils.SharedPrefs;
import com.bs.videoeditor.utils.SortOrder;
import com.bs.videoeditor.utils.Utils;
import java.util.ArrayList;
import java.util.List;

import static com.bs.videoeditor.utils.SortOrder.ID_SONG_A_Z;
import static com.bs.videoeditor.utils.SortOrder.ID_SONG_DATE_ADDED;
import static com.bs.videoeditor.utils.SortOrder.ID_SONG_DATE_ADDED_DESCENDING;
import static com.bs.videoeditor.utils.SortOrder.ID_SONG_Z_A;


public class ListVideoFragment extends AbsFragment implements VideoAdapter.ItemSelected {
    private VideoAdapter videoAdapter;
    private List<VideoModel> videoModelList = new ArrayList<>();
    private List<VideoModel> listAllVideoMolder = new ArrayList<>();
    private RecyclerView rvVideo;
    private TextView tvNoVideo;
    private String listTitle[];
    private boolean isGetVideosSpeed = true;
    private MenuItem listMenu[];
    private int mCheckAction = 0; // action is check this fragment choose video is fragment cutter == 0,addmusic == 2,speed ==1

    public static ListVideoFragment newInstance(Bundle bundle) {
        Bundle args = new Bundle();
        ListVideoFragment fragment = new ListVideoFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void initViews() {

        videoModelList = new ArrayList<>();
        videoAdapter = new VideoAdapter(videoModelList, this, getContext(), false);

        tvNoVideo = (TextView) findViewById(R.id.tv_no_video);
        rvVideo = (RecyclerView) findViewById(R.id.recycle_view);
        rvVideo.setHasFixedSize(true);
        rvVideo.setLayoutManager(new LinearLayoutManager(getContext()));
        rvVideo.setAdapter(videoAdapter);

    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadVideo();
    }

    private SearchView searchView;

    private void searchAudio(Toolbar toolbar) {
        MenuItem menuItem = toolbar.getMenu().findItem(R.id.item_search);
        searchView = (SearchView) menuItem.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
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
        videoModelList = Utils.filterVideoModel(listAllVideoMolder, s);
        videoAdapter.setFilter(videoModelList);
    }


    @Override
    public void initToolbar() {
        super.initToolbar();

        mSortOrderId = SharedPrefs.getInstance().get(Statistic.SORT_ORDER_CURRENT_CHOOSE_VIDEO, Integer.class, SortOrder.ID_SONG_A_Z);
        mCheckAction = getArguments().getInt(Statistic.ACTION, 0);

        if (mCheckAction == 1) {
            isGetVideosSpeed = false;
        }

        listTitle = new String[]{getString(R.string.cutter),
                getString(R.string.speed),
                getString(R.string.merger),
                getString(R.string.add_music)};

        getToolbar().setTitle(listTitle[mCheckAction]);
        getToolbar().getMenu().clear();
        getToolbar().inflateMenu(R.menu.menu_search1);
        searchAudio(getToolbar());

        setUpSortOrderMenu();
    }

    private void setUpSortOrderMenu() {
        int currentSortOrder = SharedPrefs.getInstance().get(Statistic.SORT_ORDER_CURRENT_CHOOSE_VIDEO, Integer.class, ID_SONG_A_Z);

        getToolbar().getMenu().setGroupCheckable(0, true, true);

        MenuItem menuItemAZ, menuItemZA, menuItemDateASC, menuItemDateDESC;
        menuItemAZ = getToolbar().getMenu().findItem(R.id.item_a_z);
        menuItemDateASC = getToolbar().getMenu().findItem(R.id.item_date_ascending);
        menuItemDateDESC = getToolbar().getMenu().findItem(R.id.item_date_descending);
        menuItemZA = getToolbar().getMenu().findItem(R.id.item_z_a);

        listMenu = new MenuItem[]{menuItemAZ, menuItemZA, menuItemDateASC, menuItemDateDESC};
        listMenu[currentSortOrder].setChecked(true);

        menuItemAZ.setOnMenuItemClickListener(menuItem -> saveIdSortOrder(ID_SONG_A_Z, menuItem));
        menuItemZA.setOnMenuItemClickListener(menuItem -> saveIdSortOrder(ID_SONG_Z_A, menuItem));
        menuItemDateASC.setOnMenuItemClickListener(menuItem -> saveIdSortOrder(ID_SONG_DATE_ADDED, menuItem));
        menuItemDateDESC.setOnMenuItemClickListener(menuItem -> saveIdSortOrder(ID_SONG_DATE_ADDED_DESCENDING, menuItem));

    }

    private boolean saveIdSortOrder(int id, @NonNull MenuItem menuItem) {
        mSortOrderId = id;
        loadVideo();
        menuItem.setChecked(true);
        SharedPrefs.getInstance().put(Statistic.SORT_ORDER_CURRENT_CHOOSE_VIDEO, id);
        return true;
    }

    private void isHasVideo() {
        if (videoModelList.size() == 0) {
            tvNoVideo.setVisibility(View.VISIBLE);
        }

        tvNoVideo.setVisibility(View.GONE);
    }

    private int mSortOrderId = SortOrder.ID_SONG_A_Z;

    private void loadVideo() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                listAllVideoMolder.clear();
                listAllVideoMolder.addAll(Utils.getVideos(getContext(), mSortOrderId, Statistic.DIR_APP + Statistic.DIR_SPEED, true));
                videoModelList.clear();
                videoModelList.addAll(listAllVideoMolder);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                isHasVideo();
                videoAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_video, container, false);
    }

    @Override
    public void onClick(int index) {

        addFragment(mCheckAction, index);

        if (searchView != null) {
            searchView.clearFocus();
        }
    }

    @Override
    public boolean onLongClick(int index) {
        return false;
    }

    @Override
    public void onOptionClick(int index) {

    }


    private void addFragment(int action, int indexVideo) {
        AbsFragment absFragment = null;
        Bundle bundle = new Bundle();
        bundle.putInt(Statistic.ACTION, action);
        bundle.putString(Statistic.PATH_VIDEO, videoModelList.get(indexVideo).getPath());
        bundle.putParcelable(Statistic.VIDEO_MODEL, videoModelList.get(indexVideo));
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

