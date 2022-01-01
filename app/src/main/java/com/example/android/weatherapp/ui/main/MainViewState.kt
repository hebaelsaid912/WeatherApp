package com.example.android.weatherapp.ui.main

import com.example.android.weatherapp.pojo.WeatherData

sealed class MainViewState {

    // idle
    object Idle: MainViewState()

    //result
    data class getWeatherData(var weatherData: WeatherData ) : MainViewState()

    // error
    data class getError(var result: String ) : MainViewState()

    // loading
    data class getLoading(var result: Boolean ) : MainViewState()
}