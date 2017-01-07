package idv.kuma.app.komica.fragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import idv.kuma.app.komica.R;
import idv.kuma.app.komica.activities.SectionDetailsActivity;
import idv.kuma.app.komica.configs.BundleKeyConfigs;
import idv.kuma.app.komica.configs.WebUrlFormaterUtils;
import idv.kuma.app.komica.entity.Promotion;
import idv.kuma.app.komica.fragments.base.BaseFragment;
import idv.kuma.app.komica.http.NetworkCallback;
import idv.kuma.app.komica.http.OkHttpClientConnect;
import idv.kuma.app.komica.manager.KomicaManager;
import idv.kuma.app.komica.widgets.IndexGridDividerDecoration;

import static android.text.Html.FROM_HTML_MODE_LEGACY;

/**
 * Created by TakumaLee on 2016/12/22.
 */

public class IndexFragment extends BaseFragment {

    private static IndexFragment instance = null;

    private TextView textView;
    private RecyclerView recyclerView;
    private PromoteAdapter adapter;
    private GridLayoutManager gridLayoutManager;

    private List<Promotion> promotionList = Collections.emptyList();

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
        textView = findViewById(view, R.id.textView_index);

        adapter = new PromoteAdapter();
        recyclerView.setAdapter(adapter);

        gridLayoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.addItemDecoration(new IndexGridDividerDecoration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            textView.setText(Html.fromHtml(getString(R.string.declared_index), FROM_HTML_MODE_LEGACY));
        } else {
            textView.setText(Html.fromHtml(getString(R.string.declared_index)));
        }
        loadPromotionList();
    }

    private void loadPromotionList() {
        OkHttpClientConnect.excuteAutoGet(WebUrlFormaterUtils.getPromoteListUrl(), new NetworkCallback() {
            @Override
            public void onFailure(IOException e) {

            }

            @Override
            public void onResponse(int responseCode, String result) {
                promotionList.clear();
                try {
                    JSONObject object = new JSONObject(result);
                    JSONArray array = object.getJSONArray("promoteList");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject promoObj = array.getJSONObject(i);
                        Promotion promotion = new Promotion();
                        promotion.setLinkUrl(promoObj.getString("promotionUrl"));
                        promotion.setTitle(promoObj.getString("promotionTitle"));
                        promotion.setImageUrl(promoObj.getString("prmotionImg"));
                        promotionList.add(promotion);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
                adapter.notifyDataSetChanged();
            }
        });
    }

    private class PromoteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_index_promote, parent, false);
            return new PromoteViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            PromoteViewHolder viewHolder = (PromoteViewHolder) holder;
            viewHolder.titleTextView.setText(promotionList.get(position).getTitle());
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("02. Promote")
                            .setAction(promotionList.get(position).getTitle() + "_Click")
                            .setLabel(promotionList.get(position).getTitle())
                            .build());
                    Intent intent = new Intent(getContext(), SectionDetailsActivity.class);
                    intent.putExtra(BundleKeyConfigs.KEY_WEB_URL, promotionList.get(position).getLinkUrl());
                    intent.putExtra(BundleKeyConfigs.KEY_WEB_TITLE, promotionList.get(position).getTitle());
                    intent.putExtra(BundleKeyConfigs.KEY_WEB_TYPE, KomicaManager.WebType.NORMAL);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return promotionList.size();
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
