package org.hicham.reactivecompass.listeners

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.WindowManager
import io.reactivex.FlowableEmitter


class AzimuthChangeListener(
        private val context: Context,
        private val emitter: FlowableEmitter<Double>
) : SensorEventListener {
    private var gravity: FloatArray = FloatArray(3)
    private var geomagnetic: FloatArray = FloatArray(3)

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent) {
        val screenRotation = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.rotation
        var axisX = SensorManager.AXIS_X
        var axisY = SensorManager.AXIS_Y
        when (screenRotation) {
            Surface.ROTATION_0 -> {
                axisX = SensorManager.AXIS_X
                axisY = SensorManager.AXIS_Y
            }

            Surface.ROTATION_90 -> {
                axisX = SensorManager.AXIS_Y
                axisY = SensorManager.AXIS_MINUS_X
            }

            Surface.ROTATION_180 -> {
                axisX = SensorManager.AXIS_MINUS_X
                axisY = SensorManager.AXIS_MINUS_Y
            }

            Surface.ROTATION_270 -> {
                axisX = SensorManager.AXIS_MINUS_Y
                axisY = SensorManager.AXIS_X
            }
        }

        var success = true
        val rMatrix = FloatArray(9)
        val rOrientated = FloatArray(9)

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rMatrix, event.values)
        } else {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER)
                gravity = event.values
            if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD)
                geomagnetic = event.values
            val I = FloatArray(9)

            success = SensorManager.getRotationMatrix(rMatrix, I, gravity, geomagnetic)
        }

        SensorManager.remapCoordinateSystem(rMatrix, axisX, axisY, rOrientated)
        if (success) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rOrientated, orientation)
            var azimuth = Math.toDegrees(orientation[0].toDouble()) // orientation contains: azimut, pitch and roll
            if (azimuth < 0)
                azimuth += 360

            emitter.onNext(azimuth)
        }
    }
}