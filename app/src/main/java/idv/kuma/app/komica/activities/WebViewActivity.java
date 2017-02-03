package idv.kuma.app.komica.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;

import com.google.android.gms.analytics.HitBuilders;

import idv.kuma.app.komica.R;
import idv.kuma.app.komica.activities.base.BaseOtherActivity;
import idv.kuma.app.komica.configs.BundleKeyConfigs;
import idv.kuma.app.komica.fragments.WebViewFragment;

public class WebViewActivity extends BaseOtherActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        String title = "";
        if (getIntent() != null) {
            title = getIntent().getStringExtra(BundleKeyConfigs.KEY_WEB_TITLE);
        }
        if (title != null) {
            setTitle(title);
        }
        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction().add(R.id.relativeLayout_webView, WebViewFragment.newInstance(getIntent().getExtras())).commit();
        }

        tracker.setScreenName("網頁: " + title);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
}
