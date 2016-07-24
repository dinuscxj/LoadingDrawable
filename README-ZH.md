
## LoadingDrawable
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-LoadingDrawable-brightgreen.svg?style=flat)](http://android-arsenal.com/details/1/3450)

 一些酷炫的加载动画， 可以与任何View配合使用，作为加载动画或者Progressbar, 此外很适合与[RecyclerRefreshLayout](https://github.com/dinuscxj/RecyclerRefreshLayout)
 配合使用作为刷新的loading 动画<br/>

 这个项目的思路源于这个动画链接 [link] (http://mp.weixin.qq.com/s?__biz=MjM5MDMxOTE5NA==&mid=402703079&idx=2&sn=2fcc6746a866dcc003c68ead9b68e595&scene=2&srcid=0302A7p723KK8E5gSzLKb2ZL&from=timeline&isappinstalled=0#wechat_redirect).<br/>
 或许你更喜欢使用Gif实现 : [GifLoadingView] (https://github.com/Rogero0o/GifLoadingView). <br/>
 
![](https://raw.githubusercontent.com/dinuscxj/LoadingDrawable/master/Preview/ShapeChangeDrawable.gif)
![](https://raw.githubusercontent.com/dinuscxj/LoadingDrawable/master/Preview/GoodsDrawable.gif)
![](https://raw.githubusercontent.com/dinuscxj/LoadingDrawable/master/Preview/AnimalDrawable.gif)
![](https://raw.githubusercontent.com/dinuscxj/LoadingDrawable/master/Preview/SceneryDrawable.gif)
![](https://raw.githubusercontent.com/dinuscxj/LoadingDrawable/master/Preview/CircleJumpDrawable.gif)
![](https://raw.githubusercontent.com/dinuscxj/LoadingDrawable/master/Preview/CircleRotateDrawable.gif)

## 功能

#### 形变系列
 * CircleBroodLoadingRenderer
 * CoolWaitLoadingRenderer
 
#### 物品系列
 * BalloonLoadingRenderer
 * WaterBottleLoadingRenderer

#### 动物系列
 * FishLoadingRenderer
 * GhostsEyeLoadingRenderer

#### 风景系列
 * DayNightLoadingRenderer
 * ElectricFanLoadingRenderer

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

## 待办事项
当我感觉bug比较少的时候，我会添加一个gradle依赖。 所以在推上去之前希望大家多提提建议和bug.

## 用法
#### Gradle
 ```
 compile project(':library')
 ```
#### 在代码里
 
  ```java
  LoadingView.setLoadingRenderer(LoadingRenderer);
  ```
 
#### 在xml中
  ```xml
  <app.dinus.com.loadingdrawable.LoadingView
     android:id="@+id/level_view"
     android:layout_width="0dp"
     android:layout_height="match_parent"
     android:layout_weight="1"
     android:background="#fff1c02e"
     app:loading_renderer="LevelLoadingRenderer"/>
  ```
  
如果LoadingView不能满足你的需求，或许你需要参考LoadingView进行自定义View

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
