package idv.kuma.app.komica.activities.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import java.util.List;

/**
 * Created by TakumaLee on 2016/7/28.
 */
public class BaseActivity extends KomicaActivityBase {

    private boolean active = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        active = false;
    }

    protected <T extends View> T findViewTById(int id) {
        T view = (T) super.findViewById(id);
        return view;
    }

    protected <T extends View> T findViewTById(View parents, int id) {
        T view = (T) parents.findViewById(id);
        return view;
    }

    protected void replaceFragment(int resId, Fragment fragment) {
        if (active) {
            getSupportFragmentManager().beginTransaction().replace(resId, fragment, fragment.getClass().getName()).commitAllowingStateLoss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        handleFragmentListActivityResult(getSupportFragmentManager().getFragments(), requestCode, resultCode, data);
    }

    private void handleFragmentListActivityResult(List<Fragment> fragmentList, int requestCode, int resultCode, Intent data) {
        if (fragmentList != null && fragmentList.size() > 0) {
            for (Fragment child : fragmentList) {
                handleFragmentActivityResult(child, requestCode, resultCode, data);
            }
        }
    }

    private void handleFragmentActivityResult(Fragment fragment, int requestCode, int resultCode, Intent data) {
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
            if (fragment.getChildFragmentManager() != null) {
                handleFragmentListActivityResult(fragment.getChildFragmentManager().getFragments(), requestCode, resultCode, data);
            }
        }
    }
}
