package com.donkingliang.imageselectdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;

import com.donkingliang.imageselectdemo.adapter.ImageAdapter;
import com.donkingliang.imageselector.utils.ImageSelector;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE = 0x00000011;
    private static final int PERMISSION_WRITE_EXTERNAL_REQUEST_CODE = 0x00000012;

    private RecyclerView rvImage;
    private ImageAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvImage = findViewById(R.id.rv_image);
        rvImage.setLayoutManager(new GridLayoutManager(this, 3));
        mAdapter = new ImageAdapter(this);
        rvImage.setAdapter(mAdapter);

        findViewById(R.id.btn_single).setOnClickListener(this);
        findViewById(R.id.btn_limit).setOnClickListener(this);
        findViewById(R.id.btn_unlimited).setOnClickListener(this);
        findViewById(R.id.btn_clip).setOnClickListener(this);
        findViewById(R.id.btn_only_take).setOnClickListener(this);
        findViewById(R.id.btn_take_and_clip).setOnClickListener(this);

        int hasWriteExternalPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteExternalPermission == PackageManager.PERMISSION_GRANTED) {
            //预加载手机图片。加载图片前，请确保app有读取储存卡权限
            ImageSelector.preload(this);
        } else {
            //没有权限，申请权限。
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE_EXTERNAL_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && data != null) {
            ArrayList<String> images = data.getStringArrayListExtra(ImageSelector.SELECT_RESULT);
            boolean isCameraImage = data.getBooleanExtra(ImageSelector.IS_CAMERA_IMAGE, false);
            System.out.println(images.get(0));
            mAdapter.refresh(images);
        }
    }

    /**
     * 处理权限申请的回调。
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_WRITE_EXTERNAL_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //预加载手机图片
                ImageSelector.preload(this);
            } else {
                //拒绝权限。
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_single:
                //单选
                ImageSelector.builder()
                        .useCamera(true) // 设置是否使用拍照
                        .setSingle(true)  //设置是否单选
                        .canPreview(true) //是否点击放大图片查看,，默认为true
                        .start(this, REQUEST_CODE); // 打开相册
                break;

            case R.id.btn_limit:
                //多选(最多9张)
                ImageSelector.builder()
                        .useCamera(true) // 设置是否使用拍照
                        .setSingle(false)  //设置是否单选
                        .canPreview(true) //是否点击放大图片查看,，默认为true
                        .setMaxSelectCount(9) // 图片的最大选择数量，小于等于0时，不限数量。
                        .start(this, REQUEST_CODE); // 打开相册
                break;

            case R.id.btn_unlimited:
                //多选(不限数量)
                ImageSelector.builder()
                        .useCamera(true) // 设置是否使用拍照
                        .setSingle(false)  //设置是否单选
                        .canPreview(true) //是否点击放大图片查看,，默认为true
                        .setMaxSelectCount(0) // 图片的最大选择数量，小于等于0时，不限数量。
                        .start(this, REQUEST_CODE); // 打开相册
                break;

            case R.id.btn_clip:
                //单选并剪裁
                ImageSelector.builder()
                        .useCamera(true) // 设置是否使用拍照
                        .setCrop(true)  // 设置是否使用图片剪切功能。
                        .setCropRatio(1.0f, 200, 200) // 图片剪切的宽高比,默认1.0f。宽固定为手机屏幕的宽。
                        .setSingle(true)  //设置是否单选
                        .canPreview(true) //是否点击放大图片查看,，默认为true
                        .start(this, REQUEST_CODE); // 打开相册
                break;

            case R.id.btn_only_take:
                //仅拍照
                ImageSelector.builder()
                        .onlyTakePhoto(true)  // 仅拍照，不打开相册
                        .start(this, REQUEST_CODE);
                break;

            case R.id.btn_take_and_clip:
                //拍照并剪裁
                ImageSelector.builder()
                        .setCrop(true) // 设置是否使用图片剪切功能。
                        .setCropRatio(1.0f, 200, 200) // 图片剪切的宽高比,默认1.0f。宽固定为手机屏幕的宽。
                        .onlyTakePhoto(true)  // 仅拍照，不打开相册
                        .start(this, REQUEST_CODE);
                break;
        }
    }
}
