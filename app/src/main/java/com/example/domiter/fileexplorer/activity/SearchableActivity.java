package com.example.domiter.fileexplorer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.domiter.fileexplorer.R;
import com.example.domiter.fileexplorer.fragment.SearchListFragment;
import com.example.domiter.fileexplorer.util.Utils;

public class SearchableActivity extends BaseActivity {
    private SearchListFragment mFragment;

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleRequest();
    }

	protected void onCreate(Bundle savedInstanceState) {
		// Presentation settings
		super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_generic);
        setupToolbar();

        // Handle the search request.
        handleRequest();
	}

    private void handleRequest() {
        // Add fragment only if it hasn't already been added.
        mFragment = (SearchListFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if(mFragment == null){
            mFragment = new SearchListFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment, mFragment, FRAGMENT_TAG).commit();
        }
    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Utils.showHome(this);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}