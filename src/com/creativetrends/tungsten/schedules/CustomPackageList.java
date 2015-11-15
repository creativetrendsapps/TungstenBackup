package com.creativetrends.tungsten.schedules;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;

import com.creativetrends.tungsten.activities.AppInfo;
import com.creativetrends.tungsten.activities.FileCreationHelper;
import com.creativetrends.tungsten.activities.FileReaderWriter;
import com.creativetrends.tungsten.activities.TungstenBackup;
import com.creativetrends.tungsten.R;

public class CustomPackageList
{
    static ArrayList<AppInfo> appInfoList = TungstenBackup.appInfoList;
    // for use with schedules
    public static void showList(Activity activity, int number)
    {
        showList(activity, Scheduler.SCHEDULECUSTOMLIST + number);
    }
    public static void showList(Activity activity, String filename)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        final FileReaderWriter frw = new FileReaderWriter(prefs.getString("pathBackupFolder", FileCreationHelper.getDefaultBackupDirPath()), filename);
        final CharSequence[] items = collectItems();
        final ArrayList<Integer> selected = new ArrayList<Integer>();
        boolean[] checked = new boolean[items.length];
        for(int i = 0; i < items.length; i++)
        {
            if(frw.contains(items[i].toString()))
            {
                checked[i] = true;
                selected.add(i);
            }
        }
        new AlertDialog.Builder(activity)
            .setTitle(R.string.customListTitle)
            .setMultiChoiceItems(items, checked, new DialogInterface.OnMultiChoiceClickListener()
            {
                public void onClick(DialogInterface dialog, int id, boolean isChecked)
                {
                    if(isChecked)
                    {
                        selected.add(id);
                    }
                    else
                    {
                        selected.remove((Integer) id); // cast as Integer to distinguish between remove(Object) and remove(index)
                    }
                }
            })
            .setPositiveButton(R.string.dialogOK, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    handleSelectedItems(frw, items, selected);
                }
            })
            .setNegativeButton(R.string.dialogCancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id){}})
            .show();
    }
    private static CharSequence[] collectItems()
    {
        ArrayList<String> list = new ArrayList<String>();
        for(AppInfo appInfo : appInfoList)
        {
            list.add(appInfo.getPackageName());
        }
        return list.toArray(new CharSequence[list.size()]);
    }
    private static void handleSelectedItems(FileReaderWriter frw, CharSequence[] items, ArrayList<Integer> selected)
    {
        frw.clear();
        for(int pos : selected)
        {
            frw.putString(items[pos].toString(), true);
        }
    }
}
