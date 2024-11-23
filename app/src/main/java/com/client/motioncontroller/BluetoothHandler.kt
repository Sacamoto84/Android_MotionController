package com.client.motioncontroller

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.welie.blessed.*
import com.welie.blessed.WriteType.WITH_RESPONSE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.nio.ByteOrder
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


@SuppressLint("StaticFieldLeak")
object BluetoothHandler {

    private lateinit var context: Context
    lateinit var centralManager: BluetoothCentralManager
    private val handler = Handler(Looper.getMainLooper())

    private val measurementFlow_ = MutableStateFlow("Waiting for measurement")

    val measurementFlow = measurementFlow_.asStateFlow()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // UUIDs for the Blood Pressure service (BLP)
    private val BLP_SERVICE_UUID: UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
    private val BLP_MEASUREMENT_CHARACTERISTIC_UUID: UUID =
        UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")

//    // UUIDs for the Health Thermometer service (HTS)
//    private val HTS_SERVICE_UUID = from16BitString("1809")
//    private val HTS_MEASUREMENT_CHARACTERISTIC_UUID = from16BitString("2A1C")
//
//    // UUIDs for the Heart Rate service (HRS)
//    private val HRS_SERVICE_UUID: UUID = UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb")
//    private val HRS_MEASUREMENT_CHARACTERISTIC_UUID: UUID = UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb")
//
//    // UUIDs for the Device Information service (DIS)
//    private val DIS_SERVICE_UUID: UUID = UUID.fromString("0000180A-0000-1000-8000-00805f9b34fb")
//    private val MANUFACTURER_NAME_CHARACTERISTIC_UUID: UUID = UUID.fromString("00002A29-0000-1000-8000-00805f9b34fb")
//    private val MODEL_NUMBER_CHARACTERISTIC_UUID: UUID = UUID.fromString("00002A24-0000-1000-8000-00805f9b34fb")
//
//    // UUIDs for the Current Time service (CTS)
//    private val CTS_SERVICE_UUID: UUID = UUID.fromString("00001805-0000-1000-8000-00805f9b34fb")
//    private val CURRENT_TIME_CHARACTERISTIC_UUID: UUID = UUID.fromString("00002A2B-0000-1000-8000-00805f9b34fb")
//
//    // UUIDs for the Battery Service (BAS)
//    private val BTS_SERVICE_UUID: UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb")
//    private val BATTERY_LEVEL_CHARACTERISTIC_UUID: UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb")
//
//    // UUIDs for the Pulse Oximeter Service (PLX)
//    val PLX_SERVICE_UUID: UUID = UUID.fromString("00001822-0000-1000-8000-00805f9b34fb")
//    private val PLX_SPOT_MEASUREMENT_CHAR_UUID: UUID = UUID.fromString("00002a5e-0000-1000-8000-00805f9b34fb")
//    private val PLX_CONTINUOUS_MEASUREMENT_CHAR_UUID: UUID = UUID.fromString("00002a5f-0000-1000-8000-00805f9b34fb")
//
//    // UUIDs for the Weight Scale Service (WSS)
//    val WSS_SERVICE_UUID: UUID = UUID.fromString("0000181D-0000-1000-8000-00805f9b34fb")
//    private val WSS_MEASUREMENT_CHAR_UUID: UUID = UUID.fromString("00002A9D-0000-1000-8000-00805f9b34fb")
//    val GLUCOSE_SERVICE_UUID: UUID = UUID.fromString("00001808-0000-1000-8000-00805f9b34fb")
//    val GLUCOSE_MEASUREMENT_CHARACTERISTIC_UUID: UUID = UUID.fromString("00002A18-0000-1000-8000-00805f9b34fb")
//    val GLUCOSE_RECORD_ACCESS_POINT_CHARACTERISTIC_UUID: UUID = UUID.fromString("00002A52-0000-1000-8000-00805f9b34fb")
//
//    // Contour Glucose Service
//    val CONTOUR_SERVICE_UUID: UUID = UUID.fromString("00000000-0002-11E2-9E96-0800200C9A66")
//    private val CONTOUR_CLOCK = UUID.fromString("00001026-0002-11E2-9E96-0800200C9A66")

    private val bluetoothPeripheralCallback = object : BluetoothPeripheralCallback() {

        override fun onServicesDiscovered(peripheral: BluetoothPeripheral) {

            peripheral.requestConnectionPriority(ConnectionPriority.HIGH)

            //peripheral.readCharacteristic(DIS_SERVICE_UUID, MANUFACTURER_NAME_CHARACTERISTIC_UUID)
            //peripheral.readCharacteristic(DIS_SERVICE_UUID, MODEL_NUMBER_CHARACTERISTIC_UUID)

            // Write Current Time if possible
//            peripheral.getCharacteristic(CTS_SERVICE_UUID, CURRENT_TIME_CHARACTERISTIC_UUID)?.let {
//                peripheral.startNotify(it)
//
//                // If it has the write property we write the current time
//                if (it.supportsWritingWithResponse()) {
//                    // Write the current time unless it is an Omron device
//                    if (!peripheral.name.contains("BLEsmart_", true)) {
//                        val currentTime = currentTimeByteArrayOf(Calendar.getInstance())
//                        peripheral.writeCharacteristic(it, currentTime, WITH_RESPONSE)
//                    }
//                }
//            }

            //peripheral.readCharacteristic(BTS_SERVICE_UUID, BATTERY_LEVEL_CHARACTERISTIC_UUID)
            peripheral.startNotify(BLP_SERVICE_UUID, BLP_MEASUREMENT_CHARACTERISTIC_UUID)
            //peripheral.startNotify(HTS_SERVICE_UUID, HTS_MEASUREMENT_CHARACTERISTIC_UUID)
            //.startNotify(HRS_SERVICE_UUID, HRS_MEASUREMENT_CHARACTERISTIC_UUID)
            //peripheral.startNotify(GLUCOSE_SERVICE_UUID, GLUCOSE_MEASUREMENT_CHARACTERISTIC_UUID)
            //peripheral.startNotify(PLX_SERVICE_UUID, PLX_SPOT_MEASUREMENT_CHAR_UUID)
            //peripheral.startNotify(PLX_SERVICE_UUID, PLX_CONTINUOUS_MEASUREMENT_CHAR_UUID)
            //peripheral.startNotify(WSS_SERVICE_UUID, WSS_MEASUREMENT_CHAR_UUID)
            //peripheral.startNotify(CONTOUR_SERVICE_UUID, CONTOUR_CLOCK)
        }

        override fun onNotificationStateUpdate(
            peripheral: BluetoothPeripheral,
            characteristic: BluetoothGattCharacteristic,
            status: GattStatus,
        ) {
            if (status == GattStatus.SUCCESS) {
                val isNotifying = peripheral.isNotifying(characteristic)
                Timber.i("!!! SUCCESS: Notify set to '%s' for %s", isNotifying, characteristic.uuid)

//                if (characteristic.uuid == CONTOUR_CLOCK) {
//                    writeContourClock(peripheral)
//                } else if (characteristic.uuid == GLUCOSE_RECORD_ACCESS_POINT_CHARACTERISTIC_UUID) {
//                    writeGetAllGlucoseMeasurements(peripheral)
//                }

            } else {
                Timber.e(
                    "!!! ERROR: Changing notification state failed for %s (%s)",
                    characteristic.uuid,
                    status
                )
            }
        }

        //При нотификации от ESP32
        override fun onCharacteristicUpdate(
            peripheral: BluetoothPeripheral,
            value: ByteArray,
            characteristic: BluetoothGattCharacteristic,
            status: GattStatus,
        ) {
            when (characteristic.uuid) {
                BLP_MEASUREMENT_CHARACTERISTIC_UUID -> {
                    val a = String(value, Charsets.UTF_8)
                    scope.launch {
                        Timber.i("!!! READ: %s", a)
                        measurementFlow_.emit(a)
                    }
                }
            }
        }

    }

    private val bluetoothCentralManagerCallback = object : BluetoothCentralManagerCallback() {

        override fun onDiscovered(peripheral: BluetoothPeripheral, scanResult: ScanResult) {
            Timber.i("!!! Found peripheral '${peripheral.name}' with RSSI ${scanResult.rssi}")
            centralManager.stopScan()

            if (peripheral.needsBonding() && peripheral.bondState == BondState.NONE) {
                // Create a bond immediately to avoid double pairing popups
                centralManager.createBond(peripheral, bluetoothPeripheralCallback)
            } else {
                centralManager.connect(peripheral, bluetoothPeripheralCallback)
            }
        }

        override fun onConnected(peripheral: BluetoothPeripheral) {
            Timber.i("!!! connected to '${peripheral.name}'")
            Toast.makeText(context, "Connected to ${peripheral.name}", LENGTH_SHORT).show()
        }

        override fun onDisconnected(peripheral: BluetoothPeripheral, status: HciStatus) {
            Timber.i("!!! disconnected '${peripheral.name}'")
            Toast.makeText(context, "Disconnected ${peripheral.name}", LENGTH_SHORT).show()
            handler.postDelayed(
                { centralManager.autoConnect(peripheral, bluetoothPeripheralCallback) },
                15000
            )
        }

        override fun onConnectionFailed(peripheral: BluetoothPeripheral, status: HciStatus) {
            Timber.e("!!! failed to connect to '${peripheral.name}'")
        }

        override fun onBluetoothAdapterStateChanged(state: Int) {

            Timber.i("!!! bluetooth adapter changed state to %d", state)
            if (state == BluetoothAdapter.STATE_ON) {
                // Bluetooth is on now, start scanning again
                // Scan for peripherals with a certain service UUIDs
                centralManager.startPairingPopupHack()
                startScanning()
            }
        }
    }

    fun startScanning() {
        if (centralManager.isNotScanning) {
            //centralManager.scanForPeripheralsWithServices( setOf( BLP_SERVICE_UUID ) )
            centralManager.scanForPeripheralsWithNames(setOf("MyESP32"))
        }
    }

    fun initialize(context: Context) {
        Timber.plant(Timber.DebugTree())
        Timber.i("!!! initializing BluetoothHandler")
        this.context = context.applicationContext
        this.centralManager =
            BluetoothCentralManager(this.context, bluetoothCentralManagerCallback, handler)
    }
}

// Peripheral extension to check if the peripheral needs to be bonded first
// This is application specific of course
fun BluetoothPeripheral.needsBonding(): Boolean {
    return name.startsWith("Contour") ||
            name.startsWith("A&D")
}
