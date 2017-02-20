package idv.kuma.app.komica.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.WindowManager;

import java.util.List;

import idv.kuma.app.komica.R;
import idv.kuma.app.komica.activities.base.BaseActivity;
import idv.kuma.app.komica.configs.BundleKeyConfigs;
import idv.kuma.app.komica.fragments.ImageFragment;

public class ImageActivity extends BaseActivity {

    private ImageFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        List<String> imgList = getIntent().getStringArrayListExtra(BundleKeyConfigs.BUNDLE_IMAGE_LIST);
//        int currentPosition = getIntent().getIntExtra(WebTypeConfigs.BINDLE_DM_PAGE, 0);
        String currentUrl = getIntent().getStringExtra(BundleKeyConfigs.BUNDLE_IMAGE_CURRENT_URL);
        String from = getIntent().getStringExtra(BundleKeyConfigs.KEY_WEB_FROM);
        fragment = ImageFragment.newInstance(from, imgList, currentUrl);
        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction().add(R.id.activity_image, fragment).commit();
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        fragment.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        fragment.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        if (fragment.isBackPressed()) {
            super.onBackPressed();
        }
    }
}
