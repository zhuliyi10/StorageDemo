package com.leory.storagedemo;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 储存管理工具
 * @Author: leory
 * @Time: 2020/11/20
 */
public class StorageManage {
    private static int sBufferSize = 524288;
    private static long dealFileCount;
    private static long srcFileCount;
    private static final String TAG = StorageManage.class.getSimpleName();

    /**
     * 保存图片到共享空间
     *
     * @param context
     * @param bitmap
     * @param relativePath 保存相对目录，共享空间的根目录只能是
     *                     <ul>
     *                       <li>{@link Environment#DIRECTORY_MUSIC}
     *                       <li>{@link Environment#DIRECTORY_PODCASTS}
     *                       <li>{@link Environment#DIRECTORY_ALARMS}
     *                       <li>{@link Environment#DIRECTORY_RINGTONES}
     *                       <li>{@link Environment#DIRECTORY_NOTIFICATIONS}
     *                       <li>{@link Environment#DIRECTORY_PICTURES}
     *                       <li>{@link Environment#DIRECTORY_MOVIES}
     *                       <li>{@link Environment#DIRECTORY_DOWNLOADS}
     *                       <li>{@link Environment#DIRECTORY_DCIM}
     *                       <li>{@link Environment#DIRECTORY_DOCUMENTS}
     *                       <li>{@link Environment#DIRECTORY_AUDIOBOOKS}
     *                     </ul>
     * @return
     */
    public static boolean saveImageToShare(Context context, Bitmap bitmap, String fileName, String relativePath) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + relativePath;
            File dir = new File(path);
            if (!dir.exists()) {
                Log.d(TAG, "创建目录 " + dir.mkdirs());
            }
            contentValues.put(MediaStore.MediaColumns.DATA, path + fileName);//添加全路径
        } else {
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath);
        }
        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        if (uri != null) {
            try {
                OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.flush();
                    outputStream.close();
                    Log.d(TAG, "添加图片成功");
                    return true;

                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    /**
     * 获取图片列表
     *
     * @param context
     * @return uri集合
     */
    public static List<Uri> getImageFromShare(Context context) {
        return getMediaFromShare(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    }

    /**
     * 获取视频集合
     *
     * @param context
     * @return uri集合
     */
    public static List<Uri> getVideoFromShare(Context context) {
        return getMediaFromShare(context, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
    }

    /**
     * 获取音频集合
     *
     * @param context
     * @return uri集合
     */
    public static List<Uri> getAudioFromShare(Context context) {
        return getMediaFromShare(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
    }

    /**
     * 获取媒体列表
     *
     * @param context
     * @param mediaUri
     * @return uri集合
     */
    private static List<Uri> getMediaFromShare(Context context, Uri mediaUri) {
        List<Uri> mediaList = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(
                mediaUri
                , null
                , null
                , null
                , MediaStore.MediaColumns.DATE_ADDED + " desc"
        );
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
                String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
                Uri uri = ContentUris.withAppendedId(mediaUri, id);
                mediaList.add(uri);
                Log.d(TAG, "getMediaFromShare: fileName=" + fileName);
                Log.d(TAG, "getMediaFromShare: uri=" + uri.toString());
            }
        }
        return mediaList;
    }

    /**
     * 通过uri删除媒体
     *
     * @param context
     * @param uri
     * @return true删除成功，false删除失败
     */
    public static boolean delMediaFromShare(Context context, Uri uri) {
        int row = 0;
        try {
            row = context.getContentResolver().delete(uri, null, null);
        } catch (SecurityException securityException) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                //android 10及以上，如果是删除其他应用的uri，则需要用户授权，否则会抛出RecoverableSecurityException异常
            }
        }

        return row > 0;
    }


    /**
     * 把android 原外部数据移动到用户私有空间
     *
     * @param srcPath  源文件夹
     * @param destPath 目标文件夹
     * @return
     */
    public static boolean moveExternalStorageToPrivate(String srcPath, String destPath, onStorageMoveListener listener) {
        if (srcPath == null || destPath == null) return false;
        srcFileCount = FileUtils.getFileCount(srcPath);
        long destFileCount = FileUtils.getFileCount(destPath);
        //如果目标文件夹已经有文件，或者源文件夹没有文件，则不需要迁移数据
        if (destFileCount != 0 || srcFileCount == 0) return false;
        File srcDir = FileUtils.getFileByPath(srcPath);
        File destDir = FileUtils.getFileByPath(destPath);
        dealFileCount = 0;
        moveDir(srcDir, destDir, listener);

        Log.d(TAG, "迁移成功了");
        return true;
    }

    /**
     * 移动文件夹
     *
     * @param srcDir
     * @param destDir
     */
    private static void moveDir(File srcDir, File destDir, onStorageMoveListener listener) {
        String destPath = destDir.getPath() + File.separator;
        FileUtils.createOrExistsDir(destPath);
        if (!srcDir.isDirectory() || !destDir.isDirectory()) return;
        File[] files = srcDir.listFiles();
        for (File file : files) {
            File oneDestFile = new File(destPath + file.getName());
            if (file.isDirectory()) {
                moveDir(file, oneDestFile, listener);
            } else {
                dealFileCount++;
                Log.d(TAG, "正在移动第" + dealFileCount + "个文件");
                if (srcFileCount != 0) {
                    float progress = 1f * dealFileCount / srcFileCount;
                    listener.onProgressUpgrade(progress);
                    Log.d(TAG, "当前的进度: " + progress);
                }
                copyFile(file, oneDestFile);
                file.delete();
            }
        }
        srcDir.delete();
    }

    /**
     * 复制文件
     *
     * @param srcFile
     * @param destFile
     */
    private static void copyFile(File srcFile, File destFile) {
        OutputStream os;
        InputStream is;
        try {
            is = new FileInputStream(srcFile);
            os = new BufferedOutputStream(new FileOutputStream(destFile), sBufferSize);
            byte[] data = new byte[sBufferSize];
            for (int len; (len = is.read(data)) != -1; ) {
                os.write(data, 0, len);
            }
            is.close();
            os.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 数据移动监听器
     */
    public interface onStorageMoveListener {
        void onProgressUpgrade(float progress);
    }
}
