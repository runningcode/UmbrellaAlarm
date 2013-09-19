package com.nelson.umbrellaalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enabled =  prefs.getBoolean(context.getString(R.string.notifications_key), true);
        if (!enabled) {
            return;
        }
        boolean useAlarm = prefs.getBoolean(context.getString(R.string.at_alarm_key), true);
        if (useAlarm) {
            Intent startServiceIntent = new Intent(context, CheckAlarmService.class);
            context.startService(startServiceIntent);
        }
    }
}
