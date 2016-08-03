package app.dinus.com.loadingdrawable.render.scenery;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.DisplayMetrics;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import app.dinus.com.loadingdrawable.DensityUtil;
import app.dinus.com.loadingdrawable.render.LoadingRenderer;

public class DayNightLoadingRenderer extends LoadingRenderer {
    private static final Interpolator MATERIAL_INTERPOLATOR = new FastOutSlowInInterpolator();
    private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    private static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final Interpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final Interpolator FASTOUTLINEARIN_INTERPOLATOR = new FastOutLinearInInterpolator();

    private static final Interpolator[] INTERPOLATORS = new Interpolator[]{LINEAR_INTERPOLATOR,
            DECELERATE_INTERPOLATOR, ACCELERATE_INTERPOLATOR, FASTOUTLINEARIN_INTERPOLATOR, MATERIAL_INTERPOLATOR};

    private static final int MAX_ALPHA = 255;
    private static final int DEGREE_360 = 360;
    private static final int MAX_SUN_RAY_COUNT = 12;

    private static final float DEFAULT_WIDTH = 200.0f;
    private static final float DEFAULT_HEIGHT = 150.0f;
    private static final float DEFAULT_STROKE_WIDTH = 2.5f;
    private static final float DEFAULT_SUN$MOON_RADIUS = 12.0f;
    private static final float DEFAULT_STAR_RADIUS = 2.5f;
    private static final float DEFAULT_SUN_RAY_LENGTH = 10.0f;
    private static final float DEFAULT_SUN_RAY_OFFSET = 3.0f;

    public static final float STAR_RISE_PROGRESS_OFFSET = 0.2f;
    public static final float STAR_DECREASE_PROGRESS_OFFSET = 0.8f;
    public static final float STAR_FLASH_PROGRESS_PERCENTAGE = 0.2f;

    private static final float MAX_SUN_ROTATE_DEGREE = DEGREE_360 / 3.0f;
    private static final float MAX_MOON_ROTATE_DEGREE = DEGREE_360 / 6.0f;
    private static final float SUN_RAY_INTERVAL_DEGREE = DEGREE_360 / 3.0f / 55;

    private static final float SUN_RISE_DURATION_OFFSET = 0.143f;
    private static final float SUN_ROTATE_DURATION_OFFSET = 0.492f;
    private static final float SUN_DECREASE_DURATION_OFFSET = 0.570f;
    private static final float MOON_RISE_DURATION_OFFSET = 0.713f;
    private static final float MOON_DECREASE_START_DURATION_OFFSET = 0.935f;
    private static final float MOON_DECREASE_END_DURATION_OFFSET = 1.0f;
    private static final float STAR_RISE_START_DURATION_OFFSET = 0.684f;
    private static final float STAR_DECREASE_START_DURATION_OFFSET = 1.0f;

    private static final int DEFAULT_COLOR = Color.parseColor("#ff21fd8e");

    private static final long ANIMATION_DURATION = 5111;

    private final Random mRandom = new Random();
    private final List<StarHolder> mStarHolders = new ArrayList<>();

    private final Paint mPaint = new Paint();
    private final RectF mTempBounds = new RectF();

    private final Animator.AnimatorListener mAnimatorListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationRepeat(Animator animator) {
            super.onAnimationRepeat(animator);
        }
    };

    private int mCurrentColor;

    private float mMaxStarOffsets;

    private float mStrokeWidth;
    private float mStarRadius;
    private float mSun$MoonRadius;
    private float mSunCoordinateY;
    private float mMoonCoordinateY;
    //the y-coordinate of the end point of the sun ray
    private float mSunRayEndCoordinateY;
    //the y-coordinate of the start point of the sun ray
    private float mSunRayStartCoordinateY;
    //the y-coordinate of the start point of the sun
    private float mInitSun$MoonCoordinateY;
    //the distance from the outside to the center of the drawable
    private float mMaxSun$MoonRiseDistance;

    private float mSunRayRotation;
    private float mMoonRotation;

    //the number of sun's rays is increasing
    private boolean mIsExpandSunRay;
    private boolean mShowStar;

    private int mSunRayCount;

    private DayNightLoadingRenderer(Context context) {
        super(context);
        init(context);
        setupPaint();
        addRenderListener(mAnimatorListener);
    }

    private void init(Context context) {
        mWidth = DensityUtil.dip2px(context, DEFAULT_WIDTH);
        mHeight = DensityUtil.dip2px(context, DEFAULT_HEIGHT);
        mStrokeWidth = DensityUtil.dip2px(context, DEFAULT_STROKE_WIDTH);

        mStarRadius = DensityUtil.dip2px(context, DEFAULT_STAR_RADIUS);
        mSun$MoonRadius = DensityUtil.dip2px(context, DEFAULT_SUN$MOON_RADIUS);
        mInitSun$MoonCoordinateY = mHeight + mSun$MoonRadius + mStrokeWidth * 2.0f;
        mMaxSun$MoonRiseDistance = mHeight / 2.0f + mSun$MoonRadius;

        mSunRayStartCoordinateY = mInitSun$MoonCoordinateY - mMaxSun$MoonRiseDistance //the center
                - mSun$MoonRadius //sub the radius
                - mStrokeWidth // sub the with the sun circle
                - DensityUtil.dip2px(context, DEFAULT_SUN_RAY_OFFSET); //sub the interval between the sun and the sun ray

        //add strokeWidth * 2.0f because the stroke cap is Paint.Cap.ROUND
        mSunRayEndCoordinateY = mSunRayStartCoordinateY - DensityUtil.dip2px(context, DEFAULT_SUN_RAY_LENGTH)
                + mStrokeWidth;

        mSunCoordinateY = mInitSun$MoonCoordinateY;
        mMoonCoordinateY = mInitSun$MoonCoordinateY;

        mCurrentColor = DEFAULT_COLOR;

        mDuration = ANIMATION_DURATION;
    }

    private void setupPaint() {
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void draw(Canvas canvas, Rect bounds) {
        int saveCount = canvas.save();

        RectF arcBounds = mTempBounds;
        arcBounds.set(bounds);

        mPaint.setAlpha(MAX_ALPHA);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mCurrentColor);

        if (mSunCoordinateY < mInitSun$MoonCoordinateY) {
            canvas.drawCircle(arcBounds.centerX(), mSunCoordinateY, mSun$MoonRadius, mPaint);
        }

        if (mMoonCoordinateY < mInitSun$MoonCoordinateY) {
            int moonSaveCount = canvas.save();
            canvas.rotate(mMoonRotation, arcBounds.centerX(), mMoonCoordinateY);
            canvas.drawPath(createMoonPath(arcBounds.centerX(), mMoonCoordinateY), mPaint);
            canvas.restoreToCount(moonSaveCount);
        }

        for (int i = 0; i < mSunRayCount; i++) {
            int sunRaySaveCount = canvas.save();
            //rotate 45 degrees can change the direction of 0 degrees to 1:30 clock
            //-mSunRayRotation means reverse rotation
            canvas.rotate(45 - mSunRayRotation
                            + (mIsExpandSunRay ? i : MAX_SUN_RAY_COUNT - i) * DEGREE_360 / MAX_SUN_RAY_COUNT,
                    arcBounds.centerX(), mSunCoordinateY);

            canvas.drawLine(arcBounds.centerX(), mSunRayStartCoordinateY, arcBounds.centerX(), mSunRayEndCoordinateY, mPaint);
            canvas.restoreToCount(sunRaySaveCount);
        }

        if (mShowStar) {
            if (mStarHolders.isEmpty()) {
                initStarHolders(arcBounds);
            }

            for (int i = 0; i < mStarHolders.size(); i++) {
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setAlpha(mStarHolders.get(i).mAlpha);
                canvas.drawCircle(mStarHolders.get(i).mCurrentPoint.x, mStarHolders.get(i).mCurrentPoint.y, mStarRadius, mPaint);
            }
        }

        canvas.restoreToCount(saveCount);
    }

    @Override
    protected void computeRender(float renderProgress) {
        if (renderProgress <= SUN_RISE_DURATION_OFFSET) {
            float sunRiseProgress = renderProgress / SUN_RISE_DURATION_OFFSET;
            mSunCoordinateY = mInitSun$MoonCoordinateY - mMaxSun$MoonRiseDistance * MATERIAL_INTERPOLATOR.getInterpolation(sunRiseProgress);
            mMoonCoordinateY = mInitSun$MoonCoordinateY;
            mShowStar = false;
        }

        if (renderProgress <= SUN_ROTATE_DURATION_OFFSET && renderProgress > SUN_RISE_DURATION_OFFSET) {
            float sunRotateProgress = (renderProgress - SUN_RISE_DURATION_OFFSET) / (SUN_ROTATE_DURATION_OFFSET - SUN_RISE_DURATION_OFFSET);
            mSunRayRotation = sunRotateProgress * MAX_SUN_ROTATE_DEGREE;

            if ((int) (mSunRayRotation / SUN_RAY_INTERVAL_DEGREE) <= MAX_SUN_RAY_COUNT) {
                mIsExpandSunRay = true;
                mSunRayCount = (int) (mSunRayRotation / SUN_RAY_INTERVAL_DEGREE);
            }

            if ((int) ((MAX_SUN_ROTATE_DEGREE - mSunRayRotation) / SUN_RAY_INTERVAL_DEGREE) <= MAX_SUN_RAY_COUNT) {
                mIsExpandSunRay = false;
                mSunRayCount = (int) ((MAX_SUN_ROTATE_DEGREE - mSunRayRotation) / SUN_RAY_INTERVAL_DEGREE);
            }
        }

        if (renderProgress <= SUN_DECREASE_DURATION_OFFSET && renderProgress > SUN_ROTATE_DURATION_OFFSET) {
            float sunDecreaseProgress = (renderProgress - SUN_ROTATE_DURATION_OFFSET) / (SUN_DECREASE_DURATION_OFFSET - SUN_ROTATE_DURATION_OFFSET);
            mSunCoordinateY = mInitSun$MoonCoordinateY - mMaxSun$MoonRiseDistance * (1.0f - ACCELERATE_INTERPOLATOR.getInterpolation(sunDecreaseProgress));
        }

        if (renderProgress <= MOON_RISE_DURATION_OFFSET && renderProgress > SUN_DECREASE_DURATION_OFFSET) {
            float moonRiseProgress = (renderProgress - SUN_DECREASE_DURATION_OFFSET) / (MOON_RISE_DURATION_OFFSET - SUN_DECREASE_DURATION_OFFSET);
            mMoonRotation = MATERIAL_INTERPOLATOR.getInterpolation(moonRiseProgress) * MAX_MOON_ROTATE_DEGREE;
            mSunCoordinateY = mInitSun$MoonCoordinateY;
            mMoonCoordinateY = mInitSun$MoonCoordinateY - mMaxSun$MoonRiseDistance * MATERIAL_INTERPOLATOR.getInterpolation(moonRiseProgress);
        }

        if (renderProgress <= STAR_DECREASE_START_DURATION_OFFSET && renderProgress > STAR_RISE_START_DURATION_OFFSET) {
            float starProgress = (renderProgress - STAR_RISE_START_DURATION_OFFSET) / (STAR_DECREASE_START_DURATION_OFFSET - STAR_RISE_START_DURATION_OFFSET);
            if (starProgress <= STAR_RISE_PROGRESS_OFFSET) {
                for (int i = 0; i < mStarHolders.size(); i++) {
                    StarHolder starHolder = mStarHolders.get(i);
                    starHolder.mCurrentPoint.y = starHolder.mPoint.y - (1.0f - starHolder.mInterpolator.getInterpolation(starProgress * 5.0f)) * (mMaxStarOffsets * 0.65f);
                    starHolder.mCurrentPoint.x = starHolder.mPoint.x;
                }
            }

            if (starProgress > STAR_RISE_PROGRESS_OFFSET && starProgress < STAR_DECREASE_PROGRESS_OFFSET) {
                for (int i = 0; i < mStarHolders.size(); i++) {
                    StarHolder starHolder = mStarHolders.get(i);
                    if (starHolder.mFlashOffset < starProgress && starProgress < starHolder.mFlashOffset + STAR_FLASH_PROGRESS_PERCENTAGE) {
                        starHolder.mAlpha = (int) (MAX_ALPHA * MATERIAL_INTERPOLATOR.getInterpolation(
                                Math.abs(starProgress - (starHolder.mFlashOffset + STAR_FLASH_PROGRESS_PERCENTAGE / 2.0f)) / (STAR_FLASH_PROGRESS_PERCENTAGE / 2.0f)));
                    }
                }
            }

            if (starProgress >= STAR_DECREASE_PROGRESS_OFFSET) {
                for (int i = 0; i < mStarHolders.size(); i++) {
                    StarHolder starHolder = mStarHolders.get(i);
                    starHolder.mCurrentPoint.y = starHolder.mPoint.y + starHolder.mInterpolator.getInterpolation((starProgress - STAR_DECREASE_PROGRESS_OFFSET) * 5.0f) * mMaxStarOffsets;
                    starHolder.mCurrentPoint.x = starHolder.mPoint.x;
                }
            }
            mShowStar = true;
        }

        if (renderProgress <= MOON_DECREASE_END_DURATION_OFFSET && renderProgress > MOON_DECREASE_START_DURATION_OFFSET) {
            float moonDecreaseProgress = (renderProgress - MOON_DECREASE_START_DURATION_OFFSET) / (MOON_DECREASE_END_DURATION_OFFSET - MOON_DECREASE_START_DURATION_OFFSET);
            mMoonCoordinateY = mInitSun$MoonCoordinateY - mMaxSun$MoonRiseDistance * (1.0f - ACCELERATE_INTERPOLATOR.getInterpolation(moonDecreaseProgress));
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

    private void initStarHolders(RectF currentBounds) {
        mStarHolders.add(new StarHolder(0.3f, new PointF(currentBounds.left + currentBounds.width() * 0.175f,
                currentBounds.top + currentBounds.height() * 0.0934f)));
        mStarHolders.add(new StarHolder(0.2f, new PointF(currentBounds.left + currentBounds.width() * 0.175f,
                currentBounds.top + currentBounds.height() * 0.62f)));
        mStarHolders.add(new StarHolder(0.2f, new PointF(currentBounds.left + currentBounds.width() * 0.2525f,
                currentBounds.top + currentBounds.height() * 0.43f)));
        mStarHolders.add(new StarHolder(0.5f, new PointF(currentBounds.left + currentBounds.width() * 0.4075f,
                currentBounds.top + currentBounds.height() * 0.0934f)));
        mStarHolders.add(new StarHolder(new PointF(currentBounds.left + currentBounds.width() * 0.825f,
                currentBounds.top + currentBounds.height() * 0.04f)));
        mStarHolders.add(new StarHolder(new PointF(currentBounds.left + currentBounds.width() * 0.7075f,
                currentBounds.top + currentBounds.height() * 0.147f)));
        mStarHolders.add(new StarHolder(new PointF(currentBounds.left + currentBounds.width() * 0.3475f,
                currentBounds.top + currentBounds.height() * 0.2567f)));
        mStarHolders.add(new StarHolder(0.6f, new PointF(currentBounds.left + currentBounds.width() * 0.5825f,
                currentBounds.top + currentBounds.height() * 0.277f)));
        mStarHolders.add(new StarHolder(new PointF(currentBounds.left + currentBounds.width() * 0.84f,
                currentBounds.top + currentBounds.height() * 0.32f)));
        mStarHolders.add(new StarHolder(new PointF(currentBounds.left + currentBounds.width() * 0.8f,
                currentBounds.top + currentBounds.height() / 0.502f)));
        mStarHolders.add(new StarHolder(0.6f, new PointF(currentBounds.left + currentBounds.width() * 0.7f,
                currentBounds.top + currentBounds.height() * 0.473f)));

        mMaxStarOffsets = currentBounds.height();
    }

    private Path createMoonPath(float moonCenterX, float moonCenterY) {
        RectF moonRectF = new RectF(moonCenterX - mSun$MoonRadius, moonCenterY - mSun$MoonRadius,
                moonCenterX + mSun$MoonRadius, moonCenterY + mSun$MoonRadius);
        Path path = new Path();
        path.addArc(moonRectF, -90, 180);
        path.quadTo(moonCenterX + mSun$MoonRadius / 2.0f, moonCenterY, moonCenterX, moonCenterY - mSun$MoonRadius);
        return path;
    }

    private class StarHolder {
        public int mAlpha;
        public PointF mCurrentPoint;

        public final PointF mPoint;
        public final float mFlashOffset;
        public final Interpolator mInterpolator;

        public StarHolder(PointF point) {
            this(1.0f, point);
        }

        public StarHolder(float flashOffset, PointF mPoint) {
            this.mAlpha = MAX_ALPHA;
            this.mCurrentPoint = new PointF();
            this.mPoint = mPoint;
            this.mFlashOffset = flashOffset;
            this.mInterpolator = INTERPOLATORS[mRandom.nextInt(INTERPOLATORS.length)];
        }
    }

    public static class Builder {
        private Context mContext;

        public Builder(Context mContext) {
            this.mContext = mContext;
        }

        public DayNightLoadingRenderer build() {
            DayNightLoadingRenderer loadingRenderer = new DayNightLoadingRenderer(mContext);
            return loadingRenderer;
        }
    }
}
