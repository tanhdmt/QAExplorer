package com.example.domiter.fileexplorer.activity;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.domiter.fileexplorer.R;
import com.example.domiter.fileexplorer.fragment.PreferenceFragment;
import com.example.domiter.fileexplorer.util.Utils;

public class PreferenceActivity extends BaseActivity {
    private PreferenceFragment mFragment;

    @SuppressWarnings("ConstantConditions")
    @Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

        setContentView(R.layout.activity_generic);
        setupToolbar();

        mFragment = (PreferenceFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if(mFragment == null){
            mFragment = new PreferenceFragment();
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment, mFragment, FRAGMENT_TAG)
                    .commit();
        }
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Utils.showHome(this);
			return true;
        default:
            return super.onOptionsItemSelected(item);
		}
	}
}
