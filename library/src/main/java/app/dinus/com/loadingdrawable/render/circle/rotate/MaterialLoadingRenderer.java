package app.dinus.com.loadingdrawable.render.circle.rotate;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.animation.Interpolator;

import app.dinus.com.loadingdrawable.DensityUtil;
import app.dinus.com.loadingdrawable.render.LoadingRenderer;

public class MaterialLoadingRenderer extends LoadingRenderer {
    private static final Interpolator MATERIAL_INTERPOLATOR = new FastOutSlowInInterpolator();

    private static final int DEGREE_360 = 360;
    private static final int NUM_POINTS = 5;

    private static final float MAX_SWIPE_DEGREES = 0.8f * DEGREE_360;
    private static final float FULL_GROUP_ROTATION = 3.0f * DEGREE_360;

    private static final float COLOR_START_DELAY_OFFSET = 0.8f;
    private static final float END_TRIM_DURATION_OFFSET = 1.0f;
    private static final float START_TRIM_DURATION_OFFSET = 0.5f;

    private static final float DEFAULT_CENTER_RADIUS = 12.5f;
    private static final float DEFAULT_STROKE_WIDTH = 2.5f;

    private static final int[] DEFAULT_COLORS = new int[]{
            Color.RED, Color.GREEN, Color.BLUE
    };

    private final Paint mPaint = new Paint();
    private final RectF mTempBounds = new RectF();

    private final Animator.AnimatorListener mAnimatorListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationRepeat(Animator animator) {
            super.onAnimationRepeat(animator);
            storeOriginals();
            goToNextColor();

            mStartDegrees = mEndDegrees;
            mRotationCount = (mRotationCount + 1) % (NUM_POINTS);
        }

        @Override
        public void onAnimationStart(Animator animation) {
            super.onAnimationStart(animation);
            mRotationCount = 0;
        }
    };

    private int[] mColors;
    private int mColorIndex;
    private int mCurrentColor;

    private float mStrokeInset;

    private float mRotationCount;
    private float mGroupRotation;

    private float mEndDegrees;
    private float mStartDegrees;
    private float mSwipeDegrees;
    private float mOriginEndDegrees;
    private float mOriginStartDegrees;

    private float mStrokeWidth;
    private float mCenterRadius;

    private MaterialLoadingRenderer(Context context) {
        super(context);
        init(context);
        setupPaint();
        addRenderListener(mAnimatorListener);
    }

    private void init(Context context) {
        mStrokeWidth = DensityUtil.dip2px(context, DEFAULT_STROKE_WIDTH);
        mCenterRadius = DensityUtil.dip2px(context, DEFAULT_CENTER_RADIUS);

        mColors = DEFAULT_COLORS;

        setColorIndex(0);
        initStrokeInset(mWidth, mHeight);
    }

    private void setupPaint() {
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void draw(Canvas canvas) {
        int saveCount = canvas.save();

        mTempBounds.set(mBounds);
        mTempBounds.inset(mStrokeInset, mStrokeInset);

        canvas.rotate(mGroupRotation, mTempBounds.centerX(), mTempBounds.centerY());

        if (mSwipeDegrees != 0) {
            mPaint.setColor(mCurrentColor);
            canvas.drawArc(mTempBounds, mStartDegrees, mSwipeDegrees, false, mPaint);
        }

        canvas.restoreToCount(saveCount);
    }

    @Override
    protected void computeRender(float renderProgress) {
        updateRingColor(renderProgress);

        // Moving the start trim only occurs in the first 50% of a single ring animation
        if (renderProgress <= START_TRIM_DURATION_OFFSET) {
            float startTrimProgress = renderProgress / START_TRIM_DURATION_OFFSET;
            mStartDegrees = mOriginStartDegrees + MAX_SWIPE_DEGREES
                    * MATERIAL_INTERPOLATOR.getInterpolation(startTrimProgress);
        }

        // Moving the end trim starts after 50% of a single ring animation completes
        if (renderProgress > START_TRIM_DURATION_OFFSET) {
            float endTrimProgress = (renderProgress - START_TRIM_DURATION_OFFSET)
                    / (END_TRIM_DURATION_OFFSET - START_TRIM_DURATION_OFFSET);
            mEndDegrees = mOriginEndDegrees + MAX_SWIPE_DEGREES
                    * MATERIAL_INTERPOLATOR.getInterpolation(endTrimProgress);
        }

        if (Math.abs(mEndDegrees - mStartDegrees) > 0) {
            mSwipeDegrees = mEndDegrees - mStartDegrees;
        }

        mGroupRotation = ((FULL_GROUP_ROTATION / NUM_POINTS) * renderProgress)
                + (FULL_GROUP_ROTATION * (mRotationCount / NUM_POINTS));
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

    private void setColorIndex(int index) {
        mColorIndex = index;
        mCurrentColor = mColors[mColorIndex];
    }

    private int getNextColor() {
        return mColors[getNextColorIndex()];
    }

    private int getNextColorIndex() {
        return (mColorIndex + 1) % (mColors.length);
    }

    private void goToNextColor() {
        setColorIndex(getNextColorIndex());
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
    }

    private int getStartingColor() {
        return mColors[mColorIndex];
    }

    private void updateRingColor(float interpolatedTime) {
        if (interpolatedTime > COLOR_START_DELAY_OFFSET) {
            mCurrentColor = evaluateColorChange((interpolatedTime - COLOR_START_DELAY_OFFSET)
                    / (1.0f - COLOR_START_DELAY_OFFSET), getStartingColor(), getNextColor());
        }
    }

    private int evaluateColorChange(float fraction, int startValue, int endValue) {
        int startA = (startValue >> 24) & 0xff;
        int startR = (startValue >> 16) & 0xff;
        int startG = (startValue >> 8) & 0xff;
        int startB = startValue & 0xff;

        int endA = (endValue >> 24) & 0xff;
        int endR = (endValue >> 16) & 0xff;
        int endG = (endValue >> 8) & 0xff;
        int endB = endValue & 0xff;

        return ((startA + (int) (fraction * (endA - startA))) << 24)
                | ((startR + (int) (fraction * (endR - startR))) << 16)
                | ((startG + (int) (fraction * (endG - startG))) << 8)
                | ((startB + (int) (fraction * (endB - startB))));
    }

    private void apply(Builder builder) {
        this.mWidth = builder.mWidth > 0 ? builder.mWidth : this.mWidth;
        this.mHeight = builder.mHeight > 0 ? builder.mHeight : this.mHeight;
        this.mStrokeWidth = builder.mStrokeWidth > 0 ? builder.mStrokeWidth : this.mStrokeWidth;
        this.mCenterRadius = builder.mCenterRadius > 0 ? builder.mCenterRadius : this.mCenterRadius;

        this.mDuration = builder.mDuration > 0 ? builder.mDuration : this.mDuration;

        this.mColors = builder.mColors != null && builder.mColors.length > 0 ? builder.mColors : this.mColors;

        setColorIndex(0);
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

        private int[] mColors;

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

        public Builder setColors(int[] colors) {
            this.mColors = colors;
            return this;
        }

        public MaterialLoadingRenderer build() {
            MaterialLoadingRenderer loadingRenderer = new MaterialLoadingRenderer(mContext);
            loadingRenderer.apply(this);
            return loadingRenderer;
        }
    }
}
