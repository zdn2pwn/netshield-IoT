package com.mistxyt.netshield_iot.core.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mistxyt.netshield_iot.MainActivity
import com.mistxyt.netshield_iot.R

object NotificationHelper {

    private const val CHANNEL_ID = "netshield_security_alerts"
    private const val CHANNEL_NAME = "NetShield Security Alerts"
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi saat domain berbahaya atau sensitif (Bokep, Judi Online, Malware) diblokir."
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
        isInitialized = true
    }

    fun isSensitiveCategory(category: String): Boolean {
        val lower = category.lowercase().trim()
        return lower.contains("adult") ||
                lower.contains("bokep") ||
                lower.contains("gambling") ||
                lower.contains("judi") ||
                lower.contains("malware") ||
                lower.contains("phishing") ||
                lower.contains("cloudflare") ||
                lower.contains("shield") ||
                lower.contains("threat")
    }

    fun sendSecurityAlert(context: Context, domain: String, category: String, clientIp: String) {
        init(context)

        // Verifikasi izin runtime POST_NOTIFICATIONS di Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_netshield_logo)
            .setContentTitle("[BLOKIR] Ancaman Sensitif Terdeteksi!")
            .setContentText("$domain ($category) dicegah dari $clientIp")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Akses ke situs $domain ($category) berhasil diblokir oleh NetShield IoT Sinkhole.\nPerangkat Klien: $clientIp")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                (System.currentTimeMillis() % 100000).toInt(),
                notification
            )
        } catch (_: SecurityException) {
            // Permission rejected
        }
    }
}
