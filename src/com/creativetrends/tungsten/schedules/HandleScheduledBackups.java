package com.creativetrends.tungsten.schedules;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.creativetrends.tungsten.activities.AppInfo;
import com.creativetrends.tungsten.activities.AppInfoHelper;
import com.creativetrends.tungsten.activities.BackupRestoreHelper;
import com.creativetrends.tungsten.activities.Crypto;
import com.creativetrends.tungsten.activities.FileCreationHelper;
import com.creativetrends.tungsten.activities.FileReaderWriter;
import com.creativetrends.tungsten.activities.TungstenBackup;
import com.creativetrends.tungsten.R;
import com.creativetrends.tungsten.activities.ShellCommands;
import com.creativetrends.tungsten.ui.NotificationHelper;

public class HandleScheduledBackups
{
    static final String TAG = TungstenBackup.TAG;

    Context context;
    PowerManager powerManager;
    ShellCommands shellCommands;
    SharedPreferences prefs;
    File backupDir;
    public HandleScheduledBackups(Context context)
    {
        this.context = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        shellCommands = new ShellCommands(prefs);
        powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    }
    public void initiateBackup(final int id, final int mode, final int subMode, final boolean excludeSystem)
    {
        new Thread(new Runnable()
        {
            public void run()
            {
                String backupDirPath = prefs.getString("pathBackupFolder", FileCreationHelper.getDefaultBackupDirPath());
                backupDir = new File(backupDirPath);
                ArrayList<AppInfo> list = AppInfoHelper.getPackageInfo(context, backupDir, false);
                ArrayList<AppInfo> listToBackUp;
                switch(mode)
                {
                    case 0:
                        // all apps
                        Collections.sort(list);
                        backup(list, subMode);
                        break;
                    case 1:
                        // user apps
                        listToBackUp = new ArrayList<AppInfo>();
                        for(AppInfo appInfo : list)
                        {
                            if(!appInfo.isSystem())
                            {
                                listToBackUp.add(appInfo);
                            }
                        }
                        Collections.sort(listToBackUp);
                        backup(listToBackUp, subMode);
                        break;
                    case 2:
                        // system apps
                        listToBackUp = new ArrayList<AppInfo>();
                        for(AppInfo appInfo : list)
                        {
                            if(appInfo.isSystem())
                            {
                                listToBackUp.add(appInfo);
                            }
                        }
                        Collections.sort(listToBackUp);
                        backup(listToBackUp, subMode);
                        break;
                    case 3:
                        // new and updated apps
                        listToBackUp = new ArrayList<AppInfo>();
                        for(AppInfo appInfo : list)
                        {
                            if((!excludeSystem || !appInfo.isSystem()) && (appInfo.getLogInfo() == null || (appInfo.getVersionCode() > appInfo.getLogInfo().getVersionCode())))
                            {
                                listToBackUp.add(appInfo);
                            }
                        }
                        Collections.sort(listToBackUp);
                        backup(listToBackUp, subMode);
                        break;
                    case 4:
                        // custom package list
                        listToBackUp = new ArrayList<AppInfo>();
                        FileReaderWriter frw = new FileReaderWriter(prefs.getString("pathBackupFolder", FileCreationHelper.defaultBackupDirPath), Scheduler.SCHEDULECUSTOMLIST + id);
                        for(AppInfo appInfo : list)
                        {
                            if(frw.contains(appInfo.getPackageName()))
                            {
                                listToBackUp.add(appInfo);
                            }
                        }
                        Collections.sort(listToBackUp);
                        backup(listToBackUp, subMode);
                        break;
                }
            }
        }).start();
    }
    public void backup(final ArrayList<AppInfo> backupList, final int subMode)
    {
        if(backupDir != null)
        {
            new Thread(new Runnable()
            {
                public void run()
                {
                    Crypto crypto = null;
                    if(prefs.getBoolean("enableCrypto", false) && Crypto.isAvailable(context))
                    {
                        crypto = new Crypto(prefs);
                        crypto.bind(context);
                    }
                    PowerManager.WakeLock wl = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
                    if(prefs.getBoolean("acquireWakelock", true))
                    {
                        wl.acquire();
                        Log.i(TAG, "wakelock acquired");
                    }
                    int id = (int) System.currentTimeMillis();
                    int total = backupList.size();
                    int i = 1;
                    boolean errorFlag = false;
                    for(AppInfo appInfo : backupList)
                    {
                        String title = context.getString(R.string.backupProgress) + " (" + i + "/" + total + ")";
                        NotificationHelper.showNotification(context, TungstenBackup.class, id, title, appInfo.getLabel(), false);
                        int ret = BackupRestoreHelper.backup(context, backupDir, appInfo, shellCommands, subMode);
                        if(ret != 0)
                            errorFlag = true;
                        else if(crypto != null)
                        {
                            crypto.encryptFromAppInfo(context, backupDir, appInfo, subMode, prefs);
                            if(crypto.isErrorSet())
                            {
                                Crypto.cleanUpEncryptedFiles(new File(backupDir, appInfo.getPackageName()), appInfo.getSourceDir(), appInfo.getDataDir(), subMode, prefs.getBoolean("backupExternalFiles", false));
                                errorFlag = true;
                            }
                        }
                        if(i == total)
                        {
                            String notificationTitle = errorFlag ? context.getString(R.string.batchFailure) : context.getString(R.string.batchSuccess);
                            String notificationMessage = context.getString(R.string.sched_notificationMessage);
                            NotificationHelper.showNotification(context, TungstenBackup.class, id, notificationTitle, notificationMessage, true);
                        }
                        i++;
                    }
                    if(wl.isHeld())
                    {
                        wl.release();
                        Log.i(TAG, "wakelock released");
                    }
                }
            }).start();
        }
    }
}
