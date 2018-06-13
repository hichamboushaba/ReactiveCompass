# ReactiveCompass
A small library that wraps orientation sensors listeners into [RxJava2](https://github.com/ReactiveX/RxJava) observables

To use:
```kotlin
val compass = Compass(this)
compass.observeAzimuth()
       .distinctUntilChanged({ i1, i2 -> Math.abs(i1 - i2) < 1 })
       .subscribe { println("Azimuth: ${it.toInt()}") }
compass.observeAccuracy()
        .distinctUntilChanged()
        .map {
            when (it) {
                        ICompass.Accuracy.ACCURATE -> "Accurate"
                        ICompass.Accuracy.MEDIUM -> "Medium"
                        ICompass.Accuracy.NOT_ACCURATE -> "Inaccurate"
                      }
              }
         .subscribe { println("Accuracy: $it") }
```
