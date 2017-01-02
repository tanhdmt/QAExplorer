package com.example.domiter.fileexplorer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;

import com.example.domiter.fileexplorer.IntentConstants;
import com.example.domiter.fileexplorer.R;
import com.example.domiter.fileexplorer.fragment.SimpleFileListFragment;
import com.example.domiter.fileexplorer.misc.FileHolder;

import java.io.File;

import static com.example.domiter.fileexplorer.IntentConstants.EXTRA_FROM_OI_FILEMANAGER;
import static com.example.domiter.fileexplorer.util.FileUtils.getFile;
import static com.example.domiter.fileexplorer.util.FileUtils.openFile;

public class FileManagerActivity extends BaseActivity {
	private SimpleFileListFragment mFragment;

    @Override
	protected void onNewIntent(Intent intent) {
		if(intent.getData() != null) {
            mFragment.openInformingPathBar(new FileHolder(getFile(intent.getData()), this),
                    true);  // We force no anim as the layout transition does not run properly when
                            // the view is not visible and consequently the buttons will stay invisible.
        }
	}

	private File resolveIntentData(){
		File data = getFile(getIntent().getData());
		if(data == null)
			return null;
		
		if (data.isFile() && !getIntent().getBooleanExtra(EXTRA_FROM_OI_FILEMANAGER, false)) {
			openFile(new FileHolder(data, this), this);

			finish();
			return null;
		} else {
            return getFile(getIntent().getData());
        }
	}
	
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

        setContentView(R.layout.activity_filemanager);
        setupToolbar();

        // Search when the user types.
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
		
		// If not called by name, open on the requested location.
		File data = resolveIntentData();

		// Add fragment only if it hasn't already been added.
		mFragment = (SimpleFileListFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
		if (mFragment == null) {
			mFragment = new SimpleFileListFragment();
			Bundle args = new Bundle();
			if (data == null)
				args.putString(IntentConstants.EXTRA_DIR_PATH,
                        Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
                                ? Environment.getExternalStorageDirectory().getAbsolutePath()
                                : "/");
			else
				args.putString(IntentConstants.EXTRA_DIR_PATH, data.toString());
			mFragment.setArguments(args);
			getSupportFragmentManager().beginTransaction().add(R.id.fragment, mFragment, FRAGMENT_TAG).commit();
		} else {
			// If we didn't rotate and data wasn't null.
			if (icicle == null && data != null)
				mFragment.openInformingPathBar(new FileHolder(new File(data.toString()), this));
		}
	}

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.options_filemanager, menu);
 		return true;
 	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

            switch (item.getItemId()) {
                case R.id.menu_search:
                    onSearchRequested();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }

    @Override
    public void onBackPressed() {
            if (!mFragment.pressBack()) {
                super.onBackPressed();
            }
    }

	@Override
	public boolean onSearchRequested() {
		Bundle appData = new Bundle();
		appData.putString(IntentConstants.EXTRA_SEARCH_INIT_PATH, mFragment.getPath());
		startSearch(null, false, appData, false);
		
		return true;
	}
}
