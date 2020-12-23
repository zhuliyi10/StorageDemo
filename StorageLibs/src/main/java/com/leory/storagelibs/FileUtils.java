package com.leory.storagelibs;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Build;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @Description: 文件处理工具
 * @Author: leory
 * @Time: 2020/11/20
 */
public class FileUtils {
    /**
     * 文件是否存在
     *
     * @param context
     * @param filePath 文件path
     * @return true存在 ,false不存在
     */
    public static boolean isFileExists(Context context, final String filePath) {
        File file = getFileByPath(filePath);
        return isFileExists(context,file);
    }
    public static boolean isFileExists(Context context, File file) {
        if (file == null) return false;
        if (file.exists()) {
            return true;
        }
        return isFileExistsApi29(context, file.getPath());
    }
    private static boolean isFileExistsApi29(Context context, String filePath) {
        if (Build.VERSION.SDK_INT >= 29) {
            try {
                Uri uri = Uri.parse(filePath);
                AssetFileDescriptor afd = context.getContentResolver().openAssetFileDescriptor(uri, "r");
                if (afd == null) return false;
                try {
                    afd.close();
                } catch (IOException ignore) {
                }
            } catch (FileNotFoundException e) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 根据文件路径获取File
     *
     * @param filePath
     * @return
     */
    public static File getFileByPath(final String filePath) {
        return filePath == null ? null : new File(filePath);
    }

    /**
     * 获取目录下包含文件数量
     *
     * @param dirPath 目录路径
     * @return
     */
    public static long getFileCount(final String dirPath) {
        return getFileCount(getFileByPath(dirPath));
    }

    private static long getFileCount(final File dir) {
        if (dir == null) return 0;
        if (!dir.isDirectory()) return 0;
        long len = 0;
        File[] files = dir.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.isDirectory()) {
                    len += getFileCount(file);
                } else {
                    len += 1;
                }
            }
        }
        return len;
    }

    /**
     * dirPath是否是文件夹
     * @param dirPath
     * @return
     */
    public static boolean isDir(final String dirPath) {
        return isDir(getFileByPath(dirPath));
    }

    /**
     * file 是否是文件夹
     * @param file
     * @return
     */
    public static boolean isDir(final File file) {
        return file != null && file.exists() && file.isDirectory();
    }

    /**
     * 创建文件夹
     * @param dirPath
     * @return
     */
    public static boolean createOrExistsDir(final String dirPath) {
        return createOrExistsDir(getFileByPath(dirPath));
    }

    /**
     * 创建文件夹
     * @param file
     * @return
     */
    public static boolean createOrExistsDir(final File file) {
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }
}
