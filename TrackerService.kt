package com.example.antitheft

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.firebase.database.FirebaseDatabase

class TrackerService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        super.onCreate()
        
        // ফোরগ্রাউন্ড সার্ভিস নোটিফিকেশন (যাতে অ্যান্ড্রয়েড সিস্টেম ব্যাকগ্রাউন্ডে সার্ভিস কিল করতে না পারে)
        createNotificationChannel()
        val notification: Notification = NotificationCompat.Builder(this, "AntiTheftChannel")
            .setContentTitle("অ্যান্টি-থেফট প্রটেকশন সক্রিয়")
            .setContentText("ডিভাইস লাইভ ট্র্যাক করা হচ্ছে...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()
        startForeground(1, notification)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // একদম ফাস্ট এবং হাই এক্যুরেসি জিপিএস রিকোয়েস্ট (প্রতি ১ সেকেন্ড পর পর আপডেট হবে)
        val locationRequest = LocationRequest.create().apply {
            interval = 1000        // ১ সেকেন্ড
            fastestInterval = 1000 // ১ সেকেন্ড
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 0.1f // সামান্য নড়াচড়া করলেই ধরবে
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val lat = location.latitude
                    val lon = location.longitude
                    val time = System.currentTimeMillis().toString()

                    // ফায়ারবেস রিয়েলটাইম ডাটাবেজে লাইভ লোকেশন পাঠানো
                    // আপনার ফায়ারবেস ডাটাবেজ ইউআরএল এখানে বসিয়ে দেবেন
                    val database = FirebaseDatabase.getInstance("https://android-app-2cd3f-default-rtdb.firebaseio.com/")
                    val ref = database.getReference("devices/877564542676551") // টার্গেট ফোনের IMEI

                    val updateData = mapOf(
                        "lat" to lat,
                        "lon" to lon,
                        "time" to time,
                        "status" to "Protected & Moving Live"
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
        // সার্ভিস বন্ধ করার চেষ্টা করলে রিস্টার্ট করার জন্য
        val broadcastIntent = Intent(this, BootReceiver::class.java)
        sendBroadcast(broadcastIntent)
    }
}
