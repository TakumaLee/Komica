package idv.kuma.app.komica.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import idv.kuma.app.komica.R;

/**
 * Created by TakumaLee on 2016/12/22.
 */
public class LoadMoreViewHolder extends RecyclerView.ViewHolder {
    public View progressBar;
    public View failText;

    public LoadMoreViewHolder(View itemView) {
        super(itemView);
        progressBar = itemView.findViewById(R.id.loadmore_progress);
        failText = itemView.findViewById(R.id.loadmore_failText);
    }
}
