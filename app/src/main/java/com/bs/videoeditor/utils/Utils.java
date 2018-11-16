package com.bs.videoeditor.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.inputmethod.InputMethodManager;

import com.bs.videoeditor.model.VideoModel;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Hung on 11/15/2018.
 */

public class Utils {

    private static Pattern pattern = Pattern.compile("time=([\\d\\w:]+)");
    public static final String[] listSpecialCharacter = new String[]{"%", "/", "#", "^", ":", "?", ","};

    public static List<VideoModel> filterVideoModel(List<VideoModel> videoEnities, String query) {
        String s = Utils.unAccent(query.toLowerCase());
        List<VideoModel> filteredModelList = new ArrayList<>();

        for (VideoModel audio : videoEnities) {
            String text = Utils.unAccent(audio.getNameAudio().toLowerCase());
            if (text.contains(s)) {
                filteredModelList.add(audio);
            }
        }
        return filteredModelList;
    }


    public static int getMediaDuration(String filePath) {
        MediaMetadataRetriever metaInfo = new MediaMetadataRetriever();
        int duration = -1;
        try {
            metaInfo.setDataSource(filePath);
            duration = Integer.valueOf(metaInfo.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        } catch (Exception e) {
            e.printStackTrace();
            metaInfo.release();
            return -1;
        } finally {
            metaInfo.release();
        }
        return duration;
    }


    public static String unAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replaceAll("Đ", "D").replaceAll("đ", "d");
    }

    public static void closeKeyboard(Activity activity) {
        if (activity != null && activity.getCurrentFocus() != null && activity.getCurrentFocus().getWindowToken() != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
            }
        }
    }


    public static String getStringSizeLengthFile(long size) {

        DecimalFormat df = new DecimalFormat("0.00");

        float sizeKb = 1024.0f;
        float sizeMo = sizeKb * sizeKb;
        float sizeGo = sizeMo * sizeKb;
        float sizeTerra = sizeGo * sizeKb;


        if (size < sizeMo)
            return df.format(size / sizeKb) + " Kb";
        else if (size < sizeGo)
            return df.format(size / sizeMo) + " Mb";
        else if (size < sizeTerra)
            return df.format(size / sizeGo) + " Gb";

        return "";
    }

    public static boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void deleteAudio(Context context, String path) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Files.getContentUri("external");
        int result = contentResolver.delete(uri,
                MediaStore.Files.FileColumns.DATA + "=?", new String[]{path});
    }

    public static long getDurationFile(String pathStr, Context context) {
        // Uri uri = Uri.parse(pathStr);
        long duration;
        MediaPlayer mp = MediaPlayer.create(context, Uri.parse(pathStr));
        if (mp == null) {
            duration = 0;
        } else {
            duration = mp.getDuration();
        }

        //long timeInMillisec = Long.parseLong(time );
        //long millSecond = Integer.parseInt(timeInMillisec);
        return duration;
    }

    public static double getFolderSize(File f) {
        double size = 0;
        if (f.isDirectory()) {
            for (File file : f.listFiles()) {
                size += getFolderSize(file);
            }
        } else {
            size = f.length();
        }
        return size;
    }

    public static String readSizeFile(double path) {
        String valueFile = null;

        double Filesize = path / 1024;

        if (Filesize >= 1000) {
            BigDecimal rowOff = new BigDecimal(Filesize / 1024).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            valueFile = rowOff + " Mb";
        } else {
            BigDecimal rowOff = new BigDecimal(Filesize).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            valueFile = rowOff + " Kb";
        }
        return valueFile;
    }

    public static long getProgress(String message, long totalDuration) {
        if (message.contains("speed")) {
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                String tempTime = String.valueOf(matcher.group(1));
                // FLog.d("getProgress: tempTime " + tempTime);
                String[] arrayTime = tempTime.split(":");
                long currentTime =
                        TimeUnit.HOURS.toSeconds(Long.parseLong(arrayTime[0]))
                                + TimeUnit.MINUTES.toSeconds(Long.parseLong(arrayTime[1]))
                                + Long.parseLong(arrayTime[2]);

                int percent = (int) (100 * currentTime / totalDuration);

                //  FLog.d("currentTime -> " + currentTime + "s % -> " + percent);

                return currentTime;
            }
        }
        return 0;
    }

    public static List<VideoModel> getVideos(Context context) {
        Uri uri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Uri ART_CONTENT_URI = Uri.parse("content://media/external/audio/albumart");
        List<VideoModel> listVideo = new ArrayList<>();
        String[] m_data = {MediaStore.Audio.Media._ID,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.ARTIST,
                MediaStore.Video.Media.ALBUM,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.RESOLUTION,
                MediaStore.Video.Media.SIZE
        };

        Cursor c = context.getContentResolver().query(uri, m_data, null, null, MediaStore.Video.Media.DATE_ADDED + " DESC ");

        if (c != null && c.moveToNext()) {
            do {
                String name, album, artist, path, id, duration, resolution;
                long size;

                id = c.getString(c.getColumnIndex(MediaStore.Video.Media._ID));
                name = c.getString(c.getColumnIndex(MediaStore.Video.Media.TITLE));
                album = c.getString(c.getColumnIndex(MediaStore.Video.Media.ALBUM));
                artist = c.getString(c.getColumnIndex(MediaStore.Video.Media.ARTIST));
                path = c.getString(c.getColumnIndex(MediaStore.Video.Media.DATA));
                duration = c.getString(c.getColumnIndex(MediaStore.Video.Media.DURATION));
                resolution = c.getString(c.getColumnIndex(MediaStore.Video.Media.RESOLUTION));
                size = c.getLong(c.getColumnIndex(MediaStore.Video.Media.SIZE));
                Flog.e(" ssssssssss  " + path + duration);
                try {
                    if (duration != null && path != null) {
                        Flog.e(" nottt  " + path + duration);
                        VideoModel video = new VideoModel(id, name, artist, album, String.valueOf(duration), path, resolution, size);
                        listVideo.add(video);
                    } else {
                        Flog.e(" bbbbbbb  " + path + duration);
                    }
                } catch (Exception e) {
                    Flog.e(" bbbbbxxxxxxxxxxxbb  " + path + duration);
                    e.printStackTrace();
                }

            } while (c.moveToNext());
        }
        if (c != null) {
            c.close();
        }
        return listVideo;
    }

    public static List<VideoModel> getStudioVideos(Context context, String section) {
        Uri uri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Uri ART_CONTENT_URI = Uri.parse("content://media/external/audio/albumart");
        List<VideoModel> listVideo = new ArrayList<>();
        String[] m_data = {MediaStore.Audio.Media._ID,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.ARTIST,
                MediaStore.Video.Media.ALBUM,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.RESOLUTION,
                MediaStore.Video.Media.SIZE
        };

        Cursor c = context.getContentResolver().query(uri, m_data, null, null, MediaStore.Video.Media.DATE_ADDED + " DESC ");

        if (c != null && c.moveToNext()) {
            do {
                String name, album, artist, path, id, duration, resolution;
                long size;

                id = c.getString(c.getColumnIndex(MediaStore.Video.Media._ID));
                name = c.getString(c.getColumnIndex(MediaStore.Video.Media.TITLE));
                album = c.getString(c.getColumnIndex(MediaStore.Video.Media.ALBUM));
                artist = c.getString(c.getColumnIndex(MediaStore.Video.Media.ARTIST));
                path = c.getString(c.getColumnIndex(MediaStore.Video.Media.DATA));
                duration = c.getString(c.getColumnIndex(MediaStore.Video.Media.DURATION));
                resolution = c.getString(c.getColumnIndex(MediaStore.Video.Media.RESOLUTION));
                size = c.getLong(c.getColumnIndex(MediaStore.Video.Media.SIZE));
                try {
                    if (duration != null && path != null && path.contains(section)) {
                        VideoModel video = new VideoModel(id, name, artist, album, String.valueOf(duration), path, resolution, size);
                        listVideo.add(video);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } while (c.moveToNext());
        }
        if (c != null) {
            c.close();
        }
        return listVideo;
    }

    public static void clearFragment(android.support.v4.app.FragmentManager fragmentManager){
        //Here we are clearing back stack fragment entries

        if (fragmentManager == null) {
            return;
        }

        int backStackEntry = fragmentManager.getBackStackEntryCount();
        if (backStackEntry > 0) {
            for (int i = 0; i < backStackEntry; i++) {
                try {
                    fragmentManager.popBackStackImmediate();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    return;
                }
            }
        } else {
            return;
        }

        //Here we are removing all the fragment that are shown here
        if (fragmentManager.getFragments() != null && fragmentManager.getFragments().size() > 0) {
            for (int i = 0; i < fragmentManager.getFragments().size(); i++) {
                Fragment mFragment = fragmentManager.getFragments().get(i);
                if (mFragment != null) {
                    fragmentManager.beginTransaction().remove(mFragment).commit();
                }
            }
        } else {
            return;
        }
    }

    public static boolean isStringHasCharacterSpecial(String text) {
        for (int i = 0; i < listSpecialCharacter.length; i++) {
            if (text.contains(listSpecialCharacter[i])) {
                return true;
            }
        }
        return false;
    }


    // return
    public static String getFileExtension(String path) {

        int lastIndexOf = path.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return "." + path.substring(lastIndexOf + 1);
    }


    public static String convertMillisecond(long millisecond) {
        long sec = (millisecond / 1000) % 60;
        long min = (millisecond / (60 * 1000)) % 60;
        long hour = millisecond / (60 * 60 * 1000);

        String s = (sec < 10) ? "0" + sec : "" + sec;
        String m = (min < 10) ? "0" + min : "" + min;
        String h = "" + hour;

        String time = "";
        if (hour > 0) {
            time = h + ":" + m + ":" + s;
        } else {
            time = m + ":" + s;
        }

        return time;
    }
}
