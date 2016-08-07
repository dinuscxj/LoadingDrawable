package app.dinus.com.loadingdrawable.render.circle.rotate;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.animation.Interpolator;

import app.dinus.com.loadingdrawable.DensityUtil;
import app.dinus.com.loadingdrawable.render.LoadingRenderer;

public class WhorlLoadingRenderer extends LoadingRenderer {
    private static final Interpolator MATERIAL_INTERPOLATOR = new FastOutSlowInInterpolator();

    private static final int DEGREE_180 = 180;
    private static final int DEGREE_360 = 360;
    private static final int NUM_POINTS = 5;

    private static final float MAX_SWIPE_DEGREES = 0.6f * DEGREE_360;
    private static final float FULL_GROUP_ROTATION = 3.0f * DEGREE_360;

    private static final float START_TRIM_DURATION_OFFSET = 0.5f;
    private static final float END_TRIM_DURATION_OFFSET = 1.0f;

    private static final float DEFAULT_CENTER_RADIUS = 12.5f;
    private static final float DEFAULT_STROKE_WIDTH = 2.5f;

    private static final int[] DEFAULT_COLORS = new int[]{
            Color.RED, Color.GREEN, Color.BLUE
    };

    private final Paint mPaint = new Paint();
    private final RectF mTempBounds = new RectF();
    private final RectF mTempArcBounds = new RectF();

    private final Animator.AnimatorListener mAnimatorListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationRepeat(Animator animator) {
            super.onAnimationRepeat(animator);
            storeOriginals();

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

    private WhorlLoadingRenderer(Context context) {
        super(context);
        init(context);
        setupPaint();
        addRenderListener(mAnimatorListener);
    }

    private void init(Context context) {
        mColors = DEFAULT_COLORS;
        mStrokeWidth = DensityUtil.dip2px(context, DEFAULT_STROKE_WIDTH);
        mCenterRadius = DensityUtil.dip2px(context, DEFAULT_CENTER_RADIUS);

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
            for (int i = 0; i < mColors.length; i++) {
                mPaint.setStrokeWidth(mStrokeWidth / (i + 1));
                mPaint.setColor(mColors[i]);
                canvas.drawArc(createArcBounds(mTempBounds, i), mStartDegrees + DEGREE_180 * (i % 2),
                        mSwipeDegrees, false, mPaint);
            }
        }

        canvas.restoreToCount(saveCount);
    }

    private RectF createArcBounds(RectF sourceArcBounds, int index) {
        int intervalWidth = 0;

        for (int i = 0; i < index; i++) {
            intervalWidth += mStrokeWidth / (i + 1.0f) * 1.5f;
        }

        int arcBoundsLeft = (int) (sourceArcBounds.left + intervalWidth);
        int arcBoundsTop = (int) (sourceArcBounds.top + intervalWidth);
        int arcBoundsRight = (int) (sourceArcBounds.right - intervalWidth);
        int arcBoundsBottom = (int) (sourceArcBounds.bottom - intervalWidth);
        mTempArcBounds.set(arcBoundsLeft, arcBoundsTop, arcBoundsRight, arcBoundsBottom);

        return mTempArcBounds;
    }

    @Override
    protected void computeRender(float renderProgress) {
        // Moving the start trim only occurs in the first 50% of a single ring animation
        if (renderProgress <= START_TRIM_DURATION_OFFSET) {
            float startTrimProgress = (renderProgress) / (1.0f - START_TRIM_DURATION_OFFSET);
            mStartDegrees = mOriginStartDegrees + MAX_SWIPE_DEGREES * MATERIAL_INTERPOLATOR.getInterpolation(startTrimProgress);
        }

        // Moving the end trim starts after 50% of a single ring animation
        if (renderProgress > START_TRIM_DURATION_OFFSET) {
            float endTrimProgress = (renderProgress - START_TRIM_DURATION_OFFSET) / (END_TRIM_DURATION_OFFSET - START_TRIM_DURATION_OFFSET);
            mEndDegrees = mOriginEndDegrees + MAX_SWIPE_DEGREES * MATERIAL_INTERPOLATOR.getInterpolation(endTrimProgress);
        }

        if (Math.abs(mEndDegrees - mStartDegrees) > 0) {
            mSwipeDegrees = mEndDegrees - mStartDegrees;
        }

        mGroupRotation = ((FULL_GROUP_ROTATION / NUM_POINTS) * renderProgress) + (FULL_GROUP_ROTATION * (mRotationCount / NUM_POINTS));
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

        mSwipeDegrees = 0;
    }

    private void apply(Builder builder) {
        this.mWidth = builder.mWidth > 0 ? builder.mWidth : this.mWidth;
        this.mHeight = builder.mHeight > 0 ? builder.mHeight : this.mHeight;
        this.mStrokeWidth = builder.mStrokeWidth > 0 ? builder.mStrokeWidth : this.mStrokeWidth;
        this.mCenterRadius = builder.mCenterRadius > 0 ? builder.mCenterRadius : this.mCenterRadius;

        this.mDuration = builder.mDuration > 0 ? builder.mDuration : this.mDuration;

        this.mColors = builder.mColors != null && builder.mColors.length > 0 ? builder.mColors : this.mColors;

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

        public WhorlLoadingRenderer build() {
            WhorlLoadingRenderer loadingRenderer = new WhorlLoadingRenderer(mContext);
            loadingRenderer.apply(this);
            return loadingRenderer;
        }
    }
}
