package com.nelson.umbrellaalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.nelson.umbrellaalarm.utils.UmbrellaLogger;
import com.nelson.umbrellaalarm.utils.Utils;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger logger = UmbrellaLogger.getLogger();
        logger.info("onReceive boot completed");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        switch (Utils.getPreferenceInt(context, prefs)) {
            case Utils.NOTIFICATIONS_DISABLED:
                logger.info("notifications are not enabled, returning");
                return;
            case Utils.AT_ALARM:
                logger.info("using alarm method starting service");
                Intent startServiceIntent = new Intent(context, CheckAlarmService.class);
                context.startService(startServiceIntent);
                break;
            case Utils.AT_TIME:
                int hourOfDay = prefs.getInt(context.getString(R.string.time_hour_key), 9);
                int minute = prefs.getInt(context.getString(R.string.time_min_key), 0);
                logger.info("using at time method creating alarm at " + hourOfDay + ":" + minute);

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                Intent timeIntent = new Intent(context, UmbrellaAlarmService.class);
                PendingIntent pendingIntent = PendingIntent.getService(context, 0, timeIntent, 0);
                long triggerAtMillis = Utils.getMillisToTime(hourOfDay, minute);
                alarmManager.setRepeating(AlarmManager.RTC, triggerAtMillis, TimeUnit.DAYS.toMillis(1), pendingIntent);
                break;
        }
    }
}
