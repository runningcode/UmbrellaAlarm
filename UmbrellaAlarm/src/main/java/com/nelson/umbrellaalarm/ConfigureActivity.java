package com.nelson.umbrellaalarm;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.widget.TimePicker;

import com.nelson.umbrellaalarm.utils.UmbrellaLogger;
import com.nelson.umbrellaalarm.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ConfigureActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener, TimePickerDialog.OnTimeSetListener {

    private static final int TIME_PICKER_DIALOG = 0;
    private static final boolean TESTING = true;

    private static String notificationsKey;
    private static String chooseDialogKey;

    private SharedPreferences sharedPreferences;
    private ListPreference mTimeListPreference;
    private Logger mLogger;

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ConfigureActivity.this);
        mLogger = UmbrellaLogger.getLogger();
        mLogger.info("oncreate");
        notificationsKey = getString(R.string.notifications_key);
        chooseDialogKey = getString(R.string.choose_dialog_key);
        addPreferencesFromResource(R.xml.preferences);
        mTimeListPreference = (ListPreference) super.findPreference(chooseDialogKey);
        if (TESTING) {
            Preference preference = new Preference(this);
            preference.setTitle("candy");
            preference.setKey("cool");
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(ConfigureActivity.this, UmbrellaAlarmService.class);
                    startService(i);
                    return false;
                }
            });
            PreferenceScreen preferenceScreen = getPreferenceScreen();
            if (preferenceScreen != null) {
                preferenceScreen.addPreference(preference);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        switch (Utils.getPreferenceInt(ConfigureActivity.this, sharedPreferences)) {
            case Utils.NOTIFICATIONS_DISABLED:
                mLogger.info("notifications disabled");
                break;
            case Utils.AT_ALARM:
                mLogger.info("at alarm");
                startCheckAlarmService();
                setAlarmSummaryText();
                break;
            case Utils.AT_TIME:
                mLogger.info("at time");
                startCheckAlarmService();
                int hour = sharedPreferences.getInt(getString(R.string.time_hour_key), 9);
                int minute = sharedPreferences.getInt(getString(R.string.time_min_key), 0);
                setAtTimeSummary(hour, minute);
                break;
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        String notifyType = sharedPreferences.getString(chooseDialogKey, "poop");
        if (key.equals(notificationsKey)) {
            boolean enabled = sharedPreferences.getBoolean(notificationsKey, true);
            if (enabled && notifyType.equals(getString(R.string.at_alarm_key))) {
                startCheckAlarmService();
            }
        } else if (key.equals(chooseDialogKey)) {
            if (notifyType.equals(getString(R.string.at_alarm_key))) {
                setAlarmSummaryText();
            } else if (notifyType.equals(getString(R.string.at_time_key))) {
                showDialog(TIME_PICKER_DIALOG);
            } else if (notifyType.equals("poop")) {
                Log.wtf(getClass().toString(), "This should never happen");
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    protected Dialog onCreateDialog(int id, Bundle args) {
        switch (id) {
            case TIME_PICKER_DIALOG:
                int hour = sharedPreferences.getInt(getString(R.string.time_hour_key), 9);
                int min = sharedPreferences.getInt(getString(R.string.time_min_key), 0);
                new TimePickerDialog(ConfigureActivity.this, this, hour, min, false).show();
                break;
        }
        return super.onCreateDialog(id, args);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(getString(R.string.time_hour_key), hourOfDay);
        editor.putInt(getString(R.string.time_min_key), minute);
        editor.commit();

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(getBaseContext(), UmbrellaAlarmService.class);
        PendingIntent pendingIntent = PendingIntent.getService(getBaseContext(), 0, intent, 0);
        long triggerAtMillis = Utils.getMillisToTime(hourOfDay, minute);
        alarmManager.setRepeating(AlarmManager.RTC, triggerAtMillis, TimeUnit.DAYS.toMillis(1), pendingIntent);

        setAtTimeSummary(hourOfDay, minute);
    }

    private void setAlarmSummaryText() {
        String nextAlarm = Settings.System.getString(getContentResolver(), Settings.System.NEXT_ALARM_FORMATTED);
        if (nextAlarm != null && !nextAlarm.equals("")) {
            mTimeListPreference.setSummary(String.format(getString(R.string.at_alarm_summary), nextAlarm));
        } else {
            mTimeListPreference.setSummary(getString(R.string.at_alarm_no_alarm_summary));
        }
    }

    private void setAtTimeSummary(int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");
        mTimeListPreference.setSummary(String.format(getString(R.string.at_time_summary), sdf.format(calendar.getTime())));
    }

    private void startCheckAlarmService() {
        Intent startServiceIntent = new Intent(getApplicationContext(), CheckAlarmService.class);
        startService(startServiceIntent);
    }
}
