package com.veriff.testapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Base class of Android app containing components like Activities and Services
 * Instantiated before all the activities or any other application objects have been created in Android app
 */
@HiltAndroidApp
class AppController : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: AppController
            private set
    }
}