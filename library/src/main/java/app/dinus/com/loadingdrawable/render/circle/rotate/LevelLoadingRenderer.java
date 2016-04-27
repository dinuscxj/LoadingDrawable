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
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import app.dinus.com.loadingdrawable.render.LoadingRenderer;

public class LevelLoadingRenderer extends LoadingRenderer {
    private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    private static final Interpolator MATERIAL_INTERPOLATOR = new FastOutSlowInInterpolator();
    private static final Interpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

    private static final int NUM_POINTS = 5;
    private static final int DEGREE_360 = 360;

    private static final float MIN_SWIPE_DEGREE = 0.1f;
    private static final float MAX_SWIPE_DEGREES = 0.8f * DEGREE_360;
    private static final float FULL_GROUP_ROTATION = 3.0f * DEGREE_360;
    private static final float MAX_ROTATION_INCREMENT = 0.25f * DEGREE_360;

    private static final float LEVEL2_SWEEP_ANGLE_OFFSET = 7.0f / 8.0f;
    private static final float LEVEL3_SWEEP_ANGLE_OFFSET = 5.0f / 8.0f;

    private static final float START_TRIM_DURATION_OFFSET = 0.5f;
    private static final float END_TRIM_DURATION_OFFSET = 1.0f;

    private static final int DEFAULT_COLOR = Color.WHITE;

    private final Paint mPaint = new Paint();
    private final RectF mTempBounds = new RectF();

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

    private int mLevel1Color;
    private int mLevel2Color;
    private int mLevel3Color;

    private float mStrokeInset;

    private float mRotationCount;
    private float mGroupRotation;

    private float mEndDegrees;
    private float mStartDegrees;
    private float mLevel1SwipeDegrees;
    private float mLevel2SwipeDegrees;
    private float mLevel3SwipeDegrees;
    private float mRotationIncrement;
    private float mOriginEndDegrees;
    private float mOriginStartDegrees;
    private float mOriginRotationIncrement;

    public LevelLoadingRenderer(Context context) {
        super(context);
        setupPaint();
        addRenderListener(mAnimatorListener);
    }

    private void setupPaint() {
        mLevel1Color = oneThirdAlphaColor(DEFAULT_COLOR);
        mLevel2Color = twoThirdAlphaColor(DEFAULT_COLOR);
        mLevel3Color = DEFAULT_COLOR;

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

        mPaint.setColor(mLevel1Color);
        canvas.drawArc(arcBounds, mEndDegrees, mLevel1SwipeDegrees, false, mPaint);
        mPaint.setColor(mLevel2Color);
        canvas.drawArc(arcBounds, mEndDegrees, mLevel2SwipeDegrees, false, mPaint);
        mPaint.setColor(mLevel3Color);
        canvas.drawArc(arcBounds, mEndDegrees, mLevel3SwipeDegrees, false, mPaint);

        canvas.restoreToCount(saveCount);
    }

    @Override
    public void computeRender(float renderProgress) {
        // Moving the start trim only occurs in the first 50% of a
        // single ring animation
        if (renderProgress <= START_TRIM_DURATION_OFFSET) {
            float startTrimProgress = (renderProgress) / START_TRIM_DURATION_OFFSET;
            mStartDegrees = mOriginStartDegrees + MAX_SWIPE_DEGREES * MATERIAL_INTERPOLATOR.getInterpolation(startTrimProgress);

            float mSwipeDegrees = MIN_SWIPE_DEGREE;
            if (Math.abs(mEndDegrees - mStartDegrees) > MIN_SWIPE_DEGREE) {
                mSwipeDegrees = mEndDegrees - mStartDegrees;
            }
            float levelSwipeDegreesProgress = Math.abs(mSwipeDegrees) / MAX_SWIPE_DEGREES;

            float level1Increment = DECELERATE_INTERPOLATOR.getInterpolation(levelSwipeDegreesProgress) - LINEAR_INTERPOLATOR.getInterpolation(levelSwipeDegreesProgress);
            float level3Increment = ACCELERATE_INTERPOLATOR.getInterpolation(levelSwipeDegreesProgress) - LINEAR_INTERPOLATOR.getInterpolation(levelSwipeDegreesProgress);

            mLevel1SwipeDegrees = -mSwipeDegrees * (1 + level1Increment);
            mLevel2SwipeDegrees = -mSwipeDegrees * LEVEL2_SWEEP_ANGLE_OFFSET;
            mLevel3SwipeDegrees = -mSwipeDegrees * LEVEL3_SWEEP_ANGLE_OFFSET * (1 + level3Increment);
        }

        // Moving the end trim starts after 50% of a single ring
        // animation completes
        if (renderProgress > START_TRIM_DURATION_OFFSET) {
            float endTrimProgress = (renderProgress - START_TRIM_DURATION_OFFSET) / (END_TRIM_DURATION_OFFSET - START_TRIM_DURATION_OFFSET);
            mEndDegrees = mOriginEndDegrees + MAX_SWIPE_DEGREES * MATERIAL_INTERPOLATOR.getInterpolation(endTrimProgress);

            float mSwipeDegrees = MIN_SWIPE_DEGREE;
            if (Math.abs(mEndDegrees - mStartDegrees) > MIN_SWIPE_DEGREE) {
                mSwipeDegrees = mEndDegrees - mStartDegrees;
            }
            float levelSwipeDegreesProgress = Math.abs(mSwipeDegrees) / MAX_SWIPE_DEGREES;

            if (levelSwipeDegreesProgress > LEVEL2_SWEEP_ANGLE_OFFSET) {
                mLevel1SwipeDegrees = -mSwipeDegrees;
                mLevel2SwipeDegrees = MAX_SWIPE_DEGREES * LEVEL2_SWEEP_ANGLE_OFFSET;
                mLevel3SwipeDegrees = MAX_SWIPE_DEGREES * LEVEL3_SWEEP_ANGLE_OFFSET;
            } else if (levelSwipeDegreesProgress > LEVEL3_SWEEP_ANGLE_OFFSET) {
                mLevel1SwipeDegrees = MIN_SWIPE_DEGREE;
                mLevel2SwipeDegrees = -mSwipeDegrees;
                mLevel3SwipeDegrees = MAX_SWIPE_DEGREES * LEVEL3_SWEEP_ANGLE_OFFSET;
            } else {
                mLevel1SwipeDegrees = MIN_SWIPE_DEGREE;
                mLevel2SwipeDegrees = MIN_SWIPE_DEGREE;
                mLevel3SwipeDegrees = -mSwipeDegrees;
            }
        }

        mGroupRotation = ((FULL_GROUP_ROTATION / NUM_POINTS) * renderProgress) + (FULL_GROUP_ROTATION * (mRotationCount / NUM_POINTS));
        mRotationIncrement = mOriginRotationIncrement + (MAX_ROTATION_INCREMENT * renderProgress);
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
        mLevel1Color = oneThirdAlphaColor(color);
        mLevel2Color = twoThirdAlphaColor(color);
        mLevel3Color = color;
    }

    @Override
    public void setStrokeWidth(float strokeWidth) {
        super.setStrokeWidth(strokeWidth);
        mPaint.setStrokeWidth(strokeWidth);
        invalidateSelf();
    }

    public void setInsets(int width, int height) {
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

        mLevel1SwipeDegrees = MIN_SWIPE_DEGREE;
        mLevel2SwipeDegrees = MIN_SWIPE_DEGREE;
        mLevel3SwipeDegrees = MIN_SWIPE_DEGREE;
    }

    private int oneThirdAlphaColor(int colorValue) {
        int startA = (colorValue >> 24) & 0xff;
        int startR = (colorValue >> 16) & 0xff;
        int startG = (colorValue >> 8) & 0xff;
        int startB = colorValue & 0xff;

        return (startA / 3 << 24)
                | (startR << 16)
                | (startG << 8)
                | startB;
    }

    private int twoThirdAlphaColor(int colorValue) {
        int startA = (colorValue >> 24) & 0xff;
        int startR = (colorValue >> 16) & 0xff;
        int startG = (colorValue >> 8) & 0xff;
        int startB = colorValue & 0xff;

        return (startA * 2 / 3 << 24)
                | (startR << 16)
                | (startG << 8)
                | startB;
    }
}
