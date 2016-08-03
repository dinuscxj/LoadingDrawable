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
import android.view.animation.Interpolator;

import app.dinus.com.loadingdrawable.DensityUtil;
import app.dinus.com.loadingdrawable.render.LoadingRenderer;

public class SwapLoadingRenderer extends LoadingRenderer {
    private static final Interpolator MATERIAL_INTERPOLATOR = new FastOutSlowInInterpolator();

    private static final long ANIMATION_DURATION = 2500;

    private static final int CIRCLE_COUNT = 5;

    //(CIRCLE_COUNT - 1) / 2 is the Circle interval width; the 2 * 2 is the both side inset
    private static final float DEFAULT_WIDTH = 15.0f * (CIRCLE_COUNT + (CIRCLE_COUNT - 1) / 2 + 2 * 2);
    //the 2 * 2 is the both side inset
    private static final float DEFAULT_HEIGHT = 15.0f * (1 + 2 * 2);
    private static final float DEFAULT_STROKE_WIDTH = 1.5f;

    private static final int DEFAULT_COLOR = Color.WHITE;

    private final Paint mPaint = new Paint();
    private final RectF mTempBounds = new RectF();

    private int mColor;

    private int mSwapIndex;

    private float mSwapThreshold;
    private float mSwapXOffsetProgress;

    private float mStrokeWidth;

    private SwapLoadingRenderer(Context context) {
        super(context);

        mDuration = ANIMATION_DURATION;
        init(context);
        setupPaint();
    }

    private void init(Context context) {
        mWidth = DensityUtil.dip2px(context, DEFAULT_WIDTH);
        mHeight = DensityUtil.dip2px(context, DEFAULT_HEIGHT);
        mStrokeWidth = DensityUtil.dip2px(context, DEFAULT_STROKE_WIDTH);

        mSwapThreshold = 1.0f / CIRCLE_COUNT;
    }

    private void setupPaint() {
        mColor = DEFAULT_COLOR;

        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void draw(Canvas canvas, Rect bounds) {
        mPaint.setColor(mColor);

        int saveCount = canvas.save();

        RectF arcBounds = mTempBounds;
        arcBounds.set(bounds);

        float cy = mHeight / 2;
        float circleRadius = computeCircleRadius(arcBounds);

        float sideOffset = 2.0f * (2 * circleRadius);
        float intervalWidth = circleRadius;

        float circleDiameter = mSwapIndex == CIRCLE_COUNT - 1
                ? circleRadius * (CIRCLE_COUNT - 1) * 3
                : circleRadius * 3;

        //x^2 + y^2 = (3 * circleRadius / 2) ^ 2
        float xMoveOffset = mSwapIndex == CIRCLE_COUNT - 1
                ? -mSwapXOffsetProgress * circleDiameter
                : mSwapXOffsetProgress * circleDiameter;
        //the y axial symmetry
        float xCoordinate = mSwapIndex == CIRCLE_COUNT - 1
                ? xMoveOffset + circleDiameter / 2
                : xMoveOffset - circleDiameter / 2;
        float yMoveOffset = (float) (mSwapIndex % 2 == 0 && mSwapIndex != CIRCLE_COUNT - 1
                ? Math.sqrt(Math.pow(circleDiameter / 2, 2.0f) - Math.pow(xCoordinate, 2.0f))
                : -Math.sqrt(Math.pow(circleDiameter / 2, 2.0f) - Math.pow(xCoordinate, 2.0f)));

        for (int i = 0; i < CIRCLE_COUNT; i++) {
            if (i == mSwapIndex) {
                mPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(circleRadius * (i * 2 + 1) + sideOffset + i * intervalWidth + xMoveOffset
                        , cy - yMoveOffset, circleRadius - mStrokeWidth / 2, mPaint);
            } else if (i == (mSwapIndex + 1) % CIRCLE_COUNT) {
                mPaint.setStyle(Paint.Style.STROKE);

                canvas.drawCircle(circleRadius * (i * 2 + 1) + sideOffset + i * intervalWidth - xMoveOffset
                        , cy + yMoveOffset, circleRadius - mStrokeWidth / 2, mPaint);
            } else {
                mPaint.setStyle(Paint.Style.STROKE);

                canvas.drawCircle(circleRadius * (i * 2 + 1) + sideOffset + i * intervalWidth, cy,
                        circleRadius - mStrokeWidth / 2, mPaint);
            }

        }

        canvas.restoreToCount(saveCount);
    }

    private float computeCircleRadius(RectF rectBounds) {
        float width = rectBounds.width();
        float height = rectBounds.height();

        //CIRCLE_COUNT + 4 is the sliding distance of both sides
        float radius = Math.min(width / (CIRCLE_COUNT + (CIRCLE_COUNT - 1) / 2 + 2 * 2) / 2, height / 2);
        return radius;
    }

    @Override
    protected void computeRender(float renderProgress) {
        mSwapIndex = (int) (renderProgress / mSwapThreshold);
        mSwapXOffsetProgress = MATERIAL_INTERPOLATOR.getInterpolation(
                (renderProgress - mSwapIndex * mSwapThreshold) / mSwapThreshold);


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

    public static class Builder {
        private Context mContext;

        public Builder(Context mContext) {
            this.mContext = mContext;
        }

        public SwapLoadingRenderer build() {
            SwapLoadingRenderer loadingRenderer = new SwapLoadingRenderer(mContext);
            return loadingRenderer;
        }
    }
}
