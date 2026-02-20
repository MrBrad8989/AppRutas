package com.example.rutapersonal.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SensorViewModel : ViewModel() {
    private val _aceleracionX = MutableStateFlow(0f)
    val aceleracionX: StateFlow<Float> = _aceleracionX

    fun iniciarSensor(context: Context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        sensorManager.registerListener(
            object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    event?.let {
                        _aceleracionX.value = it.values[0]
                    }
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            },
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }
}