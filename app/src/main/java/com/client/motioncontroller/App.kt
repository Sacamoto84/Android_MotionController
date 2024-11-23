package com.client.motioncontroller

import android.app.Application

lateinit var application : Application

//@HiltAndroidApp
class App: Application(){
    override fun onCreate() {
        super.onCreate()
        application = this
        BluetoothHandler.initialize(this.applicationContext)
    }
}