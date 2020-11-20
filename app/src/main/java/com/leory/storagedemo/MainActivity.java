package com.leory.storagedemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.hjq.permissions.OnPermission;
import com.hjq.permissions.XXPermissions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    String rootPath = "leory";
    private static String[] PERMISSIONS_STORAGE = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnAddShare = findViewById(R.id.btn_add_share);
        Button btnShowShare = findViewById(R.id.btn_show_share);
        Button btnMoveData = findViewById(R.id.btn_move_data);
        btnAddShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XXPermissions.with(MainActivity.this)
                        .permission(PERMISSIONS_STORAGE)
                        .request(new OnPermission() {
                            @Override
                            public void hasPermission(List<String> granted, boolean all) {
                                Log.d(TAG, "所有权限获取成功");

                                boolean isSuccess = StorageManage.saveImageToShare(MainActivity.this,
                                        BitmapFactory.decodeResource(getResources(), R.mipmap.baidu),
                                        System.currentTimeMillis() + "",
                                        Environment.DIRECTORY_DCIM + File.separator + rootPath);
                                if (isSuccess) {
                                    toast("图片保存成功");
                                } else {
                                    toast("图片保存失败");
                                }
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
                            public void onProgressUpgrade(float progress) {
                                Log.d(TAG, "onProgressUpgrade: "+progress);
                            }
                        });
            }
        });


    }

    private void saveImage(Bitmap bitmap) {
        File fileDir = getExternalFilesDir(rootPath);
//        Log.d(TAG, "saveImage: "+fileDir);
//        Log.d(TAG, "fileDir: exists:"+fileDir.exists());
//        if (!fileDir.exists()) {
//            Log.d(TAG, "createFile: " + fileDir.mkdir());
//        }
        String fileName = "baidu.png";
        File file = new File(fileDir, fileName);
        if (file.exists()) {
            file.delete();
            Log.d(TAG, "delete: ");
        } else {
            Log.d(TAG, "save: ");
            try {
                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

}