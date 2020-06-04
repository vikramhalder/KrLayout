# KrLayout
[![Build Status](https://travis-ci.org/heremaps/oksse.svg?branch=master)](#)

###  #Releases 

```kotlin
repositories {
    google()
    jcenter()
    maven { url "https://jitpack.io" }
}
```

```kotlin
implementation("com.github.vikramhalder:KrLayout:1.0.0")
```

### #PulsatorLayout
```xml
<com.kivred.view.PulsatorLayout
        android:id="@+id/pulsatorLayout"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

<com.kivred.view.PulsatorLayout
    android:id="@+id/pulsatorLayout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:ps_style="#F44336"
    app:ps_stroke_width="12" />
```
```java
PulsatorLayout pulsatorLayout= findViewById(R.id.pulsatorLayout);
pulsatorLayout.start();
```
![Figure 1-1](https://i.ibb.co/f23D4B7/Media1-1.gif "Figure PulsatorLayout")
