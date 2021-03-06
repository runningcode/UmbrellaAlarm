package com.nelson.umbrellaalarm;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.nelson.umbrellaalarm.Models.Forecast;
import com.nelson.umbrellaalarm.Models.ForecastCondition;
import com.nelson.umbrellaalarm.Models.Weather;
import com.nelson.umbrellaalarm.utils.UmbrellaLogger;

import org.json.JSONException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class UmbrellaAlarmService extends IntentService {

    private static final int RETRY_TIMEOUT_MINS = 5;

    private NotificationManager mNotificationManager;
    private LocationManager mLocationManager;
    private Logger logger;

    public UmbrellaAlarmService() {
        super("UmbrellaAlarmService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        logger = UmbrellaLogger.getLogger();
        logger.info("UmbrellaAlarmService onCreate");
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        logger.info("UmbrellaAlarmService onHandleIntent");
        Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location == null) {
            logger.info("UmbrellaAlarmService onHandleIntent location was null");
            // TODO save something that shows that location services were disabled
            Toast.makeText(getBaseContext(), "Location Disabled", Toast.LENGTH_SHORT).show();
            return;
        }

        String locationString = "lat=" + location.getLatitude() + "&lon=" + location.getLongitude();
        String data = new WeatherHttpClient().getWeatherData(locationString);

        if (data == null || data.equals("") || data.equals(WeatherHttpClient.UNKNOWN_HOST)) {
            logger.info("UmbrellaAlarmService onHandleIntent returned data was bad");
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
            alarmManager.set(AlarmManager.RTC, TimeUnit.MINUTES.toMillis(RETRY_TIMEOUT_MINS), pendingIntent);
            return;
        }

        try {
            Weather weather = JSONWeatherParser.getWeather(data);
            long currentTime = System.currentTimeMillis();
            ArrayList<Date> timeStamps = new ArrayList<Date>();
            ArrayList<String> descriptions = new ArrayList<String>();
            for (Forecast forecast : weather.getForecasts()) {
                long timeStamp = forecast.getTimeStamp();
                if (timeStamp > currentTime + TimeUnit.DAYS.toMillis(1)) {
                    break;
                }
                ForecastCondition forecastCondition = forecast.getForecastCondition();
                if (forecastCondition.getCondition().equals("Rain")) {
                    timeStamps.add(new Date(timeStamp));
                    descriptions.add(forecastCondition.getDescription());
                }
            }
            if (!timeStamps.isEmpty()) {
                String contentStringBuilder = createNotificationString(timeStamps, descriptions);
                notifyUser(weather.getWeatherLocation().getCityName(), contentStringBuilder);
            }
            logger.info("UmbrellaAlarmService onHandleIntent timestamps were empty, aka, no rain");
        } catch (JSONException e) {
            logger.info("UmbrellaAlarmService onHandleIntent a JSON Exception occurred");
            e.printStackTrace();
        }
    }

    private String createNotificationString(ArrayList<Date> timeStamps, ArrayList<String> descriptions) {
        DateFormat dateFormat = new SimpleDateFormat("hh:mm aa");
        StringBuilder contentStringBuilder = new StringBuilder();
        for (int i = 0; i < timeStamps.size(); i++) {
            if (i > 0) {
                contentStringBuilder.append(getString(R.string.and));
            }
            contentStringBuilder.append(String.format(getString(R.string.rain_description), descriptions.get(i), dateFormat.format(timeStamps.get(i))));
        }
        return String.format(getString(R.string.bring_umbrella), contentStringBuilder.toString());
    }

    private void notifyUser(String cityName, String contentString) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(UmbrellaAlarmService.this);
        boolean soundEnabled = sharedPreferences.getBoolean(getString(R.string.sound_key), true);
        boolean vibrateEnabled = sharedPreferences.getBoolean(getString(R.string.vibration_key), true);

        int defaults = Notification.DEFAULT_LIGHTS;
        if (soundEnabled) {
            defaults |= Notification.DEFAULT_SOUND;
        }
        if (vibrateEnabled) {
            defaults |= Notification.DEFAULT_VIBRATE;
        }

        // this pending intent is only there because of a bug in gingerbread
        PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, new Intent(), 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(contentString)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_launcher)
                .setOnlyAlertOnce(true)
                .setDefaults(defaults)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(contentString)
                        .setSummaryText(cityName))
                .build();
        mNotificationManager.notify(1, notification);
    }
}
