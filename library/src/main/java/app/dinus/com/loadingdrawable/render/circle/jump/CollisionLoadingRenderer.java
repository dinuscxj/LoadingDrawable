package app.dinus.com.loadingdrawable.render.circle.jump;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import app.dinus.com.loadingdrawable.DensityUtil;
import app.dinus.com.loadingdrawable.render.LoadingRenderer;

public class CollisionLoadingRenderer extends LoadingRenderer {
    private static final Interpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

    private static final int CIRCLE_COUNT = 7;

    //the 2 * 2 is the left and right side offset
    private static final float DEFAULT_WIDTH = 15.0f * (CIRCLE_COUNT + 2 * 2);
    //the 2 * 2 is the top and bottom side offset
    private static final float DEFAULT_HEIGHT = 15.0f * (1 + 2 * 2);

    private static final float DURATION_OFFSET = 0.25f;
    private static final float START_LEFT_DURATION_OFFSET = 0.25f;
    private static final float START_RIGHT_DURATION_OFFSET = 0.5f;
    private static final float END_RIGHT_DURATION_OFFSET = 0.75f;
    private static final float END_LEFT_DURATION_OFFSET = 1.0f;

    private static final float DEFAULT_STROKE_WIDTH = 2.5f;

    private static final int[] DEFAULT_COLORS = new int[]{
            Color.RED, Color.GREEN
    };

    private static final float[] DEFAULT_POSITIONS = new float[]{
            0.0f, 1.0f
    };

    private final Paint mPaint = new Paint();
    private final RectF mTempBounds = new RectF();

    private int[] mColors;
    private float[] mPositions;

    private float mEndXOffsetProgress;
    private float mStartXOffsetProgress;

    private float mStrokeWidth;

    private CollisionLoadingRenderer(Context context) {
        super(context);
        init(context);
        setupPaint();
    }

    private void init(Context context) {
        mWidth = DensityUtil.dip2px(context, DEFAULT_WIDTH);
        mHeight = DensityUtil.dip2px(context, DEFAULT_HEIGHT);
        mStrokeWidth = DensityUtil.dip2px(context, DEFAULT_STROKE_WIDTH);
    }

    private void setupPaint() {
        mColors = DEFAULT_COLORS;
        mPositions = DEFAULT_POSITIONS;

        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void draw(Canvas canvas, Rect bounds) {
        int saveCount = canvas.save();

        RectF arcBounds = mTempBounds;
        arcBounds.set(bounds);

        float cy = mHeight / 2;
        float circleRadius = computeCircleRadius(arcBounds);

        float sideOffset = 2.0f * (2 * circleRadius);
        float maxMoveOffset = 1.5f * (2 * circleRadius);

        LinearGradient gradient = new LinearGradient(arcBounds.left + sideOffset, 0, arcBounds.right - sideOffset, 0,
                mColors, mPositions, Shader.TileMode.CLAMP);
        mPaint.setShader(gradient);

        for (int i = 0; i < CIRCLE_COUNT; i++) {
            if (i == 0 && mStartXOffsetProgress != 0) {
                float xMoveOffset = maxMoveOffset * mStartXOffsetProgress;
                // y = ax^2 -->  if x = sideOffset, y = sideOffset ==> a = 1 / sideOffset
                float yMoveOffset = (float) (Math.pow(xMoveOffset, 2) / maxMoveOffset);
                canvas.drawCircle(circleRadius + sideOffset - xMoveOffset, cy - yMoveOffset, circleRadius, mPaint);
                continue;
            }

            if (i == CIRCLE_COUNT - 1 && mEndXOffsetProgress != 0) {
                float xMoveOffset = maxMoveOffset * mEndXOffsetProgress;
                // y = ax^2 -->  if x = sideOffset, y = sideOffset / 2 ==> a = 1 / sideOffset
                float yMoveOffset = (float) (Math.pow(xMoveOffset, 2) / maxMoveOffset);
                canvas.drawCircle(circleRadius * (CIRCLE_COUNT * 2 - 1) + sideOffset + xMoveOffset, cy - yMoveOffset, circleRadius, mPaint);
                continue;
            }

            canvas.drawCircle(circleRadius * (i * 2 + 1) + sideOffset, cy, circleRadius, mPaint);
        }

        canvas.restoreToCount(saveCount);
    }

    private float computeCircleRadius(RectF rectBounds) {
        float width = rectBounds.width();
        float height = rectBounds.height();

        //CIRCLE_COUNT + 4 is the sliding distance of both sides
        float radius = Math.min(width / (CIRCLE_COUNT + 4) / 2, height / 2);
        return radius;
    }

    @Override
    protected void computeRender(float renderProgress) {

        // Moving the start offset to left only occurs in the first 25% of a
        // single ring animation
        if (renderProgress <= START_LEFT_DURATION_OFFSET) {
            float startLeftOffsetProgress = renderProgress / DURATION_OFFSET;
            mStartXOffsetProgress = DECELERATE_INTERPOLATOR.getInterpolation(startLeftOffsetProgress);


            return;
        }

        // Moving the start offset to left only occurs between 25% and 50% of a
        // single ring animation
        if (renderProgress <= START_RIGHT_DURATION_OFFSET) {
            float startRightOffsetProgress = (renderProgress - START_LEFT_DURATION_OFFSET) / DURATION_OFFSET;
            mStartXOffsetProgress = ACCELERATE_INTERPOLATOR.getInterpolation(1.0f - startRightOffsetProgress);


            return;
        }

        // Moving the end offset to right starts between 50% and 75% a single ring
        // animation completes
        if (renderProgress <= END_RIGHT_DURATION_OFFSET) {
            float endRightOffsetProgress = (renderProgress - START_RIGHT_DURATION_OFFSET) / DURATION_OFFSET;
            mEndXOffsetProgress = DECELERATE_INTERPOLATOR.getInterpolation(endRightOffsetProgress);


            return;
        }

        // Moving the end offset to left starts after 75% of a single ring
        // animation completes
        if (renderProgress <= END_LEFT_DURATION_OFFSET) {
            float endRightOffsetProgress = (renderProgress - END_RIGHT_DURATION_OFFSET) / DURATION_OFFSET;
            mEndXOffsetProgress = ACCELERATE_INTERPOLATOR.getInterpolation(1 - endRightOffsetProgress);


            return;
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
    }

    public static class Builder {
        private Context mContext;

        public Builder(Context mContext) {
            this.mContext = mContext;
        }

        public CollisionLoadingRenderer build() {
            CollisionLoadingRenderer loadingRenderer = new CollisionLoadingRenderer(mContext);
            return loadingRenderer;
        }
    }
}
