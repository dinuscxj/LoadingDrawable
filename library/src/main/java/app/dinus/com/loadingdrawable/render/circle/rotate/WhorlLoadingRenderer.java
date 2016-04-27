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

import app.dinus.com.loadingdrawable.render.LoadingRenderer;

public class WhorlLoadingRenderer extends LoadingRenderer {
    private static final Interpolator MATERIAL_INTERPOLATOR = new FastOutSlowInInterpolator();

    private static final int DEGREE_180 = 180;
    private static final int DEGREE_360 = 360;
    private static final int NUM_POINTS = 5;

    private static final float MIN_SWIPE_DEGREE = 0.1f;
    private static final float MAX_SWIPE_DEGREES = 0.6f * DEGREE_360;
    private static final float FULL_GROUP_ROTATION = 3.0f * DEGREE_360;
    private static final float MAX_ROTATION_INCREMENT = 0.25f * DEGREE_360;

    private static final float START_TRIM_DURATION_OFFSET = 0.5f;
    private static final float END_TRIM_DURATION_OFFSET = 1.0f;

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
    private float mRotationIncrement;
    private float mOriginEndDegrees;
    private float mOriginStartDegrees;
    private float mOriginRotationIncrement;

    public WhorlLoadingRenderer(Context context) {
        super(context);
        init();
        setupPaint();
        addRenderListener(mAnimatorListener);
    }

    private void init() {
        mColors = DEFAULT_COLORS;
        setInsets((int) getWidth(), (int) getHeight());
    }

    private void setupPaint() {
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(getStrokeWidth());
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    public void draw(Canvas canvas, Rect bounds) {
        int saveCount = canvas.save();

        canvas.rotate(mGroupRotation, bounds.exactCenterX(), bounds.exactCenterY());
        RectF arcBounds = mTempBounds;
        arcBounds.set(bounds);
        arcBounds.inset(mStrokeInset, mStrokeInset);

        for (int i = 0; i < mColors.length; i++) {
            mPaint.setStrokeWidth(getStrokeWidth() / (i + 1));
            mPaint.setColor(mColors[i]);
            canvas.drawArc(createArcBounds(arcBounds, i), mStartDegrees + DEGREE_180 * (i % 2),
                    mSwipeDegrees, false, mPaint);
        }

        canvas.restoreToCount(saveCount);
    }

    private RectF createArcBounds(RectF sourceArcBounds, int index) {
        RectF arcBounds = new RectF();
        int intervalWidth = 0;

        for (int i = 0; i < index; i++) {
            intervalWidth += getStrokeWidth() / (i + 1.0f) * 1.5f;
        }

        int arcBoundsLeft = (int) (sourceArcBounds.left + intervalWidth);
        int arcBoundsTop = (int) (sourceArcBounds.top + intervalWidth);
        int arcBoundsRight = (int) (sourceArcBounds.right - intervalWidth);
        int arcBoundsBottom = (int) (sourceArcBounds.bottom - intervalWidth);
        arcBounds.set(arcBoundsLeft, arcBoundsTop, arcBoundsRight, arcBoundsBottom);

        return arcBounds;
    }

    @Override
    public void computeRender(float renderProgress) {
        // Moving the start trim only occurs in the first 50% of a
        // single ring animation
        if (renderProgress <= START_TRIM_DURATION_OFFSET) {
            float startTrimProgress = (renderProgress) / (1.0f - START_TRIM_DURATION_OFFSET);
            mStartDegrees = mOriginStartDegrees + MAX_SWIPE_DEGREES * MATERIAL_INTERPOLATOR.getInterpolation(startTrimProgress);
        }

        // Moving the end trim starts after 50% of a single ring
        // animation completes
        if (renderProgress > START_TRIM_DURATION_OFFSET) {
            float endTrimProgress = (renderProgress - START_TRIM_DURATION_OFFSET) / (END_TRIM_DURATION_OFFSET - START_TRIM_DURATION_OFFSET);
            mEndDegrees = mOriginEndDegrees + MAX_SWIPE_DEGREES * MATERIAL_INTERPOLATOR.getInterpolation(endTrimProgress);
        }

        if (Math.abs(mEndDegrees - mStartDegrees) > MIN_SWIPE_DEGREE) {
            mSwipeDegrees = mEndDegrees - mStartDegrees;
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

    public void setColors(@NonNull int[] colors) {
        mColors = colors;
    }

    @Override
    public void setStrokeWidth(float strokeWidth) {
        super.setStrokeWidth(strokeWidth);
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

        mSwipeDegrees = MIN_SWIPE_DEGREE;
    }
}
