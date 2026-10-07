package com.example

import android.app.Application
import com.example.widgets.WidgetUpdater
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class CivoraApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        try {
            PDFBoxResourceLoader.init(applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            WidgetUpdater.schedulePeriodicUpdates(this)
            WidgetUpdater.triggerImmediateUpdate(this)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}
