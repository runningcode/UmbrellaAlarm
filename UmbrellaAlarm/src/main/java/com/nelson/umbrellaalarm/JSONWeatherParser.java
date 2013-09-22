package com.nelson.umbrellaalarm;

import com.nelson.umbrellaalarm.Models.Forecast;
import com.nelson.umbrellaalarm.Models.ForecastCondition;
import com.nelson.umbrellaalarm.Models.Rain;
import com.nelson.umbrellaalarm.Models.Weather;
import com.nelson.umbrellaalarm.Models.WeatherLocation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JSONWeatherParser {
    public static Weather getWeather(String data) throws JSONException {
        WeatherLocation weatherLocation = new WeatherLocation();

        // We create out JSONObject from the data
        JSONObject jObj = new JSONObject(data);

        JSONObject cityObject = jObj.getJSONObject("city");
        weatherLocation.setCityName(cityObject.getString("name"));

        JSONObject coordObject = cityObject.getJSONObject("coord");
        weatherLocation.setLatitude(coordObject.getDouble("lat"));
        weatherLocation.setLongitude(coordObject.getDouble("lon"));

        int arrayLength = jObj.getInt("cnt");
        JSONArray forecastArray = jObj.getJSONArray("list");

        ArrayList<Forecast> forecastArrayList = new ArrayList<Forecast>();

        for (int i = 0; i < arrayLength; i++) {
            JSONObject JSONForecast = forecastArray.getJSONObject(i);
            Forecast forecast = new Forecast();
            forecast.setTimeStampSeconds(JSONForecast.getLong("dt"));

            ForecastCondition forecastCondition = new ForecastCondition();
            JSONObject weather = JSONForecast.getJSONArray("weather").getJSONObject(0);
            forecastCondition.setWeatherId(weather.getInt("id"));
            forecastCondition.setDescription(weather.getString("description"));
            forecastCondition.setCondition(weather.getString("main"));
            forecastCondition.setIcon(weather.getString("icon"));
            forecast.setForecastCondition(forecastCondition);

            if (JSONForecast.has("rain")) {
                JSONObject rainObject = JSONForecast.getJSONObject("rain");
                Rain rain = new Rain();
                rain.setTime("3h");
                rain.setAmount(rainObject.getInt("3h"));
                forecast.setRain(rain);
            }
            forecastArrayList.add(forecast);
        }
        return new Weather(weatherLocation, forecastArrayList);
    }
}
