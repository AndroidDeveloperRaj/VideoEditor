package com.bs.videoeditor.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.inputmethod.InputMethodManager;

import com.bs.videoeditor.model.MusicModel;
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

    public static final String CONVERT_LONG_TO_DATE = "dd/MM/yyyy HH:mm:ss";
    private static Pattern pattern = Pattern.compile("time=([\\d\\w:]+)");
    public static final String[] listSpecialCharacter = new String[]{"%", "/", "#", "^", ":", "?", ","};

    public static List<VideoModel> filterVideoModel(List<VideoModel> videoEnities, String query) {
        String s = Utils.unAccent(query.toLowerCase());
        List<VideoModel> filteredModelList = new ArrayList<>();

        for (VideoModel videoModel : videoEnities) {
            String text = Utils.unAccent(videoModel.getNameAudio().toLowerCase());
            if (text.contains(s)) {
                filteredModelList.add(videoModel);
            }
        }
        return filteredModelList;
    }

    public static List<MusicModel> filterSong(List<MusicModel> musicModels, String query) {
        String s = Utils.unAccent(query.toLowerCase());
        List<MusicModel> filteredModelList = new ArrayList<>();

        for (MusicModel videoModel : musicModels) {
            String text = Utils.unAccent(videoModel.getTitle().toLowerCase());
            if (text.contains(s)) {
                filteredModelList.add(videoModel);
            }
        }
        return filteredModelList;
    }

    public static String convertDate(String dateInMilliseconds, String dateFormat) {
        return DateFormat.format(dateFormat, Long.parseLong(dateInMilliseconds)).toString();
    }

    public static List<MusicModel> getMusicFiles(Context context) {
        List<MusicModel> musicModelList = new ArrayList();
        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Uri ART_CONTENT_URI = Uri.parse("content://media/external/audio/albumart");
        String[] m_data = {MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DATE_MODIFIED
        };

        Cursor c = context.getContentResolver().query(uri, m_data, android.provider.MediaStore.Audio.Media.IS_MUSIC + "=1", null, android.provider.MediaStore.Audio.Media.TITLE + " ASC");

        if (c != null && c.moveToNext()) {
            do {
                String name, album, artist, path, id, audioType, dateModifier;
                String duration;
                int albumId, artistId;


                id = c.getString(c.getColumnIndex(MediaStore.Audio.Media._ID));
                name = c.getString(c.getColumnIndex(MediaStore.Audio.Media.TITLE));
                album = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                artist = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                path = c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATA));
                duration = c.getString(c.getColumnIndex(MediaStore.Audio.Media.DURATION));
                albumId = c.getInt(c.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                dateModifier = c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED));
                String imagePath = ContentUris.withAppendedId(ART_CONTENT_URI, albumId).toString();

                MusicModel audio = new MusicModel(id, name, path, Long.parseLong(duration));

                try {
                    if (duration != null && Long.parseLong(duration) > 1000 && path != null
                            && !path.contains(".flac") && !path.contains(".ac3")
                            && !path.contains(".ape")
                            ) {
                        musicModelList.add(audio);
                    }
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }

            } while (c.moveToNext());
        }

        if (c != null) {
            c.close();
        }
        return musicModelList;
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

    public static List<VideoModel> getVideos(Context context, int sortOrder, String removeSpeedVideos, boolean isGetVideosSpeed) {

        String sort = SortOrder.SongSortOrder.SONG_A_Z;

        switch (sortOrder) {
            case SortOrder.ID_SONG_A_Z:
                sort = SortOrder.SongSortOrder.SONG_A_Z;
                break;

            case SortOrder.ID_SONG_Z_A:
                sort = SortOrder.SongSortOrder.SONG_Z_A;
                break;

            case SortOrder.ID_SONG_DATE_ADDED:
                sort = SortOrder.SongSortOrder.SONG_DATE;
                break;

            case SortOrder.ID_SONG_DATE_ADDED_DESCENDING:
                sort = SortOrder.SongSortOrder.SONG_DATE_DESC;
                break;
        }


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
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_ADDED

        };

        Cursor c = context.getContentResolver().query(uri, m_data, null, null, sort);

        if (c != null && c.moveToNext()) {
            do {
                String name, album, artist, path, id, duration, resolution, dateAdded;
                long size;

                id = c.getString(c.getColumnIndex(MediaStore.Video.Media._ID));
                name = c.getString(c.getColumnIndex(MediaStore.Video.Media.TITLE));
                album = c.getString(c.getColumnIndex(MediaStore.Video.Media.ALBUM));
                artist = c.getString(c.getColumnIndex(MediaStore.Video.Media.ARTIST));
                path = c.getString(c.getColumnIndex(MediaStore.Video.Media.DATA));
                duration = c.getString(c.getColumnIndex(MediaStore.Video.Media.DURATION));
                resolution = c.getString(c.getColumnIndex(MediaStore.Video.Media.RESOLUTION));
                size = c.getLong(c.getColumnIndex(MediaStore.Video.Media.SIZE));
                dateAdded = c.getString(c.getColumnIndex(MediaStore.Video.Media.DATE_ADDED));
                Flog.e(" path       " + path);
                try {
                    if (duration != null && path != null && Long.parseLong(duration) > 0) {
                        VideoModel video = new VideoModel(id, name, artist, album, duration, path, resolution, size, dateAdded + "000");
                        if (isGetVideosSpeed) {
                            listVideo.add(video);
                        } else {
                            if (removeSpeedVideos != null && !path.contains(removeSpeedVideos)) {
                                listVideo.add(video);
                            }
                        }
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

    public static List<VideoModel> getStudioVideos(Context context, String section, int sortOrder) {
        String sort = SortOrder.SongSortOrder.SONG_DATE_DESC;

        switch (sortOrder) {
            case SortOrder.ID_SONG_A_Z:
                sort = SortOrder.SongSortOrder.SONG_A_Z;
                break;

            case SortOrder.ID_SONG_Z_A:
                sort = SortOrder.SongSortOrder.SONG_Z_A;
                break;

            case SortOrder.ID_SONG_DATE_ADDED:
                sort = SortOrder.SongSortOrder.SONG_DATE;
                break;

            case SortOrder.ID_SONG_DATE_ADDED_DESCENDING:
                sort = SortOrder.SongSortOrder.SONG_DATE_DESC;
                break;
        }

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
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_ADDED

        };

        Cursor c = context.getContentResolver().query(uri, m_data, null, null, sort);

        if (c != null && c.moveToNext()) {
            do {
                String name, album, artist, path, id, duration, resolution, dateAdded;
                long size;

                id = c.getString(c.getColumnIndex(MediaStore.Video.Media._ID));
                name = c.getString(c.getColumnIndex(MediaStore.Video.Media.TITLE));
                album = c.getString(c.getColumnIndex(MediaStore.Video.Media.ALBUM));
                artist = c.getString(c.getColumnIndex(MediaStore.Video.Media.ARTIST));
                path = c.getString(c.getColumnIndex(MediaStore.Video.Media.DATA));
                duration = c.getString(c.getColumnIndex(MediaStore.Video.Media.DURATION));
                resolution = c.getString(c.getColumnIndex(MediaStore.Video.Media.RESOLUTION));
                size = c.getLong(c.getColumnIndex(MediaStore.Video.Media.SIZE));
                dateAdded = c.getString(c.getColumnIndex(MediaStore.Video.Media.DATE_ADDED));
                try {
                    if (duration != null && path != null && path.contains(section)) {
                        VideoModel video = new VideoModel(id, name, artist, album, String.valueOf(duration), path, resolution, size, dateAdded + "000");
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

    public static void clearFragment(android.support.v4.app.FragmentManager fragmentManager) {
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
