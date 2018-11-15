package com.bs.videoeditor.fragment;

import android.os.Bundle;

/**
 * Created by Hung on 11/15/2018.
 */

public class AddMusicFragment extends AbsFragment {
    public static AddMusicFragment newInstance(Bundle bundle ) {
        AddMusicFragment fragment = new AddMusicFragment();
        fragment.setArguments(bundle);
        return fragment;
    }
    @Override
    public void initViews() {

    }
}
