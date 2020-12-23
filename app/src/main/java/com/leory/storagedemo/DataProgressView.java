package com.leory.storagedemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * @Description: 数据更新进度条
 * @Author: leory
 * @Time: 2020/12/17
 */
public class DataProgressView extends View {
    private static final int DEFAULT_SECONDARY_STROKE_COLOR = 0x4dffffff;
    private static final int DEFAULT_PRIMARY_STROKE_COLOR = 0xffffffff;
    private static final int DEFAULT_TEXT_COLOR = 0xffffffff;
    private static int DEFAULT_PROGRESS_SIZE;
    private static int DEFAULT_STROKE_WIDTH;
    private static int DEFAULT_TEXT_SIZE;
    private int mSecondaryStrokeColor;//进度条背景颜色
    private int mPrimaryStrokeColor;//进度条颜色
    private int mStrokeWidth;//进度条宽度
    private int mProgressSize;//进度条大小，即圆的直径
    private int mTextSize;//文字大小
    private int mTextColor;//文字颜色
    private float mCurrentProgress;//当前进度
    private Paint mProgressPaint;//进度条画笔
    private Paint mTextPaint;//文字画笔
    private String mTipText;//提示文本

    public DataProgressView(Context context) {
        this(context, null);
    }

    public DataProgressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DataProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        DEFAULT_STROKE_WIDTH = dp2px(2);
        DEFAULT_TEXT_SIZE = sp2px(14);
        DEFAULT_PROGRESS_SIZE = dp2px(50);
        mStrokeWidth = DEFAULT_STROKE_WIDTH;
        mTextSize = DEFAULT_TEXT_SIZE;
        mProgressSize = DEFAULT_PROGRESS_SIZE;
        mSecondaryStrokeColor = DEFAULT_SECONDARY_STROKE_COLOR;
        mPrimaryStrokeColor = DEFAULT_PRIMARY_STROKE_COLOR;
        mTextColor = DEFAULT_TEXT_COLOR;
        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeWidth(mStrokeWidth);
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    private int dp2px(float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpValue, getResources().getDisplayMetrics());
    }

    private int sp2px(float spValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spValue, getResources().getDisplayMetrics());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        int radius = mProgressSize / 2;
        //画背景进度条
        mProgressPaint.setColor(mSecondaryStrokeColor);
        canvas.drawCircle(width / 2, height / 2, radius, mProgressPaint);
        //画进度条
        mProgressPaint.setColor(mPrimaryStrokeColor);
        float x = (width - mProgressSize) / 2;
        float y = (height - mProgressSize) / 2;
        int sweepAngle = (int) (360 * mCurrentProgress);
        RectF oval = new RectF(x, y, x + mProgressSize, y + mProgressSize);
        canvas.drawArc(oval, -90, sweepAngle, false, mProgressPaint);
        //画文字
        String text = mTipText;
        if (TextUtils.isEmpty(mTipText)) {
            text = "正在更新数据 " + (int) (mCurrentProgress * 100) + "%";
        }
        float textX = width / 2;
        float textY = height / 2 + radius + dp2px(30);
        mTextPaint.setColor(mTextColor);
        canvas.drawText(text, textX, textY, mTextPaint);
    }

    /**
     * 设置进度条背景颜色
     *
     * @param color
     */
    public void setSecondaryStrokeColor(int color) {
        mSecondaryStrokeColor = color;
    }

    /**
     * 设置进度条颜色
     *
     * @param color
     */
    public void setPrimaryStrokeColor(int color) {
        mPrimaryStrokeColor = color;
    }

    /**
     * 设置文本字体颜色
     *
     * @param color
     */
    public void setTextColor(int color) {
        mTextColor = color;
    }

    /**
     * 设置进度条大小
     *
     * @param size px
     */
    public void setProgressSize(int size) {
        mProgressSize = size;
    }

    /**
     * 设置字体大小
     *
     * @param size px
     */
    public void setTextSize(int size) {
        mTextSize = size;
        mTextPaint.setTextSize(mTextSize);
    }

    /**
     * 设置进度条宽度
     *
     * @param width px
     */
    public void setStrokeWidth(int width) {
        mStrokeWidth = width;
        mProgressPaint.setStrokeWidth(mStrokeWidth);
    }

    /**
     * 进度更新
     *
     * @param progress 进度0f-1f
     * @param tips     提示文本
     */
    public void updateProgress(float progress, String tips) {
        mCurrentProgress = progress;
        mTipText = tips;
        invalidate();
    }

    /**
     * 进度更新
     *
     * @param progress 进度0f-1f
     */
    public void updateProgress(float progress) {
        mCurrentProgress = progress;
        mTipText = null;
        invalidate();
    }
}
