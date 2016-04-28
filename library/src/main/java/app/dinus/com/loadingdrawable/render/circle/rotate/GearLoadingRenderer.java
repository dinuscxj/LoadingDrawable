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
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import app.dinus.com.loadingdrawable.render.LoadingRenderer;

public class GearLoadingRenderer extends LoadingRenderer {
    private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    private static final Interpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

    private static final int GEAR_COUNT = 4;
    private static final int NUM_POINTS = 3;
    private static final int MAX_ALPHA = 255;
    private static final int DEGREE_360 = 360;

    private static final float MIN_SWIPE_DEGREE = 0.1f;
    private static final float MAX_SWIPE_DEGREES = 0.17f * DEGREE_360;
    private static final float FULL_GROUP_ROTATION = 3.0f * DEGREE_360;
    private static final float MAX_ROTATION_INCREMENT = 0.25f * DEGREE_360;

    private static final float START_SCALE_DURATION_OFFSET = 0.3f;
    private static final float START_TRIM_DURATION_OFFSET = 0.5f;
    private static final float END_TRIM_DURATION_OFFSET = 0.7f;
    private static final float END_SCALE_DURATION_OFFSET = 1.0f;

    private static final int DEFAULT_COLOR = Color.WHITE;

    private final Paint mPaint = new Paint();
    private final RectF mTempBounds = new RectF();

    private boolean mIsScaling;

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

    private int mCurrentColor;

    private float mStrokeInset;

    private float mRotationCount;
    private float mGroupRotation;

    private float mScale;
    private float mEndDegrees;
    private float mStartDegrees;
    private float mSwipeDegrees;
    private float mRotationIncrement;
    private float mOriginEndDegrees;
    private float mOriginStartDegrees;
    private float mOriginRotationIncrement;

    public GearLoadingRenderer(Context context) {
        super(context);
        mCurrentColor = DEFAULT_COLOR;

        setupPaint();
        addRenderListener(mAnimatorListener);
    }

    private void setupPaint() {
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(getStrokeWidth());
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        setInsets((int) getWidth(), (int) getHeight());
    }

    @Override
    public void draw(Canvas canvas, Rect bounds) {
        int saveCount = canvas.save();

        canvas.rotate(mGroupRotation, bounds.exactCenterX(), bounds.exactCenterY());
        RectF arcBounds = mTempBounds;
        arcBounds.set(bounds);
        arcBounds.inset(mStrokeInset, mStrokeInset);

        arcBounds.inset(arcBounds.width() * (1.0f - mScale) / 2.0f, arcBounds.width() * (1.0f - mScale) / 2.0f);

        mPaint.setColor(mCurrentColor);
        mPaint.setAlpha((int) (MAX_ALPHA * mScale));
        mPaint.setStrokeWidth(getStrokeWidth() * mScale);
        for (int i = 0; i < GEAR_COUNT; i++) {
            canvas.drawArc(arcBounds, mStartDegrees + DEGREE_360 / GEAR_COUNT * i, mSwipeDegrees, false, mPaint);
        }

        canvas.restoreToCount(saveCount);
    }

    @Override
    public void computeRender(float renderProgress) {
        // Scaling up the start size only occurs in the first 20% of a
        // single ring animation
        if (renderProgress <= START_SCALE_DURATION_OFFSET) {
            float startScaleProgress = (renderProgress) / START_SCALE_DURATION_OFFSET;
            mScale = DECELERATE_INTERPOLATOR.getInterpolation(startScaleProgress);
        }

        // Moving the start trim only occurs between 20% to 50% of a
        // single ring animation
        if (renderProgress <= START_TRIM_DURATION_OFFSET && renderProgress > START_SCALE_DURATION_OFFSET) {
            float startTrimProgress = (renderProgress - START_SCALE_DURATION_OFFSET) / (START_TRIM_DURATION_OFFSET - START_SCALE_DURATION_OFFSET);
            mStartDegrees = mOriginStartDegrees + MAX_SWIPE_DEGREES * LINEAR_INTERPOLATOR.getInterpolation(startTrimProgress);
        }

        // Moving the end trim starts between 50% to 80% of a single ring
        // animation completes
        if (renderProgress <= END_TRIM_DURATION_OFFSET && renderProgress > START_TRIM_DURATION_OFFSET) {
            float endTrimProgress = (renderProgress - START_TRIM_DURATION_OFFSET) / (END_TRIM_DURATION_OFFSET - START_TRIM_DURATION_OFFSET);
            mEndDegrees = mOriginEndDegrees + MAX_SWIPE_DEGREES * LINEAR_INTERPOLATOR.getInterpolation(endTrimProgress);
        }

        // Scaling down the end size starts after 80% of a single ring
        // animation completes
        if (renderProgress > END_TRIM_DURATION_OFFSET) {
            float endScaleProgress = (renderProgress - END_TRIM_DURATION_OFFSET) / (END_SCALE_DURATION_OFFSET - END_TRIM_DURATION_OFFSET);
            mScale = 1.0f - ACCELERATE_INTERPOLATOR.getInterpolation(endScaleProgress);
        }

        if (Math.abs(mEndDegrees - mStartDegrees) > MIN_SWIPE_DEGREE) {
            mSwipeDegrees = mEndDegrees - mStartDegrees;
        }

        if (renderProgress <= END_TRIM_DURATION_OFFSET && renderProgress > START_SCALE_DURATION_OFFSET) {
            float rotateProgress = (renderProgress - START_SCALE_DURATION_OFFSET) / (END_TRIM_DURATION_OFFSET - START_SCALE_DURATION_OFFSET);
            mGroupRotation = ((FULL_GROUP_ROTATION / NUM_POINTS) * rotateProgress) + (FULL_GROUP_ROTATION * (mRotationCount / NUM_POINTS));
            mRotationIncrement = mOriginRotationIncrement + (MAX_ROTATION_INCREMENT * rotateProgress);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
        invalidateSelf();
    }

    @Override
    public void reset() {
        resetOriginals();
    }

    public void setColor(int color) {
        mCurrentColor = color;
    }

    @Override
    public void setStrokeWidth(float strokeWidth) {
        super.setStrokeWidth(strokeWidth);
        mPaint.setStrokeWidth(strokeWidth);
        invalidateSelf();
    }

    private void setInsets(int width, int height) {
        final float minEdge = (float) Math.min(width, height);
        float insets;
        if (getCenterRadius() <= 0 || minEdge < 0) {
            insets = (float) Math.ceil(getStrokeWidth() / 2.0f);
        } else {
            insets = minEdge / 2.0f - getCenterRadius();
        }
        mStrokeInset = insets;
    }

    private void storeOriginals() {
        mOriginEndDegrees = mEndDegrees;
        mOriginStartDegrees = mStartDegrees;
        mOriginRotationIncrement = mRotationIncrement;
    }

    private void resetOriginals() {
        mOriginEndDegrees = 0;
        mOriginStartDegrees = 0;
        mOriginRotationIncrement = 0;

        mEndDegrees = 0;
        mStartDegrees = 0;
        mRotationIncrement = 0;

        mSwipeDegrees = MIN_SWIPE_DEGREE;
    }
}
