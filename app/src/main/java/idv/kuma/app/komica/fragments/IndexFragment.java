package idv.kuma.app.komica.fragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.google.android.gms.analytics.HitBuilders;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import idv.kuma.app.komica.R;
import idv.kuma.app.komica.activities.SectionDetailsActivity;
import idv.kuma.app.komica.configs.BundleKeyConfigs;
import idv.kuma.app.komica.configs.WebUrlFormaterUtils;
import idv.kuma.app.komica.entity.Promotion;
import idv.kuma.app.komica.fragments.base.BaseFragment;
import idv.kuma.app.komica.manager.KomicaManager;
import idv.kuma.app.komica.utils.AppTools;
import idv.kuma.app.komica.widgets.IndexGridDividerDecoration;
import tw.showang.recycleradaterbase.RecyclerAdapterBase;

import static android.text.Html.FROM_HTML_MODE_LEGACY;

/**
 * Created by TakumaLee on 2016/12/22.
 */

public class IndexFragment extends BaseFragment {
    private static final String TAG = IndexFragment.class.getSimpleName();

    private static IndexFragment instance = null;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private PromoteAdapter adapter;
    private GridLayoutManager gridLayoutManager;

    private List<Promotion> promotionList = Collections.emptyList();
    private String info = "";

    private MaterialDialog materialDialog;

    public static IndexFragment newInstance() {
        if (null == instance) {
            instance = new IndexFragment();
        }
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        promotionList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_index, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("首頁");
        recyclerView = findViewById(view, R.id.recyclerView_index);
        swipeRefreshLayout = findViewById(view, R.id.swipeRefreshLayout_index);

        TextView footerTextView = new TextView(getContext());
        footerTextView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        footerTextView.setPadding(AppTools.dpToPx(32), AppTools.dpToPx(32), AppTools.dpToPx(32), AppTools.dpToPx(32));

        adapter = new PromoteAdapter(promotionList);
        adapter.setFooterView(footerTextView);
        recyclerView.setAdapter(adapter);

        gridLayoutManager = new GridLayoutManager(getContext(), 3);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position == adapter.getItemCount() - 1 || position == 0) {
                    return 3;
                }
                return 1;
            }
        });
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.addItemDecoration(new IndexGridDividerDecoration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            footerTextView.setText(Html.fromHtml(getString(R.string.declared_index), FROM_HTML_MODE_LEGACY));
        } else {
            footerTextView.setText(Html.fromHtml(getString(R.string.declared_index)));
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadPromotionList();
            }
        });

        loadPromotionList();

        materialDialog = new MaterialDialog.Builder(getContext())
                .title("最新消息")
                .content("")
                .build();

    }

    private void loadPromotionList() {
        swipeRefreshLayout.setRefreshing(true);
        AndroidNetworking.get(WebUrlFormaterUtils.getPromoteListUrl())
                .build().getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String response) {
                promotionList.clear();
                try {
                    JSONObject object = new JSONObject(response);
                    JSONArray array = object.getJSONArray("promoteList");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject promoObj = array.getJSONObject(i);
                        Promotion promotion = new Promotion();
                        promotion.setLinkUrl(promoObj.getString("promotionUrl"));
                        promotion.setTitle(promoObj.getString("promotionTitle"));
                        promotion.setImageUrl(promoObj.getString("prmotionImg"));
                        promotionList.add(promotion);
                    }
                    info = object.getString("promoteInfo");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                notifyAdapter();
            }

            @Override
            public void onError(ANError anError) {
                notifyAdapter();
            }
        });
    }

    private void notifyAdapter() {
        if (null == getActivity()) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
                adapter.notifyDataSetChanged();
                View headerView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_index_promote, null);
                TextView infoTextView = findViewById(headerView, R.id.textView_index_adapter_promote);
                infoTextView.setText("最新消息");
                adapter.setHeaderView(infoTextView);
                infoTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            materialDialog.getContentView().setText(Html.fromHtml(info, FROM_HTML_MODE_LEGACY));
                        } else {
                            materialDialog.getContentView().setText(Html.fromHtml(info));
                        }
                        materialDialog.show();
                        tracker.send(new HitBuilders.EventBuilder()
                                .setCategory("02. Promote")
                                .setAction("查看最新消息")
                                .setLabel("最新消息")
                                .build());
                    }
                });
            }
        });
    }

    private class PromoteAdapter extends RecyclerAdapterBase {

        List<Promotion> promotions;

        protected PromoteAdapter(List<Promotion> dataList) {
            super(dataList);
            this.promotions = dataList;
        }

        @Override
        protected RecyclerView.ViewHolder onCreateItemViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_index_promote, parent, false);
            return new PromoteViewHolder(view);
        }

        @Override
        protected void onBindItemViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
            PromoteViewHolder holder = (PromoteViewHolder) viewHolder;
            holder.titleTextView.setText(promotionList.get(position).getTitle());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("02. Promote")
                            .setAction(promotions.get(position).getTitle() + "_Click")
                            .setLabel(promotions.get(position).getTitle())
                            .build());
                    Intent intent = new Intent(getContext(), SectionDetailsActivity.class);
                    intent.putExtra(BundleKeyConfigs.KEY_WEB_URL, promotions.get(position).getLinkUrl());
                    intent.putExtra(BundleKeyConfigs.KEY_WEB_TITLE, promotions.get(position).getTitle());
                    intent.putExtra(BundleKeyConfigs.KEY_WEB_TYPE, KomicaManager.WebType.NORMAL);
                    intent.putExtra(BundleKeyConfigs.KEY_WEB_FROM, "Promotion");
                    startActivity(intent);
                }
            });
        }

        class PromoteViewHolder extends RecyclerView.ViewHolder {

            TextView titleTextView;

            public PromoteViewHolder(View itemView) {
                super(itemView);
                titleTextView = findViewById(itemView, R.id.textView_index_adapter_promote);
            }
        }
    }
}
