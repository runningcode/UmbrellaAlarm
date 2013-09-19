package com.nelson.umbrellaalarm.Models;

import java.util.concurrent.TimeUnit;

public class Forecast {
    private long timeStamp;
    private Temperature temperature;
    private double mPressure;
    private double mHumidity;
    private ForecastCondition forecastCondition;
    private Clouds clouds;
    private Wind wind;
    private Rain rain;
    private Snow snow;

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setTimeStampSeconds(long timeStamp) {
        this.timeStamp = timeStamp * TimeUnit.SECONDS.toMillis(1);
    }

    public ForecastCondition getForecastCondition() {
        return forecastCondition;
    }

    public void setForecastCondition(ForecastCondition forecastCondition) {
        this.forecastCondition = forecastCondition;
    }

    public Temperature getTemperature() {
        return temperature;
    }

    public void setTemperature(Temperature temperature) {
        this.temperature = temperature;
    }

    public double getPressure() {
        return mPressure;
    }
    public void setPressure(double pressure) {
        this.mPressure = pressure;
    }
    public double getHumidity() {
        return mHumidity;
    }
    public void setHumidity(double humidity) {
        this.mHumidity = humidity;
    }

    public Snow getSnow() {
        return snow;
    }

    public void setSnow(Snow snow) {
        this.snow = snow;
    }

    public Clouds getClouds() {
        return clouds;
    }

    public void setClouds(Clouds clouds) {
        this.clouds = clouds;
    }

    public Wind getWind() {
        return wind;
    }

    public void setWind(Wind wind) {
        this.wind = wind;
    }

    public Rain getRain() {
        return rain;
    }

    public void setRain(Rain rain) {
        this.rain = rain;
    }
}
