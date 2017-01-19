package idv.kuma.app.komica.fragments;

import android.lib.recaptcha.ReCaptcha;
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
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.analytics.HitBuilders;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
    private Elements inputElements;
    private String formUrl;
    private String commentBoxKey;

    private WebView webView;
    private RecyclerView recyclerView;
    private KLinearLayoutManager linearLayoutManager;
    private SectionDetailsAdapter adapter;

    private FloatingActionButton addPostFab;
    private MaterialDialog postDialog;
    private boolean isPosting = false;
    private int page = 0;
    private int pageCount = 0;
    private boolean hasAnotherPage = false;

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

    private void initView() {
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
                if (null == postDialog) {
                    postDialog = new MaterialDialog.Builder(view.getContext())
                            .customView(R.layout.layout_post, true)
                            .positiveText(R.string.confirm)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    TextInputEditText commentEditText = (TextInputEditText) postDialog.getCustomView().findViewById(R.id.editText_post_comment);
                                    switch (webType) {
                                        case KomicaManager.WebType.INTEGRATED:
                                            String submitStr = "javascript:" + "document.getElementsByTagName('form')[0].submit();";
                                            String commentStr = "javascript:" + "document.getElementsByTagName('textarea')[0].value='" + commentEditText.getText().toString().replaceAll("/\\n|\\r\\n/g","<br />") + "';";
                                            String checkStr = "javascript:" + "document.getElementById('recaptcha-anchor').setAttribute('aria-checked', true);";
                                            webView.loadUrl(commentStr + checkStr + submitStr);
                                            break;
                                        case KomicaManager.WebType.NORMAL:
                                        default:
                                            submitStr = "javascript:" + "document.getElementById('" + formElem.id() + "').submit();";
                                            webView.loadUrl("javascript:" + "document.getElementById('fcom').value='" + commentEditText.getText().toString() + "';" + submitStr);
                                            isPosting = true;
                                            Toast.makeText(getContext(), R.string.message_please_wait_for_replying, Toast.LENGTH_LONG).show();
                                            break;
                                    }
                                    tracker.send(new HitBuilders.EventBuilder()
                                            .setCategory("03. 互動")
                                            .setAction(from + "_" + title + "_回文發佈")
                                            .setLabel(from + "_" + title)
                                            .build());
//                                    loadSection();
//                                    webView.loadUrl(submitStr);
//                                    MultipartBody.Builder requestBody = new MultipartBody.Builder()
//                                            .setType(MultipartBody.FORM);
//                                    for (Element element : inputElements) {
//                                        if ("sendbtn".equals(element.attr("name"))) {
//                                            continue;
//                                        }
//                                        if ("reply".equals(element.attr("name"))) {
//                                            continue;
//                                        }
//                                        if ("noimg".equals(element.attr("name"))) {
//                                            continue;
//                                        }
//                                        if ("js".equals(element.attr("name"))) {
//                                            requestBody.addFormDataPart(element.attr("name"), "js");
//                                        }
//                                        requestBody.addFormDataPart(element.attr("name"), element.val());
//                                    }
//                                    for (Element element : formElem.getElementsByTag("textarea")) {
//                                        if ("fcom".equals(element.id())) {
//                                            requestBody.addFormDataPart(commentBoxKey, commentEditText.getText().toString());
//                                        } else {
//                                            requestBody.addFormDataPart(element.attr("name"), element.text());
//                                        }
//                                    }
//                                    Request request = new Request.Builder()
//                                            .url(formUrl)
//                                            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
//                                            .addHeader("Content-Type", "multipart/form-data; boundary=----")//
//                                            .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.95 Safari/537.36")
//                                            .addHeader("Referer", url)
//                                            .post(requestBody.build())
//                                            .build();
//                                    OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
//                                    okHttpClient.newCall(request).enqueue(new Callback() {
//                                        @Override
//                                        public void onFailure(Call call, IOException e) {
//                                            KLog.v(TAG, "Exception: " + e);
//                                        }
//
//                                        @Override
//                                        public void onResponse(Call call, Response response) throws IOException {
//                                            KLog.v(TAG, "response: " + response.body().string());
//                                        }
//                                    });
                                }
                            })
                            .build();
                }
                postDialog.show();
            }
        });

//        switch (webType) {
//            case KomicaManager.WebType.INTEGRATED:
//                addPostFab.setVisibility(View.GONE);
//                break;
//            case KomicaManager.WebType.NORMAL:
//            default:
//                addPostFab.setVisibility(View.VISIBLE);
//                break;
//        }

        getActivity().setTitle(title);
    }

    private void initWebView() {
        webView = new WebView(getContext());
//        webView.getSettings().setUserAgentString("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0");
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new JSInterface(new JSInterface.OnCallListener() {
            @Override
            public void onResponse(String result) {
                // TODO get real html
                KLog.v(TAG, "onJavaScript onResponse");


                if (isPosting) {
                    isPosting = false;
                    webView.post(new Runnable() {
                        @Override
                        public void run() {
                            webView.stopLoading();
                        }
                    });

                    loadSection();
                } else {
                    Document frameDom = Jsoup.parse(result);
                    if (!frameDom.getElementsByClass("g-recaptcha").isEmpty()) {
//                        Element element = frameDom.getElementsByTag("form").first();
//                        Element recaptchaElem = frameDom.getElementsByClass("g-recaptcha").first();
//                        return;
                    }
////                    formElem.removeClass("container");
//                    if (formElem.getElementsByTag("form").size() > 1) {
//                        formElem.getElementsByTag("form").get(1).remove();
//                    }
//                    final Element element = frameDom.getElementsByTag("form").first();
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
////                            WebView formWebView = new WebView(getContext());
////                            formWebView.getSettings().setJavaScriptEnabled(true);
//                            webView.loadData(formElem.toString(), "text/html", "");
//                            webView.removeJavascriptInterface("HtmlViewer");
////                            formWebView.loadData(element.toString(), "text/html", "");
//                            postDialog = new MaterialDialog.Builder(getContext())
//                                    .customView(webView, true)
//                                    .build();
//                        }
//                    });

//                    String checkScriptUrl = frameDom.getElementsByTag("form").first().getElementsByTag("iframe").attr("src");
//                    KLog.v(TAG, checkScriptUrl);
//                    loadScript(checkScriptUrl);
                }
//                webView.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        webView.loadData(frameDom.getElementsByTag("iframe").outerHtml(), "text/html", "utf-8");
//                    }
//                });

            }
        }), "HtmlViewer");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(final WebView view, String url) {
                super.onPageFinished(view, url);
                KLog.v(TAG, "onPageFinished");
//                view.loadUrl("javascript:(function() {" +
//                        "var items = document.getElementsByClassName('container');" +
//                        "for (i = 0; i < items.length; i++) {" +
//                        "items[i].style.display='none'" +
//                        "}" +
//                        "document.getElementsByTagName('center')[0].style.display = 'none';" +
//                        "}) ()");
//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
////                            WebView formWebView = new WebView(getContext());
////                            formWebView.getSettings().setJavaScriptEnabled(true);
////                        webView.loadData(element.toString(), "text/html", "");
////                        webView.removeJavascriptInterface("HtmlViewer");
////                            formWebView.loadData(element.toString(), "text/html", "");
//                        postDialog = new MaterialDialog.Builder(getContext())
//                                .customView(webView, true)
//                                .build();
//                    }
//                });
//                view.loadUrl("javascript:var con = document.getElementsByTagName('page-content'); " +"con[0].style.display = 'none'; ");
                view.loadUrl("javascript:window.HtmlViewer.onResponse" +
                        "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
            }

            @Override
            public void onPageCommitVisible(WebView view, String url) {
                super.onPageCommitVisible(view, url);
                KLog.v(TAG, "onPageCommitVisible");
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                KLog.v(TAG, "onJsConfirm");
                return super.onJsConfirm(view, url, message, result);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                KLog.v(TAG, "onProgressChanged");
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                KLog.v(TAG, "onJsAlert");
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
        //TODO for test
//        ((FrameLayout) findViewById(getView(), R.id.frameLayout_sectionWeb)).addView(webView);
    }

    private void loadSection() {
        switch (webType) {
            case KomicaManager.WebType.INTEGRATED:
                break;
            case KomicaManager.WebType.NORMAL:
            default:
                url = (url.contains("page_num") ? url.substring(0, url.lastIndexOf("&")) : url) + "&page_num=" + page;
                break;
        }

        if (null == getActivity()) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl(url);
            }
        });
        OkHttpClientConnect.excuteAutoGet(url, new NetworkCallback() {
            @Override
            public void onFailure(IOException e) {

            }

            @Override
            public void onResponse(int responseCode, String result) {
                if (result.contains("ReDirUrl")) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            url = url.substring(0, url.lastIndexOf("/") + 1) + "m/";
                            webView.stopLoading();
                            webView.loadUrl(url);
                        }
                    });

                }
                if (page == 0) {
                    postList.clear();
                }
                Document document = Jsoup.parse(result);
                formElem = document.getElementsByTag("form").first();
                inputElements = formElem.getElementsByTag("input");
                formUrl = url.substring(0, url.lastIndexOf("/") + 1) + formElem.attr("action");
                commentBoxKey = formElem.getElementsByTag("textarea").attr("name");
                KTitle head = CrawlerUtils.getPostList(document, url, webType).get(0);
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

    private void loadScript(String url) {
        OkHttpClientConnect.excuteAutoGet(url, new NetworkCallback() {
            @Override
            public void onFailure(IOException e) {

            }

            @Override
            public void onResponse(int responseCode, String result) {
                KLog.v(TAG, result);
                Document document = Jsoup.parse(result);
            }
        });
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
