package idv.kuma.app.komica.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

import idv.kuma.app.komica.utils.AppTools;

/**
 * Created by TakumaLee on 2016/2/1.
 */
public class RoundedCornerImageView extends ImageView {
    public RoundedCornerImageView(Context context) {
        this(context, null);
    }

    public RoundedCornerImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundedCornerImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(ImageHelper.getRoundedCornerBitmap(bm, AppTools.pxToDp(2)));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
