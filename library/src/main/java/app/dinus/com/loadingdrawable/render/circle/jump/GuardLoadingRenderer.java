package app.dinus.com.loadingdrawable.render.circle.jump;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.DisplayMetrics;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import app.dinus.com.loadingdrawable.DensityUtil;
import app.dinus.com.loadingdrawable.render.LoadingRenderer;

public class GuardLoadingRenderer extends LoadingRenderer {
    private static final Interpolator MATERIAL_INTERPOLATOR = new FastOutSlowInInterpolator();
    private static final Interpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

    private static final long ANIMATION_DURATION = 5000;

    private static final float DEFAULT_STROKE_WIDTH = 1.0f;
    private static final float DEFAULT_CENTER_RADIUS = 12.5f;
    private static final float DEFAULT_SKIP_BALL_RADIUS = 1.0f;

    private static final float START_TRIM_INIT_ROTATION = -0.5f;
    private static final float START_TRIM_MAX_ROTATION = -0.25f;
    private static final float END_TRIM_INIT_ROTATION = 0.25f;
    private static final float END_TRIM_MAX_ROTATION = 0.75f;

    private static final float START_TRIM_DURATION_OFFSET = 0.23f;
    private static final float WAVE_DURATION_OFFSET = 0.36f;
    private static final float BALL_SKIP_DURATION_OFFSET = 0.74f;
    private static final float BALL_SCALE_DURATION_OFFSET = 0.82f;
    private static final float END_TRIM_DURATION_OFFSET = 1.0f;

    private static final int DEFAULT_COLOR = Color.WHITE;
    private static final int DEFAULT_BALL_COLOR = Color.RED;

    private final Paint mPaint = new Paint();
    private final RectF mTempBounds = new RectF();
    private final RectF mCurrentBounds = new RectF();
    private final float[] mCurrentPosition = new float[2];

    private float mStrokeInset;
    private float mSkipBallSize;

    private float mScale;
    private float mEndTrim;
    private float mRotation;
    private float mStartTrim;
    private float mWaveProgress;

    private float mStrokeWidth;
    private float mCenterRadius;

    private int mColor;
    private int mBallColor;

    private PathMeasure mPathMeasure;

    private GuardLoadingRenderer(Context context) {
        super(context);

        mDuration = ANIMATION_DURATION;
        init(context);
        setupPaint();
    }

    private void init(Context context) {
        mStrokeWidth = DensityUtil.dip2px(context, DEFAULT_STROKE_WIDTH);
        mCenterRadius = DensityUtil.dip2px(context, DEFAULT_CENTER_RADIUS);
        mSkipBallSize = DensityUtil.dip2px(context, DEFAULT_SKIP_BALL_RADIUS);

        mColor = DEFAULT_COLOR;
        mBallColor = DEFAULT_BALL_COLOR;
    }

    private void setupPaint() {
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        setInsets((int) mWidth, (int) mHeight);
    }

    @Override
    protected void draw(Canvas canvas, Rect bounds) {
        RectF arcBounds = mTempBounds;
        arcBounds.set(bounds);
        arcBounds.inset(mStrokeInset, mStrokeInset);
        mCurrentBounds.set(arcBounds);

        int saveCount = canvas.save();

        //draw circle trim
        float startAngle = (mStartTrim + mRotation) * 360;
        float endAngle = (mEndTrim + mRotation) * 360;
        float sweepAngle = endAngle - startAngle;
        if (sweepAngle != 0) {
            mPaint.setColor(mColor);
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawArc(arcBounds, startAngle, sweepAngle, false, mPaint);
        }

        //draw water wave
        if (mWaveProgress < 1.0f) {
            mPaint.setColor(Color.argb((int) (Color.alpha(mColor) * (1.0f - mWaveProgress)),
                    Color.red(mColor), Color.green(mColor), Color.blue(mColor)));
            mPaint.setStyle(Paint.Style.STROKE);
            float radius = Math.min(arcBounds.width(), arcBounds.height()) / 2.0f;
            canvas.drawCircle(arcBounds.centerX(), arcBounds.centerY(), radius * (1.0f + mWaveProgress), mPaint);
        }
        //draw ball bounce
        if (mPathMeasure != null) {
            mPaint.setColor(mBallColor);
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(mCurrentPosition[0], mCurrentPosition[1], mSkipBallSize * mScale, mPaint);
        }

        canvas.restoreToCount(saveCount);
    }

    @Override
    protected void computeRender(float renderProgress) {
        if (renderProgress <= START_TRIM_DURATION_OFFSET) {
            final float startTrimProgress = (renderProgress) / START_TRIM_DURATION_OFFSET;
            mEndTrim = -MATERIAL_INTERPOLATOR.getInterpolation(startTrimProgress);
            mRotation = START_TRIM_INIT_ROTATION + START_TRIM_MAX_ROTATION
                    * MATERIAL_INTERPOLATOR.getInterpolation(startTrimProgress);
        }

        if (renderProgress <= WAVE_DURATION_OFFSET && renderProgress > START_TRIM_DURATION_OFFSET) {
            final float waveProgress = (renderProgress - START_TRIM_DURATION_OFFSET)
                    / (WAVE_DURATION_OFFSET - START_TRIM_DURATION_OFFSET);
            mWaveProgress = ACCELERATE_INTERPOLATOR.getInterpolation(waveProgress);
        }

        if (renderProgress <= BALL_SKIP_DURATION_OFFSET && renderProgress > WAVE_DURATION_OFFSET) {
            if (mPathMeasure == null) {
                mPathMeasure = new PathMeasure(createSkipBallPath(), false);
            }

            final float ballSkipProgress = (renderProgress - WAVE_DURATION_OFFSET)
                    / (BALL_SKIP_DURATION_OFFSET - WAVE_DURATION_OFFSET);
            mPathMeasure.getPosTan(ballSkipProgress * mPathMeasure.getLength(), mCurrentPosition, null);

            mWaveProgress = 1.0f;
        }

        if (renderProgress <= BALL_SCALE_DURATION_OFFSET && renderProgress > BALL_SKIP_DURATION_OFFSET) {
            final float ballScaleProgress =
                    (renderProgress - BALL_SKIP_DURATION_OFFSET)
                            / (BALL_SCALE_DURATION_OFFSET - BALL_SKIP_DURATION_OFFSET);
            if (ballScaleProgress < 0.5f) {
                mScale = 1.0f + DECELERATE_INTERPOLATOR.getInterpolation(ballScaleProgress * 2.0f);
            } else {
                mScale = 2.0f - ACCELERATE_INTERPOLATOR.getInterpolation((ballScaleProgress - 0.5f) * 2.0f) * 2.0f;
            }
        }

        if (renderProgress >= BALL_SCALE_DURATION_OFFSET) {
            final float endTrimProgress =
                    (renderProgress - BALL_SKIP_DURATION_OFFSET)
                            / (END_TRIM_DURATION_OFFSET - BALL_SKIP_DURATION_OFFSET);
            mEndTrim = -1 + MATERIAL_INTERPOLATOR.getInterpolation(endTrimProgress);
            mRotation = END_TRIM_INIT_ROTATION + END_TRIM_MAX_ROTATION
                    * MATERIAL_INTERPOLATOR.getInterpolation(endTrimProgress);

            mScale = 1.0f;
            mPathMeasure = null;
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
        mEndTrim = 0.0f;
        mRotation = 0.0f;
        mStartTrim = 0.0f;
        mWaveProgress = 1.0f;
    }

    private Path createSkipBallPath() {
        float radius = Math.min(mCurrentBounds.width(), mCurrentBounds.height()) / 2.0f;
        float radiusPow2 = (float) Math.pow(radius, 2.0f);
        float originCoordinateX = mCurrentBounds.centerX();
        float originCoordinateY = mCurrentBounds.centerY();

        float[] coordinateX = new float[]{0.0f, 0.0f, -0.8f * radius, 0.75f * radius,
                -0.45f * radius, 0.9f * radius, -0.5f * radius};
        float[] sign = new float[]{1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f};

        Path path = new Path();
        for (int i = 0; i < coordinateX.length; i++) {
            // x^2 + y^2 = radius^2 --> y = sqrt(radius^2 - x^2)
            if (i == 0) {
                path.moveTo(
                        originCoordinateX + coordinateX[i],
                        originCoordinateY + sign[i]
                                * (float) Math.sqrt(radiusPow2 - Math.pow(coordinateX[i], 2.0f)));
                continue;
            }

            path.lineTo(
                    originCoordinateX + coordinateX[i],
                    originCoordinateY + sign[i]
                            * (float) Math.sqrt(radiusPow2 - Math.pow(coordinateX[i], 2.0f)));

            if (i == coordinateX.length - 1) {
                path.lineTo(originCoordinateX, originCoordinateY);
            }
        }
        return path;
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

    public static class Builder {
        private Context mContext;

        public Builder(Context mContext) {
            this.mContext = mContext;
        }

        public GuardLoadingRenderer build() {
            GuardLoadingRenderer loadingRenderer = new GuardLoadingRenderer(mContext);
            return loadingRenderer;
        }
    }
}
