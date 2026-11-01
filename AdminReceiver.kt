package com.example.antitheft // আপনার প্রজেক্টের প্যাকেজ নেম অনুযায়ী এটি দেবেন

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class AdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(context, "অ্যান্টি-থেফট সিকিউরিটি প্রোটেকশন সক্রিয় হয়েছে!", Toast.LENGTH_SHORT).show()
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return "সতর্কতা: এই অ্যাপটি অ্যান্টি-থেফট প্রটেকশনের কারণে ডিজেবল করা যাবে না!"
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(context, "প্রোটেকশন নিষ্ক্রিয় করা হয়েছে।", Toast.LENGTH_SHORT).show()
    }
}
