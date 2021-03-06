package com.example.domiter.fileexplorer.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.example.domiter.fileexplorer.R;
import com.example.domiter.fileexplorer.activity.FileManagerActivity;
import com.example.domiter.fileexplorer.misc.FileHolder;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import static java.lang.System.currentTimeMillis;

public abstract class Notifier {
    private static final int DONE_NOTIF_LOWER_BOUND = 500;

    private static final HashMap<Integer, Long> notificationToStartTime = new HashMap<Integer, Long>();

    private Notifier() {}

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public static void showCopyProgressNotification(int filesCopied, int fileCount, int notId,
                                                    File fileGettingCopied, Context context) {
        Notification not = new NotificationCompat.Builder(context)
                .setAutoCancel(false)
                .setContentTitle(context.getString(R.string.copying))
                .setContentText(fileGettingCopied.getAbsolutePath())
                .setProgress(fileCount, filesCopied, false)
                .setOngoing(true)
//                .addAction(android.R.drawable.ic_menu_close_clear_cancel,
//                        context.getString(android.R.string.cancel), null)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_stat_notify_paste)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(context.getResources().getString(R.string.notif_copying_item,
                                fileGettingCopied.getName(), fileGettingCopied.getParent())))
                .setTicker(context.getString(R.string.copying))
                .setOnlyAlertOnce(true)
                .build();

        storeStartTimeIfNeeded(notId);

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notId, not);
    }

    public static void showCopyDoneNotification(boolean success, int notId,
                                                String toPath, Context context) {
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (!shouldShowDoneNotification(notId, success)) {
            notificationManager.cancel(notId);
        } else {
            Intent browseIntent = new Intent(context, FileManagerActivity.class);
            browseIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
            browseIntent.setData(Uri.fromFile(new File(toPath)));

            Notification not = new NotificationCompat.Builder(context)
                    .setAutoCancel(true)
                    .setContentTitle(context.getString(success ? R.string.copied : R.string.copy_error))
                    .setContentText(toPath)
                    .setContentIntent(PendingIntent.getActivity(context, 0, browseIntent,
                            PendingIntent.FLAG_CANCEL_CURRENT))
                    .setOngoing(false)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setSmallIcon(R.drawable.ic_stat_notify_paste_5)
                    .setTicker(context.getString(success ? R.string.copied : R.string.copy_error))
                    .setOnlyAlertOnce(true)
                    .build();
            notificationManager.notify(notId, not);
        }
    }

    public static void showMoveProgressNotification(int filesMoved, List<FileHolder> files,
                                                    String toPath, Context context) {
        Notification not = new NotificationCompat.Builder(context)
                .setAutoCancel(false)
                .setContentTitle(context.getString(R.string.moving))
                .setContentText(toPath)
                .setOngoing(true)
//                .addAction(android.R.drawable.ic_menu_close_clear_cancel,
//                        context.getString(android.R.string.cancel), null)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_stat_notify_paste)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(context.getResources().getString(R.string.notif_moving_item,
                                files.get(filesMoved).getName(), toPath)))
                .setTicker(context.getString(R.string.moving))
                .setOnlyAlertOnce(true)
                .build();

        storeStartTimeIfNeeded(files.hashCode());

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(files.hashCode(), not);
    }

    public static void showMoveDoneNotification(boolean success, List<FileHolder> files,
                                                String toPath, Context context) {
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        int notId = files.hashCode();

        if (!shouldShowDoneNotification(notId, success)) {
            notificationManager.cancel(notId);
        } else {
            Intent browseIntent = new Intent(context, FileManagerActivity.class);
            browseIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
            browseIntent.setData(Uri.fromFile(new File(toPath)));

            Notification not = new NotificationCompat.Builder(context)
                    .setAutoCancel(true)
                    .setContentTitle(context.getString(success ? R.string.moved : R.string.move_error))
                    .setContentText(toPath)
                    .setContentIntent(PendingIntent.getActivity(context, 0, browseIntent,
                            PendingIntent.FLAG_CANCEL_CURRENT))
                    .setOngoing(false)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setSmallIcon(R.drawable.ic_stat_notify_paste_5)
                    .setTicker(context.getString(success ? R.string.moved : R.string.move_error))
                    .setOnlyAlertOnce(true)
                    .build();
            notificationManager.notify(notId, not);
        }
    }

    public static void showNotEnoughSpaceNotification(long spaceNeeded, List<FileHolder> files,
                                                      String toPath, Context context) {
        Notification not = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setContentTitle(context.getString(R.string.notif_no_space))
                .setContentText(context.getString(R.string.notif_space_more,
                        FileUtils.formatSize(context, spaceNeeded)))
                .setContentInfo(toPath)
                .setOngoing(false)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setTicker(context.getString(R.string.notif_no_space))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(context.getString(R.string.notif_space_more,
                                FileUtils.formatSize(context, spaceNeeded))))
                .setOnlyAlertOnce(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(files.hashCode(), not);
    }

    public static void showCompressProgressNotification(int filesCompressed, int fileCount,
                                                        int notId, File zipFile, File srcFile,
                                                        Context context) {
        Notification not = new NotificationCompat.Builder(context)
                .setAutoCancel(false)
                .setContentTitle(context.getString(R.string.compressing))
                .setContentText(zipFile.getName())
                .setProgress(fileCount, filesCompressed, false)
                .setOngoing(true)
//                .addAction(android.R.drawable.ic_menu_close_clear_cancel,
//                        context.getString(android.R.string.cancel), null)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_stat_notify_compress)
                .setTicker(context.getString(R.string.compressing))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(
                        context.getString(R.string.notif_compressing_into,
                                srcFile.getName(), zipFile.getName())))
                .setOnlyAlertOnce(true)
                .build();

        storeStartTimeIfNeeded(notId);

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notId, not);
    }

    public static void showExtractProgressNotification(int extractedCount, int fileCount,
                                                       String fileName, String zipName, int notId,
                                                       Context context) {
        Notification not = new NotificationCompat.Builder(context)
                .setAutoCancel(false)
                .setContentTitle(context.getString(R.string.extracting))
                .setContentText(zipName)
                .setProgress(fileCount, extractedCount, false)
                .setOngoing(true)
//                .addAction(android.R.drawable.ic_menu_close_clear_cancel,
//                        context.getString(android.R.string.cancel), null)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_stat_notify_extract)
                .setTicker(context.getString(R.string.extracting))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(
                        context.getString(R.string.notif_extracting_from,
                                fileName, zipName)))
                .setOnlyAlertOnce(true)
                .build();

        storeStartTimeIfNeeded(notId);

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notId, not);
    }

    public static void showCompressDoneNotification(boolean success, int notId, File zipFile,
                                                    Context context) {
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (!shouldShowDoneNotification(notId, success)) {
            notificationManager.cancel(notId);
        } else {
            Intent browseIntent = new Intent(context, FileManagerActivity.class);
            browseIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
            browseIntent.setData(Uri.fromFile(zipFile.getParentFile()));

            Notification not = new NotificationCompat.Builder(context)
                    .setAutoCancel(true)
                    .setContentTitle(context.getString(success ? R.string.notif_compressed_success
                            : R.string.notif_compressed_fail))
                    .setContentText(zipFile.getName())
                    .setContentIntent(PendingIntent.getActivity(context, 0, browseIntent,
                            PendingIntent.FLAG_CANCEL_CURRENT))
                    .setOngoing(false)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setSmallIcon(R.drawable.ic_stat_notify_compress_5)
                    .setTicker(context.getString(success ? R.string.notif_compressed_success
                            : R.string.notif_compressed_fail))
                    .setOnlyAlertOnce(true)
                    .build();

            notificationManager.notify(notId, not);
        }
    }

    public static void showExtractDoneNotification(boolean success, int notId, File extractedTo,
                                                   Context context) {
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (!shouldShowDoneNotification(notId, success)) {
            notificationManager.cancel(notId);
        } else {
            Intent browseIntent = new Intent(context, FileManagerActivity.class);
            browseIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
            browseIntent.setData(Uri.fromFile(extractedTo));

            Notification not = new NotificationCompat.Builder(context)
                    .setAutoCancel(true)
                    .setContentTitle(context.getString(success ? R.string.notif_extracted_success
                            : R.string.notif_extracted_fail))
                    .setContentText(extractedTo.getName())
                    .setContentIntent(PendingIntent.getActivity(context, 0, browseIntent,
                            PendingIntent.FLAG_CANCEL_CURRENT))
                    .setOngoing(false)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setSmallIcon(R.drawable.ic_stat_notify_compress_5)
                    .setTicker(context.getString(success ? R.string.notif_extracted_success
                            : R.string.notif_extracted_fail))
                    .setOnlyAlertOnce(true)
                    .build();

            notificationManager.notify(notId, not);
        }
    }

    private static void storeStartTimeIfNeeded(int notId) {
        if (!notificationToStartTime.containsKey(notId)) {
            notificationToStartTime.put(notId, currentTimeMillis());
        }
    }

    private static boolean shouldShowDoneNotification(int notId, boolean operationSuccessful) {
        long progressDuration = currentTimeMillis() - notificationToStartTime.get(notId);
        boolean durationAboveBound = notificationToStartTime.containsKey(notId)
                && progressDuration >= DONE_NOTIF_LOWER_BOUND;
        return durationAboveBound || !operationSuccessful;
    }
}
