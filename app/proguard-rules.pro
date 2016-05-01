# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Programs\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-ignorewarnings

-optimizationpasses 88
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

#四大组件排除 自定义的view排除(因为xml中使用的是hardcode)
-keep public class * extends android.support.v4.**
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application {*;}
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.view.View {*;}
-keep public class * extends android.widget.BaseAdapter {*;}
-keep public class liubaoyua.customtext.HookMethod{*;}
-keep public class liubaoyua.customtext.entity.CustomText{*;}
-keep public class liubaoyua.customtext.HookMethod{*;}

-keepattributes *Annotation*
-keepattributes Signature
-keepattributes EnclosingMethod

-dontwarn android.support.v4.**
#忽略picasso库的警告
-dontwarn com.squareup.picasso.**
#忽略拼音库的警告
-dontwarn net.soureceforge.pinyin4j.**
-dontwarn demo.**
-keep class net.sourceforge.pinyin4j.** { *;}
-keep class demo.** { *;}


-keepclassmembers class liubaoyua.customtext.utils.Common {
    public static final <fields>;
}


-keepclassmembers class **.R$* {
  public static <fields>;
}
-keepclasseswithmembernames class * {
     native <methods>;
 }



-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}


-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

#eventbus的相关函数
-keepclassmembers class ** {
    public void onEvent*(**);
}


-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
















#-ignorewarnings
##
#-optimizationpasses 5
#-dontusemixedcaseclassnames
#-dontskipnonpubliclibraryclasses
#-dontpreverify
#-verbose
#-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
##第三方的库声明
##-libraryjars libs/commons-io-2.4.jar
##-libraryjars libs/eventbus-2.4.0.jar
##-libraryjars libs/pinyin4j-2.5.0.jar
##-libraryjars libs/greendao-1.3.7.jar
##-libraryjars libs/nineoldandroids-2.4.0.jar
##-libraryjars libs/picasso-2.5.2.jar
##-libraryjars libs/systembartint-1.0.4.jar
##四大组件排除 自定义的view排除(因为xml中使用的是hardcode)
#-keep public class * extends android.support.v4.**
#-keep public class * extends android.app.Fragment
#-keep public class * extends android.app.Activity
#-keep public class * extends android.app.Application {*;}
#-keep public class * extends android.app.Service
#-keep public class * extends android.content.BroadcastReceiver
#-keep public class * extends android.view.View {*;}
#-keep public class * extends android.widget.BaseAdapter {*;}
#
#-keepattributes *Annotation*
#-keepattributes Signature
#-keepattributes EnclosingMethod
#
#-dontwarn android.support.v4.**
##忽略picasso库的警告
##-dontwarn com.squareup.picasso.**
##忽略拼音库的警告
#-dontwarn net.soureceforge.pinyin4j.**
##-dontwarn demo.**
#-keep class net.sourceforge.pinyin4j.** { *;}
##-keep class demo.** { *;}
#
##保留greendao生成的数据库操作
##-keep class com.doloop.www.myappmgr.material.dao.*$Properties {
##    public static <fields>;
##}
##
##-keepclassmembers class liubaoyua.customtext.utils.** {
##    public static final <fields>;
##}
#
##
##-keepclassmembers class **.R$* {
##  public static <fields>;
##}
##-keepclasseswithmembernames class * {
##    native <methods>;
##}
##
##-keepclasseswithmembers class * {
##    public <init>(android.content.Context, android.util.AttributeSet);
##}
##
##-keepclasseswithmembers class * {
##    public <init>(android.content.Context, android.util.AttributeSet, int);
##}
#
##-keepclassmembers class * extends android.app.Activity {
##   public void *(android.view.View);
##}
##eventbus的相关函数
##-keepclassmembers class ** {
##    public void onEvent*(**);
##}
##
##-keepclassmembers enum * {
##    public static **[] values();
##    public static ** valueOf(java.lang.String);
##}
##
##-keep class * implements android.os.Parcelable {
##  public static final android.os.Parcelable$Creator *;
##}