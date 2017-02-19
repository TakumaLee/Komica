package idv.kuma.app.komica.manager;

import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import idv.kuma.app.komica.R;


/**
 * Created by TakumaLee on 2016/8/24.
 */
public class ShareAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private OnPlatformSelectedListener onPlatformSelectedListener;

    public static int SELECTION_COUNT = 2;

    public class ShareApp {
        public static final int FACEBOOK = 101;
        public static final int MORE = 99;
    }

    private String title;

    public ShareAdapter() {
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        switch (viewType) {
            case ShareApp.FACEBOOK:
                ImageView imageView = new ImageView(parent.getContext());
                imageView.setImageResource(R.mipmap.fb_icon);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (onPlatformSelectedListener != null) {
                            onPlatformSelectedListener.onFacebook();
                        }
                    }
                });
                viewHolder = new ShareAppViewHolder(imageView);
                break;
            case ShareApp.MORE:
            default:
                TextView textView = new TextView(parent.getContext());
                textView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                textView.setGravity(Gravity.CENTER);
                textView.setText(R.string.more);
                textView.setAllCaps(true);
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (onPlatformSelectedListener != null) {
                            onPlatformSelectedListener.onMore();
                        }
                    }
                });
                viewHolder = new ShareAppViewHolder(textView);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case ShareApp.FACEBOOK:
                break;
            case ShareApp.MORE:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return SELECTION_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        int caculatePos = position;
        switch (caculatePos) {
            case 0:
                return ShareApp.FACEBOOK;
            case 1:
            default:
                return ShareApp.MORE;
        }
    }

    public void setOnPlatformSelectedListener(OnPlatformSelectedListener onPlatformSelectedListener) {
        this.onPlatformSelectedListener = onPlatformSelectedListener;
    }

    public interface OnPlatformSelectedListener {
        void onFacebook();
        void onMore();
    }

    public class ShareAppViewHolder extends RecyclerView.ViewHolder {

        public ShareAppViewHolder(View itemView) {
            super(itemView);
        }
    }


}
