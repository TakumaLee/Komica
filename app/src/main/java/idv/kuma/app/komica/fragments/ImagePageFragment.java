package idv.kuma.app.komica.fragments;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import idv.kuma.app.komica.R;
import idv.kuma.app.komica.configs.BundleKeyConfigs;
import idv.kuma.app.komica.fragments.base.BaseFragment;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by TakumaLee on 2016/12/16.
 */

public class ImagePageFragment extends BaseFragment {

    private PhotoViewAttacher attacher;

    private ImageView imageView;
    private String imgUrl;
    private String bigImageUrl = null;

    private RectF originalRectF = new RectF();

    private boolean isLoaded = false;

    public static ImagePageFragment newInstance(String imgUrl) {
        ImagePageFragment fragment = new ImagePageFragment();
        Bundle bundle = new Bundle();
        bundle.putString(BundleKeyConfigs.BUNDLE_IMAGE_URL, imgUrl);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static ImagePageFragment newInstance(String imgUrl, String bigImgUrl) {
        ImagePageFragment fragment = new ImagePageFragment();
        Bundle bundle = new Bundle();
        bundle.putString(BundleKeyConfigs.BUNDLE_IMAGE_URL, imgUrl);
        bundle.putString(BundleKeyConfigs.BUNDLE_BIG_IMAGE_URL, bigImgUrl);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.imgUrl = getArguments().getString(BundleKeyConfigs.BUNDLE_IMAGE_URL);
        this.bigImageUrl = getArguments().getString(BundleKeyConfigs.BUNDLE_BIG_IMAGE_URL);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.adapter_photo_pager, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imageView = findViewById(view, R.id.imageView_photoViewPager);
        attacher = new PhotoViewAttacher(imageView);
        loadImage(imgUrl);
    }

    private void loadImage(String url) {
        Glide.with(imageView.getContext()).load(url).asBitmap().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                double max = 3072;
                double widthRate = max / resource.getWidth();
                double heightRate = max / resource.getHeight();
                if (widthRate < 1 || heightRate < 1) {
//                    BitmapRegionDecoder.newInstance()
                    double rate = widthRate < heightRate ? widthRate : heightRate;
                    int newWidth = (int) (resource.getWidth() * rate);
                    int newHeight = (int) (resource.getHeight() * rate);
//                    Matrix matrix = new Matrix();
//                    matrix.postScale(newWidth, newHeight);
                    resource = Bitmap.createScaledBitmap(resource, newWidth, newHeight, true);
                }
                imageView.setImageBitmap(resource);
                attacher.update();
                RectF tmpRecF = attacher.getDisplayRect();
                originalRectF = new RectF(tmpRecF);
                isLoaded = true;
            }
        });
    }

    public void loadBigImage() {
        if (hasBigImage()) {
            loadImage(bigImageUrl);
        }
    }

    public boolean hasBigImage() {
        return bigImageUrl != null;
    }

    public String getImageUrl() {
        return imgUrl;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public void hideOriginalImage() {
        imageView.setVisibility(View.GONE);
    }

    public void showOriginalImage() {
        imageView.setVisibility(View.VISIBLE);
    }

    public Rect getVisibleRect() {
        // TODO compare view with drawable image, image rect > view, get view rectf, image rect < view, get view rectf
        RectF ivRectF = new RectF(imageView.getLeft(), imageView.getTop(), imageView.getRight(), imageView.getBottom());
        RectF displayRect = attacher.getDisplayRect();
//        float viewScale     = attacher.getScale();
//
//        float imageRatio = (float) source.getWidth() / (float) source.getHeight();
//        float viewRatio = (float) imageView.getWidth() / (float) imageView.getHeight();
//
//        float scale = 0;
//        if (imageRatio > viewRatio) {
//            // scale is based on image width
//            scale = 1 / ((float) source.getWidth() / (float) imageView.getWidth() / viewScale);
//
//        } else {
//            // scale is based on image height, or 1
//            scale = 1 / ((float) source.getHeight() / (float) imageView.getHeight() / viewScale);
//        }

        // translate to bitmap scale
        if (displayRect.left < ivRectF.left
                || displayRect.top < ivRectF.top
                || displayRect.right > ivRectF.right
                || displayRect.bottom > ivRectF.bottom) {
            // TODO decide rect range
            float widthRatio = (displayRect.right - displayRect.left) / (ivRectF.right - ivRectF.left);
            float heightRatio = (displayRect.bottom - displayRect.top) / (ivRectF.bottom - ivRectF.top);
            float rightPadding = (displayRect.right - ivRectF.right) / widthRatio;
            float leftPadding = (displayRect.left - ivRectF.left) / widthRatio;
            float topPadding = (displayRect.top - ivRectF.top) / heightRatio;
            float bottomPadding = (displayRect.bottom - ivRectF.bottom) / heightRatio;

            displayRect.right = originalRectF.right - rightPadding;
            displayRect.bottom = originalRectF.bottom - bottomPadding;
//
////            ivRectF.left - originalRectF.left / originalRectF.left - x
            displayRect.left = originalRectF.left - leftPadding;;
            displayRect.top = originalRectF.top - topPadding;;
        }
//        rect.left       = -rect.left / scale;
//        rect.top        = -rect.top / scale;
//        rect.right      = rect.left + ((float) imageView.getWidth() / scale);
//        rect.bottom     = rect.top + ((float) imageView.getHeight() / scale);

//        if (rect.top<0) {
//            rect.bottom -= Math.abs(rect.top);
//            rect.top = 0;
//        }
//        if (rect.left<0) {
//            rect.right -= Math.abs(rect.left);
//            rect.left = 0;
//        }
        Rect srcRect = new Rect((int) displayRect.left,
                (int) displayRect.top,
                (int) displayRect.right,
                (int) displayRect.bottom);
        return srcRect;
    }

    public Bitmap getVisibleRectangleBitmap() {

        // TODO request the PR to this library, omg for this author.
        // TODO use imageview to get rect, and create current bitmap to return.
//        Bitmap source = attacher.getVisibleRectangleBitmap();
//        RectF rect = attacher.getDisplayRect();
//        float viewScale = attacher.getScale();
//
//        float imageRatio = (float) source.getWidth() / (float) source.getHeight();
//        float viewRatio = (float) imageView.getWidth() / (float) imageView.getHeight();
//
//        float scale = 0;
//        if (imageRatio > viewRatio) {
//            // scale is based on image width
//            scale = 1 / ((float) source.getWidth() / (float) imageView.getWidth() / viewScale);
//
//        } else {
//            // scale is based on image height, or 1
//            scale = 1 / ((float) source.getHeight() / (float) imageView.getHeight() / viewScale);
//        }
//
//        Rect srcRect = new Rect((int) rect.left,
//                (int) rect.top,
//                (int) rect.right,
//                (int) rect.bottom);
//
//        // translate to bitmap scale
//        rect.left = -rect.left / scale;
//        rect.top = -rect.top / scale;
//        rect.right = rect.left + ((float) imageView.getWidth() / scale);
//        rect.bottom = rect.top + ((float) imageView.getHeight() / scale);
//
//        if (rect.top < 0) {
//            rect.bottom -= Math.abs(rect.top);
//            rect.top = 0;
//        }
//        if (rect.left < 0) {
//            rect.right -= Math.abs(rect.left);
//            rect.left = 0;
//        }
//        Bitmap dstBmp = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.RGB_565);
//        Canvas canvas = new Canvas(dstBmp);
//        canvas.drawBitmap(source, srcRect, rect, null);
        return ((BitmapDrawable) imageView.getDrawable()).getBitmap();
    }
}
