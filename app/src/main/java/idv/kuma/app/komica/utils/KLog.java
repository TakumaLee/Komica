package idv.kuma.app.komica.utils;


import idv.kuma.app.komica.BuildConfig;

/**
 * Created by TakumaLee on 2016/3/27.
 */
public class KLog {
    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final String TAG = KLog.class.getSimpleName() + "/";

    public static void v(String msg) {
        v(TAG, msg);
    }

    public static void d(String msg) {
        d(TAG, msg);
    }

    public static void i(String msg) {
        i(TAG, msg);
    }

    public static void w(String msg) {
        w(TAG, msg);
    }

    public static void w(String msg, Throwable tr) {
        w(TAG, msg, tr);
    }

    public static void e(String msg) {
        e(TAG, msg);
    }

    public static void e(String msg, Throwable tr) {
        e(TAG, msg, tr);
    }

    public static void v(String tag, String msg) {
        if (DEBUG) {
            android.util.Log.v(TAG + tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (DEBUG) {
            android.util.Log.d(TAG + tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (DEBUG) {
            android.util.Log.i(TAG + tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (DEBUG) {
            android.util.Log.w(TAG + tag, msg);
        }
    }

    public static void w(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            android.util.Log.w(TAG + tag, msg, tr);
        }
    }

    public static void e(String tag, String msg) {
        android.util.Log.e(TAG + tag, msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        android.util.Log.e(TAG + tag, msg, tr);
    }
}
