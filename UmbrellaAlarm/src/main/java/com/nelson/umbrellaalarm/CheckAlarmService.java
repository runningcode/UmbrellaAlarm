package com.nelson.umbrellaalarm;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.nelson.umbrellaalarm.utils.UmbrellaLogger;
import com.nelson.umbrellaalarm.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class CheckAlarmService extends IntentService {

    public static final int ALARM_REQUEST_CODE = 0;
    public static final int REPEATING_REQUEST_CODE = 1;

    public CheckAlarmService() {
        super("CheckAlarmService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(CheckAlarmService.this);
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Logger logger = UmbrellaLogger.getLogger();
        logger.info("handling intent");

        // repeating check service pending intent
        Intent repeat = new Intent(CheckAlarmService.this, CheckAlarmService.class);
        PendingIntent repeatingPendingIntent = PendingIntent.getService(CheckAlarmService.this, REPEATING_REQUEST_CODE, repeat, 0);

        // check weather pending intent
        Intent alarmIntent = new Intent(CheckAlarmService.this, UmbrellaAlarmService.class);
        PendingIntent alarmPendingIntent = PendingIntent.getService(CheckAlarmService.this, ALARM_REQUEST_CODE, alarmIntent, 0 );

        switch (Utils.getPreferenceInt(CheckAlarmService.this, prefs)) {
            case Utils.NOTIFICATIONS_DISABLED:
                logger.info("notifications disabled");
                alarmManager.cancel(alarmPendingIntent);
            case Utils.AT_TIME:
                logger.info("at time");
                alarmManager.cancel(repeatingPendingIntent);
                break;
            case Utils.AT_ALARM:
                logger.info("at alarm");
                // check if user has set any new alarms every hour
                long hourInMillis = TimeUnit.HOURS.toMillis(1);
                long startTime = System.currentTimeMillis() + hourInMillis;
                alarmManager.setInexactRepeating(AlarmManager.RTC, startTime, hourInMillis, repeatingPendingIntent);
                // set alarm for next the user's next alarm
                long nextAlarm = getNextAlarm(this);
                if (nextAlarm != -1) {
                    alarmManager.set(AlarmManager.RTC, nextAlarm, alarmPendingIntent);
                }
                break;
        }
    }

    private long getNextAlarm(Context context) {
        String nextAlarm = Settings.System.getString(context.getContentResolver(),Settings.System.NEXT_ALARM_FORMATTED);

        if ((nextAlarm==null) || ("".equals(nextAlarm))) return -1;

        Calendar calendar = Calendar.getInstance();
        Calendar alarmCalendar = Calendar.getInstance();

        try {
            // java time libraries are terrible :'( this makes me cry
            SimpleDateFormat alarmDateFormat = new SimpleDateFormat("EE hh:mm");
            Date alarmDate = alarmDateFormat.parse(nextAlarm);
            alarmCalendar.setTime(alarmDate);
            calendar.set(Calendar.DAY_OF_WEEK, alarmCalendar.get(Calendar.DAY_OF_WEEK));
            calendar.set(Calendar.HOUR_OF_DAY, alarmCalendar.get(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, alarmCalendar.get(Calendar.MINUTE));
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            // add a week in case we adjust the day of week to be in the past
            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 7);
            }
            return calendar.getTimeInMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}