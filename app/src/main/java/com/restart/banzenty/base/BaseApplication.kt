package com.restart.banzenty.base

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BaseApplication : Application() {
    private val TAG = "BaseApplication"

    // Instance of the AppComponent that will be used by all the Activities in the project
//    val appComponent: AppComponent by lazy {
//        initializeComponent()
//    }

//    open fun initializeComponent(): AppComponent {
    // Creates an instance of AppComponent using its Factory constructor
    // We pass the applicationContext that will be used as Context in the graph
//        return DaggerAppComponent.factory().create(applicationContext)
//}
}