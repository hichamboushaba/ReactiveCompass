package org.hicham.reactivecompass.listeners

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import io.reactivex.FlowableEmitter
import org.hicham.reactivecompass.ICompass

/**
 * Created by Hicham on 13/06/2018.
 */
class AccuracyChangeListener(
        private val emitter: FlowableEmitter<ICompass.Accuracy>
) : SensorEventListener {
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        when (accuracy) {
            SensorManager.SENSOR_STATUS_UNRELIABLE,
            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> emitter.onNext(ICompass.Accuracy.NOT_ACCURATE)
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> emitter.onNext(ICompass.Accuracy.ACCURATE)
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> emitter.onNext(ICompass.Accuracy.MEDIUM)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
    }
}