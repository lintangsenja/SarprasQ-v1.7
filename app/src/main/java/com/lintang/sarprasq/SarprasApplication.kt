package com.lintang.sarprasq

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase

class SarprasApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initFirebase()
    }

    private fun initFirebase() {
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val app = FirebaseApp.initializeApp(this)
                Log.i("SarprasApplication", "FirebaseApp initialized successfully: ${app?.name}")
            } else {
                Log.i("SarprasApplication", "FirebaseApp already initialized")
            }

            try {
                val db = FirebaseDatabase.getInstance("https://sarpras-134d7-default-rtdb.asia-southeast1.firebasedatabase.app")
                db.setPersistenceEnabled(true)
                Log.i("SarprasApplication", "FirebaseDatabase persistence enabled for Asia-Southeast1 RTDB")
            } catch (e: Exception) {
                Log.w("SarprasApplication", "FirebaseDatabase persistence setup note: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e("SarprasApplication", "FirebaseApp initialization error: ${e.message}", e)
        }
    }
}
