package com.ychong.mvvm_demo.data.model

class AQI {
    lateinit var city:AQICity
    inner class AQICity{
        var aqi = ""
        var pm25 = ""
    }
}