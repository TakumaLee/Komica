package idv.kuma.app.komica.fragments;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.analytics.HitBuilders;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import idv.kuma.app.komica.R;
import idv.kuma.app.komica.configs.BundleKeyConfigs;
import idv.kuma.app.komica.entity.KPost;
import idv.kuma.app.komica.entity.KTitle;
import idv.kuma.app.komica.fragments.base.BaseFragment;
import idv.kuma.app.komica.http.NetworkCallback;
import idv.kuma.app.komica.http.OkHttpClientConnect;
import idv.kuma.app.komica.javascripts.JSInterface;
import idv.kuma.app.komica.manager.KomicaManager;
import idv.kuma.app.komica.utils.CrawlerUtils;
import idv.kuma.app.komica.utils.KLog;
import idv.kuma.app.komica.views.PostView;
import idv.kuma.app.komica.widgets.DividerItemDecoration;
import idv.kuma.app.komica.widgets.KLinearLayoutManager;
import tw.showang.recycleradaterbase.LoadMoreListener;
import tw.showang.recycleradaterbase.RecyclerAdapterBase;

/**
 * Created by TakumaLee on 2016/12/22.
 */

public class SectionDetailsFragment extends BaseFragment implements KomicaManager.OnUpdateConfigListener {
    private static final String TAG = SectionDetailsFragment.class.getSimpleName();

    private String url;
    private String title;
    private int webType;
    private String from;
    private Element formElem;

    private WebView webView;
    private ProgressBar reCaProgressBar;
    private RecyclerView recyclerView;
    private KLinearLayoutManager linearLayoutManager;
    private SectionDetailsAdapter adapter;

    private FloatingActionButton addPostFab;
    private MaterialDialog postDialog;
    private boolean isPosting = false;
    private int page = 0;
    private int pageCount = 0;
    private boolean hasAnotherPage = false;
    private boolean isLoadingFinished = false;

    private List<KPost> postList = Collections.emptyList();

    public static SectionDetailsFragment newInstance(String from, String url, String title, int webType) {
        SectionDetailsFragment fragment = new SectionDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(BundleKeyConfigs.KEY_WEB_FROM, from);
        bundle.putString(BundleKeyConfigs.KEY_WEB_URL, url);
        bundle.putString(BundleKeyConfigs.KEY_WEB_TITLE, title);
        bundle.putInt(BundleKeyConfigs.KEY_WEB_TYPE, webType);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        from = getArguments().getString(BundleKeyConfigs.KEY_WEB_FROM);
        url = getArguments().getString(BundleKeyConfigs.KEY_WEB_URL);
        title = getArguments().getString(BundleKeyConfigs.KEY_WEB_TITLE);
        webType = getArguments().getInt(BundleKeyConfigs.KEY_WEB_TYPE);
        postList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_section_details, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        loadSection();
        KomicaManager.getInstance().registerConfigUpdateListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        KomicaManager.getInstance().unRegisterConfigUpdateListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((LinearLayout) postDialog.getCustomView().findViewById(R.id.linearLayout_post_container)).removeAllViews();
        if (webView != null) {
            webView.stopLoading();
            webView = null;
        }
    }

    @Override
    public boolean isBackPressed() {
        if (null != postDialog) {
            postDialog.dismiss();
        }
        return super.isBackPressed();
    }

    private void initView() {
        initPostDialog();
        initWebView();
        recyclerView = findViewById(getView(), R.id.recyclerView_section_details);
        addPostFab = findViewById(getView(), R.id.fab_section_details_add_post);

        adapter = new SectionDetailsAdapter(postList);
        adapter.setLoadMoreListener(new LoadMoreListener() {
            @Override
            public void onLoadMore() {
                loadSection();
            }
        });
        linearLayoutManager = new KLinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST));
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        addPostFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("03. 互動")
                        .setAction(from + "_" + title + "_開始回文")
                        .setLabel(from + "_" + title)
                        .build());
                postDialog.show();
            }
        });

        switch (webType) {
            case KomicaManager.WebType.INTEGRATED:
                ((LinearLayout) postDialog.getCustomView().findViewById(R.id.linearLayout_post_container)).addView(reCaProgressBar);
                ((LinearLayout) postDialog.getCustomView().findViewById(R.id.linearLayout_post_container)).addView(webView);
//                postDialog = new MaterialDialog.Builder(getContext())
//                        .cancelable(false)
//                        .autoDismiss(false)
//                        .customView(webView, true)
//                        .build();
//                postDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
//                    @Override
//                    public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
//                        if (keyCode == KeyEvent.KEYCODE_BACK) {
//                            isBackPressed();
//                        }
//                        return true;
//                    }
//                });
                break;
            case KomicaManager.WebType.THREADS:
            case KomicaManager.WebType.NORMAL:
            case KomicaManager.WebType.THREADS_LIST:
            default:
                break;
        }

        getActivity().setTitle(title);
    }

    private void initPostDialog() {
        postDialog = new MaterialDialog.Builder(getContext())
                .customView(R.layout.layout_post, true)
                .positiveText(R.string.confirm)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        TextInputEditText commentEditText = (TextInputEditText) postDialog.getCustomView().findViewById(R.id.editText_post_comment);
                        String comment = commentEditText.getText().toString().replaceAll("\\n", "\\\\n");
                        KLog.v(TAG, comment);
                        switch (webType) {
                            case KomicaManager.WebType.INTEGRATED:
                                String submitStr = "javascript:" + "document.getElementsByTagName('form')[0].submit();";
                                String commentStr = "javascript:" + "document.getElementsByTagName('textarea')[0].value='" + comment + "';";
//                                            String checkStr = "javascript:" + "parent.frames['0'].document.getElementById('recaptcha-anchor').setAttribute('aria-checked', true);";
//                                String checkStr = "javascript:" + "parent.frames['0'].document.getElementById('recaptcha-anchor').click();";
//                                            String checkStr = "javascript:" + "parent.frames['0'].contentWindow.postMessage(" +
//                                                    "document.getElementById('recaptcha-anchor').setAttribute('aria-checked', true)," +
//                                                    Uri.parse(webView.getUrl()).getHost() +
//                                                    ")";
                                webView.loadUrl(commentStr + submitStr);
                                break;
                            case KomicaManager.WebType.THREADS:
                            case KomicaManager.WebType.NORMAL:
                            case KomicaManager.WebType.THREADS_LIST:
                            default:
                                submitStr = "javascript:" + "document.getElementById('" + formElem.id() + "').submit();";
                                webView.loadUrl("javascript:" + "document.getElementById('fcom').value='" + comment + "';" + submitStr);
                                break;
                        }
                        isPosting = true;
                        Toast.makeText(getContext(), R.string.message_please_wait_for_replying, Toast.LENGTH_LONG).show();
                        tracker.send(new HitBuilders.EventBuilder()
                                .setCategory("03. 互動")
                                .setAction(from + "_" + title + "_回文發佈")
                                .setLabel(from + "_" + title)
                                .build());
                    }
                })
                .build();
    }

    private void initWebView() {
        reCaProgressBar = new ProgressBar(getContext());
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
            }
        }), "HtmlViewer");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (null != reCaProgressBar) {
                    reCaProgressBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageFinished(final WebView view, String url) {
                super.onPageFinished(view, url);
                KLog.v(TAG, "onPageFinished: " + url);
                view.loadUrl("javascript:(function() {" +
                        "var items = document.getElementsByTagName('p');" +
                        "for (i = 0; i < items.length; i++) {" +
                        "items[i].style.display='none';" +
                        "}" +
                        "document.getElementsByTagName('a')[0].style.display = 'none';" +
                        "document.getElementsByTagName('center')[0].style.display = 'none';" +
                        "document.getElementsByTagName('form')[1].style.display = 'none';" +
                        "function hasClass(ele,cls) {" +
                        "     return ele.getElementsByClassName(cls).length > 0;" +
                        "}" +
                        "var trs = document.getElementsByTagName('form')[0].getElementsByTagName('tbody')[0].children;" +
                        "for (i = 0; i < trs.length; i++) {" +
                        "console.log(trs.length);" +
                        "if (hasClass(trs[i], 'g-recaptcha')) {" +
                        "console.log('Has g-recaptcha');" +
                        "trs[i].getElementsByTagName('td')[0].style.display = 'none';" +
                        "} else {" +
                        "console.log('No g-recaptcha');" +
                        "trs[i].style.display='none';" +
                        "}" +
                        "}" +
                        "}) ()");
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
//                view.loadUrl("javascript:window.HtmlViewer.onResponse" +
//                        "(document.documentElement.outerHTML);");
                isLoadingFinished = true;
                if (isPosting) {
                    isPosting = false;
                    if (null == webView) {
                        return;
                    }
                    webView.post(new Runnable() {
                        @Override
                        public void run() {
                            webView.stopLoading();
                        }
                    });

                    loadSection();
                }
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
                if (isLoadingFinished) {
                    reCaProgressBar.setVisibility(View.GONE);
                    webView.setVisibility(View.VISIBLE);
//                    view.loadUrl("javascript:window.HtmlViewer.onResponse" +
//                            "(document.documentElement.outerHTML);");
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

    private void loadSection() {
        switch (webType) {
            case KomicaManager.WebType.INTEGRATED:
                break;
            case KomicaManager.WebType.THREADS_LIST:
            case KomicaManager.WebType.THREADS:
            case KomicaManager.WebType.NORMAL:
            default:
                url = (url.contains("page_num") ? url.substring(0, url.lastIndexOf("&")) : url) + "&page_num=" + page;
                break;
        }

        if (null == getActivity()) {
            return;
        }
        OkHttpClientConnect.excuteAutoGet(url, new NetworkCallback() {
            @Override
            public void onFailure(IOException e) {

            }

            @Override
            public void onResponse(int responseCode, String result) {
                if (null == getActivity()) {
                    return;
                }
                if (result.contains("ReDirUrl")) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String reDirUrl = url.substring(0, url.lastIndexOf("/") + 1) + "m" + url.substring(url.lastIndexOf("/"));
                            webView.stopLoading();
                            webView.loadUrl(reDirUrl);
                        }
                    });

                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            webView.loadUrl(url);
                        }
                    });
                }
                if (page == 0) {
                    postList.clear();
                }
                Document document = Jsoup.parse(result);
                formElem = document.getElementsByTag("form").first();
                List<KTitle> headList = CrawlerUtils.getPostList(document, url, webType);
                KTitle head = headList.size() > 0 ? headList.get(0) : null;
                //TODO no data to load.
                if (null == head) {
                    toastNoMoreData();
                    return;
                }
                if (page == 0) {
                    postList.add(head);
                }
                postList.addAll(head.getReplyList());
                notifyAdapter();
                hasAnotherPage = !document.getElementsByClass("page_switch").isEmpty() && page != pageCount - 1;
                if (hasAnotherPage) {
                    pageCount = document.getElementsByClass("page_switch").first().getElementsByClass("link").size() + 1;
                    adapter.setLoadMoreEnable(page < pageCount - 1);
                }
            }
        });
    }

    private void toastNoMoreData() {
        if (null == getActivity()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), R.string.message_no_more_data, Toast.LENGTH_LONG).show();
                    adapter.setLoadMoreEnable(false);
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
                    if (hasAnotherPage) {
                        page++;
                    }
                }
            });
        }
    }

    @Override
    public void onConfigUpdated() {
        adapter.notifyDataSetChanged();
    }

    class SectionDetailsAdapter extends RecyclerAdapterBase {

        private List<KPost> postList;

        protected SectionDetailsAdapter(List<KPost> dataList) {
            super(dataList);
            this.postList = dataList;
        }

        @Override
        protected RecyclerView.ViewHolder onCreateItemViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
            PostView postView = new PostView(parent.getContext());
            postView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new SectionDetailsViewHolder(postView);
        }

        @Override
        protected void onBindItemViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            SectionDetailsViewHolder holder = (SectionDetailsViewHolder) viewHolder;
            KLog.v(TAG, String.valueOf(getItemPosition(holder)));
            KPost post = postList.get(getItemPosition(holder));
            ((PostView) holder.itemView).setPost(post);
            ((PostView) holder.itemView).notifyDataSetChanged();
            if (position == 0) {
                holder.itemView.setBackgroundResource(R.color.white);
            } else {
                holder.itemView.setBackgroundResource(R.color.md_blue_50);
            }

        }

    }

    class SectionDetailsViewHolder extends RecyclerView.ViewHolder {

        public SectionDetailsViewHolder(View itemView) {
            super(itemView);
        }
    }
}
