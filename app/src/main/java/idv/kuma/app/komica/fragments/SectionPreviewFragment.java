package idv.kuma.app.komica.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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

import at.huber.youtubeExtractor.OnYoutubeParseListener;
import at.huber.youtubeExtractor.YtFile;
import idv.kuma.app.komica.R;
import idv.kuma.app.komica.activities.SectionDetailsActivity;
import idv.kuma.app.komica.adapters.LoadMoreViewHolder;
import idv.kuma.app.komica.configs.BundleKeyConfigs;
import idv.kuma.app.komica.entity.KPost;
import idv.kuma.app.komica.entity.KReply;
import idv.kuma.app.komica.entity.KTitle;
import idv.kuma.app.komica.fragments.base.BaseFragment;
import idv.kuma.app.komica.http.NetworkCallback;
import idv.kuma.app.komica.http.OkHttpClientConnect;
import idv.kuma.app.komica.javascripts.JSInterface;
import idv.kuma.app.komica.manager.FacebookManager;
import idv.kuma.app.komica.manager.KomicaAccountManager;
import idv.kuma.app.komica.manager.KomicaManager;
import idv.kuma.app.komica.manager.ThirdPartyManager;
import idv.kuma.app.komica.manager.YoutubeManager;
import idv.kuma.app.komica.utils.CrawlerUtils;
import idv.kuma.app.komica.utils.KLog;
import idv.kuma.app.komica.views.CustomTabActivityHelper;
import idv.kuma.app.komica.views.PostView;
import idv.kuma.app.komica.views.WebViewFallback;
import idv.kuma.app.komica.widgets.DividerItemDecoration;
import idv.kuma.app.komica.widgets.KLinearLayoutManager;
import idv.kuma.app.komica.widgets.MutableLinkMovementMethod;
import tw.showang.recycleradaterbase.LoadMoreListener;
import tw.showang.recycleradaterbase.RecyclerAdapterBase;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.text.Html.FROM_HTML_MODE_LEGACY;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

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

    private WebView webView;
    private RecyclerView recyclerView;
    private KLinearLayoutManager linearLayoutManager;
    private SectionPreviewAdapter adapter;

    private int page = 0;
    private int pageCount = 1;

    private String link = "";
    private boolean isLinkPage = false;

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
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("04. Menu選項追蹤")
                        .setAction("更新頁面資訊_" + title)
                        .setLabel("更新頁面資訊_" + title)
                        .build());
                titlePostList.clear();
                recyclerView.scrollToPosition(0);
                loadNewSection(webType, indexUrl);
                break;
            case R.id.action_browser:
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("04. Menu選項追蹤")
                        .setLabel("使用瀏覽器_" + title)
                        .setAction("使用瀏覽器_" + title)
                        .build());
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                builder.setToolbarColor(ContextCompat.getColor(getContext(), R.color.primary));
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(getContext(), Uri.parse(url));
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
        if (webView != null) {
            webView.stopLoading();
            webView = null;
        }
    }

    private void initView() {
        recyclerView = findViewById(getView(), R.id.recyclerView_section_preview);

        adapter = new SectionPreviewAdapter(titlePostList);
        adapter.setLoadMoreEnable(true);
        adapter.setLoadMoreListener(new LoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (isLinkPage) {
                    url = url.substring(0, url.lastIndexOf("/") + 1) + link.replaceAll("[0-9]", String.valueOf(page));
                } else {
                    url = url.substring(0, url.lastIndexOf("/") + 1) + page + link;
                }
                loadSection();
            }
        });

        linearLayoutManager = new KLinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST));
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

    }

    private void initWebView() {
        webView = new WebView(getContext());
        webView.setVisibility(View.GONE);
        webView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        webView.getSettings().setUserAgentString("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0");
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new JSInterface(new JSInterface.OnCallListener() {
            @Override
            public void onResponse(String result) {
                // TODO get real html
                KLog.v(TAG, "onJavaScript onResponse");
                Document document = Jsoup.parse(result);
                if (document.getElementById("main") != null) {
                    url = document.getElementById("main").attr("src");
                    indexUrl = url;
                    loadNormalSection();
                }
            }
        }), "HtmlViewer");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(final WebView view, String url) {
                super.onPageFinished(view, url);
                KLog.v(TAG, "onPageFinished: " + url);
//                view.loadUrl("javascript:(function() {" +
//                        "var items = document.getElementsByTagName('p');" +
//                        "for (i = 0; i < items.length; i++) {" +
//                        "items[i].style.display='none';" +
//                        "}" +
//                        "document.getElementsByTagName('a')[0].style.display = 'none';" +
//                        "document.getElementsByTagName('center')[0].style.display = 'none';" +
//                        "document.getElementsByTagName('form')[1].style.display = 'none';" +
//                        "function hasClass(ele,cls) {" +
//                        "     return ele.getElementsByClassName(cls).length > 0;" +
//                        "}" +
//                        "var trs = document.getElementsByTagName('form')[0].getElementsByTagName('tbody')[0].children;" +
//                        "for (i = 0; i < trs.length; i++) {" +
//                        "console.log(trs.length);" +
//                        "if (hasClass(trs[i], 'g-recaptcha')) {" +
//                        "console.log('Has g-recaptcha');" +
//                        "trs[i].getElementsByTagName('td')[0].style.display = 'none';" +
//                        "} else {" +
//                        "console.log('No g-recaptcha');" +
//                        "trs[i].style.display='none';" +
//                        "}" +
//                        "}" +
//                        "}) ()");
//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                            WebView formWebView = new WebView(getContext());
//                            formWebView.getSettings().setJavaScriptEnabled(true);
//                        webView.loadData(element.toString(), "text/html", "");
//                        webView.removeJavascriptInterface("HtmlViewer");
//                            formWebView.loadData(element.toString(), "text/html", "");
//                        addPostFab.setVisibility(View.VISIBLE);
//                    }
//                });
//                view.loadUrl("javascript:var con = document.getElementsByTagName('page-content'); " +"con[0].style.display = 'none'; ");
//                view.loadUrl("javascript:window.HtmlViewer.onResponse" +
//                        "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
                view.loadUrl("javascript:window.HtmlViewer.onResponse" +
                        "(document.documentElement.outerHTML);");

//                if (isPosting) {
//                    isPosting = false;
//                    if (null == webView) {
//                        return;
//                    }
//                    webView.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            webView.stopLoading();
//                        }
//                    });
//
//                    loadSection();
//                }
            }

            @Override
            public void onPageCommitVisible(WebView view, String url) {
                super.onPageCommitVisible(view, url);
                KLog.v(TAG, "onPageCommitVisible");
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
                KLog.v(TAG, "onLoadResource");
                if (null == webView) {
                    return;
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                KLog.v(TAG, "shouldOverrideUrlLoading Url: " + url);
                return super.shouldOverrideUrlLoading(view, url);
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                KLog.v(TAG, "shouldOverrideUrlLoading Url: " + request.getUrl().getPath());
                return super.shouldOverrideUrlLoading(view, request);
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                KLog.v(TAG, "onJsConfirm: " + message);
                return super.onJsConfirm(view, url, message, result);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                KLog.v(TAG, "onProgressChanged");
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                KLog.v(TAG, "onJsAlert: " + message);
                return super.onJsAlert(view, url, message, result);
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                KLog.v(TAG, "onJsPrompt");
                return super.onJsPrompt(view, url, message, defaultValue, result);
            }

            @Override
            public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
                KLog.v(TAG, "onJsBeforeUnload");
                return super.onJsBeforeUnload(view, url, message, result);
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                KLog.v(TAG, "onConsoleMessage");
                return super.onConsoleMessage(consoleMessage);
            }
        });
    }

    public void loadNewSection(int webType, String url) {
        page = 0;
        pageCount = 1;
        link = "";
        adapter.setLoadMoreEnable(true);
        KomicaManager.getInstance().clearCache();
        recyclerView.scrollToPosition(0);
        this.webType = webType;
        this.indexUrl = url;
        this.url = url;
        titlePostList.clear();
        loadSection();
    }

    private void loadSection() {
        switch (webType) {
            case KomicaManager.WebType.THREADS_LIST:
//                loadWeb();
//                break;
            case KomicaManager.WebType.THREADS:
                // TODO need webview to load {backquote}
            case KomicaManager.WebType.NORMAL:
                loadNormalSection();
                break;
            case KomicaManager.WebType.INTEGRATED:
                loadIntegratedSection();
                break;
        }
    }
    private void loadWeb() {
        if (null == webView) {
            initWebView();
        }
        webView.loadUrl(url);

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

                titlePostList.addAll(CrawlerUtils.getIntegratedPostList(document, url));
                notifyAdapter();

                Element pageElem = document.getElementsByAttributeValue("border", "1").first();
                if (pageElem == null) {
                    return;
                }
                pageCount = pageElem.select("a").size();
                if (pageElem.select("a").isEmpty()) {
                    return;
                }
                String linkTmp = pageElem.select("a").first().attr("href");
                if ("".equals(link)) {
                    link = linkTmp.replaceAll("[0-9]", "");
                }
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
                titlePostList.addAll(CrawlerUtils.getPostList(document, url, webType));
                notifyAdapter();
                Element pageSwitch = document.getElementById("page_switch");
                if (null == pageSwitch) {
                    pageSwitch = document.getElementsByClass("page_switch").first();
                }
                if (null == pageSwitch) {
                    pageSwitch = document.getElementById("page_switch");
                }
                // TODO has two mode, please watch out it.
                if (pageCount <= 1 && null != pageSwitch.select("a") && !pageSwitch.getElementsByTag("table").isEmpty()) {
                    isLinkPage = false;
                    pageCount = pageSwitch.select("a").size();
                    if ("".equals(link) && pageSwitch.select("a").attr("href").contains("?")) {
                        link = pageSwitch.select("a").attr("href").replaceAll("[0-9]", "");
                    }
                } else if (pageCount <= 1 && pageSwitch.getElementsByTag("table").isEmpty()) {
                    isLinkPage = true;
                    pageCount = pageSwitch.getElementsByAttributeValue("class", "link ").size();
                    if ("".equals(link)) {
                        Element pageStartLinkElem = pageSwitch.getElementsByAttributeValue("class", "link ").first();
                        link = pageStartLinkElem.select("a").attr("href");
                    }
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

    private void notifyAdapter() {
        if (null != getActivity()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                    page++;
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
                    final List<ResolveInfo> customTabsApps = getActivity().getPackageManager().queryIntentActivities(customTabsIntent.intent, 0);

                    if (customTabsApps.size() > 0) {
                        CustomTabActivityHelper.openCustomTab(getActivity(), customTabsIntent, uri, new WebViewFallback());
                    } else {
                        // Chrome not installed. Display a toast or something to notify the user
                        customTabsIntent.launchUrl(getContext(), uri);
                    }
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
            if ((head.hasVideo() || head.hasImage()) && head.getPostImageList().size() > 0) {
                if (KomicaManager.getInstance().isSwitchLogin() && KomicaAccountManager.getInstance().isLogin()) {
                    holder.postThumbImageView.setVisibility(VISIBLE);
                    holder.postImgErrMsgTextView.setVisibility(GONE);
                    Glide.with(getContext()).load(head.getPostImageList().get(0).getImageUrl()).into(holder.postThumbImageView);
                } else {
                    holder.postThumbImageView.setVisibility(GONE);
                    holder.postImgErrMsgTextView.setVisibility(VISIBLE);
                }
            } else {
                holder.postThumbImageView.setVisibility(GONE);
                holder.postImgErrMsgTextView.setVisibility(GONE);
            }
            if (titleList.get(position).getReplyList().size() > 0) {
                holder.replyLinearLayout.setVisibility(VISIBLE);
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
                holder.replyLinearLayout.setVisibility(GONE);
            }
            holder.notifyVideo(head);
        }

        @Override
        protected RecyclerView.ViewHolder onCreateLoadMoreViewHolder(LayoutInflater from, ViewGroup parent) {
            return new LoadMoreViewHolder(from.inflate(R.layout.item_loadmore, parent, false));
        }

        @Override
        protected void onBindLoadMoreViewHolder(RecyclerView.ViewHolder viewHolder, boolean isLoadMoreFailed) {
            super.onBindLoadMoreViewHolder(viewHolder, isLoadMoreFailed);
            LoadMoreViewHolder vh = (LoadMoreViewHolder) viewHolder;
            vh.progressBar.setVisibility(isLoadMoreFailed ? GONE : VISIBLE);
            vh.failText.setVisibility(isLoadMoreFailed ? VISIBLE : GONE);
        }

    }

    class SectionPreViewHolder extends RecyclerView.ViewHolder {

        KPost post;

        TextView postIdTextView;
        TextView postTitleTextView;
        TextView postQuoteTextView;
        ImageView postThumbImageView;
        TextView postImgErrMsgTextView;
        LinearLayout postImgListContainer;

        TextView postWarnTextView;
        Button moreBtn;
        LinearLayout replyLinearLayout;

        RelativeLayout postVideoContainer;
        TextView postVideoTitleTextView;

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

            postThumbImageView.setVisibility(KomicaManager.getInstance().isSwitchLogin() && KomicaAccountManager.getInstance().isLogin() ? VISIBLE : GONE);
            postImgErrMsgTextView.setVisibility(KomicaManager.getInstance().isSwitchLogin() && KomicaAccountManager.getInstance().isLogin() ? GONE : VISIBLE);
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

            postVideoContainer = findViewById(itemView, R.id.relativeLayout_section_post_video_content_container);
            postVideoTitleTextView = findViewById(itemView, R.id.textView_section_post_video_content_title);
        }

        private void notifyVideo(KPost post) {
            this.post = post;
            if (KomicaAccountManager.getInstance().isLogin() && post.hasVideo()) {
                postVideoContainer.setVisibility(VISIBLE);
                postVideoContainer.setOnClickListener(onVideoClickListener);
            } else {
                postVideoContainer.setVisibility(GONE);
                postVideoContainer.setOnClickListener(null);
            }
        }

        private View.OnClickListener onVideoClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (post.getVideoUrl().contains("youtube") || post.getVideoUrl().contains("youtu.be")) {
                    YoutubeManager.getInstance().startParseYoutubeUrl(getActivity(), post.getVideoUrl(), new OnYoutubeParseListener() {
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

}
