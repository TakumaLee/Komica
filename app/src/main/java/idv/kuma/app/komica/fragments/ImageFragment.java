package idv.kuma.app.komica.fragments;

import android.Manifest;
import android.animation.Animator;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.rubensousa.floatingtoolbar.FloatingToolbar;
import com.google.android.gms.analytics.HitBuilders;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import idv.kuma.app.komica.R;
import idv.kuma.app.komica.adapters.TabFragmentAdapter;
import idv.kuma.app.komica.configs.BundleKeyConfigs;
import idv.kuma.app.komica.fragments.base.BaseFragment;
import idv.kuma.app.komica.manager.ShareAdapter;
import idv.kuma.app.komica.manager.ShareManager;
import idv.kuma.app.komica.utils.KLog;
import idv.kuma.app.komica.views.PhotoViewPager;


/**
 * Created by TakumaLee on 2016/11/12.
 */

public class ImageFragment extends BaseFragment {
    private static final String TAG = ImageFragment.class.getSimpleName();

    private static final long CONST_SHOW_TOUCH_TIMES = 100;

    private List<String> imgList = Collections.emptyList();
    private int currentPosition = 0;
    private String currentUrl = null;
    private String from;

    private PhotoViewPager viewPager;
    private TabFragmentAdapter adapter;

    private FloatingActionButton fab;
    private FloatingToolbar ftb;

    private CropImageView cropImageView;

    private MaterialDialog progressDialog;
    private MaterialDialog shareDialog;

    private long touchSenconds;

    public static ImageFragment newInstance(String from, List<String> imgList, String currentUrl) {
        ImageFragment fragment = new ImageFragment();
        Bundle bundle = new Bundle();
        bundle.putString(BundleKeyConfigs.KEY_WEB_FROM, from);
        bundle.putStringArrayList(BundleKeyConfigs.BUNDLE_IMAGE_LIST, (ArrayList<String>) imgList);
        bundle.putString(BundleKeyConfigs.BUNDLE_IMAGE_CURRENT_URL, currentUrl);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static ImageFragment newInstance(String from, List<String> imgList, int currentPosition) {
        ImageFragment fragment = new ImageFragment();
        Bundle bundle = new Bundle();
        bundle.putString(BundleKeyConfigs.KEY_WEB_FROM, from);
        bundle.putStringArrayList(BundleKeyConfigs.BUNDLE_IMAGE_LIST, (ArrayList<String>) imgList);
        bundle.putInt(BundleKeyConfigs.BUNDLE_IMAGE_POSITION, currentPosition);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        from = getArguments().getString(BundleKeyConfigs.KEY_WEB_FROM);
        imgList = getArguments().getStringArrayList(BundleKeyConfigs.BUNDLE_IMAGE_LIST);
        currentPosition = getArguments().getInt(BundleKeyConfigs.BUNDLE_IMAGE_POSITION);
        currentUrl = getArguments().getString(BundleKeyConfigs.BUNDLE_IMAGE_CURRENT_URL);
        if (currentUrl != null) {
            currentPosition = findImagePosition();
        }
        tracker.setScreenName("圖片閱覽_" + from);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_image, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fab = findViewById(view, R.id.fab_image);
        ftb = findViewById(view, R.id.floatingToolbar_image);
        cropImageView = findViewById(view, R.id.cropImageView_image);
        viewPager = findViewById(view, R.id.viewPager_image);
        adapter = new TabFragmentAdapter(getFragmentManager());

        for (String img : imgList) {
            adapter.addFragment(ImagePageFragment.newInstance(img), "Img List");
        }

        if (adapter.getCount() > 0) {
            viewPager.setAdapter(adapter);
        }
        viewPager.setOffscreenPageLimit(5);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currentPosition);

        shareDialog = ShareManager.getInstance().getShareDialog(getActivity(), new ShareManager.ShareEventCallback() {
            @Override
            public void onSuccess() {
                shareDialog.dismiss();
                ftb.hide();
            }

            @Override
            public void onCancel() {
                shareDialog.dismiss();
                ftb.hide();
            }

            @Override
            public void onError() {
                shareDialog.dismiss();
                ftb.hide();
            }
        }, new ShareAdapter.OnPlatformSelectedListener() {
            @Override
            public void onFacebook() {
                Bitmap shareImage = cropImageView.getCroppedImage();
                ShareManager.getInstance().share(getActivity(), ShareManager.Platform.FACEBOOK, shareImage, null, null, null);
            }

            @Override
            public void onMore() {
                Bitmap shareImage = cropImageView.getCroppedImage();
                File folder = Environment.getExternalStoragePublicDirectory("Komica+/share");
                if (!folder.exists()) {
                    folder.mkdirs();
                }
                File f = new File(Environment.getExternalStoragePublicDirectory("Komica+/share") + File.separator + "share.png");
                try {
                    f.createNewFile();
                    FileOutputStream fo = new FileOutputStream(f);
                    shareImage.compress(Bitmap.CompressFormat.PNG, 100, fo);
                    fo.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ShareManager.getInstance().share(getActivity(), ShareManager.Platform.OTHER, shareImage, Uri.fromFile(f), null, null);
            }
        });

        ftb.attachFab(fab);
        ftb.hide();
        ftb.enableAutoHide(false);
        ftb.addMorphListener(new FloatingToolbar.MorphListener() {
            @Override
            public void onMorphEnd() {
                KLog.v(TAG, "onMorphEnd");
            }

            @Override
            public void onMorphStart() {
                KLog.v(TAG, "onMorphStart");
                ImagePageFragment fragment = ((ImagePageFragment) adapter.getItem(viewPager.getCurrentItem()));
                fragment.hideOriginalImage();
                cropImageView.setVisibility(View.VISIBLE);
                cropImageView.setImageBitmap(fragment.getVisibleRectangleBitmap());
                cropImageView.setCropRect(fragment.getVisibleRect());
            }

            @Override
            public void onUnmorphStart() {
                KLog.v(TAG, "onUnmorphStart");
                ImagePageFragment fragment = ((ImagePageFragment) adapter.getItem(viewPager.getCurrentItem()));
                fragment.showOriginalImage();
                cropImageView.setVisibility(View.GONE);
                cropImageView.setImageBitmap(null);
            }

            @Override
            public void onUnmorphEnd() {
                KLog.v(TAG, "onUnmorphEnd");
            }
        });
        ftb.setClickListener(new FloatingToolbar.ItemClickListener() {
            @Override
            public void onItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_share:
                        shareDialog.show();
                        break;
                    case R.id.action_cancel:
                    default:
                        ftb.hide();
                        break;
                }
            }

            @Override
            public void onItemLongClick(MenuItem item) {

            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO expand option
                if (ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

//            } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            0);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
//            }
                } else {
                    showFloatingToobar();
                }


            }
        });
    }

    private void showFabAnimation() {
        if (getContext() == null) {
            return;
        }
        fab.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fab_fade_in));
        fab.animate()
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        fab.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .start();
    }

    private void hideFabAnimation() {
        if (getContext() == null) {
            return;
        }
        fab.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fab_fade_out));
        fab.animate()
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        fab.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchSenconds = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_UP:
                if (System.currentTimeMillis() - touchSenconds <= CONST_SHOW_TOUCH_TIMES) {
                    if (fab.isShown()) {
                        hideFabAnimation();
                    } else if (!ftb.isShowing()) {
                        showFabAnimation();
                        fab.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                hideFabAnimation();
                            }
                        }, 3000);
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        showFloatingToobar();
    }

    @Override
    public boolean isBackPressed() {
        if (ftb != null && ftb.isShowing()) {
            ftb.hide();
            return false;
        }
        return super.isBackPressed();
    }

    private void showFloatingToobar() {
        if (((ImagePageFragment) adapter.getItem(viewPager.getCurrentItem())).isLoaded()) {
            ftb.show();
        } else {
            Toast.makeText(getContext(), R.string.message_image_is_not_ready, Toast.LENGTH_LONG).show();
        }
    }

    private void notifyAdapter() {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (viewPager.getAdapter() == null) {
                    viewPager.setAdapter(adapter);
                } else {
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    private int findImagePosition() {
        //TODO use algorithm to refactor it.
        for (int i = 0; i < imgList.size(); i++) {
            if (currentUrl.equals(imgList.get(i))) {
                return i;
            }
        }
        return -1;
    }

}
