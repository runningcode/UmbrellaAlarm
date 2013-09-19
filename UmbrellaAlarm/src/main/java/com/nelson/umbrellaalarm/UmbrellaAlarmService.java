package com.nelson.umbrellaalarm;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.nelson.umbrellaalarm.Models.Forecast;
import com.nelson.umbrellaalarm.Models.ForecastCondition;

import org.json.JSONException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class UmbrellaAlarmService extends IntentService {

    private static final int RETRY_TIMEOUT_MINS = 5;

    private NotificationManager mNotificationManager;
    private LocationManager mLocationManager;

    public UmbrellaAlarmService() {
        super("UmbrellaAlarmService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location == null) {
            // TODO save something that shows that location services were disabled
            Toast.makeText(getBaseContext(), "Location Disabled", Toast.LENGTH_SHORT).show();
            return;
        }

        String locationString = "lat=" + location.getLatitude() + "&lon=" + location.getLongitude();
        String data = new WeatherHttpClient().getWeatherData(locationString);

        if (data.equals(WeatherHttpClient.UNKNOWN_HOST)) {
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
                DateFormat dateFormat = new SimpleDateFormat("hh:mm aa");
                StringBuilder contentStringBuilder = new StringBuilder("You should bring your umbrella because ");

                for (int i = 0; i < timeStamps.size(); i++) {
                    if (i > 0) {
                        contentStringBuilder.append(" and ");
                    }
                    contentStringBuilder.append("there will be ");
                    contentStringBuilder.append(descriptions.get(i));
                    contentStringBuilder.append(" from ");
                    contentStringBuilder.append(dateFormat.format(timeStamps.get(i)));
                    contentStringBuilder.append(" to ");
                    contentStringBuilder.append(dateFormat.format(new Date(timeStamps.get(i).getTime()+ TimeUnit.HOURS.toMillis(3))));
                }
                Notification notification = new NotificationCompat.Builder(this)
                        .setContentTitle("Umbrella Alarm")
                        .setContentText(contentStringBuilder.toString())
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setOnlyAlertOnce(true)
                        .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(contentStringBuilder.toString())
                            .setSummaryText(weather.getWeatherLocation().getCityName()))
                        .build();
                mNotificationManager.notify(1, notification);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
