package com.example.domiter.fileexplorer.loader;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.AsyncTaskLoader;

import com.example.domiter.fileexplorer.FileManagerApplication;
import com.example.domiter.fileexplorer.misc.FileHolder;
import com.example.domiter.fileexplorer.util.Utils;

import java.io.File;
import java.util.List;

public class SearchLoader extends AsyncTaskLoader<List<FileHolder>> {
    private List<FileHolder> mData;
    private File mRoot;
    private String mQuery;
    private Context mContext;

    public SearchLoader(Context context, File root, String query) {
        super(context);

        mRoot = root;
        mQuery = query;
        mContext = context;
    }

    @Override
    public List<FileHolder> loadInBackground() {
        return Utils.searchIn(mRoot, Utils.newFilter(mQuery),
                ((FileManagerApplication) getContext().getApplicationContext())
                        .getMimeTypes(),
                mContext, true, 100);
    }

    @Override
    public void deliverResult(List<FileHolder> data) {
        if (isReset()) {
            // The Loader has been reset; ignore the result and invalidate the data.
            releaseResources(data);
            return;
        }

        // Hold a reference to the old data so it doesn't get garbage collected.
        // We must protect it until the new data has been delivered.
        List<FileHolder> oldData = mData;
        mData = data;

        if (isStarted()) {
            // If the Loader is in a started state, deliver the results to the
            // client. The superclass method does this for us.
            super.deliverResult(data);
        }

        // Invalidate the old data as we don't need it any more.
        if (oldData != null && oldData != data) {
            releaseResources(oldData);
        }
    }

    @Override
    protected void onStartLoading() {
        if (mData != null) {
            // Deliver any previously loaded data immediately.
            deliverResult(mData);
        }

        if (takeContentChanged() || mData == null) {
            // When the observer detects a change, it should call onContentChanged()
            // on the Loader, which will cause the next call to takeContentChanged()
            // to return true. If this is ever the case (or if the current data is
            // null), we force a new load.
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        // The Loader is in a stopped state, so we should attempt to cancel the
        // current load (if there is one).
        cancelLoad();

        // Note that we leave the observer as is. Loaders in a stopped state
        // should still monitor the data source for changes so that the Loader
        // will know to force a new load if it is ever started again.
    }

    @Override
    protected void onReset() {
        // Ensure the loader has been stopped.
        onStopLoading();

        // At this point we can release the resources associated with 'mData'.
        if (mData != null) {
            releaseResources(mData);
            mData = null;
        }
    }

    @Override
    public void onCanceled(List<FileHolder> data) {
        // Attempt to cancel the current asynchronous load.
        super.onCanceled(data);

        // The load has been canceled, so we should release the resources
        // associated with 'data'.
        releaseResources(data);
    }

    private void releaseResources(List<FileHolder> oldData) {
    }
}
