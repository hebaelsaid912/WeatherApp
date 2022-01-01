package com.example.android.weatherapp.ui.main

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.android.weatherapp.R
import com.example.android.weatherapp.databinding.ActivityMainBinding
import com.example.android.weatherapp.pojo.WeatherData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.Language
import java.net.URL
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var sessionWeatherData: WeatherData? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val viewModel: WeatherViewModel by lazy {
        ViewModelProvider(this)[WeatherViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        binding.addressContainer.visibility = View.GONE
        binding.detailsContainer.visibility = View.GONE
        binding.overviewContainer.visibility = View.GONE
        binding.stateImageView.visibility = View.GONE
        fetchPermission()


    }


    private fun render() {
        lifecycleScope.launchWhenStarted {
            viewModel.state.collect {
                when (it) {
                    is MainViewState.Idle -> {
                        Log.d("MainActivity", "idle")
                        if (sessionWeatherData == null) {
                            lifecycleScope.launchWhenStarted {
                                val getCityNameJob: Job = launch {
                                    getCityNameFromUserLocation()
                                    //delay(20)
                                }
                                getCityNameJob.join()
                                launch {  binding.loadingData.visibility = View.VISIBLE
                                    binding.addressContainer.visibility = View.GONE
                                    binding.detailsContainer.visibility = View.GONE
                                    binding.overviewContainer.visibility = View.GONE
                                    binding.stateImageView.visibility = View.GONE
                                    delay(100)
                                    viewModel.intentChannel.send(MainIntent.getWeather)
                                }
                            }

                        }
                    }
                    is MainViewState.getWeatherData -> {
                        delay(2000)
                        Log.d("MainActivity", "getWeatherData")
                        sessionWeatherData = it.weatherData
                        setViewsData(sessionWeatherData!!)
                    }
                    is MainViewState.getError -> {
                        Log.d("MainActivity", "getError")
                        binding.errorText.text = it.result
                        binding.addressContainer.visibility = View.GONE
                        binding.detailsContainer.visibility = View.GONE
                        binding.overviewContainer.visibility = View.GONE
                        binding.stateImageView.visibility = View.GONE
                        binding.errorText.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
    private fun getCityNameFromUserLocation(){
        val check = checkPermission()
        Log.d("MainActivity", check.toString())
        if (check) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                val geocoder: Geocoder =
                    Geocoder(this@MainActivity, Locale.ENGLISH)
                val address = geocoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    1
                )
                 Log.d("MainActivity", address[0].countryName)
                 Log.d("MainActivity", address[0].locality)
                 Log.d("MainActivity", address[0].adminArea.toString() + " ")
                 Log.d(
                     "MainActivity",
                     address[0].featureName.toString() + " "
                 )
                 Log.d(
                     "MainActivity",
                     address[0].subAdminArea.toString() + " "
                 )
                 Log.d(
                     "MainActivity",
                     address[0].getAddressLine(0).toString() + " "
                 )
              val city = address[0].subAdminArea.toString()
                viewModel.setCityValue(city)
            }
        }
    }

    private fun fetchPermission() {
        return ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 101
        )
    }

    private fun checkPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        } else {
            fetchPermission()
            return false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("MainActivity", "onRequestPermissionsResult $requestCode $permissions $grantResults")
        when (requestCode) {
            101 -> {
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    render()
                } else {
                    Log.d("MainActivity", "onRequestPermissionsResult Canceled")
                    binding.locatinNeeded.visibility = View.VISIBLE
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setViewsData(data: WeatherData) {
        binding.loadingData.visibility = View.GONE
        binding.addressContainer.visibility = View.VISIBLE
        binding.detailsContainer.visibility = View.VISIBLE
        binding.overviewContainer.visibility = View.VISIBLE
        binding.stateImageView.visibility = View.VISIBLE
        binding.addressTv.text = "${data.location.name} , ${data.location.country}"
        binding.updatesTimeTv.text = data.current.last_updated
        val link: URL = URL("https:" + data.current.condition.icon)
        Glide.with(this)
            .load(link)
            .into(binding.stateImageView);
        binding.status.text = data.current.condition.text
        binding.temp.text = data.current.temp_c.toString() + "°C"
        binding.tempMin.text = "feels like ${data.current.feelslike_c.toString()} c"
        binding.tempMax.text = "feels like ${data.current.feelslike_f.toString()} f"
        binding.cloud.text = data.current.cloud.toString()
        binding.precipMm.text = data.current.precip_mm.toString()
        binding.precipIn.text = data.current.precip_in.toString()
        binding.pressureMm.text = data.current.pressure_mb.toString()
        binding.pressureNn.text = data.current.pressure_in.toString()
        binding.humidity.text = data.current.humidity.toString()
        binding.wind.text = " ${data.current.wind_kph.toString()}" +
                " ${data.current.wind_dir.toString()}"
    }

}