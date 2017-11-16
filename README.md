# WindowImageView

An ImageView display in RecyclerView, looks like window.

![](https://github.com/Bleoo/WindowImageView/blob/master/pictures/20171103175130.gif)

## Usage

JitPack.io, add jitpack.io repositiory and dependency to your build.gradle:

```groovy
repositories {
    maven {
        url "https://jitpack.io"
    }
}

   dependencies {
       compile 'com.github.Bleoo:WindowImageView:1.1'
   }
```


```xml
 <io.github.bleoo.windowimageview.WindowImageView
        android:id="@+id/window_image_view"
        android:layout_width="match_parent"
        android:layout_height="180dp"
        app:frescoEnable="false"
        app:src="@drawable/timg" />
```
```java
window_image_view.setFrescoEnable(true);
window_image_view.setImageResource(R.drawable.timg);
window_image_view.bindRecyclerView(recyclerView);
```

### With Fresco
```xml
app:frescoEnable="false"
```
```java
window_image_view.setFrescoEnable(true);
window_image_view.setImageURI(YourUri);
```

## MIT License

Copyright (c) 2017 Yang Liu

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.