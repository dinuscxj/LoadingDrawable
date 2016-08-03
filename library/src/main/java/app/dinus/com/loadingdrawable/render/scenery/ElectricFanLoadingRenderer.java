package app.dinus.com.loadingdrawable.render.scenery;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.DisplayMetrics;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import app.dinus.com.loadingdrawable.DensityUtil;
import app.dinus.com.loadingdrawable.R;
import app.dinus.com.loadingdrawable.render.LoadingRenderer;

public class ElectricFanLoadingRenderer extends LoadingRenderer {
    private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    private static final Interpolator MATERIAL_INTERPOLATOR = new FastOutSlowInInterpolator();
    private static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final Interpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final Interpolator FASTOUTLINEARIN_INTERPOLATOR = new FastOutLinearInInterpolator();

    private static final Interpolator[] INTERPOLATORS = new Interpolator[]{LINEAR_INTERPOLATOR,
            DECELERATE_INTERPOLATOR, ACCELERATE_INTERPOLATOR, FASTOUTLINEARIN_INTERPOLATOR, MATERIAL_INTERPOLATOR};
    private static final List<LeafHolder> mLeafHolders = new ArrayList<>();
    private static final Random mRandom = new Random();
    private final Animator.AnimatorListener mAnimatorListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationRepeat(Animator animator) {
            super.onAnimationRepeat(animator);
            reset();
        }
    };

    public static final int MODE_NORMAL = 0;
    public static final int MODE_LEAF_COUNT = 1;

    @IntDef({MODE_NORMAL, MODE_LEAF_COUNT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MODE {
    }

    private static final String PERCENTAGE_100 = "100%";

    private static final long ANIMATION_DURATION = 7333;

    private static final int LEAF_COUNT = 28;
    private static final int DEGREE_180 = 180;
    private static final int DEGREE_360 = 360;
    private static final int FULL_GROUP_ROTATION = (int) (5.25f * DEGREE_360);

    private static final int DEFAULT_PROGRESS_COLOR = 0xfffca72e;
    private static final int DEFAULT_PROGRESS_BGCOLOR = 0xfffcd49f;
    private static final int DEFAULT_ELECTRIC_FAN_BGCOLOR = 0xfffccc59;
    private static final int DEFAULT_ELECTRIC_FAN_OUTLINE_COLOR = Color.WHITE;

    private static final float DEFAULT_WIDTH = 182.0f;
    private static final float DEFAULT_HEIGHT = 65.0f;
    private static final float DEFAULT_TEXT_SIZE = 11.0f;
    private static final float DEFAULT_STROKE_WIDTH = 2.0f;
    private static final float DEFAULT_STROKE_INTERVAL = .2f;
    private static final float DEFAULT_CENTER_RADIUS = 16.0f;
    private static final float DEFAULT_PROGRESS_CENTER_RADIUS = 11.0f;

    private static final float DEFAULT_LEAF_FLY_DURATION_FACTOR = 0.1f;

    private static final float LEAF_CREATE_DURATION_INTERVAL = 1.0f / LEAF_COUNT;
    private static final float DECELERATE_DURATION_PERCENTAGE = 0.4f;
    private static final float ACCELERATE_DURATION_PERCENTAGE = 0.6f;

    private final Paint mPaint = new Paint();
    private final RectF mTempBounds = new RectF();
    private final RectF mCurrentProgressBounds = new RectF();

    private float mTextSize;
    private float mStrokeXInset;
    private float mStrokeYInset;
    private float mProgressCenterRadius;

    private float mScale;
    private float mRotation;
    private float mProgress;

    private float mNextLeafCreateThreshold;

    private int mProgressColor;
    private int mProgressBgColor;
    private int mElectricFanBgColor;
    private int mElectricFanOutlineColor;

    private float mStrokeWidth;
    private float mCenterRadius;

    @MODE
    private int mMode;
    private int mCurrentLeafCount;

    private Drawable mLeafDrawable;
    private Drawable mLoadingDrawable;
    private Drawable mElectricFanDrawable;

    private ElectricFanLoadingRenderer(Context context) {
        super(context);
        init(context);
        setupPaint();
        addRenderListener(mAnimatorListener);
    }

    private void init(Context context) {
        mMode = MODE_NORMAL;

        mWidth = DensityUtil.dip2px(context, DEFAULT_WIDTH);
        mHeight = DensityUtil.dip2px(context, DEFAULT_HEIGHT);
        mTextSize = DensityUtil.dip2px(context, DEFAULT_TEXT_SIZE);
        mStrokeWidth = DensityUtil.dip2px(context, DEFAULT_STROKE_WIDTH);
        mCenterRadius = DensityUtil.dip2px(context, DEFAULT_CENTER_RADIUS);
        mProgressCenterRadius = DensityUtil.dip2px(context, DEFAULT_PROGRESS_CENTER_RADIUS);

        mProgressColor = DEFAULT_PROGRESS_COLOR;
        mProgressBgColor = DEFAULT_PROGRESS_BGCOLOR;
        mElectricFanBgColor = DEFAULT_ELECTRIC_FAN_BGCOLOR;
        mElectricFanOutlineColor = DEFAULT_ELECTRIC_FAN_OUTLINE_COLOR;

        mLeafDrawable = context.getResources().getDrawable(R.drawable.ic_leaf);
        mLoadingDrawable = context.getResources().getDrawable(R.drawable.ic_loading);
        mElectricFanDrawable = context.getResources().getDrawable(R.drawable.ic_eletric_fan);

        mDuration = ANIMATION_DURATION;
        setInsets((int) mWidth, (int) mHeight);
    }

    private void setupPaint() {
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void draw(Canvas canvas, Rect bounds) {
        int saveCount = canvas.save();

        RectF arcBounds = mTempBounds;
        arcBounds.set(bounds);
        arcBounds.inset(mStrokeXInset, mStrokeYInset);

        mCurrentProgressBounds.set(arcBounds.left, arcBounds.bottom - 2 * mCenterRadius,
                arcBounds.right, arcBounds.bottom);

        //draw loading drawable
        mLoadingDrawable.setBounds((int) arcBounds.centerX() - mLoadingDrawable.getIntrinsicWidth() / 2,
                0,
                (int) arcBounds.centerX() + mLoadingDrawable.getIntrinsicWidth() / 2,
                mLoadingDrawable.getIntrinsicHeight());
        mLoadingDrawable.draw(canvas);

        //draw progress background
        float progressInset = mCenterRadius - mProgressCenterRadius;
        RectF progressRect = new RectF(mCurrentProgressBounds);
        //sub DEFAULT_STROKE_INTERVAL, otherwise will have a interval between progress background and progress outline
        progressRect.inset(progressInset - DEFAULT_STROKE_INTERVAL, progressInset - DEFAULT_STROKE_INTERVAL);
        mPaint.setColor(mProgressBgColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(progressRect, mProgressCenterRadius, mProgressCenterRadius, mPaint);

        //draw progress
        mPaint.setColor(mProgressColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(createProgressPath(mProgress, mProgressCenterRadius, progressRect), mPaint);

        //draw leaves
        for (int i = 0; i < mLeafHolders.size(); i++) {
            int leafSaveCount = canvas.save();
            LeafHolder leafHolder = mLeafHolders.get(i);
            Rect leafBounds = leafHolder.mLeafRect;

            canvas.rotate(leafHolder.mLeafRotation, leafBounds.centerX(), leafBounds.centerY());
            mLeafDrawable.setBounds(leafBounds);
            mLeafDrawable.draw(canvas);

            canvas.restoreToCount(leafSaveCount);
        }

        //draw progress background outline,
        //after drawing the leaves and then draw the outline of the progress background can
        //prevent the leaves from flying to the outside
        RectF progressOutlineRect = new RectF(mCurrentProgressBounds);
        float progressOutlineStrokeInset = (mCenterRadius - mProgressCenterRadius) / 2.0f;
        progressOutlineRect.inset(progressOutlineStrokeInset, progressOutlineStrokeInset);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mProgressBgColor);
        mPaint.setStrokeWidth(mCenterRadius - mProgressCenterRadius);
        canvas.drawRoundRect(progressOutlineRect, mCenterRadius, mCenterRadius, mPaint);

        //draw electric fan outline
        float electricFanCenterX = arcBounds.right - mCenterRadius;
        float electricFanCenterY = arcBounds.bottom - mCenterRadius;

        mPaint.setColor(mElectricFanOutlineColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth);
        canvas.drawCircle(arcBounds.right - mCenterRadius, arcBounds.bottom - mCenterRadius,
                mCenterRadius - mStrokeWidth / 2.0f, mPaint);

        //draw electric background
        mPaint.setColor(mElectricFanBgColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(arcBounds.right - mCenterRadius, arcBounds.bottom - mCenterRadius,
                mCenterRadius - mStrokeWidth + DEFAULT_STROKE_INTERVAL, mPaint);

        //draw electric fan
        int rotateSaveCount = canvas.save();
        canvas.rotate(mRotation, electricFanCenterX, electricFanCenterY);
        mElectricFanDrawable.setBounds((int) (electricFanCenterX - mElectricFanDrawable.getIntrinsicWidth() / 2 * mScale),
                (int) (electricFanCenterY - mElectricFanDrawable.getIntrinsicHeight() / 2 * mScale),
                (int) (electricFanCenterX + mElectricFanDrawable.getIntrinsicWidth() / 2 * mScale),
                (int) (electricFanCenterY + mElectricFanDrawable.getIntrinsicHeight() / 2 * mScale));
        mElectricFanDrawable.draw(canvas);
        canvas.restoreToCount(rotateSaveCount);

        //draw 100% text
        if (mScale < 1.0f) {
            mPaint.setTextSize(mTextSize * (1 - mScale));
            mPaint.setColor(mElectricFanOutlineColor);
            Rect textRect = new Rect();
            mPaint.getTextBounds(PERCENTAGE_100, 0, PERCENTAGE_100.length(), textRect);
            canvas.drawText(PERCENTAGE_100, electricFanCenterX - textRect.width() / 2.0f,
                    electricFanCenterY + textRect.height() / 2.0f, mPaint);
        }

        canvas.restoreToCount(saveCount);
    }

    private Path createProgressPath(float progress, float circleRadius, RectF progressRect) {
        RectF arcProgressRect = new RectF(progressRect.left, progressRect.top, progressRect.left + circleRadius * 2, progressRect.bottom);
        RectF rectProgressRect = null;

        float progressWidth = progress * progressRect.width();
        float progressModeWidth = mMode == MODE_LEAF_COUNT ?
                (float) mCurrentLeafCount / (float) LEAF_COUNT * progressRect.width() : progress * progressRect.width();

        float swipeAngle = DEGREE_180;
        //the left half circle of the progressbar
        if (progressModeWidth < circleRadius) {
            swipeAngle = progressModeWidth / circleRadius * DEGREE_180;
        }

        //the center rect of the progressbar
        if (progressModeWidth < progressRect.width() - circleRadius && progressModeWidth >= circleRadius) {
            rectProgressRect = new RectF(progressRect.left + circleRadius, progressRect.top, progressRect.left + progressModeWidth, progressRect.bottom);
        }

        //the right half circle of the progressbar
        if (progressWidth >= progressRect.width() - circleRadius) {
            rectProgressRect = new RectF(progressRect.left + circleRadius, progressRect.top, progressRect.right - circleRadius, progressRect.bottom);
            mScale = (progressRect.width() - progressWidth) / circleRadius;
        }

        //the left of the right half circle
        if (progressWidth < progressRect.width() - circleRadius) {
            mRotation = (progressWidth / (progressRect.width() - circleRadius)) * FULL_GROUP_ROTATION % DEGREE_360;

            RectF leafRect = new RectF(progressRect.left + progressWidth, progressRect.top, progressRect.right - circleRadius, progressRect.bottom);
            addLeaf(progress, leafRect);
        }

        Path path = new Path();
        path.addArc(arcProgressRect, DEGREE_180 - swipeAngle / 2, swipeAngle);

        if (rectProgressRect != null) {
            path.addRect(rectProgressRect, Path.Direction.CW);
        }

        return path;
    }

    @Override
    protected void computeRender(float renderProgress) {
        if (renderProgress < DECELERATE_DURATION_PERCENTAGE) {
            mProgress = DECELERATE_INTERPOLATOR.getInterpolation(renderProgress / DECELERATE_DURATION_PERCENTAGE) * DECELERATE_DURATION_PERCENTAGE;
        } else {
            mProgress = ACCELERATE_INTERPOLATOR.getInterpolation((renderProgress - DECELERATE_DURATION_PERCENTAGE) / ACCELERATE_DURATION_PERCENTAGE) * ACCELERATE_DURATION_PERCENTAGE + DECELERATE_DURATION_PERCENTAGE;
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
        mCurrentLeafCount = 0;
        mNextLeafCreateThreshold = 0.0f;
        mLeafHolders.clear();
    }

    protected void setInsets(int width, int height) {
        final float minEdge = (float) Math.min(width, height);
        float insetXs;
        if (mCenterRadius <= 0 || minEdge < 0) {
            insetXs = (float) Math.ceil(mCenterRadius / 2.0f);
        } else {
            insetXs = mCenterRadius;
        }
        mStrokeYInset = (float) Math.ceil(mCenterRadius / 2.0f);
        mStrokeXInset = insetXs;
    }

    private void addLeaf(float progress, RectF leafFlyRect) {
        if (progress < mNextLeafCreateThreshold) {
            return;
        }
        mNextLeafCreateThreshold += LEAF_CREATE_DURATION_INTERVAL;

        LeafHolder leafHolder = new LeafHolder();
        mLeafHolders.add(leafHolder);
        Animator leafAnimator = getAnimator(leafHolder, leafFlyRect, progress);
        leafAnimator.addListener(new AnimEndListener(leafHolder));
        leafAnimator.start();
    }

    private Animator getAnimator(LeafHolder target, RectF leafFlyRect, float progress) {
        ValueAnimator bezierValueAnimator = getBezierValueAnimator(target, leafFlyRect, progress);

        AnimatorSet finalSet = new AnimatorSet();
        finalSet.playSequentially(bezierValueAnimator);
        finalSet.setInterpolator(INTERPOLATORS[mRandom.nextInt(INTERPOLATORS.length)]);
        finalSet.setTarget(target);
        return finalSet;
    }

    private ValueAnimator getBezierValueAnimator(LeafHolder target, RectF leafFlyRect, float progress) {
        BezierEvaluator evaluator = new BezierEvaluator(getPoint1(leafFlyRect), getPoint2(leafFlyRect));

        int leafFlyStartY = (int) (mCurrentProgressBounds.bottom - mLeafDrawable.getIntrinsicHeight());
        int leafFlyRange = (int) (mCurrentProgressBounds.height() - mLeafDrawable.getIntrinsicHeight());

        int startPointY = leafFlyStartY - mRandom.nextInt(leafFlyRange);
        int endPointY = leafFlyStartY - mRandom.nextInt(leafFlyRange);

        ValueAnimator animator = ValueAnimator.ofObject(evaluator,
                new PointF((int) (leafFlyRect.right - mLeafDrawable.getIntrinsicWidth()), startPointY),
                new PointF(leafFlyRect.left, endPointY));
        animator.addUpdateListener(new BezierListener(target));
        animator.setTarget(target);

        animator.setDuration((long) ((mRandom.nextInt(300) + mDuration * DEFAULT_LEAF_FLY_DURATION_FACTOR) * (1.0f - progress)));

        return animator;
    }

    //get the pointF which belong to the right half side
    private PointF getPoint1(RectF leafFlyRect) {
        PointF point = new PointF();
        point.x = leafFlyRect.right - mRandom.nextInt((int) (leafFlyRect.width() / 2));
        point.y = (int) (leafFlyRect.bottom - mRandom.nextInt((int) leafFlyRect.height()));
        return point;
    }

    //get the pointF which belong to the left half side
    private PointF getPoint2(RectF leafFlyRect) {
        PointF point = new PointF();
        point.x = leafFlyRect.left + mRandom.nextInt((int) (leafFlyRect.width() / 2));
        point.y = (int) (leafFlyRect.bottom - mRandom.nextInt((int) leafFlyRect.height()));
        return point;
    }

    private class BezierEvaluator implements TypeEvaluator<PointF> {

        private PointF point1;
        private PointF point2;

        public BezierEvaluator(PointF point1, PointF point2) {
            this.point1 = point1;
            this.point2 = point2;
        }

        //Third-order Bezier curve formula: B(t) = point0 * (1-t)^3 + 3 * point1 * t * (1-t)^2 + 3 * point2 * t^2 * (1-t) + point3 * t^3
        @Override
        public PointF evaluate(float fraction, PointF point0, PointF point3) {

            float t = fraction;
            float tLeft = 1.0f - t;

            float x = (float) (point0.x * Math.pow(tLeft, 3) + 3 * point1.x * t * Math.pow(tLeft, 2) + 3 * point2.x * Math.pow(t, 2) * tLeft + point3.x * Math.pow(t, 3));
            float y = (float) (point0.y * Math.pow(tLeft, 3) + 3 * point1.y * t * Math.pow(tLeft, 2) + 3 * point2.y * Math.pow(t, 2) * tLeft + point3.y * Math.pow(t, 3));

            return new PointF(x, y);
        }
    }

    private class BezierListener implements ValueAnimator.AnimatorUpdateListener {

        private LeafHolder target;

        public BezierListener(LeafHolder target) {
            this.target = target;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            PointF point = (PointF) animation.getAnimatedValue();
            target.mLeafRect.set((int) point.x, (int) point.y,
                    (int) (point.x + mLeafDrawable.getIntrinsicWidth()), (int) (point.y + mLeafDrawable.getIntrinsicHeight()));
            target.mLeafRotation = target.mMaxRotation * animation.getAnimatedFraction();
        }
    }

    private class AnimEndListener extends AnimatorListenerAdapter {
        private LeafHolder target;

        public AnimEndListener(LeafHolder target) {
            this.target = target;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            mLeafHolders.remove(target);
            mCurrentLeafCount++;
        }
    }

    private class LeafHolder {
        public Rect mLeafRect = new Rect();
        public float mLeafRotation = 0.0f;

        public float mMaxRotation = mRandom.nextInt(120);
    }

    public static class Builder {
        private Context mContext;

        public Builder(Context mContext) {
            this.mContext = mContext;
        }

        public ElectricFanLoadingRenderer build() {
            ElectricFanLoadingRenderer loadingRenderer = new ElectricFanLoadingRenderer(mContext);
            return loadingRenderer;
        }
    }
}
