package idv.kuma.app.komica.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Parcel;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import at.huber.youtubeExtractor.OnYoutubeParseListener;
import at.huber.youtubeExtractor.YtFile;
import idv.kuma.app.komica.R;
import idv.kuma.app.komica.entity.KPost;
import idv.kuma.app.komica.entity.KPostImage;
import idv.kuma.app.komica.manager.KomicaAccountManager;
import idv.kuma.app.komica.manager.KomicaManager;
import idv.kuma.app.komica.manager.ThirdPartyManager;
import idv.kuma.app.komica.manager.YoutubeManager;
import idv.kuma.app.komica.utils.AppTools;
import idv.kuma.app.komica.utils.KLog;

import static android.text.Html.FROM_HTML_MODE_LEGACY;

/**
 * Created by TakumaLee on 2016/12/22.
 */

public class PostView extends LinearLayout {
    private static final String TAG = PostView.class.getSimpleName();

    public TextView postIdTextView;
    public TextView postTitleTextView;
    public TextView postQuoteTextView;
    public ImageView postThumbImageView;
    public TextView postImgErrMsgTextView;
    public LinearLayout postImgListContainer;

    public RelativeLayout postVideoContainer;
    public TextView postVideoTitleTextView;

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
//        LayoutInflater inflate = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        inflate.inflate(R.layout.layout_section_post_img_list, this);
        postImgListContainer = (LinearLayout) findViewById(R.id.linearLayout_section_preview_imgList_container);
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

        postVideoContainer = (RelativeLayout) findViewById(R.id.relativeLayout_section_post_video_content_container);
        postVideoTitleTextView = (TextView) findViewById(R.id.textView_section_post_video_content_title);
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
            notifyVideo();
        } else {
            postVideoContainer.setVisibility(GONE);
            postThumbImageView.setVisibility(GONE);
        }
        postImgListContainer.removeAllViews();
        if (post.getPostImageList().size() > 1) {
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

    private void notifyVideo() {
        if (KomicaAccountManager.getInstance().isLogin() && post.hasVideo()) {
            postVideoContainer.setVisibility(VISIBLE);
            postVideoContainer.setOnClickListener(onVideoClickListener);
        } else {
            postVideoContainer.setVisibility(GONE);
            postVideoContainer.setOnClickListener(null);
        }
    }

    private OnClickListener onVideoClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (post.getVideoUrl().contains("youtube") || post.getVideoUrl().contains("youtu.be")) {
                YoutubeManager.getInstance().startParseYoutubeUrl((Activity) getContext(), post.getVideoUrl(), new OnYoutubeParseListener() {
                    @Override
                    public int describeContents() {
                        return 0;
                    }

                    @Override
                    public void writeToParcel(Parcel dest, int flags) {

                    }

                    @Override
                    public void onUrisAvailable(String videoId, String videoTitle, SparseArray<YtFile> ytFiles) {
                        KLog.v(TAG, "onYoutube id: " + videoId);
                        KLog.v(TAG, "onYoutube title: " + videoTitle);
                        KLog.v(TAG, "onYoutube files: " + ytFiles.size() + "_" + ytFiles.get(22).getUrl());
                        KomicaManager.getInstance().startPlayerActivity(getContext(), videoTitle, ytFiles.get(22).getUrl());
                    }
                });
            } else {
                KomicaManager.getInstance().startPlayerActivity(v.getContext(), post.getTitle(), post.getVideoUrl());
            }
        }
    };

}
