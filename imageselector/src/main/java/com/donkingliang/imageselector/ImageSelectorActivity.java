package com.donkingliang.imageselector;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.os.EnvironmentCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.donkingliang.imageselector.adapter.FolderAdapter;
import com.donkingliang.imageselector.adapter.ImageAdapter;
import com.donkingliang.imageselector.entry.Folder;
import com.donkingliang.imageselector.entry.Image;
import com.donkingliang.imageselector.model.ImageModel;
import com.donkingliang.imageselector.utils.DateUtils;
import com.donkingliang.imageselector.utils.ImageSelector;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ImageSelectorActivity extends AppCompatActivity {

    private TextView tvTime;
    private TextView tvFolderName;
    private TextView tvConfirm;
    private TextView tvPreview;
    private FrameLayout btnConfirm;
    private FrameLayout btnPreview;
    private RecyclerView rvImage;
    private RecyclerView rvFolder;
    private View masking;

    private ImageAdapter mAdapter;
    private GridLayoutManager mLayoutManager;

    private ArrayList<Folder> mFolders;
    private Folder mFolder;
    private boolean applyLoadImage = false;
    private static final int PERMISSION_WRITE_EXTERNAL_REQUEST_CODE = 0x00000011;
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 0x00000012;

    private static final int CAMERA_REQUEST_CODE = 0x00000010;

    private boolean isOpenFolder;
    private boolean isShowTime;
    private boolean isInitFolder;
    private boolean isSingle;
    private boolean isViewImage = true;
    private int mMaxCount;

    private boolean useCamera = true;
    private String mPhotoPath;

    private Handler mHideHandler = new Handler();
    private Runnable mHide = new Runnable() {
        @Override
        public void run() {
            hideTime();
        }
    };

    //用于接收从外面传进来的已选择的图片列表。当用户原来已经有选择过图片，现在重新打开选择器，允许用
    // 户把先前选过的图片传进来，并把这些图片默认为选中状态。
    private ArrayList<String> mSelectedImages;

    /**
     * 启动图片选择器
     *
     * @param activity
     * @param requestCode
     * @param isSingle       是否单选
     * @param isViewImage    是否点击放大图片查看
     * @param useCamera      是否使用拍照功能
     * @param maxSelectCount 图片的最大选择数量，小于等于0时，不限数量，isSingle为false时才有用。
     * @param selected       接收从外面传进来的已选择的图片列表。当用户原来已经有选择过图片，现在重新打开
     *                       选择器，允许用户把先前选过的图片传进来，并把这些图片默认为选中状态。
     */
    public static void openActivity(Activity activity, int requestCode,
                                    boolean isSingle, boolean isViewImage, boolean useCamera,
                                    int maxSelectCount, ArrayList<String> selected) {
        Intent intent = new Intent(activity, ImageSelectorActivity.class);
        intent.putExtras(dataPackages(isSingle, isViewImage, useCamera, maxSelectCount, selected));
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 启动图片选择器
     *
     * @param fragment
     * @param requestCode
     * @param isSingle       是否单选
     * @param isViewImage    是否点击放大图片查看
     * @param useCamera      是否使用拍照功能
     * @param maxSelectCount 图片的最大选择数量，小于等于0时，不限数量，isSingle为false时才有用。
     * @param selected       接收从外面传进来的已选择的图片列表。当用户原来已经有选择过图片，现在重新打开
     *                       选择器，允许用户把先前选过的图片传进来，并把这些图片默认为选中状态。
     */
    public static void openActivity(Fragment fragment, int requestCode,
                                    boolean isSingle, boolean isViewImage, boolean useCamera,
                                    int maxSelectCount, ArrayList<String> selected) {
        Intent intent = new Intent(fragment.getContext(), ImageSelectorActivity.class);
        intent.putExtras(dataPackages(isSingle, isViewImage, useCamera, maxSelectCount, selected));
        fragment.startActivityForResult(intent, requestCode);
    }

    /**
     * 启动图片选择器
     *
     * @param fragment
     * @param requestCode
     * @param isSingle       是否单选
     * @param isViewImage    是否点击放大图片查看
     * @param useCamera      是否使用拍照功能
     * @param maxSelectCount 图片的最大选择数量，小于等于0时，不限数量，isSingle为false时才有用。
     * @param selected       接收从外面传进来的已选择的图片列表。当用户原来已经有选择过图片，现在重新打开
     *                       选择器，允许用户把先前选过的图片传进来，并把这些图片默认为选中状态。
     */
    public static void openActivity(android.app.Fragment fragment, int requestCode,
                                    boolean isSingle, boolean isViewImage, boolean useCamera,
                                    int maxSelectCount, ArrayList<String> selected) {
        Intent intent = new Intent(fragment.getActivity(), ImageSelectorActivity.class);
        intent.putExtras(dataPackages(isSingle, isViewImage, useCamera, maxSelectCount, selected));
        fragment.startActivityForResult(intent, requestCode);
    }

    public static Bundle dataPackages(boolean isSingle, boolean isViewImage, boolean useCamera,
                                      int maxSelectCount, ArrayList<String> selected) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ImageSelector.IS_SINGLE, isSingle);
        bundle.putBoolean(ImageSelector.IS_VIEW_IMAGE, isViewImage);
        bundle.putBoolean(ImageSelector.USE_CAMERA, useCamera);
        bundle.putInt(ImageSelector.MAX_SELECT_COUNT, maxSelectCount);
        bundle.putStringArrayList(ImageSelector.SELECTED, selected);
        return bundle;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_select);

        Intent intent = getIntent();
        mMaxCount = intent.getIntExtra(ImageSelector.MAX_SELECT_COUNT, 0);
        isSingle = intent.getBooleanExtra(ImageSelector.IS_SINGLE, false);
        isViewImage = intent.getBooleanExtra(ImageSelector.IS_VIEW_IMAGE, true);
        useCamera = intent.getBooleanExtra(ImageSelector.USE_CAMERA, true);
        mSelectedImages = intent.getStringArrayListExtra(ImageSelector.SELECTED);

        setStatusBarColor();
        initView();
        initListener();
        initImageList();
        checkPermissionAndLoadImages();
        hideFolderList();
        setSelectImageCount(0);
    }

    /**
     * 修改状态栏颜色
     */
    private void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#373c3d"));
        }
    }

    private void initView() {
        rvImage = (RecyclerView) findViewById(R.id.rv_image);
        rvFolder = (RecyclerView) findViewById(R.id.rv_folder);
        tvConfirm = (TextView) findViewById(R.id.tv_confirm);
        tvPreview = (TextView) findViewById(R.id.tv_preview);
        btnConfirm = (FrameLayout) findViewById(R.id.btn_confirm);
        btnPreview = (FrameLayout) findViewById(R.id.btn_preview);
        tvFolderName = (TextView) findViewById(R.id.tv_folder_name);
        tvTime = (TextView) findViewById(R.id.tv_time);
        masking = findViewById(R.id.masking);
    }

    private void initListener() {
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Image> images = new ArrayList<>();
                images.addAll(mAdapter.getSelectImages());
                toPreviewActivity(images, 0);
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm();
            }
        });

        findViewById(R.id.btn_folder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInitFolder) {
                    if (isOpenFolder) {
                        closeFolder();
                    } else {
                        openFolder();
                    }
                }
            }
        });

        masking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeFolder();
            }
        });

        rvImage.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                changeTime();
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                changeTime();
            }
        });
    }

    /**
     * 初始化图片列表
     */
    private void initImageList() {
        // 判断屏幕方向
        Configuration configuration = getResources().getConfiguration();
        if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mLayoutManager = new GridLayoutManager(this, 3);
        } else {
            mLayoutManager = new GridLayoutManager(this, 5);
        }

        rvImage.setLayoutManager(mLayoutManager);
        mAdapter = new ImageAdapter(this, mMaxCount, isSingle, isViewImage);
        rvImage.setAdapter(mAdapter);
        ((SimpleItemAnimator) rvImage.getItemAnimator()).setSupportsChangeAnimations(false);
        if (mFolders != null && !mFolders.isEmpty()) {
            setFolder(mFolders.get(0));
        }
        mAdapter.setOnImageSelectListener(new ImageAdapter.OnImageSelectListener() {
            @Override
            public void OnImageSelect(Image image, boolean isSelect, int selectCount) {
                setSelectImageCount(selectCount);
            }
        });
        mAdapter.setOnItemClickListener(new ImageAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(Image image, int position) {
                toPreviewActivity(mAdapter.getData(), position);
            }

            @Override
            public void OnCameraClick() {
                checkPermissionAndCamera();
            }
        });
    }

    /**
     * 初始化图片文件夹列表
     */
    private void initFolderList() {
        if (mFolders != null && !mFolders.isEmpty()) {
            isInitFolder = true;
            rvFolder.setLayoutManager(new LinearLayoutManager(ImageSelectorActivity.this));
            FolderAdapter adapter = new FolderAdapter(ImageSelectorActivity.this, mFolders);
            adapter.setOnFolderSelectListener(new FolderAdapter.OnFolderSelectListener() {
                @Override
                public void OnFolderSelect(Folder folder) {
                    setFolder(folder);
                    closeFolder();
                }
            });
            rvFolder.setAdapter(adapter);
        }
    }

    /**
     * 刚开始的时候文件夹列表默认是隐藏的
     */
    private void hideFolderList() {
        rvFolder.post(new Runnable() {
            @Override
            public void run() {
                rvFolder.setTranslationY(rvFolder.getHeight());
                rvFolder.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 设置选中的文件夹，同时刷新图片列表
     *
     * @param folder
     */
    private void setFolder(Folder folder) {
        if (folder != null && mAdapter != null && !folder.equals(mFolder)) {
            mFolder = folder;
            tvFolderName.setText(folder.getName());
            rvImage.scrollToPosition(0);
            mAdapter.refresh(folder.getImages(), folder.isUseCamera());
        }
    }

    private void setSelectImageCount(int count) {
        if (count == 0) {
            btnConfirm.setEnabled(false);
            btnPreview.setEnabled(false);
            tvConfirm.setText("确定");
            tvPreview.setText("预览");
        } else {
            btnConfirm.setEnabled(true);
            btnPreview.setEnabled(true);
            tvPreview.setText("预览(" + count + ")");
            if (isSingle) {
                tvConfirm.setText("确定");
            } else if (mMaxCount > 0) {
                tvConfirm.setText("确定(" + count + "/" + mMaxCount + ")");
            } else {
                tvConfirm.setText("确定(" + count + ")");
            }
        }
    }

    /**
     * 弹出文件夹列表
     */
    private void openFolder() {
        if (!isOpenFolder) {
            masking.setVisibility(View.VISIBLE);
            ObjectAnimator animator = ObjectAnimator.ofFloat(rvFolder, "translationY",
                    rvFolder.getHeight(), 0).setDuration(300);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    rvFolder.setVisibility(View.VISIBLE);
                }
            });
            animator.start();
            isOpenFolder = true;
        }
    }

    /**
     * 收起文件夹列表
     */
    private void closeFolder() {
        if (isOpenFolder) {
            masking.setVisibility(View.GONE);
            ObjectAnimator animator = ObjectAnimator.ofFloat(rvFolder, "translationY",
                    0, rvFolder.getHeight()).setDuration(300);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    rvFolder.setVisibility(View.GONE);
                }
            });
            animator.start();
            isOpenFolder = false;
        }
    }

    /**
     * 隐藏时间条
     */
    private void hideTime() {
        if (isShowTime) {
            ObjectAnimator.ofFloat(tvTime, "alpha", 1, 0).setDuration(300).start();
            isShowTime = false;
        }
    }

    /**
     * 显示时间条
     */
    private void showTime() {
        if (!isShowTime) {
            ObjectAnimator.ofFloat(tvTime, "alpha", 0, 1).setDuration(300).start();
            isShowTime = true;
        }
    }

    /**
     * 改变时间条显示的时间（显示图片列表中的第一个可见图片的时间）
     */
    private void changeTime() {
        int firstVisibleItem = getFirstVisibleItem();
        Image image = mAdapter.getFirstVisibleImage(firstVisibleItem);
        if (image != null) {
            String time = DateUtils.getImageTime(image.getTime() * 1000);
            tvTime.setText(time);
            showTime();
            mHideHandler.removeCallbacks(mHide);
            mHideHandler.postDelayed(mHide, 1500);
        }
    }

    private int getFirstVisibleItem() {
        return mLayoutManager.findFirstVisibleItemPosition();
    }

    private void confirm() {
        if (mAdapter == null) {
            return;
        }
        //因为图片的实体类是Image，而我们返回的是String数组，所以要进行转换。
        ArrayList<Image> selectImages = mAdapter.getSelectImages();
        ArrayList<String> images = new ArrayList<>();
        for (Image image : selectImages) {
            images.add(image.getPath());
        }

        //点击确定，把选中的图片通过Intent传给上一个Activity。
        setResult(images);
        finish();
    }

    private void setResult(ArrayList<String> images) {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(ImageSelector.SELECT_RESULT, images);
        setResult(RESULT_OK, intent);
    }

    private void toPreviewActivity(ArrayList<Image> images, int position) {
        if (images != null && !images.isEmpty()) {
            PreviewActivity.openActivity(this, images,
                    mAdapter.getSelectImages(), isSingle, mMaxCount, position);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (applyLoadImage) {
            applyLoadImage = false;
            checkPermissionAndLoadImages();
        }
    }

    /**
     * 处理图片预览页返回的结果
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ImageSelector.RESULT_CODE) {
            if (data != null && data.getBooleanExtra(ImageSelector.IS_CONFIRM, false)) {
                //如果用户在预览页点击了确定，就直接把用户选中的图片返回给用户。
                confirm();
            } else {
                //否则，就刷新当前页面。
                mAdapter.notifyDataSetChanged();
                setSelectImageCount(mAdapter.getSelectImages().size());
            }
        } else if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(mPhotoPath))));
                ArrayList<String> images = new ArrayList<>();
                images.add(mPhotoPath);
                setResult(images);
                finish();
            }
        }
    }

    /**
     * 横竖屏切换处理
     *
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mLayoutManager != null && mAdapter != null) {
            //切换为竖屏
            if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                mLayoutManager.setSpanCount(3);
            }
            //切换为横屏
            else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mLayoutManager.setSpanCount(5);
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 检查权限并加载SD卡里的图片。
     */
    private void checkPermissionAndLoadImages() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//            Toast.makeText(this, "没有图片", Toast.LENGTH_LONG).show();
            return;
        }
        int hasWriteExternalPermission = ContextCompat.checkSelfPermission(getApplication(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteExternalPermission == PackageManager.PERMISSION_GRANTED) {
            //有权限，加载图片。
            loadImageForSDCard();
        } else {
            //没有权限，申请权限。
            ActivityCompat.requestPermissions(ImageSelectorActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE_EXTERNAL_REQUEST_CODE);
        }
    }

    /**
     * 检查权限并拍照。
     */
    private void checkPermissionAndCamera() {
        int hasCameraPermission = ContextCompat.checkSelfPermission(getApplication(),
                Manifest.permission.CAMERA);
        if (hasCameraPermission == PackageManager.PERMISSION_GRANTED) {
            //有调起相机拍照。
            openCamera();
        } else {
            //没有权限，申请权限。
            ActivityCompat.requestPermissions(ImageSelectorActivity.this,
                    new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA_REQUEST_CODE);
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
                //允许权限，加载图片。
                loadImageForSDCard();
            } else {
                //拒绝权限，弹出提示框。
                showExceptionDialog(true);
            }
        } else if (requestCode == PERMISSION_CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //允许权限，有调起相机拍照。
                openCamera();
            } else {
                //拒绝权限，弹出提示框。
                showExceptionDialog(false);
            }
        }
    }

    /**
     * 发生没有权限等异常时，显示一个提示dialog.
     */
    private void showExceptionDialog(final boolean applyLoad) {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("提示")
                .setMessage("该相册需要赋予访问存储和拍照的权限，请到“设置”>“应用”>“权限”中配置权限。")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        finish();
                    }
                }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                startAppSettings();
                if (applyLoad) {
                    applyLoadImage = true;
                }
            }
        }).show();
    }

    /**
     * 从SDCard加载图片。
     */
    private void loadImageForSDCard() {
        ImageModel.loadImageForSDCard(this, new ImageModel.DataCallback() {
            @Override
            public void onSuccess(ArrayList<Folder> folders) {
                mFolders = folders;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mFolders != null && !mFolders.isEmpty()) {
                            initFolderList();
                            mFolders.get(0).setUseCamera(useCamera);
                            setFolder(mFolders.get(0));
                            if (mSelectedImages != null && mAdapter != null) {
                                mAdapter.setSelectedImages(mSelectedImages);
                                mSelectedImages = null;
                                setSelectImageCount(mAdapter.getSelectImages().size());
                            }
                        }
                    }
                });
            }
        });
    }

    /**
     * 调起相机拍照
     */
    private void openCamera() {
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (captureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (photoFile != null) {
                mPhotoPath = photoFile.getAbsolutePath();
                //通过FileProvider创建一个content类型的Uri
                Uri photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(captureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = String.format("JPEG_%s.jpg", timeStamp);
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }
        File tempFile = new File(storageDir, imageFileName);
        if (!Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(tempFile))) {
            return null;
        }
        return tempFile;
    }

    /**
     * 启动应用的设置
     */
    private void startAppSettings() {
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN && isOpenFolder) {
            closeFolder();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
