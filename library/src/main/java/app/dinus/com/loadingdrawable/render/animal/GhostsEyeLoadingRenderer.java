package app.dinus.com.loadingdrawable.render.animal;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.view.animation.Interpolator;

import app.dinus.com.loadingdrawable.DensityUtil;
import app.dinus.com.loadingdrawable.render.LoadingRenderer;

public class GhostsEyeLoadingRenderer extends LoadingRenderer {
    private Interpolator EYE_BALL_INTERPOLATOR = new EyeBallInterpolator();
    private Interpolator EYE_CIRCLE_INTERPOLATOR = new EyeCircleInterpolator();

    private static final float DEFAULT_WIDTH = 200.0f;
    private static final float DEFAULT_HEIGHT = 176.0f;
    private static final float DEFAULT_EYE_EDGE_WIDTH = 5.0f;

    private static final float DEFAULT_EYE_BALL_HEIGHT = 9.0f;
    private static final float DEFAULT_EYE_BALL_WIDTH = 11.0f;

    private static final float DEFAULT_EYE_CIRCLE_INTERVAL = 8.0f;
    private static final float DEFAULT_EYE_BALL_OFFSET_Y = 2.0f;
    private static final float DEFAULT_ABOVE_RADIAN_EYE_CIRCLE_OFFSET = 6.0f;
    private static final float DEFAULT_EYE_CIRCLE_RADIUS = 21.0f;
    private static final float DEFAULT_MAX_EYE_JUMP_DISTANCE = 11.0f;

    private static final float LEFT_EYE_CIRCLE$BALL_START_JUMP_UP_OFFSET = 0.0f;
    private static final float RIGHT_EYE_CIRCLE$BALL_START_JUMP_UP_OFFSET = 0.067f;

    private static final float LEFT_EYE_BALL_END_JUMP_OFFSET = 0.4f;
    private static final float LEFT_EYE_CIRCLE_END_JUMP_OFFSET = 0.533f;
    private static final float RIGHT_EYE_BALL_END_JUMP_OFFSET = 0.467f;
    private static final float RIGHT_EYE_CIRCLE_END_JUMP_OFFSET = 0.60f;

    private static final int DEGREE_180 = 180;

    private static final long ANIMATION_DURATION = 2333;

    private static final int DEFAULT_COLOR = Color.parseColor("#ff484852");

    private final Paint mPaint = new Paint();
    private final RectF mTempBounds = new RectF();

    private float mEyeInterval;
    private float mEyeCircleRadius;
    private float mMaxEyeJumptDistance;
    private float mAboveRadianEyeOffsetX;
    private float mEyeBallOffsetY;

    private float mEyeEdgeWidth;
    private float mEyeBallWidth;
    private float mEyeBallHeight;

    private float mLeftEyeCircleOffsetY;
    private float mRightEyeCircleOffsetY;
    private float mLeftEyeBallOffsetY;
    private float mRightEyeBallOffsetY;

    private int mColor;

    private GhostsEyeLoadingRenderer(Context context) {
        super(context);
        init(context);
        setupPaint();
    }

    private void init(Context context) {
        mWidth = DensityUtil.dip2px(context, DEFAULT_WIDTH);
        mHeight = DensityUtil.dip2px(context, DEFAULT_HEIGHT);
        mEyeEdgeWidth = DensityUtil.dip2px(context, DEFAULT_EYE_EDGE_WIDTH);

        mEyeInterval = DensityUtil.dip2px(context, DEFAULT_EYE_CIRCLE_INTERVAL);
        mEyeBallOffsetY = DensityUtil.dip2px(context, DEFAULT_EYE_BALL_OFFSET_Y);
        mEyeCircleRadius = DensityUtil.dip2px(context, DEFAULT_EYE_CIRCLE_RADIUS);
        mMaxEyeJumptDistance = DensityUtil.dip2px(context, DEFAULT_MAX_EYE_JUMP_DISTANCE);
        mAboveRadianEyeOffsetX = DensityUtil.dip2px(context, DEFAULT_ABOVE_RADIAN_EYE_CIRCLE_OFFSET);

        mEyeBallWidth = DensityUtil.dip2px(context, DEFAULT_EYE_BALL_WIDTH);
        mEyeBallHeight = DensityUtil.dip2px(context, DEFAULT_EYE_BALL_HEIGHT);

        mColor = DEFAULT_COLOR;

        mDuration = ANIMATION_DURATION;
    }

    private void setupPaint() {
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(mEyeEdgeWidth);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void draw(Canvas canvas, Rect bounds) {
        int saveCount = canvas.save();
        RectF arcBounds = mTempBounds;
        arcBounds.set(bounds);

        mPaint.setColor(mColor);

        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(createLeftEyeCircle(arcBounds, mLeftEyeCircleOffsetY), mPaint);
        canvas.drawPath(createRightEyeCircle(arcBounds, mRightEyeCircleOffsetY), mPaint);

        mPaint.setStyle(Paint.Style.FILL);
        //create left eye ball
        canvas.drawOval(createLeftEyeBall(arcBounds, mLeftEyeBallOffsetY), mPaint);
        //create right eye ball
        canvas.drawOval(createRightEyeBall(arcBounds, mRightEyeBallOffsetY), mPaint);

        canvas.restoreToCount(saveCount);
    }

    @Override
    protected void computeRender(float renderProgress) {
        if (renderProgress <= LEFT_EYE_BALL_END_JUMP_OFFSET && renderProgress >= LEFT_EYE_CIRCLE$BALL_START_JUMP_UP_OFFSET) {
            float eyeCircle$BallJumpUpProgress = (renderProgress - LEFT_EYE_CIRCLE$BALL_START_JUMP_UP_OFFSET) / (LEFT_EYE_BALL_END_JUMP_OFFSET - LEFT_EYE_CIRCLE$BALL_START_JUMP_UP_OFFSET);
            mLeftEyeBallOffsetY = -mMaxEyeJumptDistance * EYE_BALL_INTERPOLATOR.getInterpolation(eyeCircle$BallJumpUpProgress);
        }

        if (renderProgress <= LEFT_EYE_CIRCLE_END_JUMP_OFFSET && renderProgress >= LEFT_EYE_CIRCLE$BALL_START_JUMP_UP_OFFSET) {
            float eyeCircle$BallJumpUpProgress = (renderProgress - LEFT_EYE_CIRCLE$BALL_START_JUMP_UP_OFFSET) / (LEFT_EYE_CIRCLE_END_JUMP_OFFSET - LEFT_EYE_CIRCLE$BALL_START_JUMP_UP_OFFSET);
            mLeftEyeCircleOffsetY = -mMaxEyeJumptDistance * EYE_CIRCLE_INTERPOLATOR.getInterpolation(eyeCircle$BallJumpUpProgress);
        }

        if (renderProgress <= RIGHT_EYE_BALL_END_JUMP_OFFSET && renderProgress >= RIGHT_EYE_CIRCLE$BALL_START_JUMP_UP_OFFSET) {
            float eyeCircle$BallJumpUpProgress = (renderProgress - RIGHT_EYE_CIRCLE$BALL_START_JUMP_UP_OFFSET) / (RIGHT_EYE_BALL_END_JUMP_OFFSET - RIGHT_EYE_CIRCLE$BALL_START_JUMP_UP_OFFSET);
            mRightEyeBallOffsetY = -mMaxEyeJumptDistance * EYE_BALL_INTERPOLATOR.getInterpolation(eyeCircle$BallJumpUpProgress);
        }

        if (renderProgress <= RIGHT_EYE_CIRCLE_END_JUMP_OFFSET && renderProgress >= RIGHT_EYE_CIRCLE$BALL_START_JUMP_UP_OFFSET) {
            float eyeCircle$BallJumpUpProgress = (renderProgress - RIGHT_EYE_CIRCLE$BALL_START_JUMP_UP_OFFSET) / (RIGHT_EYE_CIRCLE_END_JUMP_OFFSET - RIGHT_EYE_CIRCLE$BALL_START_JUMP_UP_OFFSET);
            mRightEyeCircleOffsetY = -mMaxEyeJumptDistance * EYE_CIRCLE_INTERPOLATOR.getInterpolation(eyeCircle$BallJumpUpProgress);
        }
    }

    @Override
    protected void setAlpha(int alpha) {

    }

    @Override
    protected void setColorFilter(ColorFilter cf) {

    }

    @Override
    protected void reset() {
        mLeftEyeBallOffsetY = 0.0f;
        mRightEyeBallOffsetY = 0.0f;
        mLeftEyeCircleOffsetY = 0.0f;
        mRightEyeCircleOffsetY = 0.0f;
    }

    private RectF createLeftEyeBall(RectF arcBounds, float offsetY) {
        //the center of the left eye
        float leftEyeCenterX = arcBounds.centerX() - mEyeInterval / 2.0f - mEyeCircleRadius;
        float leftEyeCenterY = arcBounds.centerY() - mEyeBallOffsetY + offsetY;

        RectF rectF = new RectF(leftEyeCenterX - mEyeBallWidth / 2.0f, leftEyeCenterY - mEyeBallHeight / 2.0f,
                leftEyeCenterX + mEyeBallWidth / 2.0f, leftEyeCenterY + mEyeBallHeight / 2.0f);

        return rectF;
    }

    private RectF createRightEyeBall(RectF arcBounds, float offsetY) {
        //the center of the right eye
        float rightEyeCenterX = arcBounds.centerX() + mEyeInterval / 2.0f + mEyeCircleRadius;
        float rightEyeCenterY = arcBounds.centerY() - mEyeBallOffsetY + offsetY;

        RectF rectF = new RectF(rightEyeCenterX - mEyeBallWidth / 2.0f, rightEyeCenterY - mEyeBallHeight / 2.0f,
                rightEyeCenterX + mEyeBallWidth / 2.0f, rightEyeCenterY + mEyeBallHeight / 2.0f);

        return rectF;
    }


    private Path createLeftEyeCircle(RectF arcBounds, float offsetY) {
        Path path = new Path();

        //the center of the left eye
        float leftEyeCenterX = arcBounds.centerX() - mEyeInterval / 2.0f - mEyeCircleRadius;
        float leftEyeCenterY = arcBounds.centerY() + offsetY;
        //the bounds of left eye
        RectF leftEyeBounds = new RectF(leftEyeCenterX - mEyeCircleRadius, leftEyeCenterY - mEyeCircleRadius,
                leftEyeCenterX + mEyeCircleRadius, leftEyeCenterY + mEyeCircleRadius);
        path.addArc(leftEyeBounds, 0, DEGREE_180 + 15);
        //the above radian of of the eye
        path.quadTo(leftEyeBounds.left + mAboveRadianEyeOffsetX, leftEyeBounds.top + mEyeCircleRadius * 0.2f,
                leftEyeBounds.left + mAboveRadianEyeOffsetX / 4.0f, leftEyeBounds.top - mEyeCircleRadius * 0.15f);

        return path;
    }

    private Path createRightEyeCircle(RectF arcBounds, float offsetY) {
        Path path = new Path();

        //the center of the right eye
        float rightEyeCenterX = arcBounds.centerX() + mEyeInterval / 2.0f + mEyeCircleRadius;
        float rightEyeCenterY = arcBounds.centerY() + offsetY;
        //the bounds of left eye
        RectF leftEyeBounds = new RectF(rightEyeCenterX - mEyeCircleRadius, rightEyeCenterY - mEyeCircleRadius,
                rightEyeCenterX + mEyeCircleRadius, rightEyeCenterY + mEyeCircleRadius);
        path.addArc(leftEyeBounds, 180, -(DEGREE_180 + 15));
        //the above radian of of the eye
        path.quadTo(leftEyeBounds.right - mAboveRadianEyeOffsetX, leftEyeBounds.top + mEyeCircleRadius * 0.2f,
                leftEyeBounds.right - mAboveRadianEyeOffsetX / 4.0f, leftEyeBounds.top - mEyeCircleRadius * 0.15f);

        return path;
    }

    private class EyeCircleInterpolator implements Interpolator {

        @Override
        public float getInterpolation(float input) {
            if (input < 0.25f) {
                return input * 4.0f;
            } else if (input < 0.5f) {
                return 1.0f - (input - 0.25f) * 4.0f;
            } else if (input < 0.75f) {
                return (input - 0.5f) * 2.0f;
            } else {
                return 0.5f - (input - 0.75f) * 2.0f;
            }

        }
    }

    private class EyeBallInterpolator implements Interpolator {

        @Override
        public float getInterpolation(float input) {
            if (input < 0.333333f) {
                return input * 3.0f;
            } else {
                return 1.0f - (input - 0.333333f) * 1.5f;
            }
        }
    }

    public static class Builder {
        private Context mContext;

        public Builder(Context mContext) {
            this.mContext = mContext;
        }

        public GhostsEyeLoadingRenderer build() {
            GhostsEyeLoadingRenderer loadingRenderer = new GhostsEyeLoadingRenderer(mContext);
            return loadingRenderer;
        }
    }
}
