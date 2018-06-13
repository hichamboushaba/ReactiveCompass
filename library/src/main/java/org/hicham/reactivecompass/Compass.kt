package org.hicham.reactivecompass

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import org.hicham.reactivecompass.listeners.AccuracyChangeListener
import org.hicham.reactivecompass.listeners.AzimuthChangeListener


private const val DEFAUL_SENSOR_DELAY = 100_000 //Microseconds

class Compass(
        private val context: Context,
        private val sensorDelay: Int = DEFAUL_SENSOR_DELAY
) : ICompass {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    override fun observeAzimuth(): Flowable<Double> {
        if (!isSupported())
            throw CompassNotSupportedException()
        return Flowable.create<Double>(
                { emitter ->
                    val listener = AzimuthChangeListener(context, emitter)
                    if (!sensorManager.registerListener(listener, rotationSensor, sensorDelay)) {
                        sensorManager.registerListener(listener, accelerometer, sensorDelay)
                        sensorManager.registerListener(listener, magnetometer, sensorDelay)
                    }

                    emitter.setCancellable({ sensorManager.unregisterListener(listener) })

                },
                BackpressureStrategy.LATEST)
    }


    override fun observeAccuracy(): Flowable<ICompass.Accuracy> {
        if (!isSupported())
            throw CompassNotSupportedException()

        return Flowable.create<ICompass.Accuracy>(
                { emitter ->
                    val listener = AccuracyChangeListener(emitter)
                    sensorManager.registerListener(listener, magnetometer, sensorDelay)

                    emitter.setCancellable({ sensorManager.unregisterListener(listener) })

                },
                BackpressureStrategy.LATEST)
    }

    override fun isSupported(): Boolean =
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS)
}