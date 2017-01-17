package idv.kuma.app.komica.fragments;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.analytics.HitBuilders;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import idv.kuma.app.komica.R;
import idv.kuma.app.komica.activities.SectionDetailsActivity;
import idv.kuma.app.komica.adapters.LoadMoreViewHolder;
import idv.kuma.app.komica.configs.BundleKeyConfigs;
import idv.kuma.app.komica.entity.KReply;
import idv.kuma.app.komica.entity.KTitle;
import idv.kuma.app.komica.fragments.base.BaseFragment;
import idv.kuma.app.komica.http.NetworkCallback;
import idv.kuma.app.komica.http.OkHttpClientConnect;
import idv.kuma.app.komica.manager.FacebookManager;
import idv.kuma.app.komica.manager.KomicaAccountManager;
import idv.kuma.app.komica.manager.KomicaManager;
import idv.kuma.app.komica.manager.ThirdPartyManager;
import idv.kuma.app.komica.utils.CrawlerUtils;
import idv.kuma.app.komica.utils.KLog;
import idv.kuma.app.komica.views.PostView;
import idv.kuma.app.komica.widgets.DividerItemDecoration;
import idv.kuma.app.komica.widgets.KLinearLayoutManager;
import idv.kuma.app.komica.widgets.MutableLinkMovementMethod;
import tw.showang.recycleradaterbase.LoadMoreListener;
import tw.showang.recycleradaterbase.RecyclerAdapterBase;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.text.Html.FROM_HTML_MODE_LEGACY;

/**
 * Created by TakumaLee on 2016/12/10.
 */

public class SectionPreviewFragment extends BaseFragment implements FacebookManager.OnGetProfileListener,
        FacebookManager.OnLogoutListener, KomicaManager.OnUpdateConfigListener {
    private static final String TAG = SectionPreviewFragment.class.getSimpleName();

    private String indexUrl;
    private String url;
    private String title;
    private int webType;

    private RecyclerView recyclerView;
    private KLinearLayoutManager linearLayoutManager;
    private SectionPreviewAdapter adapter;

    private int page = 1;
    private int pageCount = 1;

    private boolean isPosting = false;

    private List<KTitle> titlePostList = Collections.emptyList();

    public static SectionPreviewFragment newInstance(String url) {
        return newInstance(url, KomicaManager.WebType.NORMAL);
    }

    public static SectionPreviewFragment newInstance(String url, int webType) {
        SectionPreviewFragment fragment = new SectionPreviewFragment();
        Bundle bundle = new Bundle();
        bundle.putString(BundleKeyConfigs.KEY_WEB_URL, url);
        bundle.putInt(BundleKeyConfigs.KEY_WEB_TYPE, webType);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        url = getArguments().getString(BundleKeyConfigs.KEY_WEB_URL);
        indexUrl = url;
        webType = getArguments().getInt(BundleKeyConfigs.KEY_WEB_TYPE);
        titlePostList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_section_preview, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        loadSection();
        ThirdPartyManager.getInstance().registerProfileListener(this);
        ThirdPartyManager.getInstance().registerLogoutListener(this);
        KomicaManager.getInstance().registerConfigUpdateListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_section_preview, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                titlePostList.clear();
                recyclerView.scrollToPosition(0);
                url = indexUrl;
                page = 1;
                loadSection();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ThirdPartyManager.getInstance().unRegisterProfileListener(this);
        ThirdPartyManager.getInstance().unRegisterLogoutListener(this);
        KomicaManager.getInstance().unRegisterConfigUpdateListener(this);
    }

    private void initView() {
        recyclerView = findViewById(getView(), R.id.recyclerView_section_preview);

        adapter = new SectionPreviewAdapter(titlePostList);
        adapter.setLoadMoreEnable(true);
        adapter.setLoadMoreListener(new LoadMoreListener() {
            @Override
            public void onLoadMore() {
                url = url.substring(0, url.lastIndexOf("/") + 1) + page + ".htm";
                loadSection();
                page++;
            }
        });

        linearLayoutManager = new KLinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST));
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    public void loadNewSection(int webType, String url) {
        KomicaManager.getInstance().clearCache();
        recyclerView.scrollToPosition(0);
        this.webType = webType;
        this.url = url;
        titlePostList.clear();
        loadSection();
    }

    private void loadSection() {
        switch (webType) {
            case KomicaManager.WebType.NORMAL:
                loadNormalSection();
                break;
            case KomicaManager.WebType.INTEGRATED:
                loadIntegratedSection();
                break;
        }
    }

    private void loadIntegratedSection() {
        OkHttpClientConnect.excuteAutoGet(url, new NetworkCallback() {
            @Override
            public void onFailure(IOException e) {

            }

            @Override
            public void onResponse(int responseCode, String result) {
                Document document = Jsoup.parse(result);
                title = document.getElementsByTag("title").text();
                notifyTitle();
                int previousSize = titlePostList.size();
                titlePostList.addAll(CrawlerUtils.getIntegratedPostList(document, url));
                notifyAdapter(previousSize);

                pageCount = document.getElementsByAttributeValue("border", "1").first().select("a").size();
            }
        });
    }

    private void loadNormalSection() {
        OkHttpClientConnect.excuteAutoGet(url, new NetworkCallback() {
            @Override
            public void onFailure(IOException e) {

            }

            @Override
            public void onResponse(int responseCode, String result) {
                Document document = Jsoup.parse(result);
                title = document.getElementsByTag("title").text();
                notifyTitle();
                int previousSize = titlePostList.size();
                titlePostList.addAll(CrawlerUtils.getPostList(document, url, webType));
                notifyAdapter(previousSize);
                Element pageSwitch = document.getElementById("page_switch");
                if (null == pageSwitch) {
                    pageSwitch = document.getElementsByClass("page_switch").first();
                }
                if (null != pageSwitch.select("a")) {
                    pageCount = pageSwitch.select("a").size();
                } else {
                    pageCount = pageSwitch.getElementsByAttributeValueContaining("class", "link").size();
                }
            }
        });
    }

    private void notifyTitle() {
        if (null != getActivity()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getActivity().setTitle(title);
                    tracker.setScreenName(title);
                    tracker.send(new HitBuilders.ScreenViewBuilder().build());
                }
            });
        }
    }

    private void notifyAdapter(final int previousSize) {
        if (null != getActivity()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyItemRangeInserted(previousSize, titlePostList.size());
                    if (page > pageCount) {
                        adapter.setLoadMoreEnable(false);
                    }
                }
            });
        }
    }

    @Override
    public void onGetProfile() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onSuccess() {

    }

    @Override
    public void onFail() {

    }

    @Override
    public void onLogout() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onConfigUpdated() {
        adapter.notifyDataSetChanged();
    }

    class SectionPreviewAdapter extends RecyclerAdapterBase {

        private List<KTitle> titleList;

        protected SectionPreviewAdapter(List<KTitle> dataList) {
            super(dataList);
            this.titleList = dataList;
        }

        @Override
        protected RecyclerView.ViewHolder onCreateItemViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
            return new SectionPreViewHolder(inflater.inflate(R.layout.adapter_section_list_item, parent, false));
        }

        @Override
        protected void onBindItemViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            ActivityManager activityManager =  (ActivityManager) getContext().getSystemService(ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memoryInfo);
            double max = Runtime.getRuntime().maxMemory() / 1048576L;
            double free = Runtime.getRuntime().freeMemory() / 1048576L;
            double total = Runtime.getRuntime().totalMemory() / 1048576L;
            KLog.i(TAG, "memory max: " + max);
            KLog.i(TAG, "memory free: " + free);
            KLog.i(TAG, "memory total: " + total);
            if (free + total > max / 2) {
                KLog.w(TAG, "Dangerous for OOM, Clear Memory.");
                System.gc();
            }
            SectionPreViewHolder holder = (SectionPreViewHolder) viewHolder;
            KLog.v(TAG, String.valueOf(position));
            final KTitle head = titleList.get(position);
            holder.postIdTextView.setText("No. " + head.getId());
            holder.postTitleTextView.setText(head.getTitle());
            holder.postWarnTextView.setText(head.getWarnText());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                holder.postQuoteTextView.setText(Html.fromHtml(head.getQuote(), FROM_HTML_MODE_LEGACY));
            } else {
                holder.postQuoteTextView.setText(Html.fromHtml(head.getQuote()));
            }
            MutableLinkMovementMethod movementMethod = new MutableLinkMovementMethod();
            movementMethod.setOnUrlClickListener(new MutableLinkMovementMethod.OnUrlClickListener() {
                @Override
                public void onUrlClick(TextView widget, Uri uri) {
//                    Intent intent = new Intent(getContext(), WebViewActivity.class);
//                    intent.putExtra(BundleKeyConfigs.KEY_WEB_URL, uri.toString());
//                    startActivity(intent);
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    builder.setToolbarColor(ContextCompat.getColor(getContext(), R.color.primary));
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.launchUrl(getContext(), uri);
                }
            });
            holder.postQuoteTextView.setMovementMethod(movementMethod);
            holder.moreBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), SectionDetailsActivity.class);
                    intent.putExtra(BundleKeyConfigs.KEY_WEB_URL, head.getDetailLink());
                    intent.putExtra(BundleKeyConfigs.KEY_WEB_TITLE, head.getTitle());
                    intent.putExtra(BundleKeyConfigs.KEY_WEB_TYPE, webType);
                    intent.putExtra(BundleKeyConfigs.KEY_WEB_FROM, title);
                    startActivity(intent);
                }
            });
            if (head.hasVideo() || head.hasImage()) {
                if (KomicaManager.getInstance().isSwitchLogin() && KomicaAccountManager.getInstance().isLogin()) {
                    holder.postThumbImageView.setVisibility(View.VISIBLE);
                    holder.postImgErrMsgTextView.setVisibility(View.GONE);
                    Glide.with(getContext()).load(head.getPostImageList().get(0).getImageUrl()).into(holder.postThumbImageView);
                } else {
                    holder.postThumbImageView.setVisibility(View.GONE);
                    holder.postImgErrMsgTextView.setVisibility(View.VISIBLE);
                }
            } else {
                holder.postThumbImageView.setVisibility(View.GONE);
                holder.postImgErrMsgTextView.setVisibility(View.GONE);
            }
            if (titleList.get(position).getReplyList().size() > 0) {
                holder.replyLinearLayout.setVisibility(View.VISIBLE);
                holder.replyLinearLayout.removeAllViews();
                for (KReply reply : titleList.get(position).getReplyList()) {
                    PostView postView = new PostView(viewHolder.itemView.getContext());
                    postView.setBackgroundResource(R.color.md_blue_50);
                    postView.setPost(reply);
                    postView.setLinkMovementMethod(movementMethod);
                    postView.notifyDataSetChanged();
                    holder.replyLinearLayout.addView(postView);
                }
            } else {
                holder.replyLinearLayout.setVisibility(View.GONE);
            }
        }

        @Override
        protected RecyclerView.ViewHolder onCreateLoadMoreViewHolder(LayoutInflater from, ViewGroup parent) {
            return new LoadMoreViewHolder(from.inflate(R.layout.item_loadmore, parent, false));
        }

        @Override
        protected void onBindLoadMoreViewHolder(RecyclerView.ViewHolder viewHolder, boolean isLoadMoreFailed) {
            super.onBindLoadMoreViewHolder(viewHolder, isLoadMoreFailed);
            LoadMoreViewHolder vh = (LoadMoreViewHolder) viewHolder;
            vh.progressBar.setVisibility(isLoadMoreFailed ? View.GONE : View.VISIBLE);
            vh.failText.setVisibility(isLoadMoreFailed ? View.VISIBLE : View.GONE);
        }

    }

    class SectionPreViewHolder extends RecyclerView.ViewHolder {

        TextView postIdTextView;
        TextView postTitleTextView;
        TextView postQuoteTextView;
        ImageView postThumbImageView;
        TextView postImgErrMsgTextView;
        LinearLayout postImgListContainer;

        TextView postWarnTextView;
        Button moreBtn;
        LinearLayout replyLinearLayout;

        public SectionPreViewHolder(View itemView) {
            super(itemView);
            LayoutInflater inflate = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            postImgListContainer = (LinearLayout) inflate.inflate(R.layout.layout_section_post_img_list, (ViewGroup) itemView);
            postIdTextView = findViewById(itemView, R.id.textView_section_post_id);
            postTitleTextView = findViewById(itemView, R.id.textView_section_post_title);
            postQuoteTextView = findViewById(itemView, R.id.textView_section_post_quote);
            postQuoteTextView.setMovementMethod(LinkMovementMethod.getInstance());
            postImgErrMsgTextView = findViewById(itemView,R.id.textView_section_post_message);
            postThumbImageView = findViewById(itemView, R.id.imageView_section_post_thumb);
            postWarnTextView = findViewById(itemView, R.id.textView_section_preview_warnText);
            moreBtn = findViewById(itemView, R.id.button_section_preview_more);
            replyLinearLayout = findViewById(itemView, R.id.linearLayout_section_preview_replyContainer);

            postThumbImageView.setVisibility(KomicaManager.getInstance().isSwitchLogin() && KomicaAccountManager.getInstance().isLogin() ? View.VISIBLE : View.GONE);
            postImgErrMsgTextView.setVisibility(KomicaManager.getInstance().isSwitchLogin() && KomicaAccountManager.getInstance().isLogin() ? View.GONE : View.VISIBLE);
            postImgErrMsgTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("00. 登入追蹤")
                            .setLabel("圖片點擊登入")
                            .setAction("圖片點擊登入")
                            .build());
                    ThirdPartyManager.getInstance().loginFacebook((Activity) getContext());
                }
            });
        }
    }

}
