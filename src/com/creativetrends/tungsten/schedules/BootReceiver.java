package com.creativetrends.tungsten.schedules;

import com.creativetrends.tungsten.activities.TungstenBackup;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class BootReceiver extends BroadcastReceiver
{
    static final String TAG = TungstenBackup.TAG;
    
    SharedPreferences prefs;
    HandleAlarms handleAlarms;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        handleAlarms = new HandleAlarms(context);
        prefs = context.getSharedPreferences("schedules", 0);
        for(int i = 0; i <= prefs.getInt("total", 0); i++)
        {
            if(prefs.getBoolean("enabled" + i, false) && prefs.getInt("repeatTime" + i, 0) > 0)
            {
                long timePlaced = prefs.getLong("timePlaced" + i, 0);
                long repeat = (long)(prefs.getInt("repeatTime" + i, 0) * AlarmManager.INTERVAL_DAY);
                long timePassed = System.currentTimeMillis() - timePlaced;
                long hourOfDay = handleAlarms.timeUntilNextEvent(0, prefs.getInt("hourOfDay" + i, 0));
                long timeLeft = prefs.getLong("timeUntilNextEvent" + i, 0) - timePassed;
                if(timeLeft < (5 * 60000))
                {
                    handleAlarms.setAlarm(i, AlarmManager.INTERVAL_FIFTEEN_MINUTES, repeat);
                }
                else if(timeLeft < (24 * AlarmManager.INTERVAL_HOUR))
                {
                    if(hourOfDay > 0)
                    {
                        handleAlarms.setAlarm(i, hourOfDay, repeat);
                    }
                    else
                    {
                        handleAlarms.setAlarm(i, AlarmManager.INTERVAL_FIFTEEN_MINUTES, repeat);
                    }
                }
                else
                {
                    handleAlarms.setAlarm(i, timeLeft, repeat);
                }
            }
        }
    }
}