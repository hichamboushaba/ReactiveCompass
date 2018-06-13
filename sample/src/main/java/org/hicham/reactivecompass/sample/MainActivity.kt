package org.hicham.reactivecompass.sample

import android.animation.ObjectAnimator
import android.databinding.BindingAdapter
import android.databinding.DataBindingUtil
import android.databinding.ObservableDouble
import android.databinding.ObservableField
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import org.hicham.reactivecompass.Compass
import org.hicham.reactivecompass.ICompass
import org.hicham.reactivecompass.sample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var compass: ICompass

    private val compassRotation = ObservableDouble()
    private val azimuth = ObservableField<String>()
    private val accuracy = ObservableField<String>()

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.compassRotation = compassRotation
        binding.accuracy = accuracy
        binding.azimuth = azimuth
        binding.executePendingBindings()
        compass = Compass(this)
    }

    override fun onResume() {
        super.onResume()
        val azimuthObservable = compass.observeSensor()
                .distinctUntilChanged({ i1, i2 -> Math.abs(i1 - i2) < 1 })
                .publish()

        azimuthObservable
                .subscribe {
                    azimuth.set("Angle: ${it.toInt()}")
                }

        azimuthObservable
                .map { 360.0 - it }
                .subscribe {
                    val newRotation = it % 360
                    compassRotation.set(newRotation)
                }
                .addTo(disposables)

        azimuthObservable.connect()

        compass.observeAccuracy()
                .distinctUntilChanged()
                .map {
                    when (it) {
                        ICompass.Accuracy.ACCURATE -> "Accurate"
                        ICompass.Accuracy.MEDIUM -> "Medium"
                        ICompass.Accuracy.NOT_ACCURATE -> "Inaccurate"
                    }
                }
                .subscribe {
                    accuracy.set("Accuracy: $it")
                }
                .addTo(disposables)
    }
}


@BindingAdapter("rotateTo")
fun rotateTo(view: View, angle: Double) {

    val oldAnimation = view.tag as ObjectAnimator?

    val currentRotation = view.rotation

    val newAngle = if (Math.abs(angle - currentRotation) > 180) {
        if (angle > currentRotation) angle - 360
        else angle + 360
    } else angle

    Log.d("ReactiveCompass", "rotate from $currentRotation to $newAngle")

    val rotateAnimation = ObjectAnimator.ofFloat(view,
            View.ROTATION, currentRotation, newAngle.toFloat())
    rotateAnimation.interpolator = LinearInterpolator()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
        rotateAnimation.setAutoCancel(true)
    } else {
        oldAnimation?.cancel()
    }

    view.tag = rotateAnimation

    rotateAnimation.start()
}
