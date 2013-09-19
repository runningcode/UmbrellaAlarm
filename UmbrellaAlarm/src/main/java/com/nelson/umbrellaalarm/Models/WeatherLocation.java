package com.nelson.umbrellaalarm.Models;

public class WeatherLocation {

    private Double mLatitude;
    private Double mLongitude;
    private String mCityName;

    public WeatherLocation(){
    }

    public WeatherLocation(Double latitude, Double longitude){
        mLatitude = latitude;
        mLongitude = longitude;
    }

    public Double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(Double latitude) {
        mLatitude = latitude;
    }

    public Double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(Double longitude) {
        mLongitude = longitude;
    }

    public String getCityName() {
        return mCityName;
    }

    public void setCityName(String cityName) {
        mCityName = cityName;
    }
}
