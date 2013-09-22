package com.nelson.umbrellaalarm.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.nelson.umbrellaalarm.R;

import java.util.Calendar;

public class Utils {

    public static final int NOTIFICATIONS_DISABLED = 0;
    public static final int AT_ALARM = 1;
    public static final int AT_TIME = 2;

    public static int getPreferenceInt(Context context, SharedPreferences sharedPreferences) {
        String notifyType = sharedPreferences.getString(context.getString(R.string.choose_dialog_key), "poop");
        boolean enabled = sharedPreferences.getBoolean(context.getString(R.string.notifications_key), true);
        if (!enabled) {
            return NOTIFICATIONS_DISABLED;
        } else if (notifyType.equals(context.getString(R.string.at_alarm_key))) {
            return AT_ALARM;
        } else if (notifyType.equals(context.getString(R.string.at_time_key))) {
            return AT_TIME;
        } else {
            Log.wtf("getPreferenceInt", "This should never happen");
            return -1;
        }
    }

    public static long getMillisToTime(int hour, int min) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, min);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DATE, 1);
        }
        return calendar.getTimeInMillis();
    }
}
