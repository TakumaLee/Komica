package idv.kuma.app.komica.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import tw.showang.recycleradaterbase.RecyclerAdapterBase;

/**
 * Created by TakumaLee on 2016/12/22.
 */

public class SectionDetailsFragment extends BaseFragment implements KomicaManager.OnUpdateConfigListener {
    private static final String TAG = SectionDetailsFragment.class.getSimpleName();

    private String url;
    private String title;
    private int webType;

    private RecyclerView recyclerView;
    private KLinearLayoutManager linearLayoutManager;
    private SectionDetailsAdapter adapter;

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

        adapter = new SectionDetailsAdapter(postList);
        linearLayoutManager = new KLinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST));
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

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
