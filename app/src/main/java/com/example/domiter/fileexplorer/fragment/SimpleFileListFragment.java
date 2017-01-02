package com.example.domiter.fileexplorer.fragment;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import com.example.domiter.fileexplorer.FileManagerApplication;
import com.example.domiter.fileexplorer.IntentConstants;
import com.example.domiter.fileexplorer.R;
import com.example.domiter.fileexplorer.adapter.FileHolderListAdapter;
import com.example.domiter.fileexplorer.dialog.*;
import com.example.domiter.fileexplorer.misc.FileHolder;
import com.example.domiter.fileexplorer.service.ZipService;
import com.example.domiter.fileexplorer.util.*;
import com.example.domiter.fileexplorer.view.PathController;
import com.example.domiter.fileexplorer.view.widget.AnimatedFileListContainer;
import com.example.domiter.fileexplorer.view.widget.PathView;
import com.example.domiter.fileexplorer.view.widget.PathViewCompatibleToolbar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static com.example.domiter.fileexplorer.fragment.PreferenceFragment.getMediaScanFromPreference;
import static com.example.domiter.fileexplorer.util.CopyHelper.Operation.COPY;
import static com.example.domiter.fileexplorer.view.PathController.OnDirectoryChangedListener;
import static com.example.domiter.fileexplorer.view.Themer.setStatusBarColour;
import static com.example.domiter.fileexplorer.view.widget.PathView.ActivityProvider;


public class SimpleFileListFragment extends FileListFragment {
    private static final String INSTANCE_STATE_PATHBAR_MODE = "pathbar_mode";

    private static final int REQUEST_CODE_MULTISELECT = 2;

    private static HashMap<String, ScrollPosition> sScrollPositions = new HashMap<String, ScrollPosition>();

    private PathController mPathBar;
    private AnimatedFileListContainer mTransitionView;
    private ActionMode mActionMode;
    private boolean mActionsEnabled = true;
    private int mNavigationDirection = 0;
    private View heroView;

    private AbsListView.MultiChoiceModeListener mMultiChoiceModeListener = new AbsListView.MultiChoiceModeListener() {

        @Override
        public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
            menu.clear();

            switch (getListView().getCheckedItemCount()) {
                // Single selection
                case 1:
                    File file = ((FileHolder) getListAdapter().getItem(getCheckedItemPosition())).getFile();
                    inflateSingleChoiceMenu(mode.getMenuInflater(), menu);

                    // If selected item is a directory
                    if (file.isDirectory()) {
                        menu.removeItem(R.id.menu_send);
                    }

                    // If selected item is a zip archive
                    if (!FileUtils.checkIfZipArchive(file)) {
                        menu.findItem(R.id.menu_file_ops).getSubMenu()
                                .removeItem(R.id.menu_extract);
                    } else {
                        menu.findItem(R.id.menu_file_ops).getSubMenu()
                                .removeItem(R.id.menu_compress);
                    }
                    break;
                // Multiple selection
                default:
                    inflateMultipleChoiceMenu(mode.getMenuInflater(), menu);

                    // If all items are directories
                    boolean foldersOnly = true;
                    for (FileHolder fileHolder : getCheckedItems()) {
                        foldersOnly &= fileHolder.getFile().isDirectory();
                    }
                    if(foldersOnly) {
                        menu.removeItem(R.id.menu_send);
                    }
                    break;
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(android.view.ActionMode mode) {
            mActionMode = null;
            mPathBar.setEnabled(true);
            setStatusBarColour(getActivity(), false);
        }

        @Override
        public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
            mActionMode = mode;
            mPathBar.setEnabled(false);
            setStatusBarColour(getActivity(), true);

            return true;
        }

        @Override
        public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.menu_select_all) {
                for (int i = 0; i < getListView().getCount(); i++)
                    getListView().setItemChecked(i, true);
                return true;
            }

            switch (getListView().getCheckedItemCount()) {
                // Single selection
                case 1:
                    return handleSingleSelectionAction(mode, item);
                // Multiple selection
                default:
                    return handleMultipleSelectionAction(mode, item);
            }
        }

        @Override
        public void onItemCheckedStateChanged(android.view.ActionMode mode,
                                              int position, long id, boolean checked) {
            if (getListView().getCheckedItemCount() != 0) {
                mode.setTitle(getListView().getCheckedItemCount() + " "
                        + getString(R.string.selected));

                // Force actions' refresh
                mode.invalidate();
            }
        }
    };
    private FileHolderListAdapter.OnItemToggleListener mOnItemToggleListener = new FileHolderListAdapter.OnItemToggleListener() {
        @Override
        public void onItemToggle(int position) {
            getListView().setItemChecked(position,
                    !Utils.getItemChecked(getListView(), position));
        }
    };
    private ActivityProvider mActivityProvider = new ActivityProvider() {
        @Override
        public Activity getActivity() {
            return SimpleFileListFragment.this.getActivity();
        }
    };

    private boolean handleMultipleSelectionAction(ActionMode mode, MenuItem item) {
        DialogFragment dialog;
        Bundle args;
        ArrayList<FileHolder> fItems = getCheckedItems();

        switch (item.getItemId()) {
            case R.id.menu_send:
                mode.finish();
                Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                ArrayList<Uri> uris = new ArrayList<Uri>();
                intent.setType("text/plain");

                for (FileHolder fh : fItems) {
                    if(!fh.getFile().isDirectory())
                        uris.add(FileUtils.getUri(fh.getFile()));
                }

                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);

                try {
                    startActivity(Intent.createChooser(intent, getString(R.string.send_chooser_title)));
                    return true;
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getActivity(), R.string.send_not_available, Toast.LENGTH_SHORT).show();
                    return true;
                }
            case R.id.menu_delete:
                mode.finish();
                dialog = new MultiDeleteDialog();
                dialog.setTargetFragment(this, 0);
                args = new Bundle();
                args.putParcelableArrayList(IntentConstants.EXTRA_DIALOG_FILE_HOLDER, new ArrayList<Parcelable>(fItems));
                dialog.setArguments(args);
                dialog.show(getFragmentManager(), MultiDeleteDialog.class.getName());
                return true;
            case R.id.menu_move:
                mode.finish();
                ((FileManagerApplication) getActivity().getApplication()).getCopyHelper().cut(fItems);
                getActivity().supportInvalidateOptionsMenu();
                return true;
            case R.id.menu_copy:
                mode.finish();
                ((FileManagerApplication) getActivity().getApplication()).getCopyHelper().copy(fItems);
                getActivity().supportInvalidateOptionsMenu();
                return true;
            case R.id.menu_compress:
                mode.finish();
                dialog = new MultiCompressDialog();
                dialog.setTargetFragment(this, 0);
                args = new Bundle();
                args.putParcelableArrayList(IntentConstants.EXTRA_DIALOG_FILE_HOLDER, new ArrayList<Parcelable>(fItems));
                dialog.setArguments(args);
                dialog.show(getFragmentManager(), MultiCompressDialog.class.getName());
                return true;
            default:
                return false;
        }
    }

    private boolean handleSingleSelectionAction(ActionMode mode, MenuItem item) {
        FileHolder fItem = (FileHolder) getListAdapter().getItem(getCheckedItemPosition());
        DialogFragment dialog;
        Bundle args;

        switch (item.getItemId()) {
            case R.id.menu_create_shortcut:
                mode.finish();
                Utils.createShortcut(fItem, getActivity());
                return true;

            case R.id.menu_move:
                mode.finish();
                ((FileManagerApplication) getActivity().getApplication()).getCopyHelper().cut(fItem);
                getActivity().supportInvalidateOptionsMenu();
                return true;

            case R.id.menu_copy:
                mode.finish();
                ((FileManagerApplication) getActivity().getApplication()).getCopyHelper().copy(fItem);
                getActivity().supportInvalidateOptionsMenu();
                return true;

            case R.id.menu_delete:
                mode.finish();
                dialog = new SingleDeleteDialog();
                dialog.setTargetFragment(SimpleFileListFragment.this, 0);
                args = new Bundle();
                args.putParcelable(IntentConstants.EXTRA_DIALOG_FILE_HOLDER, fItem);
                dialog.setArguments(args);
                dialog.show(getFragmentManager(), SingleDeleteDialog.class.getName());
                return true;

            case R.id.menu_rename:
                mode.finish();
                dialog = new RenameDialog();
                dialog.setTargetFragment(SimpleFileListFragment.this, 0);
                args = new Bundle();
                args.putParcelable(IntentConstants.EXTRA_DIALOG_FILE_HOLDER, fItem);
                dialog.setArguments(args);
                dialog.show(getFragmentManager(), RenameDialog.class.getName());
                return true;

            case R.id.menu_send:
                mode.finish();
                Utils.sendFile(fItem, getActivity());
                return true;

            case R.id.menu_details:
                mode.finish();
                dialog = new DetailsDialog();
                dialog.setTargetFragment(this, 0);
                args = new Bundle();
                args.putParcelable(IntentConstants.EXTRA_DIALOG_FILE_HOLDER, fItem);
                dialog.setArguments(args);
                dialog.show(getFragmentManager(), DetailsDialog.class.getName());
                return true;

            case R.id.menu_compress:
                mode.finish();
                dialog = new SingleCompressDialog();
                dialog.setTargetFragment(this, 0);
                args = new Bundle();
                args.putParcelable(IntentConstants.EXTRA_DIALOG_FILE_HOLDER, fItem);
                dialog.setArguments(args);
                dialog.show(getFragmentManager(), SingleCompressDialog.class.getName());
                return true;

            case R.id.menu_extract:
                mode.finish();
                File extractTo = new File(fItem.getFile().getParentFile(),
                        FileUtils.getNameWithoutExtension(fItem.getFile()));
                extractTo.mkdirs();

                // We just extract on the current directory. If the user needs to put it in another dir,
                // he/she can copy/cut the file
                ZipService.extractTo(getActivity(), fItem, extractTo);
                return true;

            default:
                return false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            firstTimeAnimation(container);
        }

        PathViewCompatibleToolbar toolbar = (PathViewCompatibleToolbar)
                getActivity().findViewById(R.id.toolbar);
        if (!toolbar.hasPathView()) {
            PathView pathView = new PathView(toolbar.getContext());
            pathView.setActivityProvider(mActivityProvider);
            toolbar.addView(pathView);
        }

        return inflater.inflate(R.layout.fragment_filelist_simple, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPathBar = (PathController) getActivity().findViewById(R.id.pathview);
        mTransitionView = (AnimatedFileListContainer) view.findViewById(R.id.zoomview);

        // Handle mPath differently if we restore state or just initially create the view.
        if (savedInstanceState == null) {
            mPathBar.setInitialDirectory(getPath());
        } else {
            mPathBar.cd(getPath());
        }
        mPathBar.setOnDirectoryChangedListener(new OnDirectoryChangedListener() {
            @Override
            public void directoryChanged(File newCurrentDir) {
                open(new FileHolder(newCurrentDir, getActivity()));
            }
        });
        if (savedInstanceState != null && savedInstanceState.getBoolean(INSTANCE_STATE_PATHBAR_MODE)) {
            mPathBar.switchToManualInput();
        }
        // Removed else clause as the other mode is the default.

        initContextualActions();

        // For animations' sake
        ((ViewFlipper) view.findViewById(R.id.flipper)).setInAnimation(null);
        ((ViewFlipper) view.findViewById(R.id.flipper)).setOutAnimation(null);
    }

    private void firstTimeAnimation(@SuppressWarnings("UnusedParameters") final View root) {
        // NO-OP for now. Need to fine tune it on a real device.
        // Everyone hates lag on launch.
    }

    /**
     * Override this to handle initialization of list item long clicks.
     */
    private void initContextualActions() {
        if (mActionsEnabled) {
            getListView().setMultiChoiceModeListener(mMultiChoiceModeListener);
            getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            ((FileHolderListAdapter) getListAdapter()).setOnItemToggleListener(mOnItemToggleListener);

            setHasOptionsMenu(true);
        }
    }

    @Override
    public void onListItemClick(AbsListView l, View v, int position, long id) {
        FileHolder item = (FileHolder) mAdapter.getItem(position);
        heroView = v;
        openInformingPathBar(item);
        heroView = null;
    }

    /**
     * Use this to open files and folders using this fragment. Appropriately handles pathbar updates.
     *
     * @param item The dir/file to open.
     */
    public void openInformingPathBar(FileHolder item) {
        openInformingPathBar(item, false);
    }

    public void openInformingPathBar(FileHolder item, boolean forceNoAnim) {
        if (mPathBar == null)
            open(item);
        else
            mPathBar.cd(item.getFile(), forceNoAnim);
    }

    /**
     * Point this Fragment to show the contents of the passed file.
     *
     * @param f If same as current, does nothing.
     */
    private void open(FileHolder f) {
        if (!f.getFile().exists())
            return;

        if (f.getFile().isDirectory()) {
            openDir(f);
        } else if (f.getFile().isFile()) {
            openFile(f);
        }
    }

    private void openFile(FileHolder fileholder) {
        FileUtils.openFile(fileholder, getActivity());
    }

    /**
     * Attempts to open a directory for browsing.
     * Override this to handle folder click behavior.
     *
     * @param fileHolder The holder of the directory to open.
     */
    private void openDir(FileHolder fileHolder) {
        // Avoid unnecessary attempts to load.
        if (fileHolder.getFile().getAbsolutePath().equals(getPath()))
            return;

        if(getListAdapter().getCount() > 0) {
            keepFolderScroll();
        }

        // Save required data for animation
        mNavigationDirection = Utils.getNavigationDirection(new File(getPath()), fileHolder.getFile());
        mTransitionView.setupAnimations(mNavigationDirection, heroView);

        // Load
        setPath(fileHolder.getFile());
        refresh();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.options_filelist_simple, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (getActivity() == null) {
            return;
        }

        boolean showMediaScanMenuItem = getMediaScanFromPreference(getActivity());
        if (hasScanner() && !isScannerRunning() && showMediaScanMenuItem) {
            // We only know about ".nomedia" once scanning is finished.
            boolean noMedia = isMediaScannerDisabledForPath();
            menu.findItem(R.id.menu_media_scan_include).setVisible(noMedia);
            menu.findItem(R.id.menu_media_scan_exclude).setVisible(!noMedia);
        } else {
            menu.findItem(R.id.menu_media_scan_include).setVisible(false);
            menu.findItem(R.id.menu_media_scan_exclude).setVisible(false);
        }

        CopyHelper copyHelper = ((FileManagerApplication) getActivity().getApplication()).getCopyHelper();
        MenuItem pasteAction = menu.findItem(R.id.menu_paste);
        if (copyHelper.canPaste()) {
            int stringResource = (copyHelper.getOperationType() == COPY
                    ? R.plurals.menu_copy_items_to : R.plurals.menu_move_items_to);
            pasteAction.setTitle(getResources().getQuantityString(stringResource,
                    copyHelper.getItemCount(), copyHelper.getItemCount()));

            pasteAction.getIcon().setLevel(copyHelper.getItemCount());
            pasteAction.setVisible(true);
            menu.findItem(R.id.menu_clear_clipboard).setVisible(true);
        } else {
            pasteAction.setVisible(false);
            menu.findItem(R.id.menu_clear_clipboard).setVisible(false);
        }
    }

    @Override
    protected void onDataApplied() {
        super.onDataApplied();
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_clear_clipboard:
                ((FileManagerApplication) getActivity().getApplication())
                        .getCopyHelper().clear();
                getActivity().supportInvalidateOptionsMenu();
                return true;

            case R.id.menu_create_folder:
                CreateDirectoryDialog dialog = new CreateDirectoryDialog();
                dialog.setTargetFragment(this, 0);
                Bundle args = new Bundle();
                args.putString(IntentConstants.EXTRA_DIR_PATH, getPath());
                dialog.setArguments(args);
                dialog.show(getActivity().getSupportFragmentManager(), CreateDirectoryDialog.class.getName());
                return true;

            case R.id.menu_media_scan_include:
                includeInMediaScan();
                return true;

            case R.id.menu_media_scan_exclude:
                excludeFromMediaScan();
                return true;

            case R.id.menu_paste:
                if (((FileManagerApplication) getActivity().getApplication()).getCopyHelper().canPaste()) {
                    ((FileManagerApplication) getActivity().getApplication()).getCopyHelper().paste(
                            getActivity().getApplicationContext(), new File(getPath()));
                } else {
                    Toast.makeText(getActivity(),
                            R.string.nothing_to_paste, Toast.LENGTH_LONG)
                            .show();
                }
                getActivity().supportInvalidateOptionsMenu();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Automatically refresh to display possible changes done through the multiselect fragment.
        if (requestCode == REQUEST_CODE_MULTISELECT)
            refresh();
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void includeInMediaScan() {
        // Delete the .nomedia file.
        File file = new File(mPathBar.getCurrentDirectory(), FileUtils.NOMEDIA_FILE_NAME);
        if (file.delete()) {
            MediaScannerUtils.informFolderAdded(getActivity().getApplicationContext(),
                    file.getParentFile());

            Toast.makeText(getActivity(),
                    getString(R.string.media_library_added), Toast.LENGTH_LONG)
                    .show();
        } else {
            // That didn't work.
            Toast.makeText(getActivity(),
                    getString(R.string.error_generic), Toast.LENGTH_LONG)
                    .show();
        }
        refresh();
    }

    private void excludeFromMediaScan() {
        // Create the .nomedia file.
        File file = new File(mPathBar.getCurrentDirectory(), FileUtils.NOMEDIA_FILE_NAME);
        try {
            if (file.createNewFile()) {
                MediaScannerUtils.informFolderDeleted(getActivity().getApplicationContext(),
                        file.getParentFile());

                Toast.makeText(getActivity(),
                        getString(R.string.media_library_removed), Toast.LENGTH_LONG)
                        .show();
            } else {
                Toast.makeText(getActivity(),
                        getString(R.string.error_media_scan), Toast.LENGTH_LONG)
                        .show();
            }
        } catch (IOException e) {
            // That didn't work.
            Logger.log(e);
            Toast.makeText(getActivity(),
                    getString(R.string.error_media_scan), Toast.LENGTH_LONG)
                    .show();
        }
        refresh();
    }

    public void browseToHome() {
        mPathBar.cd(mPathBar.getInitialDirectory());
    }

    public boolean pressBack() {
        return mPathBar.onBackPressed();
    }

    /**
     * Set whether to show menu and selection actions. Must be set before OnViewCreated is called.
     *
     * @param enabled Whether the actions are to be shown.
     */
    public void setActionsEnabled(boolean enabled) {
        mActionsEnabled = enabled;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(INSTANCE_STATE_PATHBAR_MODE, mPathBar.getMode() == PathController.Mode.MANUAL_INPUT);
    }

    /**
     * Override to inflate more items for the single choiceCAB. Always call the super implementation.
     *
     * @param menuInflater The inflater to use.
     * @param menu         The menu to inflate to.
     */
    void inflateSingleChoiceMenu(MenuInflater menuInflater, Menu menu) {
        menuInflater.inflate(R.menu.cab_single, menu);
    }

    /**
     * Override to inflate more items for the multiple choice CAB. Always call the super implementation.
     *
     * @param menuInflater The inflater to use.
     * @param menu         The menu to inflate to.
     */
    void inflateMultipleChoiceMenu(MenuInflater menuInflater, Menu menu) {
        menuInflater.inflate(R.menu.cab_multi, menu);
    }

    /**
     * This is error free only when FileHolderListAdapter uses stableIds and getItemId(int) returns the int passed (the position of the item).
     *
     * @return
     */
    int getCheckedItemPosition() {
        return (int) getListView().getCheckedItemIds()[0];
    }

    private void useFolderScroll(final ScrollPosition pos) {
        if(getView() != null) {
            Utils.scrollToPosition(getListView(), pos, false);
        }
    }

    private void keepFolderScroll() {
        sScrollPositions.put(getPath(), new ScrollPosition(getListView().getFirstVisiblePosition(),
                getListView().getChildAt(0).getTop()));
    }

    /**
     * @return A {@link FileHolder} list with the currently selected items.
     */
    private ArrayList<FileHolder> getCheckedItems() {
        ArrayList<FileHolder> items = new ArrayList<FileHolder>();

        for (long pos : getListView().getCheckedItemIds()) {
            items.add((FileHolder) getListAdapter().getItem((int) pos));
        }

        return items;
    }

    @Override
    protected void onListVisibilityChanging(boolean visible) {
        if (visible) {
            if (sScrollPositions.containsKey(getPath())) {
                ScrollPosition pos = sScrollPositions.get(getPath());
                useFolderScroll(pos);
            } else {
                useFolderScroll(new ScrollPosition(0, 0));
            }
        }
    }

    @Override
    protected void onListVisibilityChanged(boolean visible) {
        if (!visible && mTransitionView != null) {
            if (mNavigationDirection == 1) {
                mTransitionView.animateFwd();
            } else if (mNavigationDirection == -1) {
                mTransitionView.animateBwd();
            } else {
                // Do not animate.
                mTransitionView.clearAnimations();
            }
        }
    }

    @Override
    protected void onEmptyViewClicked() {
        mPathBar.cd(mPathBar.getCurrentDirectory().getParentFile());
    }

    public void closeActionMode() {
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    public static class ScrollPosition {
        public int index;
        public int top;

        public ScrollPosition(int index, int top) {
            this.index = index;
            this.top = top;
        }
    }
}
