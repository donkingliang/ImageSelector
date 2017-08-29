package com.donkingliang.imageselector.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.WindowManager;

public class ClipImageView extends AppCompatImageView {

    private PointF mDownPoint;
    private PointF mMiddlePoint;
    private Matrix mMatrix;
    private Matrix mTempMatrix;

    private int mBitmapWidth;
    private int mBitmapHeight;

    private final int MODE_NONE = 0;
    private final int MODE_DRAG = 1;
    private final int MODE_ZOOM = 2;
    private final int MODE_POINTER_UP = 3;
    private int CURR_MODE = MODE_NONE;

    private float mLastDistance;

    private Paint mFrontGroundPaint = new Paint();
    private int mTargetWidth;
    private int mTargetHeight;
    private Xfermode mXfermode;
    private Rect r;
    private RectF rf;

    private float mCircleCenterX, mCircleCenterY;
    private float mCircleX, mCircleY;
    private boolean isCutImage;

    public ClipImageView(Context context) {
        super(context);
        setRadius();
    }

    public ClipImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setRadius();
    }

    /**
     * 设置要剪裁的图片
     *
     * @param bitmap
     */
    public void setBitmapData(Bitmap bitmap) {

        if (bitmap == null) {
            return;
        }

        mBitmapHeight = bitmap.getHeight();
        mBitmapWidth = bitmap.getWidth();
        setImageBitmap(bitmap);
        init();
    }

    private void init() {
        mDownPoint = new PointF();
        mMiddlePoint = new PointF();
        mMatrix = new Matrix();
        mTempMatrix = new Matrix();
        mFrontGroundPaint.setColor(Color.parseColor("#ac000000"));
        mFrontGroundPaint.setAntiAlias(true);
        mXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);

        setScaleType(ScaleType.MATRIX);
        post(new Runnable() {
            @Override
            public void run() {
                center();
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mCircleCenterX = getWidth() / 2;
        mCircleCenterY = getHeight() / 2;
        mCircleX = mCircleCenterX - mTargetWidth / 2;
        mCircleY = mCircleCenterY - mTargetHeight / 2;
    }

    private void setRadius() {

        int width = getScreenWidth(getContext());
        int height = getScreenHeight(getContext());

        if (width > height) {
            mTargetWidth = height;
            mTargetHeight = height;
        } else {
            mTargetWidth = width;
            mTargetHeight = width;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isCutImage) {
            return;
        }
        if (rf == null || rf.isEmpty()) {
            r = new Rect(0, 0, getWidth(), getHeight());
            rf = new RectF(r);
        }
        // 画入前景圆形蒙板层
        int sc = canvas.saveLayer(rf, null, Canvas.MATRIX_SAVE_FLAG
                | Canvas.CLIP_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
                | Canvas.FULL_COLOR_LAYER_SAVE_FLAG
                | Canvas.CLIP_TO_LAYER_SAVE_FLAG | Canvas.ALL_SAVE_FLAG);
        //画入矩形黑色半透明蒙板层
        canvas.drawRect(r, mFrontGroundPaint);
        //设置Xfermode，目的是为了去除矩形黑色半透明蒙板层和圆形的相交部分
        mFrontGroundPaint.setXfermode(mXfermode);
        //画入正方形
        canvas.drawRect(mCircleCenterX - mTargetWidth / 2, mCircleCenterY - mTargetHeight / 2,
                mCircleCenterX + mTargetWidth / 2, mCircleCenterY + mTargetHeight / 2, mFrontGroundPaint);

        canvas.restoreToCount(sc);
        //清除Xfermode，防止影响下次画图
        mFrontGroundPaint.setXfermode(null);
    }

    /**
     * 截取Bitmap
     *
     * @return
     */
    public Bitmap clipImage() {
        isCutImage = true;
        Paint paint = new Paint();
        setDrawingCacheEnabled(true);
        Bitmap bitmap = getDrawingCache();
        Bitmap targetBitmap = Bitmap.createBitmap(mTargetWidth, mTargetHeight,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(targetBitmap);
        RectF dst = new RectF(-bitmap.getWidth() / 2 + mTargetWidth / 2, -getHeight()
                / 2 + mTargetHeight / 2, bitmap.getWidth() / 2
                + mTargetWidth / 2, getHeight() / 2 + mTargetHeight / 2);

        canvas.drawBitmap(bitmap, null, dst, paint);
        setDrawingCacheEnabled(false);
        bitmap.recycle();
        bitmap = null;
        isCutImage = false;
        //返回正方形图片
        return targetBitmap;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mMatrix == null) {
            return super.onTouchEvent(event);
        }

        float[] values = new float[9];
        mMatrix.getValues(values);
        float left = values[Matrix.MTRANS_X];
        float top = values[Matrix.MTRANS_Y];
        float right = (left + mBitmapWidth * values[Matrix.MSCALE_X]);
        float bottom = (top + mBitmapHeight * values[Matrix.MSCALE_Y]);
        float x = 0f;
        float y = 0f;

        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                CURR_MODE = MODE_DRAG;
                mDownPoint.set(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (getDistance(event) > 10f) {
                    CURR_MODE = MODE_ZOOM;
                    midPoint(mMiddlePoint, event);
                    mLastDistance = getDistance(event);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                //如果当前模式为拖曳（单指触屏）
                if (CURR_MODE == MODE_DRAG || CURR_MODE == MODE_POINTER_UP) {
                    if (CURR_MODE == MODE_DRAG) {

                        x = event.getX() - mDownPoint.x;
                        y = event.getY() - mDownPoint.y;
                        //left靠边
                        if (x + left > mCircleX) {
                            x = 0;
                        }
                        //right靠边
                        if (x + right < mCircleX + mTargetWidth) {
                            x = 0;
                        }
                        //top靠边
                        if (y + top > mCircleY) {
                            y = 0;
                        }
                        //bottom靠边
                        if (y + bottom < mCircleY + mTargetHeight) {
                            y = 0;
                        }
                        mMatrix.postTranslate(x, y);
                        mDownPoint.set(event.getX(), event.getY());

                    } else {
                        CURR_MODE = MODE_DRAG;
                        mDownPoint.set(event.getX(), event.getY());
                    }
                } else {
                    //否则当前模式为缩放（双指触屏）
                    float distance = getDistance(event);
                    if (distance > 10f) {
                        float scale = distance / mLastDistance;

                        //left靠边
                        if (left >= mCircleX) {
                            mMiddlePoint.x = 0;
                        }
                        //right靠边
                        if (right <= mCircleX + mTargetWidth) {
                            mMiddlePoint.x = right;
                        }
                        //top靠边
                        if (top >= mCircleY) {
                            mMiddlePoint.y = 0;
                        }
                        //bottom靠边
                        if (bottom <= mCircleY + mTargetHeight) {
                            mMiddlePoint.y = bottom;
                        }
                        mTempMatrix.set(mMatrix);
                        mTempMatrix.postScale(scale, scale, mMiddlePoint.x, mMiddlePoint.y);

                        float[] temp_values = new float[9];
                        mTempMatrix.getValues(temp_values);
                        float temp_left = temp_values[Matrix.MTRANS_X];
                        float temp_top = temp_values[Matrix.MTRANS_Y];
                        float temp_right = (temp_left + mBitmapWidth * temp_values[Matrix.MSCALE_X]);
                        float temp_bottom = (temp_top + mBitmapHeight * temp_values[Matrix.MSCALE_Y]);
                        //靠边预判断
                        if (temp_left > mCircleX || temp_right < mCircleX + mTargetWidth ||
                                temp_top > mCircleY || temp_bottom < mCircleY + mTargetHeight) {
                            return true;
                        }
                        mMatrix.postScale(scale, scale, mMiddlePoint.x, mMiddlePoint.y);
                        mLastDistance = getDistance(event);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                CURR_MODE = MODE_NONE;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                CURR_MODE = MODE_POINTER_UP;
                break;
        }
        setImageMatrix(mMatrix);
        return true;
    }


    /**
     * 两点的距离
     */
    private float getDistance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 两点的中点
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /**
     * 横向、纵向居中
     */
    protected void center() {

        float height = mBitmapHeight;
        float width = mBitmapWidth;
        float screenWidth = getWidth();
        float screenHeight = getHeight();
        float scale = 1f;
        if (width >= height) {
            scale = screenWidth / width;

            if (scale * height < mTargetHeight) {
                scale = mTargetHeight / height;
            }

        } else {
            if (height <= screenHeight) {
                scale = screenWidth / width;
            } else {
                scale = screenHeight / height;
            }

            if (scale * width < mTargetWidth) {
                scale = mTargetWidth / width;
            }
        }

        float deltaX = (screenWidth - width * scale) / 2f;
        float deltaY = (screenHeight - height * scale) / 2f;
        mMatrix.postScale(scale, scale);
        mMatrix.postTranslate(deltaX, deltaY);
        setImageMatrix(mMatrix);
    }

    /**
     * 获得屏幕宽度
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    /**
     * 获得屏幕高度
     */
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }
}
