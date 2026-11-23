package com.mistxyt.netshield_iot.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Utility helper to standardize all timestamp rendering across NetShield IoT
 * into Waktu Indonesia Barat (WIB, UTC+7 / Asia/Jakarta).
 */
object DateTimeHelper {

    val WIB_TIME_ZONE: TimeZone = TimeZone.getTimeZone("Asia/Jakarta")

    /**
     * Resolves incoming raw timestamp. If the raw timestamp is an ESP32 uptime (millis())
     * or missing, returns current wall-clock epoch ms.
     */
    fun resolveValidEpochMs(rawTimestamp: Long?): Long {
        if (rawTimestamp == null || rawTimestamp <= 0L) {
            return System.currentTimeMillis()
        }
        // Epoch 1700000000000L = ~Nov 2023. Any value less is likely ESP32 millis() uptime
        return if (rawTimestamp < 1700000000000L) {
            System.currentTimeMillis()
        } else {
            rawTimestamp
        }
    }

    /**
     * Formats timestamp into "HH:mm:ss WIB"
     * Example: "04:12:30 WIB"
     */
    fun formatWibTime(timestamp: Long): String {
        val validMs = resolveValidEpochMs(timestamp)
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.forLanguageTag("id-ID"))
        sdf.timeZone = WIB_TIME_ZONE
        return "${sdf.format(Date(validMs))} WIB"
    }

    /**
     * Formats timestamp into "d MMM, HH:mm WIB"
     * Example: "24 Nov, 04:12 WIB"
     */
    fun formatWibShortDateTime(timestamp: Long): String {
        val validMs = resolveValidEpochMs(timestamp)
        val sdf = SimpleDateFormat("d MMM, HH:mm", Locale.forLanguageTag("id-ID"))
        sdf.timeZone = WIB_TIME_ZONE
        return "${sdf.format(Date(validMs))} WIB"
    }

    /**
     * Formats timestamp into full "dd/MM/yyyy HH:mm:ss WIB"
     * Example: "24/11/2026 04:12:30 WIB"
     */
    fun formatWibFullDateTime(timestamp: Long): String {
        val validMs = resolveValidEpochMs(timestamp)
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.forLanguageTag("id-ID"))
        sdf.timeZone = WIB_TIME_ZONE
        return "${sdf.format(Date(validMs))} WIB"
    }
}
