package com.leory.storagedemo;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.hjq.permissions.OnPermission;
import com.hjq.permissions.XXPermissions;
import com.leory.storagelibs.FileUtils;
import com.leory.storagelibs.StorageManage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    String rootPath = "leory";
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE
            , Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.INTERNET
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnAddShare = findViewById(R.id.btn_add_share);
        Button btnShowShare = findViewById(R.id.btn_show_share);
        Button btnMoveData = findViewById(R.id.btn_move_data);
        Button btnFile = findViewById(R.id.btn_file);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Log.d(TAG, "isExternalStorageLegacy: " + Environment.isExternalStorageLegacy());
        }
        XXPermissions.with(MainActivity.this)
                .permission(PERMISSIONS_STORAGE)
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean all) {
                        Log.d(TAG, "所有权限获取成功");
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean never) {
                        if (never) {
                            Log.d(TAG, "被永久拒绝授权，请手动授予存储权限");
                        } else {
                            Log.d(TAG, "noPermission: ");
                        }
                    }
                });

        btnAddShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage(BitmapFactory.decodeResource(getResources(), R.mipmap.baidu));
//                String picPath = StorageManage.saveBitmapToPublic(MainActivity.this,
//                        BitmapFactory.decodeResource(getResources(), R.mipmap.baidu),
//                        rootPath,
//                        System.currentTimeMillis() + ".png");
//
//                if (picPath != null) {
//                    toast("图片保存成功");
//                } else {
//                    toast("图片保存失败");
//                }
            }
        });
        btnShowShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StorageManage.getImageFromShare(MainActivity.this);
            }
        });
        btnMoveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StorageManage.moveExternalStorageToPrivate(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + rootPath
                        , getExternalFilesDir("").getAbsolutePath() + File.separator + rootPath, new StorageManage.onStorageMoveListener() {
                            @Override
                            public void noNeedMove() {

                            }

                            @Override
                            public void onProgressUpgrade(float progress) {
                                Log.d(TAG, "onProgressUpgrade: " + progress);
                            }

                            @Override
                            public void onMoveBegin() {

                            }

                            @Override
                            public void onMoveEnd() {

                            }
                        });
            }
        });

        btnFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickFile();
            }
        });
    }

    /**
     * 打开文件
     */
    private void openFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
//        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {
//                "image/*"
////                "application/pdf", // .pdf
////                "application/vnd.oasis.opendocument.text", // .odt
////                "text/plain" // .txt
//        });
        startActivityForResult(intent, 1);
    }

    /**
     * 简单分享
     */
    private void shareSheet() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
        sendIntent.setType("text/plain");
//        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(sendIntent);
    }

    /**
     * 选择文件
     */
    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    private void saveImage(Bitmap bitmap) {
//        File fileDir = getExternalFilesDir(rootPath);
        File fileDir = new File(Environment.getExternalStorageDirectory() + File.separator + rootPath);
//        Log.d(TAG, "saveImage: "+fileDir);
//        Log.d(TAG, "fileDir: exists:"+fileDir.exists());
        if (!fileDir.exists()) {
            Log.d(TAG, "createFile: " + fileDir.mkdir());
        }
        String fileName = "baidu.jpg";
        File file = new File(fileDir, fileName);
//        if (file.exists()) {
//            file.delete();
//            Log.d(TAG, "delete: ");
//        } else {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            MediaScannerConnection.scanFile(this, new String[]{(fileDir + File.separator + fileName)},
                    null, new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            Log.d(TAG, "onScanCompleted: uri=" + uri);
                            Log.d(TAG, "onScanCompleted: " + path);
                        }
                    });
//                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + fileDir + File.separator + fileName)));
            fos.flush();
            fos.close();
            toast("图片添加成功");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }

    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

}