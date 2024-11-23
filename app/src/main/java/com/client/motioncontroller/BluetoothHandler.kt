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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

enum class BLE_STATE {
    DISCONNECT,
    CONNECTED,
    CONNECTING,
    SCANNING,
    ERROR
}

@SuppressLint("StaticFieldLeak")
object BluetoothHandler {

    val ble_state = MutableStateFlow(BLE_STATE.DISCONNECT)

    private val measurementFlow_ = MutableStateFlow("Waiting for measurement")
    val measurementFlow = measurementFlow_.asStateFlow()

    private lateinit var context: Context
    lateinit var centralManager: BluetoothCentralManager
    private val handler = Handler(Looper.getMainLooper())

    var peripheralEsp: BluetoothPeripheral? = null

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // UUIDs for the Blood Pressure service (BLP)
    val BLP_SERVICE_UUID: UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
    val BLP_MEASUREMENT_CHARACTERISTIC_UUID: UUID =
        UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")

    private val bluetoothPeripheralCallback = object : BluetoothPeripheralCallback() {

        override fun onServicesDiscovered(peripheral: BluetoothPeripheral) {
            peripheralEsp = peripheral

            peripheral.requestConnectionPriority(ConnectionPriority.HIGH)
            //peripheral.readCharacteristic(DIS_SERVICE_UUID, MANUFACTURER_NAME_CHARACTERISTIC_UUID)
            peripheral.startNotify(BLP_SERVICE_UUID, BLP_MEASUREMENT_CHARACTERISTIC_UUID)
        }

        override fun onNotificationStateUpdate(
            peripheral: BluetoothPeripheral,
            characteristic: BluetoothGattCharacteristic,
            status: GattStatus,
        ) {
            if (status == GattStatus.SUCCESS) {
                val isNotifying = peripheral.isNotifying(characteristic)
                Timber.i("!!! SUCCESS: Notify set to '%s' for %s", isNotifying, characteristic.uuid)
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

        override fun onCharacteristicWrite(
            peripheral: BluetoothPeripheral,
            value: ByteArray,
            characteristic: BluetoothGattCharacteristic,
            status: GattStatus,
        ) {
            Timber.d("!!! WRITING")
            super.onCharacteristicWrite(peripheral, value, characteristic, status)
        }
    }

    private val bluetoothCentralManagerCallback = object : BluetoothCentralManagerCallback() {

        override fun onDiscovered(peripheral: BluetoothPeripheral, scanResult: ScanResult) {
            Timber.i("!!! Found peripheral '${peripheral.name}' with RSSI ${scanResult.rssi}")
            centralManager.stopScan()
            ble_state.value = BLE_STATE.CONNECTING
            centralManager.connect(peripheral, bluetoothPeripheralCallback)
        }

        override fun onConnected(peripheral: BluetoothPeripheral) {
            peripheralEsp = peripheral
            ble_state.value = BLE_STATE.CONNECTED
            Timber.i("!!! connected to '${peripheral.name}'")
            Toast.makeText(context, "Connected to ${peripheral.name}", LENGTH_SHORT).show()
        }

        override fun onDisconnected(peripheral: BluetoothPeripheral, status: HciStatus) {
            peripheralEsp = null
            ble_state.value = BLE_STATE.DISCONNECT
            Timber.i("!!! disconnected '${peripheral.name}'")
            Toast.makeText(context, "Disconnected ${peripheral.name}", LENGTH_SHORT).show()
            handler.postDelayed(
                { centralManager.autoConnect(peripheral, bluetoothPeripheralCallback) },
                15000
            )
        }

        override fun onConnectionFailed(peripheral: BluetoothPeripheral, status: HciStatus) {
            Timber.e("!!! failed to connect to '${peripheral.name}'")
            ble_state.value = BLE_STATE.ERROR
            peripheralEsp = null
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
            ble_state.value = BLE_STATE.SCANNING
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

    fun send(value: ByteArray) {
        try {
            if (peripheralEsp != null) {
                peripheralEsp!!.writeCharacteristic(
                    BLP_SERVICE_UUID,
                    BLP_MEASUREMENT_CHARACTERISTIC_UUID,
                    value,
                    WriteType.WITH_RESPONSE
                )
            }
        } catch (e: Exception) {
            Timber.e(e.localizedMessage)
        }
    }

    fun send(s: String) {
        val value = s.toByteArray(Charsets.UTF_8)
        send(value)
    }

}

