package idv.kuma.app.komica.widgets;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import idv.kuma.app.komica.utils.AppTools;

/**
 * Created by TakumaLee on 2017/1/7.
 */

public class IndexGridDividerDecoration extends RecyclerView.ItemDecoration {

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.set(AppTools.dpToPx(8), AppTools.dpToPx(16), AppTools.dpToPx(8), AppTools.dpToPx(16));
    }
}
