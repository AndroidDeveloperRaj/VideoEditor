package com.bs.videoeditor.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;

import com.bs.videoeditor.R;
import com.bs.videoeditor.adapter.StudioAdapter;
import com.bs.videoeditor.statistic.Statistic;
import com.bs.videoeditor.utils.Flog;
import com.bs.videoeditor.utils.SharedPrefs;
import com.bs.videoeditor.utils.SortOrder;
import com.bs.videoeditor.utils.Utils;

import static com.bs.videoeditor.statistic.Statistic.INDEX_ADD_MUSIC;
import static com.bs.videoeditor.statistic.Statistic.INDEX_CUTTER;
import static com.bs.videoeditor.statistic.Statistic.INDEX_MERGER;
import static com.bs.videoeditor.statistic.Statistic.INDEX_SPEED;
import static com.bs.videoeditor.utils.SortOrder.ID_SONG_A_Z;
import static com.bs.videoeditor.utils.SortOrder.ID_SONG_DATE_ADDED;
import static com.bs.videoeditor.utils.SortOrder.ID_SONG_Z_A;

public class StudioFragment extends AbsFragment {
    public static final int CUTTER = 0;
    public static final int SPEED = 1;
    public static final int MERGER = 2;
    public static final int ADD_MUSIC = 3;
    private StudioAdapter studioAdapter;
    private ViewPager viewPager;
    private SearchView searchView;
    private int CHECK_STATE_ADD = 0;
    private int OPEN_FRAGMENT = 0;
    private StudioFragmentDetail studioFragmentDetail;

    private void addTabFragment() {

        OPEN_FRAGMENT = getArguments().getInt(Statistic.OPEN_FRAGMENT, 0);

        studioAdapter = new StudioAdapter(getChildFragmentManager());

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(studioAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(CUTTER).setText(getString(R.string.cutter));
        tabLayout.getTabAt(SPEED).setText(getString(R.string.speed));
        tabLayout.getTabAt(MERGER).setText(getString(R.string.merger));
        tabLayout.getTabAt(ADD_MUSIC).setText(getString(R.string.add_music));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                getContext().sendBroadcast(new Intent(Statistic.CLEAR_ACTION_MODE));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        Flog.e(" open nnnnn " + OPEN_FRAGMENT);
        if (OPEN_FRAGMENT == INDEX_CUTTER) {
            viewPager.setCurrentItem(INDEX_CUTTER);

        } else if (OPEN_FRAGMENT == INDEX_SPEED) {
            viewPager.setCurrentItem(INDEX_SPEED);

        } else if (OPEN_FRAGMENT == INDEX_ADD_MUSIC) {
            viewPager.setCurrentItem(INDEX_ADD_MUSIC);

        } else {
            viewPager.setCurrentItem(INDEX_MERGER);
        }

        viewPager.setOffscreenPageLimit(3);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_studio, container, false);
    }

    @Override
    public void initViews() {
        addTabFragment();
    }

    @Override
    public void initToolbar() {
        super.initToolbar();
        getToolbar().getMenu().clear();
        getToolbar().inflateMenu(R.menu.menu_search1);
        getToolbar().setNavigationOnClickListener(view -> {
            getActivity().onBackPressed();
            Utils.closeKeyboard(getActivity());
        });
        searchAudio();
        setUpSortOrderMenu(getToolbar().getMenu().findItem(R.id.action_sort_order).getSubMenu());
    }

    private void setUpSortOrderMenu(@NonNull SubMenu sortOrderMenu) {

        int currentSortOrder = SharedPrefs.getInstance().get(Statistic.SORT_ORDER_CURRENT, Integer.class, ID_SONG_A_Z);

        sortOrderMenu.clear();

        sortOrderMenu.add(0, R.id.action_song_sort_order_asc, 0, R.string.sort_order_a_z)
                .setChecked(currentSortOrder == ID_SONG_A_Z);
        sortOrderMenu.add(0, R.id.action_song_sort_order_desc, 1, R.string.sort_order_z_a)
                .setChecked(currentSortOrder == ID_SONG_Z_A);
        sortOrderMenu.add(0, R.id.action_song_sort_order_year, 2, R.string.sort_date_added)
                .setChecked(currentSortOrder == ID_SONG_DATE_ADDED);

        sortOrderMenu.setGroupCheckable(0, true, true);

        sortOrderMenu.findItem(R.id.action_song_sort_order_asc).setOnMenuItemClickListener(item -> saveIdSortOrder(ID_SONG_A_Z));
        sortOrderMenu.findItem(R.id.action_song_sort_order_desc).setOnMenuItemClickListener(item -> saveIdSortOrder(ID_SONG_Z_A));
        sortOrderMenu.findItem(R.id.action_song_sort_order_year).setOnMenuItemClickListener(item -> saveIdSortOrder(ID_SONG_DATE_ADDED));

    }

    private boolean saveIdSortOrder(int id) {
        Flog.e("xxxxxxx    idddddd    "+ id);
        SharedPrefs.getInstance().put(Statistic.SORT_ORDER_CURRENT, id);
        getContext().sendBroadcast(new Intent(Statistic.UPDATE_CHOOSE_SORT_ORDER).putExtra(Statistic.SORT_ORDER_CURRENT, id));
        return true;
    }

    private void searchAudio() {
        MenuItem menuItem = getToolbar().getMenu().findItem(R.id.item_search);
        searchView = (SearchView) menuItem.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //actionSearch(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                PagerAdapter pagerAdapter = viewPager.getAdapter();

                for (int i = 0; i < pagerAdapter.getCount(); i++) {

                    Fragment viewPagerFragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, i);

                    if (viewPagerFragment.isAdded()) {

                        if (viewPagerFragment instanceof StudioFragmentDetail) {

                            studioFragmentDetail = (StudioFragmentDetail) viewPagerFragment;

                            if (studioFragmentDetail != null) {
                                studioFragmentDetail.beginSearch(s);
                            }
                        }
                    }
                }

                return true;
            }
        });
    }

    public static StudioFragment newInstance(Bundle bundle) {
        StudioFragment fragment = new StudioFragment();
        fragment.setArguments(bundle);
        return fragment;
    }
}
