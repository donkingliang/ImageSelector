package com.donkingliang.imageselector.utils;

import android.app.Activity;

import com.donkingliang.imageselector.ImageSelectorActivity;

/**
 * 提供给外界相册的调用的工具类
 */
public class ImageSelectorUtils {

    //图片选择的结果
    public static final String SELECT_RESULT = "select_result";

    /**
     * 打开相册，选择图片,可多选,不限数量。
     *
     * @param activity
     * @param requestCode
     */
    public static void openPhoto(Activity activity, int requestCode) {
        openPhoto(activity, requestCode, false, 0);
    }

    /**
     * 打开相册，选择图片,可多选,限制最大的选择数量。
     *
     * @param activity
     * @param requestCode
     * @param isSingle       是否单选
     * @param maxSelectCount 图片的最大选择数量，小于等于0时，不限数量，isSingle为false时才有用。
     */
    public static void openPhoto(Activity activity, int requestCode,
                                 boolean isSingle, int maxSelectCount) {
        ImageSelectorActivity.openActivity(activity, requestCode, isSingle, maxSelectCount);
    }
}
