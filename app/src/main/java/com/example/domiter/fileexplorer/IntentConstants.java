package com.example.domiter.fileexplorer;

public final class IntentConstants {

	public static final String ACTION_PICK_FILE = "org.openintents.action.PICK_FILE";

	public static final String ACTION_PICK_DIRECTORY = "org.openintents.action.PICK_DIRECTORY";

	/**
	 * The title to display.
	 */
	public static final String EXTRA_TITLE = "org.openintents.extra.TITLE";

	/**
	 * The text on the button to display.
	 */
	public static final String EXTRA_BUTTON_TEXT = "org.openintents.extra.BUTTON_TEXT";

	/**
	 * Flag indicating to show only writeable files and folders.
	 */
	public static final String EXTRA_WRITEABLE_ONLY = "org.openintents.extra.WRITEABLE_ONLY";

    /**
     * The path to prioritize in search. Usually denotes the path the user was on when the search was initiated.
     */
    public static final String EXTRA_SEARCH_INIT_PATH = "org.openintents.extra.SEARCH_INIT_PATH";

	/**
	 * The search query as sent to SearchService.
	 */
	public static final String EXTRA_SEARCH_QUERY = "org.openintents.extra.SEARCH_QUERY";

	public static final String EXTRA_DIR_PATH = "org.openintents.extra.DIR_PATH";

	/**
	 * Extension by which to filter.
	 */
	public static final String EXTRA_FILTER_FILETYPE = "org.openintents.extra.FILTER_FILETYPE";

	/**
	 * Mimetype by which to filter.
	 */
	public static final String EXTRA_FILTER_MIMETYPE = "org.openintents.extra.FILTER_MIMETYPE";

	/**
	 * Only show directories.
	 */
	public static final String EXTRA_DIRECTORIES_ONLY = "org.openintents.extra.DIRECTORIES_ONLY";

	public static final String EXTRA_DIALOG_FILE_HOLDER = "org.openintents.extra.DIALOG_FILE";

	public static final String EXTRA_IS_GET_CONTENT_INITIATED = "org.openintents.extra.ENABLE_ACTIONS";

	public static final String EXTRA_FILENAME = "org.openintents.extra.FILENAME";

    public static final String EXTRA_FROM_OI_FILEMANAGER = "org.openintents.extra.FROM_OI_FILEMANAGER";
    public static final String ACTION_REFRESH_LIST = "org.openintents.action.REFRESH_LIST";
    public static final String ACTION_REFRESH_THEME = "org.openintents.action.REFRESH_THEME";
}
