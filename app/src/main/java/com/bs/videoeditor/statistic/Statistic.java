package com.bs.videoeditor.statistic;

import android.os.Environment;

import java.io.File;

/**
 * Created by Hung on 11/15/2018.
 */

public class Statistic {

    public static final String DIR_APP = "/BVideoEditor";
    //    public static final String DIR_CONVERTER = "/Converter";
    public static final String DIR_CUTTER = "/Cutter";
    public static final String DIR_SPEED = "/Speed";
    public static final String DIR_MERGER = "/Merger";
    public static final String DIR_ADD_MUSIC = "/AddMusic";
    public static final String ACTION = "actrion";
    public static final String PATH_VIDEO = "path_video";
    public static final String VIDEO_MODEL = "video_model";
    public static final String CHECK_STUDIO_FRAGMENT = "check_studio";
    public static final String CLEAR_ACTION_MODE = "clear_action_mode";
    public static final String OPEN_CUTTER_STUDIO = "open_cutter_studio";
    public static final String CHECK_OPEN_STUDIO = "add_studio";
    public static final int FROM_MAIN = 0;
    public static final String OPEN_FRAGMENT = "open_fragment";
    public static final int INDEX_CUTTER = 0;
    public static final int INDEX_SPEED = 1;
    public static final int INDEX_MERGER = 2;
    public static final int INDEX_ADD_MUSIC = 3;
    public static final String LIST_VIDEO = "list_video";
    public static final String DURATION = "duration_video_merger";
    public static final String UPDATE_CHOOSE_VIDEO = "update_choose_video";
    public static final String MODEL = "model";
    public static final String EXTENSION_MP4 = ".mp4";
    public static final String OPEN_MERGER_STUDIO = "open_merger_studio";
    public static final String OPEN_SPEED_STUDIO = "open_speed_studio";
    public static final String OPEN_ADD_MUSIC_STUDIO = "open_music_studio";
    public static final String UPDATE_DELETE_RECORD = "update_delete_video";
    public static final String MUSIC = "music";
    public static final String SEND_PATH_ADD_MUSIC = "send_path_add_music";
    public static final String PATH_MUSIC = "path_music";
    public static final String FORMAT_MP4 = ".mp4";
    public static final String ARRAY_COMMAND ="command" ;
    public static final String TITLE_VIDEO ="title_video" ;
    public static File mSdCard = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
    public static File APP_DIRECTORY = new File(mSdCard, "BVideoEditor");
    public static final File TEMP_DIRECTORY = new File(APP_DIRECTORY, ".temp");
    public static final File TEMP_DIRECTORY_AUDIO = new File(APP_DIRECTORY, ".temp_audio");
    public static long mDeleteFileCount = 0;

    public static boolean deleteFile(File mFile) {
        boolean idDelete = false;
        if (mFile == null) {
//            return 0;
        }
        if (mFile.exists()) {
            if (mFile.isDirectory()) {
                File[] children = mFile.listFiles();
                if (children != null && children.length > 0) {
                    for (File child : children) {
                        mDeleteFileCount += child.length();
                        idDelete = deleteFile(child);
                    }
                }
                mDeleteFileCount += mFile.length();
                idDelete = mFile.delete();
            } else {
                mDeleteFileCount += mFile.length();
                idDelete = mFile.delete();
            }
        }
        return idDelete;
    }
}
