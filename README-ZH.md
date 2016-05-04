
## LoadingDrawable
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-LoadingDrawable-brightgreen.svg?style=flat)](http://android-arsenal.com/details/1/3450)

 一些酷炫的加载动画， 可以与任何View配合使用，作为加载动画或者Progressbar, 此外很适合与[RecyclerRefreshLayout](https://github.com/dinuscxj/RecyclerRefreshLayout)
 配合使用作为刷新的loading 动画

![](https://raw.githubusercontent.com/dinuscxj/LoadingDrawable/master/Preview/SceneryDrawable.gif?width=300)
![](https://raw.githubusercontent.com/dinuscxj/LoadingDrawable/master/Preview/CircleJumpDrawable.gif?width=300)
![](https://raw.githubusercontent.com/dinuscxj/LoadingDrawable/master/Preview/CircleRotateDrawable.gif?width=300)

## 功能
#### 圆形滚动系列
 * GearLoadingRenderer
 * WhorlLoadingRenderer
 * LevelLoadingRenderer
 * MaterialLoadingRenderer

#### 圆形跳动系列
 * SwapLoadingRenderer
 * GuardLoadingRenderer
 * DanceLoadingRenderer
 * CollisionLoadingRenderer

#### 风景系列
 * DayNightRenderer
 * ElectricFanLoadingRenderer


## 待办事项
当我感觉bug比较少的时候，我会添加一个gradle依赖。 所以在推上去之前希望大家多提提建议和bug.

## 用法
#### Gradle
 ```
 compile project(':library')
 ```
#### 在代码里
 用在ImageView中
 ```java
 ImageView.setImageDrawable(new LoadingDrawable(new GearLoadingRenderer(Context)));
 ImageView.setImageDrawable(new LoadingDrawable(new WhorlLoadingRenderer(Context)));
 ImageView.setImageDrawable(new LoadingDrawable(new LevelLoadingRenderer(Context)));
 ImageView.setImageDrawable(new LoadingDrawable(new MaterialLoadingRenderer(Context)));
  ```

 用在View中
 ```java
 View.setBackground(new LoadingDrawable(new GearLoadingRenderer(Context)));
 View.setBackground(new LoadingDrawable(new WhorlLoadingRenderer(Context)));
 View.setBackground(new LoadingDrawable(new LevelLoadingRenderer(Context)));
 View.setBackground(new LoadingDrawable(new MaterialLoadingRenderer(Context)));
  ```
## 详解
#### 概述
这个项目是基于Drawable编写的动画加载库，Drawable具有轻量级的、高效性、复用性强的特点。缺点就是使用是有门槛的
如果你对于Drawable的特性不是特别了解, 和View配合使用会有诸多麻烦，建议使用前先调研一下Drawable最为背景（background）
和作为ImageView的内容时的区别。本项目主要采用了策略模式（Strategy）通过给LoadingDrawable设置不同的LoadingRenderer(渲染器)
来绘制不同的加载动画。

#### LoadingDrawable
LoadingDrawable这个类继承Drawable并实现接口Animatable（我感觉写Drawable相关的动画都会实现的接口），构造函数必须传入
LoadingRenderer的子类。并通过回调Callback与LoadingRenderer进行交互。

#### LoadingRenderer
LoadingRenderer主要负责给LoadingDrawable绘制的。 核心方法 draw(Canvas, Rect) 和 computeRender(float)，
其中draw(Canvas, Rect)顾名思义，负责绘制， computeRender 负责计算当前的进度需要绘制的形状的大小，位置，其参数
是有类内部的成员变量mRenderAnimator负责传递。

#### 圆形滚动系列
圆形滚动系列（GearLoadingRenderer、WhorlLoadingRenderer、LevelLoadingRenderer、MaterialLoadingRenderer）代码
相似度很高，无非都是不断改变绘制弧度的大小和绘制的位置。所以只详细讲解MaterialLoadingRenderer(下图的第二个动画)。<br/>
![](https://raw.githubusercontent.com/dinuscxj/LoadingDrawable/master/Preview/CircleRotateDrawable.gif?width=300)<br/>
首先draw方法进行详解， 详见下面代码注释：
```java
public void draw(Canvas canvas, Rect bounds) {
    //保存画布状态
    int saveCount = canvas.save();
    //围绕bounds中心旋转画布mGroupRotation角度
    canvas.rotate(mGroupRotation, bounds.exactCenterX(), bounds.exactCenterY());
    RectF arcBounds = mTempBounds;
    arcBounds.set(bounds);
    //这个绘制圆环总要设置的，无论在View的onDraw 还是在Drawable 的draw方法里都是不能紧贴边界绘制圆环的
    //否则会发现所绘制圆环的边界有一半被裁剪掉
    arcBounds.inset(mStrokeInset, mStrokeInset);
    //给画笔设置颜色
    mPaint.setColor(mCurrentColor);
    //下面代码是这个动画的核心代码， Material效果的动画无非就是通过不断改变绘制弧度的开始角度和绘制弧度的大小
    canvas.drawArc(arcBounds, mStartDegrees, mSwipeDegrees, false, mPaint);
    //恢复画布状态
    canvas.restoreToCount(saveCount);
  }
```
对于mStartDegrees和mSwipeDegrees是如何计算的呢？

```java
public void computeRender(float renderProgress) {
    //更新所绘制弧度的颜色，从本次动画的最后20%进行颜色渐变切换
    updateRingColor(renderProgress);
    //动画的前50% 不断增大开始角度的大小（不改变结束角度的大小）从而不断增大绘制弧度的大小
    if (renderProgress <= START_TRIM_DURATION_OFFSET) {
        float startTrimProgress = (renderProgress) / START_TRIM_DURATION_OFFSET;
        mStartDegrees = mOriginStartDegrees + MAX_SWIPE_DEGREES * MATERIAL_INTERPOLATOR.getInterpolation(startTrimProgress);
    }
    //动画的后50% 不断增大结束角度的大小（不改变开始角度的大小）从而不断减小绘制弧度的大小
    if (renderProgress > START_TRIM_DURATION_OFFSET) {
        float endTrimProgress = (renderProgress - START_TRIM_DURATION_OFFSET) / (END_TRIM_DURATION_OFFSET - START_TRIM_DURATION_OFFSET);
        mEndDegrees = mOriginEndDegrees + MAX_SWIPE_DEGREES * MATERIAL_INTERPOLATOR.getInterpolation(endTrimProgress);
    }
    //这句主要是为了防止canvas调用drawArc方法绘制sweepAngle为0时闪烁的问题
    if (Math.abs(mEndDegrees - mStartDegrees) > MIN_SWIPE_DEGREE) {
        mSwipeDegrees = mEndDegrees - mStartDegrees;
    }
    //下面这两行用于旋转画布是绘制的弧度看起来是在不断转动
    mGroupRotation = ((FULL_ROTATION / NUM_POINTS) * renderProgress) + (FULL_ROTATION * (mRotationCount / NUM_POINTS));
    mRotation = originRotation + (ROTATION_FACTOR * renderProgress);
    invalidateSelf();
  }
```

#### 圆形跳动系列
圆形跳动系列（CollisionLoadingRenderer，DanceLoadingRenderer, GuardLoadingRenderer, SwapLoadingRenderer）所设计的数学知识比较多，
需要对圆、抛物线、直线的函数有一定的了解， 并且会计算交点。其中（CollisionLoadingRenderer，SwapLoadingRenderer）相对比较简单，
（DanceLoadingRenderer, GuardLoadingRenderer）比较复杂，这两个相同点：都是圆与直线之间的动画处理，
不同点：DanceLoadingRender设计的状态变换更多，而GuardLoadingRenderer设计的知识点难度更大。 所以这里对GuardLoadingRenderer（）（下图第三那个）
进行详解。希望大家也可以尝试对代码进行分析，只有这样你才会进步的更快。 分析代码的能力对于程序员的成长非常大。废话不多说了.<br/>
![](https://raw.githubusercontent.com/dinuscxj/LoadingDrawable/master/Preview/CircleJumpDrawable.gif?width=300)<br/>
首先还是对 draw方法进行解释：
``` java
@Override
  public void draw(Canvas canvas, Rect bounds) {
    //初始化arcBounds 设置inset 保证所绘制圆环不会被裁减
    RectF arcBounds = mTempBounds;
    arcBounds.set(bounds);
    arcBounds.inset(mStrokeInset, mStrokeInset);
    //mCurrentBounds保存当前可安全绘制区域
    mCurrentBounds.set(arcBounds);
    //保存画布的状态
    int saveCount = canvas.save();
    //不断改变绘制弧度的开始角度和绘制弧度的大小
    float startAngle = (mStartTrim + mRotation) * 360;
    float endAngle = (mEndTrim + mRotation) * 360;
    float sweepAngle = endAngle - startAngle;
    if (sweepAngle != 0) {
      mPaint.setColor(mColor);
      mPaint.setStyle(Paint.Style.STROKE);
      canvas.drawArc(arcBounds, startAngle, sweepAngle, false, mPaint);
    }
    //绘制水波纹 初始半径大小就是圆环的半径， 最大是圆环半径的2倍，
    //通过mWaveProgress不断扩大半径和减少绘制水波纹的透明度
    if (mWaveProgress < 1.0f) {
      mPaint.setColor(Color.argb((int) (Color.alpha(mColor) * (1.0f - mWaveProgress)),
          Color.red(mColor), Color.green(mColor), Color.blue(mColor)));
      mPaint.setStyle(Paint.Style.STROKE);
      float radius = Math.min(arcBounds.width(), arcBounds.height()) / 2.0f;
      canvas.drawCircle(arcBounds.centerX(), arcBounds.centerY(), radius * (1.0f + mWaveProgress), mPaint);
    }
    //绘制跳动球的位置 只是简单的绘制Circle
    if (mPathMeasure != null) {
      mPaint.setColor(mBallColor);
      mPaint.setStyle(Paint.Style.FILL);
      canvas.drawCircle(mCurrentPosition[0], mCurrentPosition[1], mSkipBallSize * mScale, mPaint);
    }
    canvas.restoreToCount(saveCount);
  }
```
像这种涉及数学较多的，核心代码都在计算中
```java
 public void computeRender(float renderProgress) {
     //动画的前START_TRIM_DURATION_OFFSET 不断减少结束角度的大小（不改变开始角度的大小）从而不断增大绘制弧度的大小
     //并不断增大mRotation（反向增大， START_TRIM_INIT_ROTATION、 START_TRIM_MAX_ROTATION 都是负数）是反向旋转
     if (renderProgress <= START_TRIM_DURATION_OFFSET) {
       final float startTrimProgress = (renderProgress) / START_TRIM_DURATION_OFFSET;
       mEndTrim = -MATERIAL_INTERPOLATOR.getInterpolation(startTrimProgress);
       mRotation = START_TRIM_INIT_ROTATION + START_TRIM_MAX_ROTATION
               * MATERIAL_INTERPOLATOR.getInterpolation(startTrimProgress);
     }
     //动画在（START_TRIM_DURATION_OFFSET， WAVE_DURATION_OFFSET］之间不断扩大水波纹的半径
     if (renderProgress <= WAVE_DURATION_OFFSET && renderProgress > START_TRIM_DURATION_OFFSET) {
       final float waveProgress = (renderProgress - START_TRIM_DURATION_OFFSET)
               / (WAVE_DURATION_OFFSET - START_TRIM_DURATION_OFFSET);
       mWaveProgress = ACCELERATE_INTERPOLATOR.getInterpolation(waveProgress);
     }
     //动画在（WAVE_DURATION_OFFSET， BALL_SKIP_DURATION_OFFSET］之间通过PathMeasure获取当前跳动的小球
     //应该所在的坐标，不熟悉PathMeasure需要google和baidu一下了。做复杂动画必须了解的知识点
     if (renderProgress <= BALL_SKIP_DURATION_OFFSET && renderProgress > WAVE_DURATION_OFFSET) {
       if (mPathMeasure == null) {
         mPathMeasure = new PathMeasure(createSkipBallPath(), false);
       }
       final float ballSkipProgress = (renderProgress - WAVE_DURATION_OFFSET)
               / (BALL_SKIP_DURATION_OFFSET - WAVE_DURATION_OFFSET);
       mPathMeasure.getPosTan(ballSkipProgress * mPathMeasure.getLength(), mCurrentPosition, null);
       mWaveProgress = 1.0f;
     }
     //动画在（BALL_SKIP_DURATION_OFFSET， BALL_SCALE_DURATION_OFFSET］之间通过mScale缩放跳动小球的半径
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
     //动画的在[BALL_SCALE_DURATION_OFFSET， 1.0f］不断增加结束角度的大小（不改变开始角度的大小）从而不断减小绘制弧度的大小
     //并不断加大mRotation（正向增大， END_TRIM_INIT_ROTATION、 END_TRIM_MAX_ROTATION 都是正数）从而正向旋转
     if (renderProgress >= BALL_SCALE_DURATION_OFFSET) {
       final float endTrimProgress =
           (renderProgress - BALL_SKIP_DURATION_OFFSET)
               / (END_TRIM_DURATION_OFFSET - BALL_SKIP_DURATION_OFFSET);
       mEndTrim = -1 + MATERIAL_INTERPOLATOR.getInterpolation(endTrimProgress);
       mRotation = END_TRIM_INIT_ROTATION + END_TRIM_MAX_ROTATION
               * MATERIAL_INTERPOLATOR.getInterpolation(endTrimProgress);
       //重置参数，防止不必要的绘制
       mScale = 1.0f;
       mPathMeasure = null;
     }
   }
   //小球跳动路径的核心路径计算函数
   //圆的公式 x^2 + y^2 = radius^2 --> y = sqrt(radius^2 - x^2) 或 y = －sqrt(radius^2 - x^2
   private Path createSkipBallPath() {
     //绘制圆环的半径
     float radius = Math.min(mCurrentBounds.width(), mCurrentBounds.height()) / 2.0f;
     //绘制圆环的半径的平方
     float radiusPow2 = (float) Math.pow(radius, 2.0f);
     //原点x坐标
     float originCoordinateX = mCurrentBounds.centerX();
     //原点y坐标
     float originCoordinateY = mCurrentBounds.centerY();
     //跳动的小球的x坐标取样点
     float[] coordinateX = new float[] {0.0f, 0.0f, -0.8f * radius, 0.75f * radius,
         -0.45f * radius, 0.9f * radius, -0.5f * radius};
     //跳动的小球的y坐标正负值取样点（y坐标可能呢正负）
     float[] sign = new float[] {1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f};
     Path path = new Path();
     //由x坐标计算y坐标的公式见函数开头
     for (int i = 0; i < coordinateX.length; i++) {
       //第一个点是moveTo
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
       //最后一个点， 指向圆环中心
       if (i == coordinateX.length - 1) {
         path.lineTo(originCoordinateX, originCoordinateY);
       }
     }
     return path;
   }
```

## 杂谈
如果你喜欢LoadingDrawable或者在使用它， 你可以

 * star这个项目
 * 提一些建议， 谢谢。

## License
    Copyright 2015-2019 dinus

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
