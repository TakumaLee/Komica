package idv.kuma.app.komica.views;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import idv.kuma.app.komica.activities.WebViewActivity;
import idv.kuma.app.komica.configs.BundleKeyConfigs;

/**
 * Created by TakumaLee on 2017/1/26.
 */

public class WebViewFallback implements CustomTabActivityHelper.CustomTabFallback {
    @Override
    public void openUri(Activity activity, Uri uri) {
        Intent intent = new Intent(activity, WebViewActivity.class);
        intent.putExtra(BundleKeyConfigs.KEY_WEB_URL, uri.toString());
        activity.startActivity(intent);
    }
}
