package idv.kuma.app.komica.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

public class AppTools {
    private static final String TAG = AppTools.class.getSimpleName();

    private static final String EMPTY = "";

    public static String getVersionName(Context context) {
        String versionName = null;
        try {
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.COMPONENT_ENABLED_STATE_DEFAULT).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    public static int getWindowSizeWidth(Context context) {
        if (null == context) {
            return 0;
        }
        return ((Activity) context).getWindow().getDecorView().getWidth();
    }

    public static int getWindowSizeHeight(Context context) {
        if (null == context) {
            return 0;
        }
        return ((Activity) context).getWindow().getDecorView().getHeight();
    }


    public static Object getSimpleKey(String key, String result) {

        Object resultObj = null;

        try {
            JSONObject object = new JSONObject(result);
            resultObj = object.get(key);
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            return resultObj;
        }
    }

    public static ColorStateList getSimpleColorState(int color) {
        return new ColorStateList(new int[][]{new int[0]}, new int[]{color});
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int spToPx(int sp) {
        return (int) (sp * Resources.getSystem().getDisplayMetrics().scaledDensity);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static boolean checkFolder(String folderName) {
        File folder = new File(folderName);
        return (folder.exists() && folder.isDirectory()) ? true : folder.mkdirs();
    }

    /**
     * <p>
     * Joins the elements of the provided {@code Iterator} into a single String
     * containing the provided elements.
     * </p>
     * <p/>
     * <p>
     * No delimiter is added before or after the list. Null objects or empty
     * strings within the iteration are represented by empty strings.
     * </p>
     *
     * @param iterator  the {@code Iterator} of values to join together, may be null
     * @param separator the separator character to use
     * @return the joined String, {@code null} if null iterator input
     */
    public static String join(Iterator<?> iterator, String separator) {
        if (iterator == null) {
            return null;
        }
        if (!iterator.hasNext()) {
            return EMPTY;
        }
        Object first = iterator.next();
        if (!iterator.hasNext()) {
            return first == null ? EMPTY : first.toString();
        }
        StringBuilder buf = new StringBuilder(256);
        if (first != null) {
            buf.append(first);
        }
        while (iterator.hasNext()) {
            buf.append(separator);
            Object obj = iterator.next();
            if (obj != null) {
                buf.append(obj);
            }
        }
        return buf.toString();
    }

    public static String join(Map<?, ?> map, char separator, char valueStartChar, char valueEndChar) {

        if (map == null) {
            return null;
        }
        if (map.size() == 0) {
            return EMPTY;
        }
        StringBuilder buf = new StringBuilder(256);
        boolean isFirst = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (isFirst) {
                buf.append(entry.getKey());
                buf.append(valueStartChar);
                buf.append(entry.getValue());
                buf.append(valueEndChar);
                isFirst = false;
            } else {
                buf.append(separator);
                buf.append(entry.getKey());
                buf.append(valueStartChar);
                buf.append(entry.getValue());
                buf.append(valueEndChar);
            }
        }

        return buf.toString();
    }


}
