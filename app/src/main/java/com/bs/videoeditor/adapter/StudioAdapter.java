package com.bs.videoeditor.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.bs.videoeditor.fragment.StudioFragment;
import com.bs.videoeditor.fragment.StudioFragmentDetail;
import com.bs.videoeditor.statistic.Statistic;


public class StudioAdapter extends FragmentStatePagerAdapter {

    private static final String CUTTER = "Cutter";
    private static final String MERGER = "Merger";
    private static final String CONVERTER = "Converter";
    private static final String RECORDER = "Recorder";
    private static final int INDEX_CUTTER = 0;
    private static final int INDEX_SPEED = 1;
    private static final int INDEX_MERGER = 2;
    private static final int INDEX_ADD_MUSIC = 3;

    private String[] listTab = new String[]{CUTTER, MERGER, CONVERTER, RECORDER};

    public StudioAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case INDEX_CUTTER:
                Bundle bundle = new Bundle();
                bundle.putString(Statistic.CHECK_STUDIO_FRAGMENT, Statistic.DIR_APP + Statistic.DIR_CUTTER);
                return StudioFragmentDetail.newInstance(bundle);

            case INDEX_MERGER:
                Bundle b1 = new Bundle();
                b1.putString(Statistic.CHECK_STUDIO_FRAGMENT, Statistic.DIR_APP + Statistic.DIR_MERGER);
                return StudioFragmentDetail.newInstance(b1);

            case INDEX_SPEED:
                Bundle b2 = new Bundle();
                b2.putString(Statistic.CHECK_STUDIO_FRAGMENT, Statistic.DIR_APP + Statistic.DIR_SPEED);
                return StudioFragmentDetail.newInstance(b2);

            case INDEX_ADD_MUSIC:
                Bundle b3 = new Bundle();
                b3.putString(Statistic.CHECK_STUDIO_FRAGMENT, Statistic.DIR_APP + Statistic.DIR_ADD_MUSIC);
                return StudioFragmentDetail.newInstance(b3);
        }

        return null;
    }

    @Override
    public int getCount() {
        return listTab.length;
    }
}
