package com.example.antitheft // আপনার প্রজেক্টের প্যাকেজ নেম অনুযায়ী এটি দেবেন

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Looper
import com.google.android.gms.location.*
import com.google.firebase.database.FirebaseDatabase

class TrackerService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val locationRequest = LocationRequest.create().apply {
            interval = 10000 // প্রতি ১০ সেকেন্ড পর পর লোকেশন চেক করবে
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val lat = location.latitude
                    val lon = location.longitude
                    val time = System.currentTimeMillis().toString()

                    // ফায়ারবেস রিয়েলটাইম ডাটাবেজে লাইভ লোকেশন পাঠানো
                    val database = FirebaseDatabase.getInstance("আপনার_ফায়ারবেস_ইউআরএল_এখানে_দিন")
                    val ref = database.getReference("devices/আপনার_আইএমইআই_নম্বর")

                    val updateData = mapOf(
                        "lat" to lat,
                        "lon" to lon,
                        "time" to time,
                        "status" to "Moving & Active"
                    )
                    ref.updateChildren(updateData)
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
