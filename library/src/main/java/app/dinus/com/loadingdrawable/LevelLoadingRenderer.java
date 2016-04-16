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
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

public class LevelLoadingRenderer extends LoadingRenderer{
  private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
  private static final Interpolator MATERIAL_INTERPOLATOR = new FastOutSlowInInterpolator();
  private static final Interpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
  private static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

  private static final int NUM_POINTS = 5;
  private static final int DEGREE_360 = 360;

  private static final float FULL_ROTATION = 1080.0f;
  private static final float ROTATION_FACTOR = 0.25f;
  private static final float MAX_PROGRESS_ARC = 0.8f;
  private static final float LEVEL2_SWEEP_ANGLE_OFFSET = 7.0f / 8.0f;
  private static final float LEVEL3_SWEEP_ANGLE_OFFSET = 5.0f / 8.0f;
  private static final float START_TRIM_DURATION_OFFSET = 0.5f;
  private static final float END_TRIM_START_DELAY_OFFSET = 0.5f;

  private static final int DEFAULT_COLOR = Color.WHITE;

  private final Paint mPaint = new Paint();
  private final RectF mTempBounds = new RectF();

  private final Animator.AnimatorListener mAnimatorListener = new AnimatorListenerAdapter() {
    @Override
    public void onAnimationRepeat(Animator animator) {
      super.onAnimationRepeat(animator);
      storeOriginals();

      mStartTrim = mEndTrim;
      mRotationCount = (mRotationCount + 1) % (NUM_POINTS);
    }

    @Override
    public void onAnimationStart(Animator animation) {
      super.onAnimationStart(animation);
      mRotationCount = 0;
    }
  };

  private boolean mIsRenderingFirstHalf;

  private int mLevel1Color;
  private int mLevel2Color;
  private int mLevel3Color;

  private float mStrokeInset;

  private float mEndTrim;
  private float mRotation;
  private float mStartTrim;
  private float mRotationCount;
  private float mGroupRotation;
  private float mOriginEndTrim;
  private float mOriginRotation;
  private float mOriginStartTrim;

  public LevelLoadingRenderer(Context context) {
    super(context);
    setupPaint();
    addRenderListener(mAnimatorListener);
  }

  private void setupPaint() {
    mLevel1Color = oneThirdAlphaColor(DEFAULT_COLOR);
    mLevel2Color = twoThirdAlphaColor(DEFAULT_COLOR);
    mLevel3Color = DEFAULT_COLOR;

    mPaint.setAntiAlias(true);
    mPaint.setStrokeWidth(getStrokeWidth());
    mPaint.setStyle(Paint.Style.STROKE);
    mPaint.setStrokeCap(Paint.Cap.ROUND);

    setInsets((int) getWidth(), (int) getHeight());
  }

  @Override
  public void draw(Canvas canvas, Rect bounds) {
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

    if (mIsRenderingFirstHalf) {
      float renderPercentage = Math.abs(mStartTrim - mEndTrim) / MAX_PROGRESS_ARC;

      float topIncrement = DECELERATE_INTERPOLATOR.getInterpolation(renderPercentage) - LINEAR_INTERPOLATOR.getInterpolation(renderPercentage);
      float bottomIncrement = ACCELERATE_INTERPOLATOR.getInterpolation(renderPercentage) - LINEAR_INTERPOLATOR.getInterpolation(renderPercentage);

      mPaint.setColor(mLevel1Color);
      canvas.drawArc(arcBounds, endAngle, -sweepAngle * (1 + topIncrement), false, mPaint);
      mPaint.setColor(mLevel2Color);
      canvas.drawArc(arcBounds, endAngle, -sweepAngle * LEVEL2_SWEEP_ANGLE_OFFSET, false, mPaint);
      mPaint.setColor(mLevel3Color);
      canvas.drawArc(arcBounds, endAngle, -sweepAngle * LEVEL3_SWEEP_ANGLE_OFFSET * (1 + bottomIncrement), false, mPaint);
    } else {
      float renderPercentage = Math.abs(mStartTrim - mEndTrim) / MAX_PROGRESS_ARC;
      float totalSweepAngle = MAX_PROGRESS_ARC * DEGREE_360;

      if (renderPercentage > LEVEL2_SWEEP_ANGLE_OFFSET) {
        mPaint.setColor(mLevel1Color);
        canvas.drawArc(arcBounds, endAngle, -sweepAngle, false, mPaint);

        mPaint.setColor(mLevel2Color);
        canvas.drawArc(arcBounds, endAngle, totalSweepAngle * LEVEL2_SWEEP_ANGLE_OFFSET, false, mPaint);

        mPaint.setColor(mLevel3Color);
        canvas.drawArc(arcBounds, endAngle, totalSweepAngle * LEVEL3_SWEEP_ANGLE_OFFSET, false, mPaint);
      }  else if (renderPercentage > LEVEL3_SWEEP_ANGLE_OFFSET) {
        mPaint.setColor(mLevel2Color);
        canvas.drawArc(arcBounds, endAngle, -sweepAngle, false, mPaint);

        mPaint.setColor(mLevel3Color);
        canvas.drawArc(arcBounds, endAngle, totalSweepAngle * LEVEL3_SWEEP_ANGLE_OFFSET, false, mPaint);
      } else {
        mPaint.setColor(mLevel3Color);
        canvas.drawArc(arcBounds, endAngle, -sweepAngle, false, mPaint);
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

    // Moving the start trim only occurs in the first 50% of a
    // single ring animation
    if (renderProgress <= START_TRIM_DURATION_OFFSET) {
      float startTrimProgress = (renderProgress) / START_TRIM_DURATION_OFFSET;
      mStartTrim = originStartTrim + ((MAX_PROGRESS_ARC - minProgressArc) * MATERIAL_INTERPOLATOR.getInterpolation(startTrimProgress));

      mIsRenderingFirstHalf = true;
    }

    // Moving the end trim starts after 50% of a single ring
    // animation completes
    if (renderProgress > END_TRIM_START_DELAY_OFFSET) {
      float endTrimProgress = (renderProgress - START_TRIM_DURATION_OFFSET) / (1.0f - START_TRIM_DURATION_OFFSET);
      mEndTrim = originEndTrim + ((MAX_PROGRESS_ARC - minProgressArc) * MATERIAL_INTERPOLATOR.getInterpolation(endTrimProgress));

      mIsRenderingFirstHalf = false;
    }

    mGroupRotation = ((FULL_ROTATION / NUM_POINTS) * renderProgress) + (FULL_ROTATION * (mRotationCount / NUM_POINTS));
    mRotation = originRotation + (ROTATION_FACTOR * renderProgress);

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
    mLevel1Color = oneThirdAlphaColor(color);
    mLevel2Color = twoThirdAlphaColor(color);
    mLevel3Color = color;
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

  public void setInsets(int width, int height) {
    final float minEdge = (float) Math.min(width, height);
    float insets;
    if (getCenterRadius() <= 0 || minEdge < 0) {
      insets = (float) Math.ceil(getStrokeWidth() / 2.0f);
    } else {
      insets = minEdge / 2.0f - getCenterRadius();
    }
    mStrokeInset = insets;
  }

  public float getOriginRotation() {
    return mOriginRotation;
  }

  public void storeOriginals() {
    mOriginStartTrim = mStartTrim;
    mOriginEndTrim = mEndTrim;
    mOriginRotation = mRotation;
  }

  public void resetOriginals() {
    mOriginStartTrim = 0;
    mOriginEndTrim = 0;
    mOriginRotation = 0;
    setStartTrim(0);
    setEndTrim(0);
    setRotation(0);
  }

  private float getMinProgressArc() {
    return (float) Math.toRadians(getStrokeWidth() / (2 * Math.PI * getCenterRadius()));
  }

  private int oneThirdAlphaColor(int colorValue) {
    int startA = (colorValue >> 24) & 0xff;
    int startR = (colorValue >> 16) & 0xff;
    int startG = (colorValue >> 8) & 0xff;
    int startB = colorValue & 0xff;

    return (startA / 3 << 24)
        | (startR << 16)
        | (startG << 8)
        | startB;
  }

  private int twoThirdAlphaColor(int colorValue) {
    int startA = (colorValue >> 24) & 0xff;
    int startR = (colorValue >> 16) & 0xff;
    int startG = (colorValue >> 8) & 0xff;
    int startB = colorValue & 0xff;

    return (startA * 2 / 3 << 24)
        | (startR << 16)
        | (startG << 8)
        | startB;
  }
}
