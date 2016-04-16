package app.dinus.com.loadingdrawable;

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
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

public class GearLoadingRenderer extends LoadingRenderer {
  private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
  private static final Interpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
  private static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

  private static final float FULL_ROTATION = 1080.0f;
  private static final float ROTATION_FACTOR = 0.25f;
  private static final float MAX_PROGRESS_ARC = 0.17f;
  private static final float START_TRIM_DURATION_OFFSET = 0.5f;
  private static final float START_SCALE_DURATION_OFFSET = 0.3f;
  private static final float END_TRIM_START_DELAY_OFFSET = 0.5f;
  private static final float END_SCALE_START_DELAY_OFFSET = 0.7f;

  private static final int GEAR_COUNT = 4;
  private static final int NUM_POINTS = 3;
  private static final int MAX_ALPHA = 255;
  private static final int DEGREE_360 = 360;

  private static final int DEFAULT_COLOR = Color.WHITE;

  private final Paint mPaint = new Paint();
  private final RectF mTempBounds = new RectF();

  private boolean mIsScaling;

  private final Animator.AnimatorListener mAnimatorListener = new AnimatorListenerAdapter() {
    @Override
    public void onAnimationRepeat(Animator animator) {
      super.onAnimationRepeat(animator);
      storeOriginals();

      mStartTrim = mEndTrim;;
      mRotationCount = (mRotationCount + 1) % NUM_POINTS;
    }

    @Override
    public void onAnimationStart(Animator animation) {
      super.onAnimationStart(animation);
      mRotationCount = 0;
    }
  };

  private int mCurrentColor;

  private float mStrokeInset;

  private float mScale;
  private float mEndTrim;
  private float mRotation;
  private float mStartTrim;
  private float mRotationCount;
  private float mGroupRotation;
  private float mOriginEndTrim;
  private float mOriginRotation;
  private float mOriginStartTrim;

  public GearLoadingRenderer(Context context) {
    super(context);
    mCurrentColor = DEFAULT_COLOR;

    setupPaint();
    addRenderListener(mAnimatorListener);
  }

  private void setupPaint() {
    mPaint.setAntiAlias(true);
    mPaint.setStrokeWidth(getStrokeWidth());
    mPaint.setStyle(Paint.Style.STROKE);
    mPaint.setStrokeCap(Paint.Cap.ROUND);

    setInsets((int) getWidth(), (int) getHeight());
  }

  @Override
  public void draw(Canvas canvas, Rect bounds) {
    mPaint.setColor(mCurrentColor);

    int saveCount = canvas.save();

    canvas.rotate(mGroupRotation, bounds.exactCenterX(), bounds.exactCenterY());
    RectF arcBounds = mTempBounds;
    arcBounds.set(bounds);
    arcBounds.inset(mStrokeInset, mStrokeInset);

    if (mStartTrim == mEndTrim) {
      mStartTrim = mEndTrim + getMinProgressArc();
    }

    float startAngle = (mStartTrim + mRotation) * DEGREE_360;
    float endAngle = (mEndTrim + mRotation) * DEGREE_360;
    float sweepAngle = endAngle - startAngle;

    if (mIsScaling) {
      mPaint.setAlpha((int) (MAX_ALPHA * mScale));
      mPaint.setStrokeWidth(getStrokeWidth() * mScale);
      arcBounds.inset(arcBounds.width() * (1 - mScale) / 2, arcBounds.height() * (1 - mScale) / 2);
      for (int i = 0; i < GEAR_COUNT; i++) {
        canvas.drawArc(arcBounds, startAngle + DEGREE_360 / GEAR_COUNT * i, sweepAngle, false, mPaint);
      }
    } else {
      mPaint.setAlpha(MAX_ALPHA);

      for (int i = 0; i < GEAR_COUNT; i++) {
        canvas.drawArc(arcBounds, startAngle + DEGREE_360 / GEAR_COUNT * i, sweepAngle, false, mPaint);
      }
    }

    canvas.restoreToCount(saveCount);
  }

  @Override
  public void computeRender(float renderProgress) {
    final float minProgressArc = getMinProgressArc();
    final float originEndTrim = mOriginEndTrim;
    final float originStartTrim = mOriginStartTrim;
    final float originRotation = mOriginRotation;

    // Scaling up the start size only occurs in the first 20% of a
    // single ring animation
    if (renderProgress <= START_SCALE_DURATION_OFFSET) {
      float startScaleProgress = (renderProgress) / START_SCALE_DURATION_OFFSET;
      mScale = DECELERATE_INTERPOLATOR.getInterpolation(startScaleProgress);

      mIsScaling = true;
      invalidateSelf();
      return;
    }

    // Moving the start trim only occurs between 20% to 50% of a
    // single ring animation
    if (renderProgress <= START_TRIM_DURATION_OFFSET && renderProgress > START_SCALE_DURATION_OFFSET) {
      float startTrimProgress = (renderProgress - START_SCALE_DURATION_OFFSET) / (START_TRIM_DURATION_OFFSET - START_SCALE_DURATION_OFFSET);
      mStartTrim = originStartTrim + ((MAX_PROGRESS_ARC - minProgressArc) * LINEAR_INTERPOLATOR.getInterpolation(startTrimProgress));

      mIsScaling = false;
    }

    // Moving the end trim starts between 50% to 80% of a single ring
    // animation completes
    if (renderProgress > END_TRIM_START_DELAY_OFFSET && renderProgress < END_SCALE_START_DELAY_OFFSET) {
      float endTrimProgress = (renderProgress - END_TRIM_START_DELAY_OFFSET) / (END_SCALE_START_DELAY_OFFSET - END_TRIM_START_DELAY_OFFSET);
      mEndTrim = originEndTrim + ((MAX_PROGRESS_ARC - minProgressArc) * LINEAR_INTERPOLATOR.getInterpolation(endTrimProgress));

      mIsScaling = false;
    }

    // Scaling down the end size starts after 80% of a single ring
    // animation completes
    if (renderProgress > END_SCALE_START_DELAY_OFFSET) {
      float endScaleProgress = (renderProgress - END_SCALE_START_DELAY_OFFSET) / (1.0f - END_SCALE_START_DELAY_OFFSET);
      mScale = 1.0f - ACCELERATE_INTERPOLATOR.getInterpolation(endScaleProgress);

      mIsScaling = true;
      invalidateSelf();
      return ;
    }

    float rotateProgress = (renderProgress - START_SCALE_DURATION_OFFSET) / (END_SCALE_START_DELAY_OFFSET - START_SCALE_DURATION_OFFSET);
    mGroupRotation = ((FULL_ROTATION / NUM_POINTS) * rotateProgress) + (FULL_ROTATION * (mRotationCount / NUM_POINTS));
    mRotation = originRotation + (ROTATION_FACTOR * rotateProgress);

    invalidateSelf();
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

  public void setColor(int color) {
    mCurrentColor = color;
  }

  @Override
  public void setStrokeWidth(float strokeWidth) {
    super.setStrokeWidth(strokeWidth);
    mPaint.setStrokeWidth(strokeWidth);
    invalidateSelf();
  }

  public void setStartTrim(float startTrim) {
    mStartTrim = startTrim;
    invalidateSelf();
  }

  public float getStartTrim() {
    return mStartTrim;
  }

  public void setEndTrim(float endTrim) {
    mEndTrim = endTrim;
    invalidateSelf();
  }

  public float getEndTrim() {
    return mEndTrim;
  }

  public void setRotation(float rotation) {
    mRotation = rotation;
    invalidateSelf();
  }

  public float getRotation() {
    return mRotation;
  }

  public void setScale(float scale) {
    this.mScale = scale;
    invalidateSelf();
  }

  public float getScale() {
    return mScale;
  }

  private void setInsets(int width, int height) {
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
    mOriginStartTrim = mStartTrim;
    mOriginEndTrim = mEndTrim;
    mOriginRotation = mRotation;
  }

  private void resetOriginals() {
    mOriginStartTrim = 0;
    mOriginEndTrim = 0;
    mOriginRotation = 0;
    setStartTrim(0);
    setEndTrim(0);
    setRotation(0);
    setScale(0);
  }

  private float getMinProgressArc() {
    return (float) Math.toRadians(getStrokeWidth() / (2 * Math.PI * getCenterRadius()));
  }
}
