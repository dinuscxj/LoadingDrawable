
## LoadingDrawable
 some android loading drawable, can be combined with any View as the loading View,
 and is especially suitable for the loading animation of the [RecyclerRefreshLayout](https://github.com/dinuscxj/RecyclerRefreshLayout).

![](https://raw.githubusercontent.com/dinuscxj/LoadingDrawable/master/Preview/CircleLoadingDrawable.gif?width=300)
## Features
 * GearLoadingDrawable
 * WhorlLoadingDrawable
 * LevelLoadingDrawable
 * MaterialLoadingDrawable

## TODO
 When I feel less bugs enough, I will add a gradle dependency. So I hope you will make more Suggestions.

## Usage
#### Gradle
 ```
 compile project(':library')
 ```

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
