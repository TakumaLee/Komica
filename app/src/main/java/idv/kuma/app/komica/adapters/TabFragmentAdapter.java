package idv.kuma.app.komica.adapters;

import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TakumaLee on 15/6/26.
 */
public class TabFragmentAdapter extends FragmentStatePagerAdapter {
    private final List<Fragment> mFragments = new ArrayList<>();
    private final List<String> mFragmentTitles = new ArrayList<>();
    private boolean enable = true;

    public TabFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    public void enableTitle(boolean enable) {
        this.enable = enable;
    }

    public void addFragment(Fragment fragment, String title) {
        mFragments.add(fragment);
        mFragmentTitles.add(title);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (!enable) {
            return super.getPageTitle(position);
        }
        return mFragmentTitles.get(position);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    public void removeAllFragment() {
        mFragments.clear();
        mFragmentTitles.clear();
    }
}
