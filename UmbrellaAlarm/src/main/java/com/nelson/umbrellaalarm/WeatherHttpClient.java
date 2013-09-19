package com.nelson.umbrellaalarm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.UnknownHostException;

public class WeatherHttpClient {

    private static final String FORECAST_WEATHER_URL = "http://api.openweathermap.org/data/2.5/forecast?";
    public static final String UNKNOWN_HOST = "UNKNOWN_HOST";

    public String getWeatherData(String location) {
        HttpURLConnection httpURLConnection = null;
        InputStream is = null;

        try {
            URL url = new URL(FORECAST_WEATHER_URL + location);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.connect();

            StringBuilder buffer = new StringBuilder();
            is = httpURLConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                buffer.append(line);
                buffer.append("\r\n");
            }

            is.close();
            httpURLConnection.disconnect();
            return buffer.toString();
        } catch (UnknownHostException e) {
            // probably no internet
            e.printStackTrace();
            return UNKNOWN_HOST;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try { is.close(); } catch (Throwable t) {t.printStackTrace();}
            try { httpURLConnection.disconnect(); } catch (Throwable t) {t.printStackTrace();}
        }
        return null;
    }
}
