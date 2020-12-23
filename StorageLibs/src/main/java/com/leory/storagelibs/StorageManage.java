package com.leory.storagelibs;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
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
    private static MediaScannerConnection mScanner;

    /**
     * @param context
     * @param bitmap
     * @param appRootName app根目录名称
     * @param fileName    文件名，带后缀
     * @return 返回图片的路径
     */
    public static String saveBitmapToPublic(Context context, Bitmap bitmap, String appRootName, String fileName) {
        return saveBitmapToPublic(context, bitmap, appRootName, fileName, 100);
    }

    public static String saveBitmapToPublic(Context context, Bitmap bitmap, String appRootName, String fileName, int quality) {
        String imageType = "image/jpeg";
        Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;//压缩格式
        if (fileName.endsWith("png")) {
            format = Bitmap.CompressFormat.PNG;
            imageType = "image/png";
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, imageType);
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator + appRootName;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            File dir = new File(path);
            if (!dir.exists()) {
                Log.d(TAG, "创建目录 " + dir.mkdirs());
            }
            contentValues.put(MediaStore.MediaColumns.DATA, path + File.separator + fileName);//添加全路径
        } else {
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + File.separator + appRootName);
        }
        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        if (uri != null) {
            try {
                OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
                if (outputStream != null) {
                    bitmap.compress(format, quality, outputStream);
                    outputStream.flush();
                    outputStream.close();
                    Log.d(TAG, "图片保存成功" + (path + File.separator + fileName));
                    MediaScannerConnection.scanFile(context, new String[]{(path + File.separator + fileName)},
                            null, new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.d(TAG, "onScanCompleted: uri=" + uri);
                                    Log.d(TAG, "onScanCompleted: " + path);
                                }
                            });
                    return path + File.separator + fileName;

                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;

    }

    /**
     * 通过路径获取媒体的uri
     *
     * @param context
     * @param path
     * @return
     */
    public static Uri getMediaUriFromPath(Context context, String path) {
        Uri mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = context.getContentResolver().query(mediaUri,
                null,
                MediaStore.Images.Media.DISPLAY_NAME + "= ?",
                new String[]{path.substring(path.lastIndexOf("/") + 1)},
                null);

        Uri uri = null;
        if (cursor.moveToFirst()) {
            uri = ContentUris.withAppendedId(mediaUri,
                    cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID)));
        }
        cursor.close();
        return uri;
    }

    /**
     * 通过mediaUri获取bitmap
     *
     * @param context
     * @param uri
     * @return
     */
    public static Bitmap getBitmapFromMediaUri(Context context, Uri uri) {
        if (uri == null) return null;
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 根据media uri获取path
     *
     * @param context
     * @param uri
     * @return
     */
    public static String getPathFromMediaUri(Context context, Uri uri) {
        String path = null;
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }

            cursor.close();
        }
        return path;
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
     * 在主线程中调用此方法
     *
     * @param srcPath  源文件夹
     * @param destPath 目标文件夹
     * @return
     */
    public static boolean moveExternalStorageToPrivate(String srcPath, String destPath, onStorageMoveListener listener) {
        Handler mainHandler = new Handler();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Log.d(TAG, "isExternalStorageLegacy: " + Environment.isExternalStorageLegacy());
            if (!Environment.isExternalStorageLegacy()) {//采用分区，不能读写外部数据
                listener.noNeedMove();
                return false;
            }
        }
        if (srcPath == null || destPath == null) {
            listener.noNeedMove();
            return false;
        }

        srcFileCount = FileUtils.getFileCount(srcPath);
        long destFileCount = FileUtils.getFileCount(destPath);
        //如果源文件夹没有文件，则不需要迁移数据
        if (srcFileCount == 0) {
            listener.noNeedMove();
            return false;
        }
        listener.onMoveBegin();
        File srcDir = FileUtils.getFileByPath(srcPath);
        File destDir = FileUtils.getFileByPath(destPath);
        dealFileCount = 0;
        new Thread(new Runnable() {
            @Override
            public void run() {
                moveDir(mainHandler, srcDir, destDir, listener);
                Log.d(TAG, "迁移成功了");
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onMoveEnd();
                    }
                });

            }
        }).start();
        return true;
    }

    /**
     * 移动文件夹
     *
     * @param mainHandler 主线程handler
     * @param srcDir
     * @param destDir
     */
    private static void moveDir(Handler mainHandler, File srcDir, File destDir, onStorageMoveListener listener) {
        String destPath = destDir.getPath() + File.separator;
        FileUtils.createOrExistsDir(destPath);
        if (!srcDir.isDirectory() || !destDir.isDirectory()) return;
        File[] files = srcDir.listFiles();
        for (File file : files) {
            File oneDestFile = new File(destPath + file.getName());
            if (file.isDirectory()) {
                moveDir(mainHandler, file, oneDestFile, listener);
            } else {
                dealFileCount++;
                Log.d(TAG, "正在移动第" + dealFileCount + "个文件");
                if (srcFileCount != 0) {
                    float progress = 1f * dealFileCount / srcFileCount;
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onProgressUpgrade(progress);
                        }
                    });
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
     * 所有的回调都在主线程中
     */
    public interface onStorageMoveListener {
        void noNeedMove();//不需要移动

        void onProgressUpgrade(float progress);//移动中，progress百分比

        void onMoveBegin();//开始移动

        void onMoveEnd();//移动结束

    }
}
