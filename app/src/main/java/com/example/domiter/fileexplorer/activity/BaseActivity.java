package com.example.domiter.fileexplorer.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import com.example.domiter.fileexplorer.R;
import com.example.domiter.fileexplorer.view.Themer;

import static com.example.domiter.fileexplorer.IntentConstants.ACTION_REFRESH_THEME;

abstract class BaseActivity extends FragmentActivity {
    static final String FRAGMENT_TAG = "content_fragment";

    private final BroadcastReceiver mThemeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (PreferenceActivity.class.equals(BaseActivity.this.getClass())) {
                finish();
                startActivity(getIntent());
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            } else {
                recreate();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Themer.applyTheme(this);

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mThemeReceiver, new IntentFilter(ACTION_REFRESH_THEME));
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mThemeReceiver);
        super.onDestroy();
    }

    /**
     * Always call this after setContent() or it will not have any effect.
     */
    protected void setupToolbar() {
        View toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setActionBar((android.widget.Toolbar) toolbar);
            //noinspection ConstantConditions
            getActionBar().setTitle("File Explorer");
        }
    }
}
