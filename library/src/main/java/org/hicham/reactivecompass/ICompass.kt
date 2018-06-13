package org.hicham.reactivecompass

import io.reactivex.Flowable


interface ICompass
{
    fun observeAccuracy(): Flowable<Accuracy>
    fun isSupported(): Boolean
    fun observeAzimuth(): Flowable<Double>

    enum class Accuracy
    {
        ACCURATE,
        MEDIUM,
        NOT_ACCURATE
    }
}