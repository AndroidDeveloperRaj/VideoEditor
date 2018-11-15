package com.bs.videoeditor.fragment;

import android.os.Bundle;

/**
 * Created by Hung on 11/15/2018.
 */

public class StudioFragmentDetail extends AbsFragment {
    @Override
    public void initViews() {

    }

    public static StudioFragmentDetail newInstance(Bundle bundle) {

        Bundle args = new Bundle();

        StudioFragmentDetail fragment = new StudioFragmentDetail();
        fragment.setArguments(bundle);
        return fragment;
    }
}
