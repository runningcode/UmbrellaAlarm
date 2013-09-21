package com.nelson.umbrellaalarm;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.Locale;

public class WeatherHttpClient {

    private static final String FORECAST_WEATHER_URL = "http://api.openweathermap.org/data/2.5/forecast?lang=";
    public static final String UNKNOWN_HOST = "UNKNOWN_HOST";

    public String getWeatherData(String location) {
        String languageCode = getLanguageCode();
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(FORECAST_WEATHER_URL + languageCode + "&" + location);

        try {
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            if (httpEntity != null) {
                InputStream inputStream = httpEntity.getContent();
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                StringBuilder buffer = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    buffer.append(line);
                    buffer.append("\r\n");
                }
                return buffer.toString();
            }
        } catch (UnknownHostException e) {
            // probably no internet
            e.printStackTrace();
            return UNKNOWN_HOST;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getLanguageCode() {
        String language = Locale.getDefault().getLanguage();
        if (language.equals("fr")) {
            return language;
        } else if (language.equals("es")) {
            // OpenWeatherMap api uses a non-standard code for spanish
            return "sp";
        } else if (language.equals("de")) {
            return language;
        }
        // default to english if we don't support the language
        return "en";
    }
}
