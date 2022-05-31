package com.lubaspc.connectios

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.color.DynamicColors

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}