package app.dinus.com.loadingdrawable.render.circle.rotate;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.IntRange;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import app.dinus.com.loadingdrawable.DensityUtil;
import app.dinus.com.loadingdrawable.render.LoadingRenderer;

public class GearLoadingRenderer extends LoadingRenderer {
    private static final Interpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

    private static final int GEAR_COUNT = 4;
    private static final int NUM_POINTS = 3;
    private static final int MAX_ALPHA = 255;
    private static final int DEGREE_360 = 360;

    private static final int DEFAULT_GEAR_SWIPE_DEGREES = 60;

    private static final float FULL_GROUP_ROTATION = 3.0f * DEGREE_360;

    private static final float START_SCALE_DURATION_OFFSET = 0.3f;
    private static final float START_TRIM_DURATION_OFFSET = 0.5f;
    private static final float END_TRIM_DURATION_OFFSET = 0.7f;
    private static final float END_SCALE_DURATION_OFFSET = 1.0f;

    private static final float DEFAULT_CENTER_RADIUS = 12.5f;
    private static final float DEFAULT_STROKE_WIDTH = 2.5f;

    private static final int DEFAULT_COLOR = Color.WHITE;

    private final Paint mPaint = new Paint();
    private final RectF mTempBounds = new RectF();

    private final Animator.AnimatorListener mAnimatorListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationRepeat(Animator animator) {
            super.onAnimationRepeat(animator);
            storeOriginals();

            mStartDegrees = mEndDegrees;
            mRotationCount = (mRotationCount + 1) % NUM_POINTS;
        }

        @Override
        public void onAnimationStart(Animator animation) {
            super.onAnimationStart(animation);
            mRotationCount = 0;
        }
    };

    private int mColor;

    private int mGearCount;
    private int mGearSwipeDegrees;

    private float mStrokeInset;

    private float mRotationCount;
    private float mGroupRotation;

    private float mScale;
    private float mEndDegrees;
    private float mStartDegrees;
    private float mSwipeDegrees;
    private float mOriginEndDegrees;
    private float mOriginStartDegrees;

    private float mStrokeWidth;
    private float mCenterRadius;

    private GearLoadingRenderer(Context context) {
        super(context);

        init(context);
        setupPaint();
        addRenderListener(mAnimatorListener);
    }

    private void init(Context context) {
        mStrokeWidth = DensityUtil.dip2px(context, DEFAULT_STROKE_WIDTH);
        mCenterRadius = DensityUtil.dip2px(context, DEFAULT_CENTER_RADIUS);

        mColor = DEFAULT_COLOR;

        mGearCount = GEAR_COUNT;
        mGearSwipeDegrees = DEFAULT_GEAR_SWIPE_DEGREES;
    }

    private void setupPaint() {
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        initStrokeInset(mWidth, mHeight);
    }

    @Override
    protected void draw(Canvas canvas) {
        int saveCount = canvas.save();

        mTempBounds.set(mBounds);
        mTempBounds.inset(mStrokeInset, mStrokeInset);
        mTempBounds.inset(mTempBounds.width() * (1.0f - mScale) / 2.0f, mTempBounds.width() * (1.0f - mScale) / 2.0f);

        canvas.rotate(mGroupRotation, mTempBounds.centerX(), mTempBounds.centerY());

        mPaint.setColor(mColor);
        mPaint.setAlpha((int) (MAX_ALPHA * mScale));
        mPaint.setStrokeWidth(mStrokeWidth * mScale);

        if (mSwipeDegrees != 0) {
            for (int i = 0; i < mGearCount; i++) {
                canvas.drawArc(mTempBounds, mStartDegrees + DEGREE_360 / mGearCount * i, mSwipeDegrees, false, mPaint);
            }
        }

        canvas.restoreToCount(saveCount);
    }

    @Override
    protected void computeRender(float renderProgress) {
        // Scaling up the start size only occurs in the first 20% of a single ring animation
        if (renderProgress <= START_SCALE_DURATION_OFFSET) {
            float startScaleProgress = (renderProgress) / START_SCALE_DURATION_OFFSET;
            mScale = DECELERATE_INTERPOLATOR.getInterpolation(startScaleProgress);
        }

        // Moving the start trim only occurs between 20% to 50% of a single ring animation
        if (renderProgress <= START_TRIM_DURATION_OFFSET && renderProgress > START_SCALE_DURATION_OFFSET) {
            float startTrimProgress = (renderProgress - START_SCALE_DURATION_OFFSET) / (START_TRIM_DURATION_OFFSET - START_SCALE_DURATION_OFFSET);
            mStartDegrees = mOriginStartDegrees + mGearSwipeDegrees * startTrimProgress;
        }

        // Moving the end trim starts between 50% to 80% of a single ring animation
        if (renderProgress <= END_TRIM_DURATION_OFFSET && renderProgress > START_TRIM_DURATION_OFFSET) {
            float endTrimProgress = (renderProgress - START_TRIM_DURATION_OFFSET) / (END_TRIM_DURATION_OFFSET - START_TRIM_DURATION_OFFSET);
            mEndDegrees = mOriginEndDegrees + mGearSwipeDegrees * endTrimProgress;
        }

        // Scaling down the end size starts after 80% of a single ring animation
        if (renderProgress > END_TRIM_DURATION_OFFSET) {
            float endScaleProgress = (renderProgress - END_TRIM_DURATION_OFFSET) / (END_SCALE_DURATION_OFFSET - END_TRIM_DURATION_OFFSET);
            mScale = 1.0f - ACCELERATE_INTERPOLATOR.getInterpolation(endScaleProgress);
        }

        if (renderProgress <= END_TRIM_DURATION_OFFSET && renderProgress > START_SCALE_DURATION_OFFSET) {
            float rotateProgress = (renderProgress - START_SCALE_DURATION_OFFSET) / (END_TRIM_DURATION_OFFSET - START_SCALE_DURATION_OFFSET);
            mGroupRotation = ((FULL_GROUP_ROTATION / NUM_POINTS) * rotateProgress) + (FULL_GROUP_ROTATION * (mRotationCount / NUM_POINTS));
        }

        if (Math.abs(mEndDegrees - mStartDegrees) > 0) {
            mSwipeDegrees = mEndDegrees - mStartDegrees;
        }
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
        resetOriginals();
    }

    private void initStrokeInset(float width, float height) {
        float minSize = Math.min(width, height);
        float strokeInset = minSize / 2.0f - mCenterRadius;
        float minStrokeInset = (float) Math.ceil(mStrokeWidth / 2.0f);
        mStrokeInset = strokeInset < minStrokeInset ? minStrokeInset : strokeInset;
    }

    private void storeOriginals() {
        mOriginEndDegrees = mEndDegrees;
        mOriginStartDegrees = mEndDegrees;
    }

    private void resetOriginals() {
        mOriginEndDegrees = 0;
        mOriginStartDegrees = 0;

        mEndDegrees = 0;
        mStartDegrees = 0;

        mSwipeDegrees = 1;
    }

    private void apply(Builder builder) {
        this.mWidth = builder.mWidth > 0 ? builder.mWidth : this.mWidth;
        this.mHeight = builder.mHeight > 0 ? builder.mHeight : this.mHeight;
        this.mStrokeWidth = builder.mStrokeWidth > 0 ? builder.mStrokeWidth : this.mStrokeWidth;
        this.mCenterRadius = builder.mCenterRadius > 0 ? builder.mCenterRadius : this.mCenterRadius;

        this.mDuration = builder.mDuration > 0 ? builder.mDuration : this.mDuration;

        this.mColor = builder.mColor != 0 ? builder.mColor : this.mColor;

        this.mGearCount = builder.mGearCount > 0 ? builder.mGearCount : this.mGearCount;
        this.mGearSwipeDegrees = builder.mGearSwipeDegrees > 0 ? builder.mGearSwipeDegrees : this.mGearSwipeDegrees;

        setupPaint();
        initStrokeInset(this.mWidth, this.mHeight);
    }

    public static class Builder {
        private Context mContext;

        private int mWidth;
        private int mHeight;
        private int mStrokeWidth;
        private int mCenterRadius;

        private int mDuration;

        private int mColor;

        private int mGearCount;
        private int mGearSwipeDegrees;

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

        public Builder setCenterRadius(int centerRadius) {
            this.mCenterRadius = centerRadius;
            return this;
        }

        public Builder setDuration(int duration) {
            this.mDuration = duration;
            return this;
        }

        public Builder setColor(int color) {
            this.mColor = color;
            return this;
        }

        public Builder setGearCount(int gearCount) {
            this.mGearCount = gearCount;
            return this;
        }

        public Builder setGearSwipeDegrees(@IntRange(from = 0, to = 360) int gearSwipeDegrees) {
            this.mGearSwipeDegrees = gearSwipeDegrees;
            return this;
        }

        public GearLoadingRenderer build() {
            GearLoadingRenderer loadingRenderer = new GearLoadingRenderer(mContext);
            loadingRenderer.apply(this);
            return loadingRenderer;
        }
    }
}
