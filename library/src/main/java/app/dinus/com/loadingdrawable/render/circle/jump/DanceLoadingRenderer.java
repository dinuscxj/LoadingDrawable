package app.dinus.com.loadingdrawable.render.circle.jump;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import app.dinus.com.loadingdrawable.DensityUtil;
import app.dinus.com.loadingdrawable.render.LoadingRenderer;

public class DanceLoadingRenderer extends LoadingRenderer {
    private static final Interpolator MATERIAL_INTERPOLATOR = new FastOutSlowInInterpolator();
    private static final Interpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

    private static final long ANIMATION_DURATION = 1888;

    private static final float DEFAULT_CENTER_RADIUS = 12.5f;
    private static final float DEFAULT_STROKE_WIDTH = 1.5f;
    private static final float DEFAULT_DANCE_BALL_RADIUS = 2.0f;

    private static final int NUM_POINTS = 3;
    private static final int DEGREE_360 = 360;
    private static final int RING_START_ANGLE = -90;
    private static final int DANCE_START_ANGLE = 0;
    private static final int DANCE_INTERVAL_ANGLE = 60;

    private static final int DEFAULT_COLOR = Color.WHITE;

    //the center coordinate of the oval
    private static final float[] POINT_X = new float[NUM_POINTS];
    private static final float[] POINT_Y = new float[NUM_POINTS];
    //1: the coordinate x from small to large; -1: the coordinate x from large to small
    private static final int[] DIRECTION = new int[]{1, 1, -1};

    private static final float BALL_FORWARD_START_ENTER_DURATION_OFFSET = 0f;
    private static final float BALL_FORWARD_END_ENTER_DURATION_OFFSET = 0.125f;

    private static final float RING_FORWARD_START_ROTATE_DURATION_OFFSET = 0.125f;
    private static final float RING_FORWARD_END_ROTATE_DURATION_OFFSET = 0.375f;

    private static final float CENTER_CIRCLE_FORWARD_START_SCALE_DURATION_OFFSET = 0.225f;
    private static final float CENTER_CIRCLE_FORWARD_END_SCALE_DURATION_OFFSET = 0.475f;

    private static final float BALL_FORWARD_START_EXIT_DURATION_OFFSET = 0.375f;
    private static final float BALL_FORWARD_END_EXIT_DURATION_OFFSET = 0.54f;

    private static final float RING_REVERSAL_START_ROTATE_DURATION_OFFSET = 0.5f;
    private static final float RING_REVERSAL_END_ROTATE_DURATION_OFFSET = 0.75f;

    private static final float BALL_REVERSAL_START_ENTER_DURATION_OFFSET = 0.6f;
    private static final float BALL_REVERSAL_END_ENTER_DURATION_OFFSET = 0.725f;

    private static final float CENTER_CIRCLE_REVERSAL_START_SCALE_DURATION_OFFSET = 0.675f;
    private static final float CENTER_CIRCLE_REVERSAL_END_SCALE_DURATION_OFFSET = 0.875f;

    private static final float BALL_REVERSAL_START_EXIT_DURATION_OFFSET = 0.875f;
    private static final float BALL_REVERSAL_END_EXIT_DURATION_OFFSET = 1.0f;

    private final Paint mPaint = new Paint();
    private final RectF mTempBounds = new RectF();
    private final RectF mCurrentBounds = new RectF();

    private float mScale;
    private float mRotation;
    private float mStrokeInset;

    private float mCenterRadius;
    private float mStrokeWidth;
    private float mDanceBallRadius;
    private float mShapeChangeWidth;
    private float mShapeChangeHeight;

    private int mColor;
    private int mArcColor;

    private DanceLoadingRenderer(Context context) {
        super(context);
        init(context);
        setupPaint();
    }

    private void init(Context context) {
        mStrokeWidth = DensityUtil.dip2px(context, DEFAULT_STROKE_WIDTH);
        mCenterRadius = DensityUtil.dip2px(context, DEFAULT_CENTER_RADIUS);
        mDanceBallRadius = DensityUtil.dip2px(context, DEFAULT_DANCE_BALL_RADIUS);

        setColor(DEFAULT_COLOR);
        setInsets((int) mWidth, (int) mHeight);
        mDuration = ANIMATION_DURATION;
    }

    private void setupPaint() {
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void draw(Canvas canvas, Rect bounds) {
        int saveCount = canvas.save();

        mTempBounds.set(bounds);
        mTempBounds.inset(mStrokeInset, mStrokeInset);
        mCurrentBounds.set(mTempBounds);

        float outerCircleRadius = Math.min(mTempBounds.height(), mTempBounds.width()) / 2.0f;
        float interCircleRadius = outerCircleRadius / 2.0f;
        float centerRingWidth = interCircleRadius - mStrokeWidth / 2;

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mColor);
        mPaint.setStrokeWidth(mStrokeWidth);
        canvas.drawCircle(mTempBounds.centerX(), mTempBounds.centerY(), outerCircleRadius, mPaint);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(mTempBounds.centerX(), mTempBounds.centerY(), interCircleRadius * mScale, mPaint);

        if (mRotation != 0) {
            mPaint.setColor(mArcColor);
            mPaint.setStyle(Paint.Style.STROKE);
            //strokeWidth / 2.0f + mStrokeWidth / 2.0f is the center of the inter circle width
            mTempBounds.inset(centerRingWidth / 2.0f + mStrokeWidth / 2.0f, centerRingWidth / 2.0f + mStrokeWidth / 2.0f);
            mPaint.setStrokeWidth(centerRingWidth);
            canvas.drawArc(mTempBounds, RING_START_ANGLE, mRotation, false, mPaint);
        }

        mPaint.setColor(mColor);
        mPaint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < NUM_POINTS; i++) {
            canvas.rotate(i * DANCE_INTERVAL_ANGLE, POINT_X[i], POINT_Y[i]);
            RectF rectF = new RectF(POINT_X[i] - mDanceBallRadius - mShapeChangeWidth / 2.0f,
                    POINT_Y[i] - mDanceBallRadius - mShapeChangeHeight / 2.0f,
                    POINT_X[i] + mDanceBallRadius + mShapeChangeWidth / 2.0f,
                    POINT_Y[i] + mDanceBallRadius + mShapeChangeHeight / 2.0f);
            canvas.drawOval(rectF, mPaint);
            canvas.rotate(-i * DANCE_INTERVAL_ANGLE, POINT_X[i], POINT_Y[i]);
        }

        canvas.restoreToCount(saveCount);
    }

    @Override
    protected void computeRender(float renderProgress) {
        float radius = Math.min(mCurrentBounds.height(), mCurrentBounds.width()) / 2.0f;
        //the origin coordinate is the centerLeft of the field mCurrentBounds
        float originCoordinateX = mCurrentBounds.left;
        float originCoordinateY = mCurrentBounds.top + radius;

        if (renderProgress <= BALL_FORWARD_END_ENTER_DURATION_OFFSET && renderProgress > BALL_FORWARD_START_ENTER_DURATION_OFFSET) {
            final float ballForwardEnterProgress = (renderProgress - BALL_FORWARD_START_ENTER_DURATION_OFFSET) / (BALL_FORWARD_END_ENTER_DURATION_OFFSET - BALL_FORWARD_START_ENTER_DURATION_OFFSET);

            mShapeChangeHeight = (0.5f - ballForwardEnterProgress) * mDanceBallRadius / 2.0f;
            mShapeChangeWidth = -mShapeChangeHeight;
            //y = k(x - r)--> k = tan(angle)
            //(x - r)^2 + y^2 = r^2
            // compute crossover point --> (k(x -r)) ^ 2 + (x - )^2 = r^2
            // so x --> [r + r / sqrt(k ^ 2 + 1), r - r / sqrt(k ^ 2 + 1)]
            for (int i = 0; i < NUM_POINTS; i++) {
                float k = (float) Math.tan((DANCE_START_ANGLE + DANCE_INTERVAL_ANGLE * i) / 360.0f * (2.0f * Math.PI));
                // progress[-1, 1]
                float progress = (ACCELERATE_INTERPOLATOR.getInterpolation(ballForwardEnterProgress) / 2.0f - 0.5f) * 2.0f * DIRECTION[i];
                POINT_X[i] = (float) (radius + progress * (radius / Math.sqrt(Math.pow(k, 2.0f) + 1.0f)));
                POINT_Y[i] = k * (POINT_X[i] - radius);

                POINT_X[i] += originCoordinateX;
                POINT_Y[i] += originCoordinateY;
            }
        }

        if (renderProgress <= RING_FORWARD_END_ROTATE_DURATION_OFFSET && renderProgress > RING_FORWARD_START_ROTATE_DURATION_OFFSET) {
            final float forwardRotateProgress = (renderProgress - RING_FORWARD_START_ROTATE_DURATION_OFFSET) / (RING_FORWARD_END_ROTATE_DURATION_OFFSET - RING_FORWARD_START_ROTATE_DURATION_OFFSET);
            mRotation = DEGREE_360 * MATERIAL_INTERPOLATOR.getInterpolation(forwardRotateProgress);
        }

        if (renderProgress <= CENTER_CIRCLE_FORWARD_END_SCALE_DURATION_OFFSET && renderProgress > CENTER_CIRCLE_FORWARD_START_SCALE_DURATION_OFFSET) {
            final float centerCircleScaleProgress = (renderProgress - CENTER_CIRCLE_FORWARD_START_SCALE_DURATION_OFFSET) / (CENTER_CIRCLE_FORWARD_END_SCALE_DURATION_OFFSET - CENTER_CIRCLE_FORWARD_START_SCALE_DURATION_OFFSET);

            if (centerCircleScaleProgress <= 0.5f) {
                mScale = 1.0f + DECELERATE_INTERPOLATOR.getInterpolation(centerCircleScaleProgress * 2.0f) * 0.2f;
            } else {
                mScale = 1.2f - ACCELERATE_INTERPOLATOR.getInterpolation((centerCircleScaleProgress - 0.5f) * 2.0f) * 0.2f;
            }

        }

        if (renderProgress <= BALL_FORWARD_END_EXIT_DURATION_OFFSET && renderProgress > BALL_FORWARD_START_EXIT_DURATION_OFFSET) {
            final float ballForwardExitProgress = (renderProgress - BALL_FORWARD_START_EXIT_DURATION_OFFSET) / (BALL_FORWARD_END_EXIT_DURATION_OFFSET - BALL_FORWARD_START_EXIT_DURATION_OFFSET);
            mShapeChangeHeight = (ballForwardExitProgress - 0.5f) * mDanceBallRadius / 2.0f;
            mShapeChangeWidth = -mShapeChangeHeight;
            for (int i = 0; i < NUM_POINTS; i++) {
                float k = (float) Math.tan((DANCE_START_ANGLE + DANCE_INTERVAL_ANGLE * i) / 360.0f * (2.0f * Math.PI));
                float progress = (DECELERATE_INTERPOLATOR.getInterpolation(ballForwardExitProgress) / 2.0f) * 2.0f * DIRECTION[i];
                POINT_X[i] = (float) (radius + progress * (radius / Math.sqrt(Math.pow(k, 2.0f) + 1.0f)));
                POINT_Y[i] = k * (POINT_X[i] - radius);

                POINT_X[i] += originCoordinateX;
                POINT_Y[i] += originCoordinateY;
            }
        }

        if (renderProgress <= RING_REVERSAL_END_ROTATE_DURATION_OFFSET && renderProgress > RING_REVERSAL_START_ROTATE_DURATION_OFFSET) {
            float scaledTime = (renderProgress - RING_REVERSAL_START_ROTATE_DURATION_OFFSET) / (RING_REVERSAL_END_ROTATE_DURATION_OFFSET - RING_REVERSAL_START_ROTATE_DURATION_OFFSET);
            mRotation = DEGREE_360 * MATERIAL_INTERPOLATOR.getInterpolation(scaledTime) - 360;
        } else if (renderProgress > RING_REVERSAL_END_ROTATE_DURATION_OFFSET) {
            mRotation = 0.0f;
        }

        if (renderProgress <= BALL_REVERSAL_END_ENTER_DURATION_OFFSET && renderProgress > BALL_REVERSAL_START_ENTER_DURATION_OFFSET) {
            final float ballReversalEnterProgress = (renderProgress - BALL_REVERSAL_START_ENTER_DURATION_OFFSET) / (BALL_REVERSAL_END_ENTER_DURATION_OFFSET - BALL_REVERSAL_START_ENTER_DURATION_OFFSET);
            mShapeChangeHeight = (0.5f - ballReversalEnterProgress) * mDanceBallRadius / 2.0f;
            mShapeChangeWidth = -mShapeChangeHeight;

            for (int i = 0; i < NUM_POINTS; i++) {
                float k = (float) Math.tan((DANCE_START_ANGLE + DANCE_INTERVAL_ANGLE * i) / 360.0f * (2.0f * Math.PI));
                float progress = (0.5f - ACCELERATE_INTERPOLATOR.getInterpolation(ballReversalEnterProgress) / 2.0f) * 2.0f * DIRECTION[i];
                POINT_X[i] = (float) (radius + progress * (radius / Math.sqrt(Math.pow(k, 2.0f) + 1.0f)));
                POINT_Y[i] = k * (POINT_X[i] - radius);

                POINT_X[i] += originCoordinateX;
                POINT_Y[i] += originCoordinateY;
            }
        }

        if (renderProgress <= CENTER_CIRCLE_REVERSAL_END_SCALE_DURATION_OFFSET && renderProgress > CENTER_CIRCLE_REVERSAL_START_SCALE_DURATION_OFFSET) {
            final float centerCircleScaleProgress = (renderProgress - CENTER_CIRCLE_REVERSAL_START_SCALE_DURATION_OFFSET) / (CENTER_CIRCLE_REVERSAL_END_SCALE_DURATION_OFFSET - CENTER_CIRCLE_REVERSAL_START_SCALE_DURATION_OFFSET);

            if (centerCircleScaleProgress <= 0.5f) {
                mScale = 1.0f + DECELERATE_INTERPOLATOR.getInterpolation(centerCircleScaleProgress * 2.0f) * 0.2f;
            } else {
                mScale = 1.2f - ACCELERATE_INTERPOLATOR.getInterpolation((centerCircleScaleProgress - 0.5f) * 2.0f) * 0.2f;
            }

        }

        if (renderProgress <= BALL_REVERSAL_END_EXIT_DURATION_OFFSET && renderProgress > BALL_REVERSAL_START_EXIT_DURATION_OFFSET) {
            final float ballReversalExitProgress = (renderProgress - BALL_REVERSAL_START_EXIT_DURATION_OFFSET) / (BALL_REVERSAL_END_EXIT_DURATION_OFFSET - BALL_REVERSAL_START_EXIT_DURATION_OFFSET);
            mShapeChangeHeight = (ballReversalExitProgress - 0.5f) * mDanceBallRadius / 2.0f;
            mShapeChangeWidth = -mShapeChangeHeight;

            for (int i = 0; i < NUM_POINTS; i++) {
                float k = (float) Math.tan((DANCE_START_ANGLE + DANCE_INTERVAL_ANGLE * i) / 360.0f * (2.0f * Math.PI));
                float progress = (0.0f - DECELERATE_INTERPOLATOR.getInterpolation(ballReversalExitProgress) / 2.0f) * 2.0f * DIRECTION[i];
                POINT_X[i] = (float) (radius + progress * (radius / Math.sqrt(Math.pow(k, 2.0f) + 1.0f)));
                POINT_Y[i] = k * (POINT_X[i] - radius);

                POINT_X[i] += originCoordinateX;
                POINT_Y[i] += originCoordinateY;
            }
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
        mScale = 1.0f;
        mRotation = 0;
    }

    private void setColor(int color) {
        mColor = color;
        mArcColor = halfAlphaColor(mColor);
    }

    private void setRotation(float rotation) {
        mRotation = rotation;

    }

    private void setDanceBallRadius(float danceBallRadius) {
        this.mDanceBallRadius = danceBallRadius;

    }

    private float getDanceBallRadius() {
        return mDanceBallRadius;
    }

    private float getRotation() {
        return mRotation;
    }

    private void setInsets(int width, int height) {
        final float minEdge = (float) Math.min(width, height);
        float insets;
        if (mCenterRadius <= 0 || minEdge < 0) {
            insets = (float) Math.ceil(mStrokeWidth / 2.0f);
        } else {
            insets = minEdge / 2.0f - mCenterRadius;
        }
        mStrokeInset = insets;
    }

    private int halfAlphaColor(int colorValue) {
        int startA = (colorValue >> 24) & 0xff;
        int startR = (colorValue >> 16) & 0xff;
        int startG = (colorValue >> 8) & 0xff;
        int startB = colorValue & 0xff;

        return ((startA / 2) << 24)
                | (startR << 16)
                | (startG << 8)
                | startB;
    }

    public static class Builder {
        private Context mContext;

        public Builder(Context mContext) {
            this.mContext = mContext;
        }

        public DanceLoadingRenderer build() {
            DanceLoadingRenderer loadingRenderer = new DanceLoadingRenderer(mContext);
            return loadingRenderer;
        }
    }
}
