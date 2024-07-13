package com.gttcgf.nanoscan;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import androidx.annotation.Nullable;

public class CircularProgressBarView extends View {

    private Paint mPaint; // 画笔
    private CircleBarAnim anim; // 动画
    private float progressSweepAngle;//进度条圆弧扫过的角度

    // 以下是自定义参数
    private int mAnnulusWidth; // 圆环宽度
    private int mProgressWidth; // 进度条宽度
    private int mAnnulusColor; // 圆环颜色
    private int mLoadColor; // 加载进度圆弧扫过的颜色
    private int mProgress = 0; // 当前进度
    private int maxProgress = 100; // 最大进度，默认100
    public int startAngle = -90; // 开始圆点角度

    public CircularProgressBarView(Context context) {
        this(context, null);
    }

    public CircularProgressBarView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircularProgressBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 获取自定义属性
        TypedArray value = context.getTheme().obtainStyledAttributes(attrs, R.styleable.AnnulusCustomizeView, defStyleAttr, 0);
        int indexCount = value.getIndexCount();
        for (int i = 0; i < indexCount; i++) {
            int parm = value.getIndex(i);
            if (parm == R.styleable.AnnulusCustomizeView_startAngle) {
                startAngle = value.getInt(parm, 90);
            } else if (parm == R.styleable.AnnulusCustomizeView_annulusWidth) {
                mAnnulusWidth = value.getDimensionPixelSize(parm,
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                10,
                                getResources().getDisplayMetrics()));
            } else if (parm == R.styleable.AnnulusCustomizeView_progressWidth) {
                mProgressWidth = value.getDimensionPixelSize(parm,
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                10,
                                getResources().getDisplayMetrics()));
            } else if (parm == R.styleable.AnnulusCustomizeView_annulusColor) {
                mAnnulusColor = value.getColor(parm, Color.BLACK);
            } else if (parm == R.styleable.AnnulusCustomizeView_loadColor) {
                mLoadColor = value.getColor(parm, Color.BLACK);
            } else if (parm == R.styleable.AnnulusCustomizeView_progress) {
                mProgress = value.getInt(parm, 10);
            }
        }
        value.recycle();
        // 动画
        anim = new CircleBarAnim();
        mPaint = new Paint();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // TODO:绘制圆环
        // 获取圆形坐标
        int centre = getWidth() / 2;
        // 获取半径
        int radius = centre - mAnnulusWidth / 2 - 10;
        // 取消锯齿
        mPaint.setAntiAlias(true);
        // 设置画笔宽度
        mPaint.setStrokeWidth(mAnnulusWidth);
        // 设置空心
        mPaint.setStyle(Paint.Style.STROKE);
        // 设置画笔颜色
        mPaint.setColor(mAnnulusColor);
        canvas.drawCircle(centre, centre, radius, mPaint);

        // TODO:画圆弧，进度
        // 获取进度条中心点
        // 进度条半径
        int progressRadius = centre - mAnnulusWidth / 2 - 10;
        // 设置进度颜色
        mPaint.setColor(mLoadColor);
        mPaint.setStrokeWidth(mProgressWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        // 用于定义的圆弧的形状和大小的界限
        RectF ovalStroke = new RectF(centre - progressRadius, centre - progressRadius,
                centre + progressRadius, centre + progressRadius);
        canvas.drawArc(ovalStroke, startAngle, progressSweepAngle, false, mPaint);
    }

    /**
     * 设置进度
     *
     * @param progress 进度值
     * @param time     动画时间范围
     */
    public synchronized void setProgress(final int progress, int time) {
        anim.setDuration(time);
        this.startAnimation(anim);
        this.mProgress = progress;
    }

    // 动画
    public class CircleBarAnim extends Animation {
        public CircleBarAnim() {

        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            progressSweepAngle = interpolatedTime * (mProgress * 360 / maxProgress);//这里计算进度条的比例

            postInvalidate();
        }
    }

    public synchronized int getmProgress() {
        return mProgress;
    }
}
