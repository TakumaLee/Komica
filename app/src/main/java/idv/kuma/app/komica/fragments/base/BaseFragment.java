package idv.kuma.app.komica.fragments.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.MenuItem;
import android.view.View;

/**
 * Created by TakumaLee on 2016/5/10.
 */
public class BaseFragment extends KomicaFragmentBase {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected <T extends View> T findViewById(View v, int resId) {
        return (T) v.findViewById(resId);
    }

    public void replaceChildFragment(int resId, Fragment fragment) {
        getFragmentManager().beginTransaction().replace(resId, fragment).commit();
    }

    public boolean isBackPressed() {
        return true;
    }

    public void onGroupItemClick(MenuItem item) {

    }
}
