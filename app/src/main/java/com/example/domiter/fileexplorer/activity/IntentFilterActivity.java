package com.example.domiter.fileexplorer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import com.example.domiter.fileexplorer.IntentConstants;
import com.example.domiter.fileexplorer.R;
import com.example.domiter.fileexplorer.fragment.FileListFragment;
import com.example.domiter.fileexplorer.fragment.PickFileListFragment;
import com.example.domiter.fileexplorer.fragment.PreferenceFragment;
import com.example.domiter.fileexplorer.util.FileUtils;

import java.io.File;

public class IntentFilterActivity extends BaseActivity {
	private FileListFragment mFragment;

	protected void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);

        setContentView(R.layout.activity_generic);
        setupToolbar();

		Intent intent = getIntent();
		// Initialize arguments
		Bundle extras = intent.getExtras();
		if (extras == null)
			extras = new Bundle();
		// Add a path if path is not specified in this activity's call
		if (!extras.containsKey(IntentConstants.EXTRA_DIR_PATH)) {
			// Set a default path so that we launch a proper list.
			File defaultFile = new File(
                    PreferenceFragment.getDefaultPickFilePath(this));
			if (!defaultFile.exists()) {
                PreferenceFragment.setDefaultPickFilePath(this, Environment
						.getExternalStorageDirectory().getAbsolutePath());
				defaultFile = new File(
                        PreferenceFragment.getDefaultPickFilePath(this));
			}
			extras.putString(IntentConstants.EXTRA_DIR_PATH,
					defaultFile.getAbsolutePath());
		}

		// Add a path if a path has been specified in this activity's call.
		File data = FileUtils.getFile(getIntent().getData());
		if (data != null) {
			File dir = FileUtils.getPathWithoutFilename(data);
			if (dir != null) {
				extras.putString(IntentConstants.EXTRA_DIR_PATH,
						data.getAbsolutePath());
			}
			if (dir != data){
				// data is a file
				extras.putString(IntentConstants.EXTRA_FILENAME, data.getName());
			}
		}

		// Add a mimetype filter if it was specified through the type of the
		// intent.
		if (!extras.containsKey(IntentConstants.EXTRA_FILTER_MIMETYPE)
				&& intent.getType() != null)
			extras.putString(IntentConstants.EXTRA_FILTER_MIMETYPE,
					intent.getType());

		// Actually fill the ui
		chooseListType(intent, extras);
	}

	private void chooseListType(Intent intent, Bundle extras) {
		// Item pickers
		if (IntentConstants.ACTION_PICK_DIRECTORY.equals(intent.getAction())
				|| IntentConstants.ACTION_PICK_FILE.equals(intent.getAction())
				|| Intent.ACTION_GET_CONTENT.equals(intent.getAction())){
			if (intent.hasExtra(IntentConstants.EXTRA_TITLE))
				getActionBar().setTitle(intent.getStringExtra(IntentConstants.EXTRA_TITLE));
			else
                getActionBar().setTitle(R.string.pick_title);

			mFragment = (PickFileListFragment) getSupportFragmentManager()
					.findFragmentByTag(PickFileListFragment.class.getName());

			// Only add if it doesn't exist
			if (mFragment == null) {
				mFragment = new PickFileListFragment();

				// Pass extras through to the list fragment. This helps
				// centralize the path resolving, etc.
				extras.putBoolean(
						IntentConstants.EXTRA_IS_GET_CONTENT_INITIATED,
						intent.getAction().equals(Intent.ACTION_GET_CONTENT));
				extras.putBoolean(
						IntentConstants.EXTRA_DIRECTORIES_ONLY,
						intent.getAction().equals(
								IntentConstants.ACTION_PICK_DIRECTORY));

				mFragment.setArguments(extras);
				getSupportFragmentManager()
						.beginTransaction()
						.add(R.id.fragment, mFragment,
								PickFileListFragment.class.getName()).commit();
			}
		} else {
            // Don't stay alive without a UI
            finish();
        }
	}

    @Override
    protected void setupToolbar() {
        super.setupToolbar();
        if (getActionBar() != null) {
            getActionBar().setDisplayShowTitleEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        if (mFragment instanceof PickFileListFragment) {
            if (!((PickFileListFragment) mFragment).pressBack()) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }
}
