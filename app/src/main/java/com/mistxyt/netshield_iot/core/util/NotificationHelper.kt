package com.mistxyt.netshield_iot.core.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mistxyt.netshield_iot.MainActivity
import com.mistxyt.netshield_iot.R
import java.util.concurrent.ConcurrentHashMap

object NotificationHelper {

    private const val TAG = "NotificationHelper"
    private const val CHANNEL_ID = "netshield_security_alerts_v3"
    private const val CHANNEL_NAME = "NetShield Security Alerts"
    private const val NOTIFICATION_ID_SECURITY_ALERT = 1001
    private var isInitialized = false

    // Anti-spam deduplikasi per domain utama (apex) dalam 45 detik
    private val lastAlertTimeMap = ConcurrentHashMap<String, Long>()
    // Throttle global: jeda minimal 8 detik antar notifikasi suara apa pun
    private var lastGlobalAlertTime: Long = 0L

    fun init(context: Context) {
        if (isInitialized) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi suara instan saat situs terlarang (Pornografi, Judi Online, Malware) diblokir."
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 150, 300)
                setSound(soundUri, audioAttributes)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
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
                lower.contains("porn") ||
                lower.contains("gambling") ||
                lower.contains("judi") ||
                lower.contains("slot") ||
                lower.contains("gacor") ||
                lower.contains("malware") ||
                lower.contains("phishing") ||
                lower.contains("cloudflare") ||
                lower.contains("shield") ||
                lower.contains("threat")
    }

    @Synchronized
    fun sendSecurityAlert(context: Context, domain: String, category: String, clientIp: String) {
        val now = System.currentTimeMillis()
        val mainDomain = DomainFilterHelper.extractMainDomain(domain).lowercase().trim()

        // 1. Throttle global: cegah suara ganda dalam interval 8 detik
        if (now - lastGlobalAlertTime < 8000L) {
            return
        }

        // 2. Cooldown per domain: cegah spam berulang untuk situs yang sama dalam 45 detik
        val lastTime = lastAlertTimeMap[mainDomain] ?: 0L
        if (now - lastTime < 45000L) {
            return
        }

        lastAlertTimeMap[mainDomain] = now
        lastGlobalAlertTime = now

        init(context)

        // 1. Putar suara notifikasi secara langsung via RingtoneManager sebagai jaminan terdengar
        try {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context.applicationContext, soundUri)
            ringtone?.play()
        } catch (e: Exception) {
            Log.e(TAG, "Gagal memutar ringtone langsung: ${e.message}")
        }

        // 2. Periksa izin notifikasi untuk menampilkan banner visual di Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w(TAG, "Izin POST_NOTIFICATIONS belum diberikan, notifikasi visual dilewati.")
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

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_netshield_logo)
            .setContentTitle(context.getString(R.string.notification_alert_title))
            .setContentText(context.getString(R.string.notification_alert_text, mainDomain, category, clientIp))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(context.getString(R.string.notification_alert_big, mainDomain, category, clientIp))
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 300, 150, 300))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_SECURITY_ALERT,
                notification
            )
        } catch (e: Exception) {
            Log.e(TAG, "Gagal memposting notifikasi: ${e.message}")
        }
    }
}
