package app.dinus.com.loadingdrawable.render.goods;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.DisplayMetrics;
import android.view.animation.Interpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import app.dinus.com.loadingdrawable.DensityUtil;
import app.dinus.com.loadingdrawable.render.LoadingRenderer;

public class WaterBottleLoadingRenderer extends LoadingRenderer {
    private static final Interpolator MATERIAL_INTERPOLATOR = new FastOutSlowInInterpolator();

    private static final float DEFAULT_WIDTH = 200.0f;
    private static final float DEFAULT_HEIGHT = 150.0f;
    private static final float DEFAULT_STROKE_WIDTH = 1.5f;
    private static final float DEFAULT_BOTTLE_WIDTH = 30;
    private static final float DEFAULT_BOTTLE_HEIGHT = 43;
    private static final float WATER_LOWEST_POINT_TO_BOTTLENECK_DISTANCE = 30;

    private static final int DEFAULT_WAVE_COUNT = 5;
    private static final int DEFAULT_WATER_DROP_COUNT = 25;

    private static final int MAX_WATER_DROP_RADIUS = 5;
    private static final int MIN_WATER_DROP_RADIUS = 1;

    private static final int DEFAULT_BOTTLE_COLOR = Color.parseColor("#FFDAEBEB");
    private static final int DEFAULT_WATER_COLOR = Color.parseColor("#FF29E3F2");

    private static final float DEFAULT_TEXT_SIZE = 7.0f;

    private static final String LOADING_TEXT = "loading";

    private static final long ANIMATION_DURATION = 11111;

    private final Random mRandom = new Random();

    private final Paint mPaint = new Paint();
    private final RectF mCurrentBounds = new RectF();
    private final RectF mBottleBounds = new RectF();
    private final RectF mWaterBounds = new RectF();
    private final Rect mLoadingBounds = new Rect();
    private final List<WaterDropHolder> mWaterDropHolders = new ArrayList<>();

    private float mTextSize;
    private float mProgress;

    private float mBottleWidth;
    private float mBottleHeight;
    private float mStrokeWidth;
    private float mWaterLowestPointToBottleneckDistance;

    private int mBottleColor;
    private int mWaterColor;

    private int mWaveCount;

    private WaterBottleLoadingRenderer(Context context) {
        super(context);
        init(context);
        setupPaint();
    }

    private void init(Context context) {
        mTextSize = DensityUtil.dip2px(context, DEFAULT_TEXT_SIZE);

        mWidth = DensityUtil.dip2px(context, DEFAULT_WIDTH);
        mHeight = DensityUtil.dip2px(context, DEFAULT_HEIGHT);
        mStrokeWidth = DensityUtil.dip2px(context, DEFAULT_STROKE_WIDTH);

        mBottleWidth = DensityUtil.dip2px(context, DEFAULT_BOTTLE_WIDTH);
        mBottleHeight = DensityUtil.dip2px(context, DEFAULT_BOTTLE_HEIGHT);
        mWaterLowestPointToBottleneckDistance = DensityUtil.dip2px(context, WATER_LOWEST_POINT_TO_BOTTLENECK_DISTANCE);

        mBottleColor = DEFAULT_BOTTLE_COLOR;
        mWaterColor = DEFAULT_WATER_COLOR;

        mWaveCount = DEFAULT_WAVE_COUNT;

        mDuration = ANIMATION_DURATION;
    }

    private void setupPaint() {
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
    }

    @Override
    protected void draw(Canvas canvas, Rect bounds) {
        int saveCount = canvas.save();

        RectF arcBounds = mCurrentBounds;
        arcBounds.set(bounds);
        //draw bottle
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mBottleColor);
        canvas.drawPath(createBottlePath(mBottleBounds), mPaint);

        //draw water
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setColor(mWaterColor);
        canvas.drawPath(createWaterPath(mWaterBounds, mProgress), mPaint);

        //draw water drop
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mWaterColor);
        for (WaterDropHolder waterDropHolder : mWaterDropHolders) {
            if (waterDropHolder.mNeedDraw) {
                canvas.drawCircle(waterDropHolder.mInitX, waterDropHolder.mCurrentY, waterDropHolder.mRadius, mPaint);
            }
        }

        //draw loading text
        mPaint.setColor(mBottleColor);
        canvas.drawText(LOADING_TEXT, mBottleBounds.centerX() - mLoadingBounds.width() / 2.0f,
                mBottleBounds.bottom + mBottleBounds.height() * 0.2f, mPaint);
        canvas.restoreToCount(saveCount);
    }

    @Override
    protected void computeRender(float renderProgress) {
        if (mCurrentBounds.width() <= 0) {
            return;
        }

        RectF arcBounds = mCurrentBounds;
        //compute gas tube bounds
        mBottleBounds.set(arcBounds.centerX() - mBottleWidth / 2.0f, arcBounds.centerY() - mBottleHeight / 2.0f,
                arcBounds.centerX() + mBottleWidth / 2.0f, arcBounds.centerY() + mBottleHeight / 2.0f);
        //compute pipe body bounds
        mWaterBounds.set(mBottleBounds.left + mStrokeWidth * 1.5f, mBottleBounds.top + mWaterLowestPointToBottleneckDistance,
                mBottleBounds.right - mStrokeWidth * 1.5f, mBottleBounds.bottom - mStrokeWidth * 1.5f);

        //compute wave progress
        float totalWaveProgress = renderProgress * mWaveCount;
        float currentWaveProgress = totalWaveProgress - ((int) totalWaveProgress);

        if (currentWaveProgress > 0.5f) {
            mProgress = 1.0f - MATERIAL_INTERPOLATOR.getInterpolation((currentWaveProgress - 0.5f) * 2.0f);
        } else {
            mProgress = MATERIAL_INTERPOLATOR.getInterpolation(currentWaveProgress * 2.0f);
        }

        //init water drop holders
        if (mWaterDropHolders.isEmpty()) {
            initWaterDropHolders(mBottleBounds, mWaterBounds);
        }

        //compute the location of these water drops
        for (WaterDropHolder waterDropHolder : mWaterDropHolders) {
            if (waterDropHolder.mDelayDuration < renderProgress
                    && waterDropHolder.mDelayDuration + waterDropHolder.mDuration > renderProgress) {
                float riseProgress = (renderProgress - waterDropHolder.mDelayDuration) / waterDropHolder.mDuration;
                riseProgress = riseProgress < 0.5f ? riseProgress * 2.0f : 1.0f - (riseProgress - 0.5f) * 2.0f;
                waterDropHolder.mCurrentY = waterDropHolder.mInitY -
                        MATERIAL_INTERPOLATOR.getInterpolation(riseProgress) * waterDropHolder.mRiseHeight;
                waterDropHolder.mNeedDraw = true;
            } else {
                waterDropHolder.mNeedDraw = false;
            }
        }

        //measure loading text
        mPaint.setTextSize(mTextSize);
        mPaint.getTextBounds(LOADING_TEXT, 0, LOADING_TEXT.length(), mLoadingBounds);
    }

    private Path createBottlePath(RectF bottleRect) {
        float bottleneckWidth = bottleRect.width() * 0.3f;
        float bottleneckHeight = bottleRect.height() * 0.415f;
        float bottleneckDecorationWidth = bottleneckWidth * 1.1f;
        float bottleneckDecorationHeight = bottleneckHeight * 0.167f;

        Path path = new Path();
        //draw the left side of the bottleneck decoration
        path.moveTo(bottleRect.centerX() - bottleneckDecorationWidth * 0.5f, bottleRect.top);
        path.quadTo(bottleRect.centerX() - bottleneckDecorationWidth * 0.5f - bottleneckWidth * 0.15f, bottleRect.top + bottleneckDecorationHeight * 0.5f,
                bottleRect.centerX() - bottleneckWidth * 0.5f, bottleRect.top + bottleneckDecorationHeight);
        path.lineTo(bottleRect.centerX() - bottleneckWidth * 0.5f, bottleRect.top + bottleneckHeight);

        //draw the left side of the bottle's body
        float radius = (bottleRect.width() - mStrokeWidth) / 2.0f;
        float centerY = bottleRect.bottom - 0.86f * radius;
        RectF bodyRect = new RectF(bottleRect.left, centerY - radius, bottleRect.right, centerY + radius);
        path.addArc(bodyRect, 255, -135);

        //draw the bottom of the bottle
        float bottleBottomWidth = bottleRect.width() / 2.0f;
        path.lineTo(bottleRect.centerX() - bottleBottomWidth / 2.0f, bottleRect.bottom);
        path.lineTo(bottleRect.centerX() + bottleBottomWidth / 2.0f, bottleRect.bottom);

        //draw the right side of the bottle's body
        path.addArc(bodyRect, 60, -135);

        //draw the right side of the bottleneck decoration
        path.lineTo(bottleRect.centerX() + bottleneckWidth * 0.5f, bottleRect.top + bottleneckDecorationHeight);
        path.quadTo(bottleRect.centerX() + bottleneckDecorationWidth * 0.5f + bottleneckWidth * 0.15f, bottleRect.top + bottleneckDecorationHeight * 0.5f,
                bottleRect.centerX() + bottleneckDecorationWidth * 0.5f, bottleRect.top);

        return path;
    }

    private Path createWaterPath(RectF waterRect, float progress) {
        Path path = new Path();

        path.moveTo(waterRect.left, waterRect.top);

        //Similar to the way draw the bottle's bottom sides
        float radius = (waterRect.width() - mStrokeWidth) / 2.0f;
        float centerY = waterRect.bottom - 0.86f * radius;
        float bottleBottomWidth = waterRect.width() / 2.0f;
        RectF bodyRect = new RectF(waterRect.left, centerY - radius, waterRect.right, centerY + radius);

        path.addArc(bodyRect, 187.5f, -67.5f);
        path.lineTo(waterRect.centerX() - bottleBottomWidth / 2.0f, waterRect.bottom);
        path.lineTo(waterRect.centerX() + bottleBottomWidth / 2.0f, waterRect.bottom);
        path.addArc(bodyRect, 60, -67.5f);

        //draw the water waves
        float cubicXChangeSize = waterRect.width() * 0.35f * progress;
        float cubicYChangeSize = waterRect.height() * 1.2f * progress;

        path.cubicTo(waterRect.left + waterRect.width() * 0.80f - cubicXChangeSize, waterRect.top - waterRect.height() * 1.2f + cubicYChangeSize,
                waterRect.left + waterRect.width() * 0.55f - cubicXChangeSize, waterRect.top - cubicYChangeSize,
                waterRect.left, waterRect.top - mStrokeWidth / 2.0f);

        path.lineTo(waterRect.left, waterRect.top);

        return path;
    }

    private void initWaterDropHolders(RectF bottleRect, RectF waterRect) {
        float bottleRadius = bottleRect.width() / 2.0f;
        float lowestWaterPointY = waterRect.top;
        float twoSidesInterval = 0.2f * bottleRect.width();
        float atLeastDelayDuration = 0.1f;

        float unitDuration = 0.1f;
        float delayDurationRange = 0.6f;
        int radiusRandomRange = MAX_WATER_DROP_RADIUS - MIN_WATER_DROP_RADIUS;
        float currentXRandomRange = bottleRect.width() * 0.6f;

        for (int i = 0; i < DEFAULT_WATER_DROP_COUNT; i++) {
            WaterDropHolder waterDropHolder = new WaterDropHolder();
            waterDropHolder.mRadius = MIN_WATER_DROP_RADIUS + mRandom.nextInt(radiusRandomRange);
            waterDropHolder.mInitX = bottleRect.left + twoSidesInterval + mRandom.nextFloat() * currentXRandomRange;
            waterDropHolder.mInitY = lowestWaterPointY + waterDropHolder.mRadius / 2.0f;
            waterDropHolder.mRiseHeight = getMaxRiseHeight(bottleRadius, waterDropHolder.mRadius, waterDropHolder.mInitX - bottleRect.left)
                    * (0.2f + 0.8f * mRandom.nextFloat());
            waterDropHolder.mDelayDuration = atLeastDelayDuration + mRandom.nextFloat() * delayDurationRange;
            waterDropHolder.mDuration = waterDropHolder.mRiseHeight / bottleRadius * unitDuration;

            mWaterDropHolders.add(waterDropHolder);
        }
    }

    private float getMaxRiseHeight(float bottleRadius, float waterDropRadius, float currentX) {
        float coordinateX = currentX - bottleRadius;
        float bottleneckRadius = bottleRadius * 0.3f;
        if (coordinateX - waterDropRadius > -bottleneckRadius
                && coordinateX + waterDropRadius < bottleneckRadius) {
            return bottleRadius * 2.0f;
        }

        return (float) (Math.sqrt(Math.pow(bottleRadius, 2.0f) - Math.pow(coordinateX, 2.0f)) - waterDropRadius);
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

    private class WaterDropHolder {
        public float mCurrentY;

        public float mInitX;
        public float mInitY;
        public float mDelayDuration;
        public float mRiseHeight;

        public float mRadius;
        public float mDuration;

        public boolean mNeedDraw;
    }

    public static class Builder {
        private Context mContext;

        public Builder(Context mContext) {
            this.mContext = mContext;
        }

        public WaterBottleLoadingRenderer build() {
            WaterBottleLoadingRenderer loadingRenderer = new WaterBottleLoadingRenderer(mContext);
            return loadingRenderer;
        }
    }
}