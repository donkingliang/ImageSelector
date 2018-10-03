package com.donkingliang.imageselector;

import android.app.Activity;
import android.support.v4.app.Fragment;

import com.donkingliang.imageselector.ClipImageActivity;
import com.donkingliang.imageselector.ImageSelectorActivity;
import com.donkingliang.imageselector.loader.BaseImageLoader;

import java.util.ArrayList;

/**
 * Depiction:
 * Author:lry
 * Date:2018/6/25
 */
public class ImageSelector {

    // 图片选择的结果
    public static final String SELECT_RESULT = "select_result";
    // 最大的图片选择数
    public static final String MAX_SELECT_COUNT = "max_select_count";
    // 否单选
    public static final String IS_SINGLE = "is_single";
    // 是否点击放大图片查看
    public static final String IS_VIEW_IMAGE = "is_view_image";
    // 是否使用拍照功能
    public static final String USE_CAMERA = "is_camera";
    // 原来已选择的图片
    public static final String SELECTED = "selected";
    // 初始位置
    public static final String POSITION = "position";

    public static final String IS_CONFIRM = "is_confirm";

    public static final int RESULT_CODE = 0x00000012;

    public static ImageSelectorBuilder builder() {
        return SingleHolder.INSTANCE;
    }

    private static class SingleHolder{
        private static final ImageSelectorBuilder INSTANCE = new ImageSelectorBuilder();
    }

    public static class ImageSelectorBuilder {

        private boolean isCrop = false;
        private boolean useCamera = true;
        private boolean isSingle = false;
        private boolean isViewImage = true;
        private int maxSelectCount;
        private ArrayList<String> selected;
        private BaseImageLoader mBaseImageLoader;

        /**
         * 是否使用图片剪切功能。默认false。如果使用了图片剪切功能，相册只能单选。
         *
         * @param isCrop
         * @return
         */
        public ImageSelectorBuilder setCrop(boolean isCrop) {
            this.isCrop = isCrop;
            return this;
        }

        /**
         * 是否单选
         *
         * @param isSingle
         * @return
         */
        public ImageSelectorBuilder setSingle(boolean isSingle) {
            this.isSingle = isSingle;
            return this;
        }

        /**
         * 是否点击放大图片查看,，默认为true
         *
         * @param isViewImage
         * @return
         */
        public ImageSelectorBuilder setViewImage(boolean isViewImage) {
            this.isViewImage = isViewImage;
            return this;
        }

        /**
         * 是否使用拍照功能。
         *
         * @param useCamera 默认为true
         * @return
         */
        public ImageSelectorBuilder useCamera(boolean useCamera) {
            this.useCamera = useCamera;
            return this;
        }

        /**
         * 图片的最大选择数量，小于等于0时，不限数量，isSingle为false时才有用。
         *
         * @param maxSelectCount
         * @return
         */
        public ImageSelectorBuilder setMaxSelectCount(int maxSelectCount) {
            this.maxSelectCount = maxSelectCount;
            return this;
        }

        /**
         * 接收从外面传进来的已选择的图片列表。当用户原来已经有选择过图片，现在重新打开
         * 选择器，允许用户把先前选过的图片传进来，并把这些图片默认为选中状态。
         *
         * @param selected
         * @return
         */
        public ImageSelectorBuilder setSelected(ArrayList<String> selected) {
            this.selected = selected;
            return this;
        }


        public BaseImageLoader getImageLoader() {
            return mBaseImageLoader;
        }

        public ImageSelectorBuilder setImageLoader(BaseImageLoader imageLoader) {
            this.mBaseImageLoader = imageLoader;
            return this;
        }

        /**
         * 打开相册
         *
         * @param activity
         * @param requestCode
         */
        public void start(Activity activity, int requestCode) {
            if (isCrop) {
                ClipImageActivity.openActivity(activity, requestCode, isViewImage, useCamera, selected);
            } else {
                ImageSelectorActivity.openActivity(activity, requestCode, isSingle, isViewImage,
                        useCamera, maxSelectCount, selected);
            }
        }

        /**
         * 打开相册
         *
         * @param fragment
         * @param requestCode
         */
        public void start(Fragment fragment, int requestCode) {
            if (isCrop) {
                ClipImageActivity.openActivity(fragment, requestCode, isViewImage, useCamera, selected);
            } else {
                ImageSelectorActivity.openActivity(fragment, requestCode, isSingle, isViewImage,
                        useCamera, maxSelectCount, selected);
            }
        }

        /**
         * 打开相册
         *
         * @param fragment
         * @param requestCode
         */
        public void start(android.app.Fragment fragment, int requestCode) {
            if (isCrop) {
                ClipImageActivity.openActivity(fragment, requestCode, isViewImage, useCamera, selected);
            } else {
                ImageSelectorActivity.openActivity(fragment, requestCode, isSingle, isViewImage,
                        useCamera, maxSelectCount, selected);
            }
        }
    }

}
