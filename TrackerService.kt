package com.example.antitheft

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TrackerService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        super.onCreate()
        
        createNotificationChannel()
        val notification: Notification = NotificationCompat.Builder(this, "AntiTheftChannel")
            .setContentTitle("অ্যান্টি-থেফট প্রটেকশন সক্রিয়")
            .setContentText("ডিভাইস লাইভ ট্র্যাক করা হচ্ছে...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()
        startForeground(1, notification)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val locationRequest = LocationRequest.create().apply {
            interval = 1000        
            fastestInterval = 1000 
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 0.1f 
        }

        val database = FirebaseDatabase.getInstance("https://android-app-2cd3f-default-rtdb.firebaseio.com/")
        val deviceRef = database.getReference("devices/877564542676551")

        // রিমোট কমান্ড শোনার জন্য লিসেনার (অ্যাডমিন প্যানেল থেকে লক কমান্ড আসলে কাজ করবে)
        val commandRef = database.getReference("devices/877564542676551/command")
        commandRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cmd = snapshot.getValue(String::class.java)
                if (cmd == "lock") {
                    val policyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as android.app.admin.DevicePolicyManager
                    try {
                        policyManager.lockNow() // তাৎক্ষণিকভাবে স্ক্রিন লক করে দেবে
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val lat = location.latitude
                    val lon = location.longitude
                    val time = System.currentTimeMillis().toString()

                    val updateData = mapOf(
                        "lat" to lat,
                        "lon" to lon,
                        "time" to time,
                        "status" to "Protected & Moving Live"
                    )
                    deviceRef.updateChildren(updateData)
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                "AntiTheftChannel",
                "Anti-Theft Background Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        val broadcastIntent = Intent(this, BootReceiver::class.java)
        sendBroadcast(broadcastIntent)
    }
}
