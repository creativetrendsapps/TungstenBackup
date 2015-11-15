package com.creativetrends.tungsten.dialogs;

import com.creativetrends.tungsten.activities.AppInfo;
import com.creativetrends.tungsten.activities.TungstenBackup;
import com.creativetrends.tungsten.R;

import android.app.AlertDialog;
import android.app.Dialog;
//import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class BackupOptionsDialogFragment extends DialogFragment
{
    final static String TAG = TungstenBackup.TAG;
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Bundle arguments = getArguments();
        final AppInfo appInfo = arguments.getParcelable("appinfo");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(appInfo.getLabel());
        builder.setMessage(R.string.backup);
        if(appInfo.getSourceDir().length() > 0)
        {
            builder.setNegativeButton(R.string.handleApk, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    TungstenBackup obackup = (TungstenBackup) getActivity();
                    obackup.callBackup(appInfo, AppInfo.MODE_APK);
                }
            });
            builder.setPositiveButton(R.string.handleBoth, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    TungstenBackup obackup = (TungstenBackup) getActivity();
                    obackup.callBackup(appInfo, AppInfo.MODE_BOTH);
                }
            });
        }
        builder.setNeutralButton(R.string.handleData, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                TungstenBackup obackup = (TungstenBackup) getActivity();
                obackup.callBackup(appInfo, AppInfo.MODE_DATA);
            }
        });
        return builder.create();
    }
}
