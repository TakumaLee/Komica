# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/TakumaLee/Documents/Mobile/android/sdk/tools/proguard/proguard-android.txt
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

# Design.Widget
# Keep all
-keep class android.support.design.widget.** { *; }
-keep interface android.support.design.widget.** { *; }
-dontwarn android.support.design.**

# or if you don't want to keep all of the design library components you can use:

-keepattributes *Annotation*
-keep public class * extends android.support.design.widget.CoordinatorLayout.Behavior { *; }
-keep public class * extends android.support.design.widget.ViewOffsetBehavior { *; }

# OrmLite uses reflection
-keep class com.j256.**
-keepclassmembers class com.j256.** { *; }
-keep enum com.j256.**
-keepclassmembers enum com.j256.** { *; }
-keep interface com.j256.**
-keepclassmembers interface com.j256.** { *; }

-keep class com.nostra13.universalimageloader.**{ *; }


# FACEBOOK uses
-keep class com.facebook.**
-keepclassmembers class com.facebook.** { *; }
-dontwarn org.apache.**

# FACEBOOK NETWORK CONNECTION
-keep class com.facebook.android.*
-keep class android.webkit.WebViewClient
-keep class * extends android.webkit.WebViewClient
-keepclassmembers class * extends android.webkit.WebViewClient {
    <methods>;
}

# Parse uses
-keep class com.parse.**
-keepclassmembers class com.parse.** { *; }

# OKhttp
-dontwarn okio.**
-dontwarn com.squareup.okhttp.**
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }


-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

# Flurry
-keep class com.flurry.** { *; }
-dontwarn com.flurry.**
-keepattributes *Annotation*,EnclosingMethod,Signature
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# INMOBI
-keep class com.inmobi.** { *; }

-dontwarn com.inmobi.**

# Google Play Services library
-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

-keepnames @com.google.android.gms.common.annotation.KeepName class *

-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Keep the classes/members we need for client functionality.
-keep @interface android.support.annotation.Keep
-keep @android.support.annotation.Keep class *
-keepclasseswithmembers class * {
  @android.support.annotation.Keep <fields>;
}
-keepclasseswithmembers class * {
  @android.support.annotation.Keep <methods>;
}

# Google IMA
-keep class com.google.ads.interactivemedia.** { *; }

-dontwarn com.google.ads.interactivemedia.v3.api.**

# Mobile App Tracking
-keep public class com.mobileapptracker.** {
    public *;
}

-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Firebase Real time database
# Add this global rule
-keepattributes Signature

# This rule will properly ProGuard all the model classes in
# the package com.yourcompany.models. Modify to fit the structure
# of your app.
-keepclassmembers class idv.kuma.app.komica.entity.push.** {
  *;
}

# Branch.io
-keep class com.google.android.gms.ads.identifier.** { *; }

# Amazon
-keep class org.apache.commons.logging.**               { *; }
-keep class com.amazonaws.**                            { *; }
-keep class com.amazonaws.services.sqs.QueueUrlHandler  { *; }
-keep class com.amazonaws.javax.xml.transform.sax.*     { public *; }
-keep class com.amazonaws.javax.xml.stream.**           { *; }
-keep class com.amazonaws.services.**.model.*Exception* { *; }
-keep class org.codehaus.**                             { *; }
-keepattributes Signature,*Annotation*

-dontwarn javax.xml.stream.events.**
-dontwarn org.codehaus.jackson.**
-dontwarn org.apache.commons.logging.impl.**
-dontwarn org.apache.http.conn.scheme.**
-dontwarn com.amazonaws.**

# Glide
-keep class com.bumptech.glide.integration.okhttp3.OkHttpGlideModule
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

# glide okHttp
-keep class com.bumptech.glide.integration.okhttp.OkHttpGlideModule

# Crashlytics Detector
-keepattributes SourceFile,LineNumberTable

# MoPub Proguard Config
# NOTE: You should also include the Android Proguard config found with the build tools:
# $ANDROID_HOME/tools/proguard/proguard-android.txt

# Keep public classes and methods.
-keepclassmembers class com.mopub.** { public *; }
-keep public class com.mopub.**
-keep public class android.webkit.JavascriptInterface {}

# Explicitly keep any custom event classes in any package.
-keep class * extends com.mopub.mobileads.CustomEventBanner {}
-keep class * extends com.mopub.mobileads.CustomEventInterstitial {}
-keep class * extends com.mopub.nativeads.CustomEventNative {}

# Support for Android Advertiser ID.
-keep class com.google.android.gms.common.GooglePlayServicesUtil {*;}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient {*;}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info {*;}

# Support for Google Play Services
# http://developer.android.com/google/play-services/setup.html
-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# SQL APP Proguard
#-dontwarn com.viewpagerindicator.**
-keep class idv.kuma.app.komica.model.**
-keepclassmembers class idv.kuma.app.komica.model.** { *; }

-keep class idv.kuma.app.komica.dao.**
-keepclassmembers class idv.kuma.app.komica.dao.** { *; }

-keepnames class idv.kuma.app.komica.DMCApplication

# ANDROID
-keep class android.support.v7.widget.SearchView { *; }
-dontwarn android.net.http.AndroidHttpClient
-dontwarn android.content.Context
-dontwarn java.lang.CharSequence
-dontwarn android.app.Notification
-dontwarn android.net.SSLCertificateSocketFactory

-keep public class android.support.v7.widget.** { *; }
-keep public class android.support.v7.internal.widget.** { *; }
-keep public class android.support.v7.internal.view.menu.** { *; }

-keep public class * extends android.support.v4.view.ActionProvider {
    public <init>(android.content.Context);
}

## remove all logs
-assumenosideeffects class * extends android.util.Log {
public static boolean isLoggable(java.lang.String, int);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** d(...);
    public static *** e(...);
}
-assumenosideeffects class android.util.Log {
public static boolean isLoggable(java.lang.String, int);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** d(...);
    public static *** e(...);
}

-dontwarn com.arrownock.**

-dontwarn com.avos.**

-keep public class org.jsoup.** {
    public *;
}

# Icon
-keep class .R
-keep class **.R$* {
    <fields>;
}

# Sinch
-keepclasseswithmembernames class * {
    native <methods>;
}

-dontwarn org.apache.http.annotation.**

-keep class com.sinch.** { *; }
-keep interface com.sinch.** { *; }
-keep class org.webrtc.** { *; }

-keep class com.google.android.gms.** { *; }

-dontwarn com.google.android.gms.**
-dontwarn android.media.**

# MMseg4j
-keep class com.chenlb.mmseg4j.** { *; }
-dontwarn com.chenlb.mmseg4j.**