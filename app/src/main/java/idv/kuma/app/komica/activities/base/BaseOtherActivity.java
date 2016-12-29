package idv.kuma.app.komica.activities.base;

import android.os.Bundle;
import android.view.MenuItem;

/**
 * Created by TakumaLee on 2016/5/29.
 */
public class BaseOtherActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // finish();
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
