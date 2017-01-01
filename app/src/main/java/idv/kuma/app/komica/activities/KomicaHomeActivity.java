package idv.kuma.app.komica.activities;

import android.os.Bundle;

import com.google.android.gms.analytics.HitBuilders;

import idv.kuma.app.komica.R;
import idv.kuma.app.komica.activities.base.BaseActivity;
import idv.kuma.app.komica.fragments.KomicaHomeFragment;
import idv.kuma.app.komica.fragments.base.BaseFragment;

public class KomicaHomeActivity extends BaseActivity {

    KomicaHomeFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_komica_home);

        tracker.setScreenName("首頁");
        tracker.send(new HitBuilders.ScreenViewBuilder().build());

        fragment = new KomicaHomeFragment();
        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction().add(R.id.activity_komica_home, fragment).commit();
        }
    }

    @Override
    public void onBackPressed() {
        try {
            BaseFragment fragmentBase = (BaseFragment) getSupportFragmentManager().getFragments().get(0);
            if (fragmentBase == null || fragmentBase.isBackPressed()) {
                super.onBackPressed();
            }
        } catch (ClassCastException e) {

        }
    }
}
