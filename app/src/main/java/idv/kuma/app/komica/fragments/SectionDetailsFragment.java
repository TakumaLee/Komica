package idv.kuma.app.komica.fragments;

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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

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
import idv.kuma.app.komica.manager.KomicaManager;
import idv.kuma.app.komica.utils.CrawlerUtils;
import idv.kuma.app.komica.utils.KLog;
import idv.kuma.app.komica.views.PostView;
import idv.kuma.app.komica.widgets.DividerItemDecoration;
import idv.kuma.app.komica.widgets.KLinearLayoutManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import tw.showang.recycleradaterbase.RecyclerAdapterBase;

/**
 * Created by TakumaLee on 2016/12/22.
 */

public class SectionDetailsFragment extends BaseFragment implements KomicaManager.OnUpdateConfigListener {
    private static final String TAG = SectionDetailsFragment.class.getSimpleName();

    private String url;
    private String title;
    private int webType;
    private String formUrl;
    private String commentBoxKey;

    private RecyclerView recyclerView;
    private KLinearLayoutManager linearLayoutManager;
    private SectionDetailsAdapter adapter;

    private FloatingActionButton addPostFab;
    private MaterialDialog postDialog;

    private List<KPost> postList = Collections.emptyList();

    public static SectionDetailsFragment newInstance(String url, String title, int webType) {
        SectionDetailsFragment fragment = new SectionDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(BundleKeyConfigs.KEY_WEB_URL, url);
        bundle.putString(BundleKeyConfigs.KEY_WEB_TITLE, title);
        bundle.putInt(BundleKeyConfigs.KEY_WEB_TYPE, webType);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        url = getArguments().getString(BundleKeyConfigs.KEY_WEB_URL);
        title = getArguments().getString(BundleKeyConfigs.KEY_WEB_TITLE);
        webType = getArguments().getInt(BundleKeyConfigs.KEY_WEB_TYPE);
        postList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_section_details, container, false);
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
        recyclerView = findViewById(getView(), R.id.recyclerView_section_details);
        addPostFab = findViewById(getView(), R.id.fab_section_details_add_post);

        adapter = new SectionDetailsAdapter(postList);
        linearLayoutManager = new KLinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST));
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        addPostFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null == postDialog) {
                    postDialog = new MaterialDialog.Builder(view.getContext())
                            .customView(R.layout.layout_post, true)
                            .positiveText(R.string.confirm)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    TextInputEditText commentEditText = (TextInputEditText) postDialog.getCustomView().findViewById(R.id.editText_post_comment);
                                    MultipartBody.Builder requestBody = new MultipartBody.Builder()
                                            .setType(MultipartBody.FORM)
                                            .addFormDataPart(commentBoxKey, commentEditText.getText().toString());
                                    Request request = new Request.Builder()
                                            .url(formUrl)
                                            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                                            .addHeader("Content-Type", "multipart/form-data")//; boundary=----
                                            .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.95 Safari/537.36")
                                            .addHeader("Referer", url)
                                            .post(requestBody.build())
                                            .build();
                                    OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
                                    okHttpClient.newCall(request).enqueue(new Callback() {
                                        @Override
                                        public void onFailure(Call call, IOException e) {
                                            KLog.v(TAG, "Exception: " + e);
                                        }

                                        @Override
                                        public void onResponse(Call call, Response response) throws IOException {
                                            KLog.v(TAG, "response: " + response.body().string());
                                        }
                                    });
                                }
                            })
                            .build();
                }
                postDialog.show();
            }
        });

        getActivity().setTitle(title);
    }

    private void loadSection() {
        OkHttpClientConnect.excuteAutoGet(url, new NetworkCallback() {
            @Override
            public void onFailure(IOException e) {

            }

            @Override
            public void onResponse(int responseCode, String result) {
                Document document = Jsoup.parse(result);
                formUrl = url.substring(0, url.lastIndexOf("/") + 1) + document.getElementsByTag("form").attr("action");
                commentBoxKey = document.getElementsByTag("form").first().getElementsByTag("textarea").attr("name");
                KTitle head = CrawlerUtils.getPostList(document, url, webType).get(0);
                postList.add(head);
                postList.addAll(head.getReplyList());
                notifyAdapter();
            }
        });
    }

    private void notifyAdapter() {
        if (null != getActivity()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
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
