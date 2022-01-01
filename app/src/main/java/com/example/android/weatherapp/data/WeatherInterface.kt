package com.example.android.weatherapp.data

import com.example.android.weatherapp.pojo.WeatherData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherInterface {

    @GET("current.json?")
    suspend fun getWeatherData(@Query("q") city: String,
                       @Query("key") key: String ,
                       @Query("aqi") aqi: String) : WeatherData
}