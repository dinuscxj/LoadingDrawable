package app.dinus.com.loadingdrawable.render.circle.jump;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import app.dinus.com.loadingdrawable.DensityUtil;
import app.dinus.com.loadingdrawable.render.LoadingRenderer;

public class SwapLoadingRenderer extends LoadingRenderer {
    private static final Interpolator ACCELERATE_DECELERATE_INTERPOLATOR = new AccelerateDecelerateInterpolator();

    private static final long ANIMATION_DURATION = 2500;

    private static final int DEFAULT_CIRCLE_COUNT = 5;

    private static final float DEFAULT_BALL_RADIUS = 7.5f;
    private static final float DEFAULT_WIDTH = 15.0f * 11;
    private static final float DEFAULT_HEIGHT = 15.0f * 5;
    private static final float DEFAULT_STROKE_WIDTH = 1.5f;

    private static final int DEFAULT_COLOR = Color.WHITE;

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int mColor;

    private int mSwapIndex;
    private int mBallCount;

    private float mBallSideOffsets;
    private float mBallCenterY;
    private float mBallRadius;
    private float mBallInterval;
    private float mSwapBallOffsetX;
    private float mSwapBallOffsetY;
    private float mASwapThreshold;

    private float mStrokeWidth;

    private SwapLoadingRenderer(Context context) {
        super(context);

        init(context);
        adjustParams();
        setupPaint();
    }

    private void init(Context context) {
        mWidth = DensityUtil.dip2px(context, DEFAULT_WIDTH);
        mHeight = DensityUtil.dip2px(context, DEFAULT_HEIGHT);
        mBallRadius = DensityUtil.dip2px(context, DEFAULT_BALL_RADIUS);
        mStrokeWidth = DensityUtil.dip2px(context, DEFAULT_STROKE_WIDTH);

        mColor = DEFAULT_COLOR;
        mDuration = ANIMATION_DURATION;
        mBallCount = DEFAULT_CIRCLE_COUNT;

        mBallInterval = mBallRadius;
    }

    private void adjustParams() {
        mBallCenterY = mHeight / 2.0f;
        mBallSideOffsets = (mWidth - mBallRadius * 2 * mBallCount - mBallInterval * (mBallCount - 1)) / 2.0f;

        mASwapThreshold = 1.0f / mBallCount;
    }

    private void setupPaint() {
        mPaint.setColor(mColor);
        mPaint.setStrokeWidth(mStrokeWidth);
    }

    @Override
    protected void draw(Canvas canvas) {
        int saveCount = canvas.save();

        for (int i = 0; i < mBallCount; i++) {
            if (i == mSwapIndex) {
                mPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(mBallSideOffsets + mBallRadius * (i * 2 + 1) + i * mBallInterval + mSwapBallOffsetX
                        , mBallCenterY - mSwapBallOffsetY, mBallRadius, mPaint);
            } else if (i == (mSwapIndex + 1) % mBallCount) {
                mPaint.setStyle(Paint.Style.STROKE);
                canvas.drawCircle(mBallSideOffsets + mBallRadius * (i * 2 + 1) + i * mBallInterval - mSwapBallOffsetX
                        , mBallCenterY + mSwapBallOffsetY, mBallRadius - mStrokeWidth / 2, mPaint);
            } else {
                mPaint.setStyle(Paint.Style.STROKE);
                canvas.drawCircle(mBallSideOffsets + mBallRadius * (i * 2 + 1) + i * mBallInterval, mBallCenterY
                        , mBallRadius - mStrokeWidth / 2, mPaint);
            }
        }

        canvas.restoreToCount(saveCount);
    }

    @Override
    protected void computeRender(float renderProgress) {
        mSwapIndex = (int) (renderProgress / mASwapThreshold);

        // Swap trace : x^2 + y^2 = r ^ 2
        float swapTraceProgress = ACCELERATE_DECELERATE_INTERPOLATOR.getInterpolation(
                (renderProgress - mSwapIndex * mASwapThreshold) / mASwapThreshold);

        float swapTraceRadius = mSwapIndex == mBallCount - 1
                ? (mBallRadius * 2 * (mBallCount - 1) + mBallInterval * (mBallCount - 1)) / 2
                : (mBallRadius * 2 + mBallInterval) / 2;

        // Calculate the X offset of the swap ball
        mSwapBallOffsetX = mSwapIndex == mBallCount - 1
                ? -swapTraceProgress * swapTraceRadius * 2
                : swapTraceProgress * swapTraceRadius * 2;

        // if mSwapIndex == mBallCount - 1 then (swapTraceRadius, swapTraceRadius) as the origin of coordinates
        // else (-swapTraceRadius, -swapTraceRadius) as the origin of coordinates
        float xCoordinate = mSwapIndex == mBallCount - 1
                ? mSwapBallOffsetX + swapTraceRadius
                : mSwapBallOffsetX - swapTraceRadius;

        // Calculate the Y offset of the swap ball
        mSwapBallOffsetY = (float) (mSwapIndex % 2 == 0 && mSwapIndex != mBallCount - 1
                ? Math.sqrt(Math.pow(swapTraceRadius, 2.0f) - Math.pow(xCoordinate, 2.0f))
                : -Math.sqrt(Math.pow(swapTraceRadius, 2.0f) - Math.pow(xCoordinate, 2.0f)));

    }

    @Override
    protected void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    protected void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    @Override
    protected void reset() {
    }

    private void apply(Builder builder) {
        this.mWidth = builder.mWidth > 0 ? builder.mWidth : this.mWidth;
        this.mHeight = builder.mHeight > 0 ? builder.mHeight : this.mHeight;
        this.mStrokeWidth = builder.mStrokeWidth > 0 ? builder.mStrokeWidth : this.mStrokeWidth;

        this.mBallRadius = builder.mBallRadius > 0 ? builder.mBallRadius : this.mBallRadius;
        this.mBallInterval = builder.mBallInterval > 0 ? builder.mBallInterval : this.mBallInterval;
        this.mBallCount = builder.mBallCount > 0 ? builder.mBallCount : this.mBallCount;

        this.mColor = builder.mColor != 0 ? builder.mColor : this.mColor;

        this.mDuration = builder.mDuration > 0 ? builder.mDuration : this.mDuration;

        adjustParams();
        setupPaint();
    }

    public static class Builder {
        private Context mContext;

        private int mWidth;
        private int mHeight;
        private int mStrokeWidth;

        private int mBallCount;
        private int mBallRadius;
        private int mBallInterval;

        private int mDuration;

        private int mColor;

        public Builder(Context mContext) {
            this.mContext = mContext;
        }

        public Builder setWidth(int width) {
            this.mWidth = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.mHeight = height;
            return this;
        }

        public Builder setStrokeWidth(int strokeWidth) {
            this.mStrokeWidth = strokeWidth;
            return this;
        }

        public Builder setBallRadius(int ballRadius) {
            this.mBallRadius = ballRadius;
            return this;
        }

        public Builder setBallInterval(int ballInterval) {
            this.mBallInterval = ballInterval;
            return this;
        }

        public Builder setBallCount(int ballCount) {
            this.mBallCount = ballCount;
            return this;
        }

        public Builder setColor(int color) {
            this.mColor = color;
            return this;
        }

        public Builder setDuration(int duration) {
            this.mDuration = duration;
            return this;
        }

        public SwapLoadingRenderer build() {
            SwapLoadingRenderer loadingRenderer = new SwapLoadingRenderer(mContext);
            loadingRenderer.apply(this);
            return loadingRenderer;
        }
    }
}
