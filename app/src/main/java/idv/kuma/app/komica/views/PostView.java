package idv.kuma.app.komica.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import idv.kuma.app.komica.R;
import idv.kuma.app.komica.entity.KPost;
import idv.kuma.app.komica.entity.KPostImage;
import idv.kuma.app.komica.manager.KomicaAccountManager;
import idv.kuma.app.komica.manager.KomicaManager;
import idv.kuma.app.komica.manager.ThirdPartyManager;
import idv.kuma.app.komica.utils.AppTools;

import static android.text.Html.FROM_HTML_MODE_LEGACY;

/**
 * Created by TakumaLee on 2016/12/22.
 */

public class PostView extends LinearLayout {

    public TextView postIdTextView;
    public TextView postTitleTextView;
    public TextView postQuoteTextView;
    public ImageView postThumbImageView;
    public TextView postImgErrMsgTextView;
    public LinearLayout postImgListContainer;

    private KPost post;

    public PostView(Context context) {
        this(context, null);
    }

    public PostView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PostView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.layout_section_post, this, true);
        initView();
    }

    private void initView() {
        LayoutInflater inflate = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        postImgListContainer = (LinearLayout) inflate.inflate(R.layout.layout_section_post_img_list, this);
        postIdTextView = (TextView) findViewById(R.id.textView_section_post_id);
        postTitleTextView = (TextView) findViewById(R.id.textView_section_post_title);
        postQuoteTextView = (TextView) findViewById(R.id.textView_section_post_quote);
        postImgErrMsgTextView = (TextView) findViewById(R.id.textView_section_post_message);
        postThumbImageView = (ImageView) findViewById(R.id.imageView_section_post_thumb);
        postThumbImageView.setVisibility(KomicaManager.getInstance().isSwitchLogin() && KomicaAccountManager.getInstance().isLogin() ? View.VISIBLE : View.GONE);
        postImgErrMsgTextView.setVisibility(KomicaManager.getInstance().isSwitchLogin() && KomicaAccountManager.getInstance().isLogin() ? View.GONE : View.VISIBLE);
        postImgErrMsgTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ThirdPartyManager.getInstance().loginFacebook((Activity) getContext());
            }
        });
    }

    public void setPost(KPost post) {
        this.post = post;
    }

    public void setLinkMovementMethod(LinkMovementMethod movementMethod) {
        postQuoteTextView.setMovementMethod(movementMethod);
    }

    public void notifyDataSetChanged() {
        postIdTextView.setText("No. " + post.getId());
        postTitleTextView.setText(post.getTitle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            postQuoteTextView.setText(Html.fromHtml(post.getQuote(), FROM_HTML_MODE_LEGACY));
        } else {
            postQuoteTextView.setText(Html.fromHtml(post.getQuote()));
        }
        if ((post.hasImage() || post.hasVideo()) && post.getPostImageList().size() > 0) {
            postThumbImageView.setVisibility(KomicaAccountManager.getInstance().isLogin() ? VISIBLE : GONE);
            Glide.with(getContext()).load(post.getPostImageList().get(0).getImageUrl()).into(postThumbImageView);
        } else {
            postThumbImageView.setVisibility(GONE);
        }
        if (post.getPostImageList().size() > 1) {
            postImgListContainer.removeAllViews();
            for (KPostImage postImage : post.getPostImageList()) {
                final ImageView imageView = new ImageView(getContext());
                imageView.setVisibility(KomicaAccountManager.getInstance().isLogin() ? VISIBLE : GONE);
                imageView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                imageView.setAdjustViewBounds(true);
                if (postImage.getImageUrl().contains("gif")) {
                    Glide.with(getContext()).load(postImage.getImageUrl()).into(imageView);
                } else {
                    Glide.with(getContext()).load(postImage.getImageUrl()).asBitmap().into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            double max = AppTools.getWindowSizeWidth(getContext()) / 2;
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
                                imageView.setImageBitmap(resource);
                            }
                        }
                    });
                }
                postImgListContainer.addView(imageView);
            }
        }
    }
}
