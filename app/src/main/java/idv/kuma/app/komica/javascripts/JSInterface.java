package idv.kuma.app.komica.javascripts;

import android.webkit.JavascriptInterface;

/**
 * Created by TakumaLee on 2017/1/4.
 */

public class JSInterface {
    private static final String TAG = JSInterface.class.getSimpleName();

    public interface OnCallListener {
        void onResponse(String result);
    }

    private OnCallListener listener;

    public JSInterface(OnCallListener listener) {
        this.listener = listener;
    }

    @JavascriptInterface
    public void onResponse(String result) {
        listener.onResponse(result);
    }
}
