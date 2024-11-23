package com.client.motioncontroller

import android.app.Application
import com.client.motioncontroller.model.StatusDriver
import kotlinx.coroutines.flow.MutableStateFlow

lateinit var application : Application

val statusDriver  = MutableStateFlow(StatusDriver())

//@HiltAndroidApp
class App: Application(){
    override fun onCreate() {
        super.onCreate()
        application = this
        BluetoothHandler.initialize(this.applicationContext)
    }
}