package com.example.android.weatherapp.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.weatherapp.data.WeatherClient
import com.example.android.weatherapp.pojo.WeatherData
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.await
import java.lang.Exception

class WeatherViewModel() : ViewModel() {
    val intentChannel = Channel<MainIntent>(Channel.UNLIMITED)
    private val _viewState = MutableStateFlow<MainViewState>(MainViewState.Idle)
    public val state: StateFlow<MainViewState> get() = _viewState

    private var city: String = ""
    public fun setCityValue(city:String){
         this.city = city
    }
    public fun getCityValue():String{
        return city
    }

    private val weatherClient: WeatherClient by lazy {
        WeatherClient()
    }

    init {
            processIntent()
    }

    // process
    private fun processIntent() {
        Log.d("WeatherViewModel","1111111")
        viewModelScope.launch {
            intentChannel.consumeAsFlow().collect() {
                Log.d("WeatherViewModel","22222222")

                when (it) {
                    is MainIntent.getWeather -> getWeatherData()
                }
            }
        }
    }

    // reduce
    private fun getWeatherData() {
        viewModelScope.launch {
            val weatherData = async { getWeather() }
            _viewState.value = try {
                Log.d("WeatherViewModel","3333333333 ${weatherData.await()}")
                MainViewState.getWeatherData(weatherData.await())
            } catch (e: Exception) {
                Log.d("WeatherViewModel","44444444444 ${e.message}")
                MainViewState.getError(e.message!!)
            }
            Log.d("WeatherViewModel","********* ${_viewState.value}")
        }
    }

    private fun getWeather() : WeatherData{
       val result:WeatherData =  runBlocking{
           weatherClient.setCityName(getCityValue())
           Log.d("WeatherViewModel","555555555  ${weatherClient.getData()}")
              weatherClient.getData()
        }
        return result
    }
}