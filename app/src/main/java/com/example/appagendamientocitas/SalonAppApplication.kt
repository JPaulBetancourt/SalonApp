package com.example.appagendamientocitas

import android.app.Application
import com.example.appagendamientocitas.util.ReminderRescheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SalonAppApplication : Application() {

    @Inject
    lateinit var reminderRescheduler: ReminderRescheduler

    override fun onCreate() {
        super.onCreate()
        reminderRescheduler.rescheduleAll()
    }
}