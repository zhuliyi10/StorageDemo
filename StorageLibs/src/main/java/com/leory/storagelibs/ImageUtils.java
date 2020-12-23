package com.leory.storagelibs;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @Description: 图片操作工具
 * @Author: leory
 * @Time: 2020/11/25
 */
public class ImageUtils {
    /**
     * 通过mediaPath获取bitmap
     * @param context
     * @param mediaPath
     * @param maxWidth
     * @param maxHeight
     * @return
     */
    public static Bitmap getBitmap(Context context, String mediaPath, float maxWidth, float maxHeight){
        return getBitmap(context,getMediaUriFromPath(context,mediaPath),maxWidth,maxHeight);
    }
    /**
     * 通过图片的mediaUri获取bitmap 原图尺寸
     *
     * @param context
     * @param mediaUri
     * @return
     */
    public static Bitmap getBitmap(Context context, Uri mediaUri) {
        return getBitmap(context, mediaUri, -1, -1);
    }

    /**
     * 通过图片的mediaUri获取bitmap
     *
     * @param context
     * @param mediaUri  图片的uri
     * @param maxWidth  最大宽度
     * @param maxHeight 最大高度
     * @return bitmap
     */
    public static Bitmap getBitmap(Context context, Uri mediaUri, float maxWidth, float maxHeight) {
        if (mediaUri == null) return null;
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(mediaUri);
            if (maxWidth == -1 || maxHeight == -1) {
                return BitmapFactory.decodeStream(inputStream);
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            int height = options.outHeight;
            int width = options.outWidth;
            int inSampleSize = 1;
            while (height > maxHeight || width > maxWidth) {
                height >>= 1;
                width >>= 1;
                inSampleSize <<= 1;
            }
            options.inJustDecodeBounds=false;
            options.inSampleSize = inSampleSize;
            options.inDither= true;
            options.inPreferredConfig= Bitmap.Config.ARGB_8888;
            inputStream=context.getContentResolver().openInputStream(mediaUri);
            Bitmap bitmap= BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();
            return bitmap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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
        String[] projection = new String[] {
                MediaStore.Video.Media._ID
        };
        Uri mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = context.getContentResolver().query(mediaUri,
                projection,
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
}
