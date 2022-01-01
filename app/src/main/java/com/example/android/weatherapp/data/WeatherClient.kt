package com.example.android.weatherapp.data

import com.example.android.weatherapp.pojo.WeatherData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherClient {
    private lateinit var City :String
    private val apiKey = "ca439864b32c4d948b3155211213012"
    private val aqi = "no"
    private val apiURL = "https://api.weatherapi.com/v1/"

    var retrofit:WeatherInterface = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(apiURL)
        .build()
        .create(WeatherInterface::class.java)

    public fun setCityName( city: String){
        City = city
    }
   suspend fun getData(): WeatherData {
        return retrofit.getWeatherData(City,apiKey,aqi)
    }
}