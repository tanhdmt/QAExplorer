package com.example.domiter.fileexplorer.util;

import android.util.Log;

import com.example.domiter.fileexplorer.BuildConfig;

public class Logger {
    private static final boolean LOG_ENABLED = BuildConfig.DEBUG;

    private static final String TAG_DEFAULT = "DIR_Default";
    public static final String TAG_OBSERVER = "DIR_FileObserver";
    public static final String TAG_DIRSCANNER = "DIR_DirectoryScanner";
    public static final String TAG_THUMBLOADER = "DIR_ThumbnailLoader";
    public static final String TAG_MEDIASCANNER = "DIR_MediaScanner";
    public static final String TAG_PATHBAR = "DIR_PathBar";
    public static final String TAG_ANIMATION = "DIR_Animation";

    private Logger(){}

    public static void log(Throwable t) {
        if (LOG_ENABLED) {
            t.printStackTrace();
        }
    }

    public static void log(String msg) {
        logV(TAG_DEFAULT, msg);
    }

    public static void log(int priority, String msg) {
        log(priority, TAG_DEFAULT, msg);
    }

    public static void logV(String tag, String msg) {
        log(Log.VERBOSE, tag, msg);
    }

    public static void log(int priority, String tag, String msg) {
        if (LOG_ENABLED) {
            Log.println(priority, tag, msg);
        }
    }
}
