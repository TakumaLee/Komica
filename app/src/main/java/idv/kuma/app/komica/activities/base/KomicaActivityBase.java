package idv.kuma.app.komica.activities.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.analytics.FirebaseAnalytics;

import idv.kuma.app.komica.KomicaApplication;
import idv.kuma.app.komica.entity.MyAccount;
import idv.kuma.app.komica.manager.KomicaAccountManager;
import idv.kuma.app.komica.manager.KomicaManager;
import idv.kuma.app.komica.manager.ThirdPartyManager;


/**
 * Created by TakumaLee on 2016/11/12.
 */

public abstract class KomicaActivityBase extends AppCompatActivity {

    protected MyAccount myAccount;
    protected Tracker tracker;
    protected FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myAccount = KomicaAccountManager.getInstance().getMyAccount();
        tracker = ((KomicaApplication) getApplication()).getDefaultTracker();
        GoogleAnalytics.getInstance(this).setLocalDispatchPeriod(1);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        KomicaManager.getInstance().clearCache();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ThirdPartyManager.getInstance().onActivityResult(requestCode, resultCode, data);
    }
}
