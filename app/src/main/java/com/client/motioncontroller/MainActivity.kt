package com.client.motioncontroller

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanResult
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.client.motioncontroller.ui.theme.MotionControllerTheme
import com.welie.blessed.BluetoothCentralManager
import com.welie.blessed.BluetoothCentralManagerCallback
import com.welie.blessed.BluetoothPeripheral
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import timber.log.Timber


class MainActivity : ComponentActivity() {

    private val defaultScope = CoroutineScope(Dispatchers.Default)

    private val mainHandler = Handler(Looper.getMainLooper())

    //private var gattServiceConn: GattServiceConn? = null

    //private var gattServiceData: GattService.DataPlane? = null

    private val myCharacteristicValueChangeNotifications = Channel<String>()

//    private val requestPermissionLauncher = registerForActivityResult(
//        ActivityResultContracts.RequestMultiplePermissions()
//    ) { states ->
//        if (states[Manifest.permission.BLUETOOTH_CONNECT] == true ||
//            states[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
//            startForegroundService(Intent(this, GattService::class.java))
//        } else {
//            finish()
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val permissions = when (Build.VERSION.SDK_INT) {
//            // Before API 31, we need to request the ACCESS_FINE_LOCATION permission in
//            // order to scan for Bluetooth LE devices.
//            in Build.VERSION_CODES.JELLY_BEAN_MR2 .. Build.VERSION_CODES.R -> arrayOf(
//                Manifest.permission.ACCESS_FINE_LOCATION
//            )
//            // Starting from API 31 it's enough to request for Bluetooth permissions,
//            // as the BLUETOOTH_SCAN permission was declared with "neverForLocation" flag.
//            else -> arrayOf(
//                Manifest.permission.BLUETOOTH_CONNECT,
//                Manifest.permission.BLUETOOTH_SCAN,
//            )
//        }

//        // The service of type "connectedDevice" can only be started if the app has the
//        // BLUETOOTH_CONNECT permission granted.
//        if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED ||
//            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            startForegroundService(Intent(this, GattService::class.java))
//        } else {
//            requestPermissionLauncher.launch(permissions)
//        }
        
//        defaultScope.launch {
//            for (newValue in myCharacteristicValueChangeNotifications) {
//                mainHandler.run {
//                    println("!!! $newValue")
//                }
//            }
//        }

        enableEdgeToEdge()
        setContent {
            MotionControllerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()) {

                        val measurementText = BluetoothHandler.measurementFlow.collectAsState()
                        Text(text = measurementText.value, fontSize = 24.sp)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        restartScanning()
    }

    private fun restartScanning() {
        if (!BluetoothHandler.centralManager.isBluetoothEnabled) {
            enableBleRequest.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            return
        }

        if (BluetoothHandler.centralManager.permissionsGranted()) {
            BluetoothHandler.startScanning()
        } else {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        val missingPermissions = BluetoothHandler.centralManager.getMissingPermissions()
        if (missingPermissions.isNotEmpty() && !permissionRequestInProgress) {
            permissionRequestInProgress = true
            blePermissionRequest.launch(missingPermissions)
        }
    }

    private var permissionRequestInProgress = false
    private val blePermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissionRequestInProgress = false
            permissions.entries.forEach {
                Timber.d("${it.key} = ${it.value}")
            }
        }

    private val enableBleRequest = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            restartScanning()
        }
    }






//    override fun onStart() {
//        super.onStart()
////        val latestGattServiceConn = GattServiceConn()
////        if (bindService(Intent(GattService.DATA_PLANE_ACTION, null, this, GattService::class.java), latestGattServiceConn, 0)) {
////            gattServiceConn = latestGattServiceConn
////        }
//    }

//    override fun onStop() {
//        super.onStop()
////        if (gattServiceConn != null) {
////            unbindService(gattServiceConn!!)
////            gattServiceConn = null
////        }
//    }

//    override fun onDestroy() {
//        super.onDestroy()
//        // We only want the service around for as long as our app is being run on the device
////        stopService(Intent(this, GattService::class.java))
//    }

//    private inner class GattServiceConn : ServiceConnection {
//        override fun onServiceDisconnected(name: ComponentName?) {
//            if (BuildConfig.DEBUG && GattService::class.java.name != name?.className) {
//                error("!!! Disconnected from unknown service")
//            } else {
//                gattServiceData = null
//            }
//        }
//
//        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
//            if (BuildConfig.DEBUG && GattService::class.java.name != name?.className)
//                error("!!! Connected to unknown service")
//            else {
//                gattServiceData = service as GattService.DataPlane
//                gattServiceData?.setMyCharacteristicChangedChannel(myCharacteristicValueChangeNotifications)
//            }
//        }
//    }

}
