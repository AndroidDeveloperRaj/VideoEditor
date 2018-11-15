package com.bs.videoeditor.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;

import com.bs.videoeditor.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by tanpt on 21/08/2017.
 */

public class VideoUtil {
    public static final String VIDEO_FORMAT = ".mp4";

    public static String normalizeFileName(String name) {
        int pos = name.lastIndexOf(".");
        if (pos != -1) {
            return name.substring(0, pos);
        }
        return name;
    }

    /**
     *
     * @param filePath
     * @return duration of video file or -1 if fail to retrieve duration
     */
    public static int getDurationVideo(String filePath) {
        MediaMetadataRetriever metaInfo = new MediaMetadataRetriever();
        int duration = -1;
        try {
            metaInfo.setDataSource(filePath);
            duration = Integer.valueOf(metaInfo.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            metaInfo.release();
        }

        return duration;
    }

    public static int rename(Context context, String newName, String newPath, long id) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.TITLE, newName);
        values.put(MediaStore.Video.Media.DATA, newPath);
        return context.getContentResolver().update(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                values, MediaStore.Video.Media._ID + "=?", new String[] {String.valueOf(id)}
        );
    }

    public static void modifyDurationVideo(int id, int newDuration, ContentResolver contentResolver) {
        ContentValues cv = new ContentValues(1);
        cv.put(MediaStore.Video.Media.DURATION, newDuration);
        contentResolver.update(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                cv, MediaStore.Video.Media._ID + "=" + id, null
        );
    }


    public static void showDialog(Context context, int title, int content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(content)
                .setPositiveButton(R.string.ok, null)
                .setCancelable(true)
                .show();
    }

    public static void showDialog(Context context, int title, int content, DialogInterface.OnClickListener callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(content)
                .setPositiveButton(R.string.ok, callback)
                .setCancelable(false)
                .show();
    }

    public static long delete(Context context, long videoId) {
        if (context == null || videoId == -1) return videoId;

        return context.getContentResolver().delete(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Video.Media._ID + " ='" + videoId + "'",
                null
        );
    }

    public static Bitmap convertToBitmap(int[] data, int width, int height) {
        return Bitmap.createBitmap(data, width, height, Bitmap.Config.ARGB_8888);
    }

    public static int getDurationVideo(Uri uri, Context context) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//use one of overloaded setDataSource() functions to set your data source
        retriever.setDataSource(context, uri);
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        return (int) (Long.parseLong(time) / 1000);
    }

    public static String normalizeTime(long time) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(time),
                TimeUnit.MILLISECONDS.toSeconds(time) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time))
        );
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static String getDefaultVideoPath() {
        return Environment.getExternalStorageDirectory().getPath() + "/Bsoft/Reverse Video";
    }

    public static String getVideoPathFromPref(Context c) {
//        String path = SharedPrefsUtils.getStringPreference(c, Keys.Pref.VIDEO_PATH_KEY, getDefaultVideoPath());
//        File file = new File(path);
//        if (!file.exists()) {
//            file.mkdirs();
//        }
//        return path;
        return null;
    }

    private static String getDefaultVideoName() {
        return "Reverse_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }

    public static String getDefaultOutputPath(Context context) {
        return getVideoPathFromPref(context) + "/" + getDefaultVideoName() + VIDEO_FORMAT;

    }
}
