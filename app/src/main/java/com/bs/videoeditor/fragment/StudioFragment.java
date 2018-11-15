package com.bs.videoeditor.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bs.videoeditor.R;
import com.bs.videoeditor.adapter.StudioAdapter;
import com.bs.videoeditor.statistic.Statistic;

public class StudioFragment extends AbsFragment {
    public static final int CUTTER = 0;
    public static final int SPEED = 1;
    public static final int MERGER = 2;
    public static final int ADD_MUSIC = 3;
    private StudioAdapter studioAdapter;
    private ViewPager viewPager;
    public Toolbar toolbar;
    private SearchView searchView;
    private StudioFragmentDetail studioFragmentDetail;
    private int CHECK_STATE_ADD = 0;
    private int OPEN_FRAGMENT = 0;


    private void addTabFragment() {

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


//        if (CHECK_STATE_ADD == Keys.FROM_RECORDER) {
//            viewPager.setCurrentItem(AUDIO_RECORDER);
//            viewPager.setOffscreenPageLimit(3);
//            return;
//        }
//
//        if (OPEN_FRAGMENT == AUDIO_MERGER) {
//
//            viewPager.setCurrentItem(AUDIO_MERGER);
//            viewPager.setOffscreenPageLimit(3);
//
//        } else if (OPEN_FRAGMENT == AUDIO_CONVERTER) {
//
//            viewPager.setCurrentItem(AUDIO_CONVERTER);
//            viewPager.setOffscreenPageLimit(3);
//
//        } else {
//
//            viewPager.setCurrentItem(AUDIO_CUTTER);
//            viewPager.setOffscreenPageLimit(3);
//        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_studio, container, false);
    }

    @Override
    public void initViews() {

        getToolbar().getMenu().clear();

        addTabFragment();
    }

    public static StudioFragment newInstance() {
        StudioFragment fragment = new StudioFragment();
        //fragment.setArguments(bundle);
        return fragment;
    }
}
