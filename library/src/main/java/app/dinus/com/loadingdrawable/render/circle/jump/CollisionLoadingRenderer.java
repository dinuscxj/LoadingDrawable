package app.dinus.com.loadingdrawable.render.circle.jump;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.Size;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import app.dinus.com.loadingdrawable.DensityUtil;
import app.dinus.com.loadingdrawable.render.LoadingRenderer;

public class CollisionLoadingRenderer extends LoadingRenderer {
    private static final Interpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

    private static final int MAX_ALPHA = 255;
    private static final int OVAL_ALPHA = 64;

    private static final int DEFAULT_BALL_COUNT = 7;

    private static final float DEFAULT_OVAL_HEIGHT = 1.5f;
    private static final float DEFAULT_BALL_RADIUS = 7.5f;
    private static final float DEFAULT_WIDTH = 15.0f * 11;
    private static final float DEFAULT_HEIGHT = 15.0f * 4;

    private static final float START_LEFT_DURATION_OFFSET = 0.25f;
    private static final float START_RIGHT_DURATION_OFFSET = 0.5f;
    private static final float END_RIGHT_DURATION_OFFSET = 0.75f;
    private static final float END_LEFT_DURATION_OFFSET = 1.0f;

    private static final int[] DEFAULT_COLORS = new int[]{
            Color.parseColor("#FF28435D"), Color.parseColor("#FFC32720")
    };

    private static final float[] DEFAULT_POSITIONS = new float[]{
            0.0f, 1.0f
    };

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF mOvalRect = new RectF();

    @Size(2)
    private int[] mColors;
    private float[] mPositions;

    private float mOvalVerticalRadius;

    private float mBallRadius;
    private float mBallCenterY;
    private float mBallSideOffsets;
    private float mBallMoveXOffsets;
    private float mBallQuadCoefficient;

    private float mLeftBallMoveXOffsets;
    private float mLeftBallMoveYOffsets;
    private float mRightBallMoveXOffsets;
    private float mRightBallMoveYOffsets;

    private float mLeftOvalShapeRate;
    private float mRightOvalShapeRate;

    private int mBallCount;

    private CollisionLoadingRenderer(Context context) {
        super(context);
        init(context);
        adjustParams();
        setupPaint();
    }

    private void init(Context context) {
        mBallRadius = DensityUtil.dip2px(context, DEFAULT_BALL_RADIUS);
        mWidth = DensityUtil.dip2px(context, DEFAULT_WIDTH);
        mHeight = DensityUtil.dip2px(context, DEFAULT_HEIGHT);
        mOvalVerticalRadius = DensityUtil.dip2px(context, DEFAULT_OVAL_HEIGHT);

        mColors = DEFAULT_COLORS;
        mPositions = DEFAULT_POSITIONS;

        mBallCount = DEFAULT_BALL_COUNT;

        //mBallMoveYOffsets = mBallQuadCoefficient * mBallMoveXOffsets ^ 2
        // ==> if mBallMoveYOffsets == mBallMoveXOffsets
        // ==> mBallQuadCoefficient = 1.0f / mBallMoveXOffsets;
        mBallMoveXOffsets = 1.5f * (2 * mBallRadius);
        mBallQuadCoefficient = 1.0f / mBallMoveXOffsets;
    }

    private void adjustParams() {
        mBallCenterY = mHeight / 2.0f;
        mBallSideOffsets = (mWidth - mBallRadius * 2.0f * (mBallCount - 2)) / 2;
    }

    private void setupPaint() {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setShader(new LinearGradient(mBallSideOffsets, 0, mWidth - mBallSideOffsets, 0,
                mColors, mPositions, Shader.TileMode.CLAMP));
    }

    @Override
    protected void draw(Canvas canvas) {
        int saveCount = canvas.save();

        for (int i = 1; i < mBallCount - 1; i++) {
            mPaint.setAlpha(MAX_ALPHA);
            canvas.drawCircle(mBallRadius * (i * 2 - 1) + mBallSideOffsets, mBallCenterY, mBallRadius, mPaint);

            mOvalRect.set(mBallRadius * (i * 2 - 2) + mBallSideOffsets, mHeight - mOvalVerticalRadius * 2,
                    mBallRadius * (i * 2) + mBallSideOffsets, mHeight);
            mPaint.setAlpha(OVAL_ALPHA);
            canvas.drawOval(mOvalRect, mPaint);
        }

        //draw the first ball
        mPaint.setAlpha(MAX_ALPHA);
        canvas.drawCircle(mBallSideOffsets - mBallRadius - mLeftBallMoveXOffsets,
                mBallCenterY - mLeftBallMoveYOffsets, mBallRadius, mPaint);

        mOvalRect.set(mBallSideOffsets - mBallRadius - mBallRadius * mLeftOvalShapeRate - mLeftBallMoveXOffsets,
                mHeight - mOvalVerticalRadius - mOvalVerticalRadius * mLeftOvalShapeRate,
                mBallSideOffsets - mBallRadius + mBallRadius * mLeftOvalShapeRate - mLeftBallMoveXOffsets,
                mHeight - mOvalVerticalRadius + mOvalVerticalRadius * mLeftOvalShapeRate);
        mPaint.setAlpha(OVAL_ALPHA);
        canvas.drawOval(mOvalRect, mPaint);

        //draw the last ball
        mPaint.setAlpha(MAX_ALPHA);
        canvas.drawCircle(mBallRadius * (mBallCount * 2 - 3) + mBallSideOffsets + mRightBallMoveXOffsets,
                mBallCenterY - mRightBallMoveYOffsets, mBallRadius, mPaint);

        mOvalRect.set(mBallRadius * (mBallCount * 2 - 3) - mBallRadius * mRightOvalShapeRate + mBallSideOffsets + mRightBallMoveXOffsets,
                mHeight - mOvalVerticalRadius - mOvalVerticalRadius * mRightOvalShapeRate,
                mBallRadius * (mBallCount * 2 - 3) + mBallRadius * mRightOvalShapeRate + mBallSideOffsets + mRightBallMoveXOffsets,
                mHeight - mOvalVerticalRadius + mOvalVerticalRadius * mRightOvalShapeRate);
        mPaint.setAlpha(OVAL_ALPHA);
        canvas.drawOval(mOvalRect, mPaint);

        canvas.restoreToCount(saveCount);
    }

    @Override
    protected void computeRender(float renderProgress) {
        // Moving the left ball to the left sides only occurs in the first 25% of a jump animation
        if (renderProgress <= START_LEFT_DURATION_OFFSET) {
            float startLeftOffsetProgress = renderProgress / START_LEFT_DURATION_OFFSET;
            computeLeftBallMoveOffsets(DECELERATE_INTERPOLATOR.getInterpolation(startLeftOffsetProgress));
            return;
        }

        // Moving the left ball to the origin location only occurs between 25% and 50% of a jump ring animation
        if (renderProgress <= START_RIGHT_DURATION_OFFSET) {
            float startRightOffsetProgress = (renderProgress - START_LEFT_DURATION_OFFSET) / (START_RIGHT_DURATION_OFFSET - START_LEFT_DURATION_OFFSET);
            computeLeftBallMoveOffsets(ACCELERATE_INTERPOLATOR.getInterpolation(1.0f - startRightOffsetProgress));
            return;
        }

        // Moving the right ball to the right sides only occurs between 50% and 75% of a jump animation
        if (renderProgress <= END_RIGHT_DURATION_OFFSET) {
            float endRightOffsetProgress = (renderProgress - START_RIGHT_DURATION_OFFSET) / (END_RIGHT_DURATION_OFFSET - START_RIGHT_DURATION_OFFSET);
            computeRightBallMoveOffsets(DECELERATE_INTERPOLATOR.getInterpolation(endRightOffsetProgress));
            return;
        }

        // Moving the right ball to the origin location only occurs after 75% of a jump animation
        if (renderProgress <= END_LEFT_DURATION_OFFSET) {
            float endRightOffsetProgress = (renderProgress - END_RIGHT_DURATION_OFFSET) / (END_LEFT_DURATION_OFFSET - END_RIGHT_DURATION_OFFSET);
            computeRightBallMoveOffsets(ACCELERATE_INTERPOLATOR.getInterpolation(1 - endRightOffsetProgress));
            return;
        }

    }

    private void computeLeftBallMoveOffsets(float progress) {
        mRightBallMoveXOffsets = 0.0f;
        mRightBallMoveYOffsets = 0.0f;

        mLeftOvalShapeRate = 1.0f - progress;
        mLeftBallMoveXOffsets = mBallMoveXOffsets * progress;
        mLeftBallMoveYOffsets = (float) (Math.pow(mLeftBallMoveXOffsets, 2) * mBallQuadCoefficient);
    }

    private void computeRightBallMoveOffsets(float progress) {
        mLeftBallMoveXOffsets = 0.0f;
        mLeftBallMoveYOffsets = 0.0f;

        mRightOvalShapeRate = 1.0f - progress;
        mRightBallMoveXOffsets = mBallMoveXOffsets * progress;
        mRightBallMoveYOffsets = (float) (Math.pow(mRightBallMoveXOffsets, 2) * mBallQuadCoefficient);
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

    private void apply(Builder builder) {
        this.mWidth = builder.mWidth > 0 ? builder.mWidth : this.mWidth;
        this.mHeight = builder.mHeight > 0 ? builder.mHeight : this.mHeight;

        this.mOvalVerticalRadius = builder.mOvalVerticalRadius > 0 ? builder.mOvalVerticalRadius : this.mOvalVerticalRadius;
        this.mBallRadius = builder.mBallRadius > 0 ? builder.mBallRadius : this.mBallRadius;
        this.mBallMoveXOffsets = builder.mBallMoveXOffsets > 0 ? builder.mBallMoveXOffsets : this.mBallMoveXOffsets;
        this.mBallQuadCoefficient = builder.mBallQuadCoefficient > 0 ? builder.mBallQuadCoefficient : this.mBallQuadCoefficient;
        this.mBallCount = builder.mBallCount > 0 ? builder.mBallCount : this.mBallCount;

        this.mDuration = builder.mDuration > 0 ? builder.mDuration : this.mDuration;

        this.mColors = builder.mColors != null ? builder.mColors : this.mColors;

        adjustParams();
        setupPaint();
    }

    public static class Builder {
        private Context mContext;

        private int mWidth;
        private int mHeight;

        private float mOvalVerticalRadius;

        private int mBallCount;
        private float mBallRadius;
        private float mBallMoveXOffsets;
        private float mBallQuadCoefficient;

        private int mDuration;

        @Size(2)
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

        public Builder setOvalVerticalRadius(int ovalVerticalRadius) {
            this.mOvalVerticalRadius = ovalVerticalRadius;
            return this;
        }

        public Builder setBallRadius(int ballRadius) {
            this.mBallRadius = ballRadius;
            return this;
        }

        public Builder setBallMoveXOffsets(int ballMoveXOffsets) {
            this.mBallMoveXOffsets = ballMoveXOffsets;
            return this;
        }

        public Builder setBallQuadCoefficient(int ballQuadCoefficient) {
            this.mBallQuadCoefficient = ballQuadCoefficient;
            return this;
        }

        public Builder setBallCount(int ballCount) {
            this.mBallCount = ballCount;
            return this;
        }

        public Builder setColors(@Size(2) int[] colors) {
            this.mColors = colors;
            return this;
        }

        public Builder setDuration(int duration) {
            this.mDuration = duration;
            return this;
        }

        public CollisionLoadingRenderer build() {
            CollisionLoadingRenderer loadingRenderer = new CollisionLoadingRenderer(mContext);
            loadingRenderer.apply(this);
            return loadingRenderer;
        }
    }
}
