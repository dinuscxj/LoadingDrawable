
## LoadingDrawable
[中文版文档](https://github.com/dinuscxj/LoadingDrawable/blob/master/README-ZH.md)&nbsp;&nbsp;&nbsp;
[源码解析](http://www.jianshu.com/p/1c3c6fc1b7ff)<br/>
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-LoadingDrawable-brightgreen.svg?style=flat)](http://android-arsenal.com/details/1/3450)

 some android loading drawable, can be combined with any View as the loading View and Progressbar,
 and is especially suitable for the loading animation of the [RecyclerRefreshLayout](https://github.com/dinuscxj/RecyclerRefreshLayout).

![](https://raw.githubusercontent.com/dinuscxj/LoadingDrawable/master/Preview/SceneryDrawable.gif?width=300)
![](https://raw.githubusercontent.com/dinuscxj/LoadingDrawable/master/Preview/CircleJumpDrawable.gif?width=300)
![](https://raw.githubusercontent.com/dinuscxj/LoadingDrawable/master/Preview/CircleRotateDrawable.gif?width=300)

## Features
#### Circle Rotate
 * GearLoadingRenderer
 * WhorlLoadingRenderer
 * LevelLoadingRenderer
 * MaterialLoadingRenderer

#### Circle Jump
 * SwapLoadingRenderer
 * GuardLoadingRenderer
 * DanceLoadingRenderer
 * CollisionLoadingRenderer

#### Scenery
 *DayNightRenderer
 *ElectricFanLoadingRenderer

## TODO
 When I feel less bugs enough, I will add a gradle dependency. So I hope you will make more Suggestions or Issues.

## Usage
#### Gradle
 ```
 compile project(':library')
 ```
#### In java

 Used with ImageView
 ```java
 ImageView.setImageDrawable(new LoadingDrawable(new GearLoadingRenderer(Context)));
 ImageView.setImageDrawable(new LoadingDrawable(new WhorlLoadingRenderer(Context)));
 ImageView.setImageDrawable(new LoadingDrawable(new LevelLoadingRenderer(Context)));
 ImageView.setImageDrawable(new LoadingDrawable(new MaterialLoadingRenderer(Context)));
  ```

 Used with View
 ```java
 View.setBackground(new LoadingDrawable(new GearLoadingRenderer(Context)));
 View.setBackground(new LoadingDrawable(new WhorlLoadingRenderer(Context)));
 View.setBackground(new LoadingDrawable(new LevelLoadingRenderer(Context)));
 View.setBackground(new LoadingDrawable(new MaterialLoadingRenderer(Context)));
  ```

## Misc
 If you like LoadingDrawable or use it, could you please:

 * star this repo
 * send me some feedback. Thanks!

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
