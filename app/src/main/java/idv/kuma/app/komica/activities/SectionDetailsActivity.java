package idv.kuma.app.komica.activities;

import android.os.Bundle;

import idv.kuma.app.komica.R;
import idv.kuma.app.komica.activities.base.BaseOtherActivity;
import idv.kuma.app.komica.configs.BundleKeyConfigs;
import idv.kuma.app.komica.fragments.SectionDetailsFragment;
import idv.kuma.app.komica.fragments.base.BaseFragment;
import idv.kuma.app.komica.manager.KomicaManager;

public class SectionDetailsActivity extends BaseOtherActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_section_details);

        String from = getIntent().getStringExtra(BundleKeyConfigs.KEY_WEB_FROM);
        String url = "https://alleyneblade.mymoe.moe/queensblade/pixmicat.php?res=866868";//getIntent().getStringExtra(BundleKeyConfigs.KEY_WEB_URL);
        String title = getIntent().getStringExtra(BundleKeyConfigs.KEY_WEB_TITLE);
        int webType = getIntent().getIntExtra(BundleKeyConfigs.KEY_WEB_TYPE, KomicaManager.WebType.INTEGRATED);
        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction().add(R.id.activity_section_details, SectionDetailsFragment.newInstance(from, url, title, webType)).commit();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        KomicaManager.getInstance().clearCache();
    }
}
