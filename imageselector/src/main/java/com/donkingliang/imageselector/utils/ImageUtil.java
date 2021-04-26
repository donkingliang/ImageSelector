package com.donkingliang.imageselector.utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageUtil {

    /**
     * 获取缓存图片的文件夹
     *
     * @param context
     * @return
     */
    public static String getImageCacheDir(Context context) {
        File file = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            if (VersionUtils.isAndroidQ()) {
                file = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            } else {
                file = context.getExternalCacheDir();
            }
        }

        if (file == null) {
            file = context.getCacheDir();
        }
        return file.getPath() + File.separator + "image_select";
    }

    /**
     * 保存图片
     *
     * @param bitmap
     * @param path
     * @param name
     * @return
     */
    public static String saveImage(Bitmap bitmap, String path, String name) {
        FileOutputStream b = null;
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();// 创建文件夹
        }

        String fileName = path + File.separator + name + ".jpg";

        try {
            b = new FileOutputStream(fileName);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, b);// 把数据写入文件
            return fileName;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (b != null) {
                    b.flush();
                    b.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public static Bitmap zoomBitmap(Bitmap bm, int reqWidth, int reqHeight) {
        // 获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例
        float scaleWidth = ((float) reqWidth) / width;
        float scaleHeight = ((float) reqHeight) / height;
        float scale = Math.min(scaleWidth, scaleHeight);
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix,
                true);
        return newbm;
    }

    /**
     * 根据计算的inSampleSize，得到压缩后图片
     *
     * @param pathName
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    @SuppressLint("NewApi")
    public static Bitmap decodeSampledBitmapFromFile(Context context, String pathName, int reqWidth, int reqHeight) {

        int degree = 0;

        Uri uri = UriUtils.getImageContentUri(context, pathName);
        ParcelFileDescriptor parcelFileDescriptor = null;
        FileDescriptor fileDescriptor = null;
        try {

            parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
            fileDescriptor = parcelFileDescriptor.getFileDescriptor();

            ExifInterface exifInterface = null;
            if (VersionUtils.isAndroidQ()) {
                exifInterface = new ExifInterface(fileDescriptor);
            } else {
                exifInterface = new ExifInterface(pathName);
            }

            int result = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            switch (result) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        try {

            // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            if (VersionUtils.isAndroidQ()) {
                BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
            } else {
                BitmapFactory.decodeFile(pathName, options);
            }
            // 调用上面定义的方法计算inSampleSize值
            options.inSampleSize = calculateInSampleSize(options, reqWidth,
                    reqHeight);

            // 使用获取到的inSampleSize值再次解析图片
            options.inJustDecodeBounds = false;
//            options.inPreferredConfig = Bitmap.Config.RGB_565;

            Bitmap bitmap = null;
            if (VersionUtils.isAndroidQ()) {
                bitmap = getBitmapFromUri(context, uri, options);
            } else {
                bitmap = BitmapFactory.decodeFile(pathName, options);
            }

            parcelFileDescriptor.close();

            if (degree != 0) {
                Bitmap newBitmap = rotateImageView(bitmap, degree);
                bitmap.recycle();
                bitmap = null;
                return newBitmap;
            }

            return bitmap;
        } catch (OutOfMemoryError error) {
            Log.e("eee", "内存泄露！");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取Bitmap
     *
     * @param context
     * @param uri
     * @return
     */
    public static Bitmap getBitmapFromUri(Context context, Uri uri) {
        return getBitmapFromUri(context, uri, null);
    }

    /**
     * 获取Bitmap
     *
     * @param context
     * @param uri
     * @return
     */
    public static Bitmap getBitmapFromUri(Context context, Uri uri, BitmapFactory.Options options) {
        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    context.getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
            parcelFileDescriptor.close();
            return image;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 旋转图片
     *
     * @param bitmap
     * @param angle
     * @return Bitmap
     */
    public static Bitmap rotateImageView(Bitmap bitmap, int angle) {
        //旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 创建新的图片
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * 计算inSampleSize，用于压缩图片
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        // 源图片的宽度
        int width = options.outWidth;
        int height = options.outHeight;
        int inSampleSize = 1;

        if (width > reqWidth && height > reqHeight) {
//         计算出实际宽度和目标宽度的比率
            int widthRatio = Math.round((float) width / (float) reqWidth);
            int heightRatio = Math.round((float) height / (float) reqHeight);
            inSampleSize = Math.max(widthRatio, heightRatio);
        }

        return inSampleSize;
    }

    public static boolean isEffective(String path) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        return options.outWidth > 0 && options.outHeight > 0;
    }

    public static boolean isEffective(Context context, Uri uri) {
        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    context.getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
            return options.outWidth > 0 && options.outHeight > 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 是否是剪切返回的图片
     *
     * @param context
     * @param path
     * @return
     */
    public static boolean isCutImage(Context context, String path) {
        return isCutImage(getImageCacheDir(context), path);
    }

    /**
     * 是否是剪切返回的图片
     *
     * @param dir
     * @param path
     * @return
     */
    public static boolean isCutImage(String dir, String path) {
        if (!StringUtils.isEmptyString(path)) {
            return path.startsWith(dir);
        }
        return false;
    }

    /**
     * 保存拍照的图片
     *
     * @param context
     * @param uri
     * @param takeTime 调起相机拍照的时间
     */
    public static void savePicture(final Context context, final Uri uri, final long takeTime) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (isNeedSavePicture(context, takeTime)) {
                    context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                }
            }
        }).start();
    }

    /**
     * 是否需要保存拍照的图片
     *
     * @param context
     * @return
     */
    private static boolean isNeedSavePicture(Context context, long takeTime) {
        //扫描图片
        Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                .buildUpon().appendQueryParameter("limit","1").build();
        ContentResolver mContentResolver = context.getContentResolver();
        Cursor mCursor = mContentResolver.query(mImageUri, new String[]{
                        MediaStore.Images.Media.DATE_ADDED,
                        MediaStore.Images.Media._ID,
                        MediaStore.Images.Media.SIZE},
                MediaStore.MediaColumns.SIZE + ">0",
                null,
                MediaStore.Files.FileColumns._ID + " DESC");

        //读取扫描到的图片
        if (mCursor != null && mCursor.getCount() > 0 && mCursor.moveToFirst()) {
            //获取图片时间
            long time = mCursor.getLong(mCursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
            int id = mCursor.getInt(mCursor.getColumnIndex(MediaStore.Images.Media._ID));
            if (String.valueOf(time).length() < 13) {
                time *= 1000;
            }
            mCursor.close();

            // 如果照片的插入时间大于相机的拍照时间，就认为是拍照图片已插入
            return time + 1000 < takeTime;
        }
        return true;
    }
}