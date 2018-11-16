package com.bs.videoeditor.utils;

import android.os.Environment;

import java.io.File;

public class Keys {
    public static final String DIR_APP = "/BMusicEditor";
    public static final String DIR_CONVERTER = "/Converter";
    public static final String DIR_CUTTER = "/Cutter";
    public static final String DIR_MERGER = "/Merger";
    public static final String DIR_RECORDER = "/Recorder";
    public static final String START_SERVICE = "start_service";
    public static final String STOP_SERVICE = "stop_servcie";
    public static final String TIME = "time";
    public static final String FILE_SIZE = "file_size";
    public static final String STOP_RECORD = "stop_record";
    public static final String TITLE = "title";
    public static final String FILE_NAME = "file_name";
    public static final String CHECK_OPEN_STUDIO = "open_studio";
    public static final int FROM_RECORDER = 1;
    public static final int FROM_MAIN = 0;
    public static final String CHECK_STUDIO_FRAGMENT = "studio_fragment";
    public static final int STUDIO_CUTTER = 0;
    public static final int STUDIO_MERGER = 1;
    public static final int STUDIO_CONVERTER = 2;
    public static final int STUDIO_RECORDER = 3;
    public static final String UPDATE_SELECT_SONG = "update_select_song";
    public static final String LIST_SONG = "list__song";
    public static final String UPDATE_LIST_STUDIO = "update_list_studio";
    public static final String DURATION = "duration";
    public static final String CLEAR_LIST_AUDIO_MERGER = "clear_merger_list";
    public static final String OPEN_FRAGMENT = "open_fragment";
    public static final String OPEN_STUDIO_CONVERTER = "open_studio_converter";
    public static final String OPEN_STUDIO_CUTTER = "open_studio_cutter";
    public static final String UPDATE_DELETE_RECORD = "update_delete_audio";
    public static final String CLEAR_ACTION_MODE = "clear_action_mode";
    public static final String FORMAT_RECORDER = "format_record";
    public static final String BITRATE_RECORDER = "bitrate_record";
    public static final String UPDATE_CHOOSE_AUDIO = "choose_audio";
    public static final String MODEL = "model";
    public static final String STATE_SCREEN_OFF = "screen_off";
    public static File mSdCard = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
    public static File APP_DIRECTORY = new File(mSdCard, "BVideoEditor");
    public static final File TEMP_DIRECTORY = new File(APP_DIRECTORY, ".temp");
}
