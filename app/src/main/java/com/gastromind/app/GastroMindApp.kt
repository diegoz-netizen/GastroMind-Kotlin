package com.gastromind.app

import android.app.Application
import com.gastromind.app.di.AppContainer

class GastroMindApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
