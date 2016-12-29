package idv.kuma.app.komica.utils;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonUtils {

    private static boolean isJSONObject(String str) {
        JSONObject object = null;
        try {
            object = new JSONObject(str);

        } catch (Exception e) {
            return false;
        }
        if (object != null)
            return true;
        return false;
    }

    public static Object convertJSON(String str) {
        JSONObject object = null;
        JSONArray array = null;
        try {
            if (isJSONObject(str)) {
                object = new JSONObject(str);
            } else {
                array = new JSONArray(str);
                return array;
            }
        } catch (Exception e) {
        }
        return object;
    }

}
