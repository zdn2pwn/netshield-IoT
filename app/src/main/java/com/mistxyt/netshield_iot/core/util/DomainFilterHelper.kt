package com.mistxyt.netshield_iot.core.util

object DomainFilterHelper {

    private val NOISY_SUFFIXES = listOf(
        ".arpa",
        ".local",
        ".internal",
        ".lan",
        ".home",
        ".aaplimg.com",
        ".apple-dns.net",
        ".1e100.net",
        ".gvt1.com",
        ".gvt2.com",
        ".gvt3.com",
        ".googleusercontent.com"
    )

    private val NOISY_CONTAINS = listOf(
        // DoH & DNS Resolvers
        "dns.google",
        "cloudflare-dns",
        "one.one.one.one",
        "dns.quad9",

        // Apple Background Telemetry & Services
        "mask.icloud",
        "mask-h2.icloud",
        "metrics.icloud",
        "gateway.icloud",
        "xp.apple.com",
        "smoot.apple.com",
        "gsa.apple.com",
        "cl5.apple.com",
        "push.apple.com",
        "weather-data.apple.com",
        "configuration.apple.com",
        "time.apple.com",
        "time-ios.apple.com",
        "bag.itunes.apple.com",
        "init.itunes.apple.com",
        "ocsp.apple.com",
        "apple-cloudkit.com",
        "captive.apple.com",

        // Google / Android Background Telemetry
        "connectivitycheck.gstatic",
        "android.clients.google",
        "play.googleapis.com",
        "clients1.google",
        "clients2.google",
        "clients3.google",
        "clients4.google",
        "clients5.google",
        "clients6.google",
        "app-measurement.com",
        "crashlytics.com",
        "firebaseinstallations",

        // Microsoft / OS Probes
        "msftconnecttest",
        "msftncsi",
        "trafficmanager.net",
        "windowsupdate.com",
        "telemetry.microsoft.com",

        // Certificate & Network Noise
        "ocsp.",
        "crl.",
        "digicert.com",
        "sectigo.com",
        "letsencrypt.org",
        "pool.ntp.org",

        // Video / CDN Shards (bukan domain situs utama)
        "googlevideo.com",
        "fbcdn.net",
        "cdninstagram.com"
    )

    /**
     * Mengembalikan true jika domain merupakan telemetri internal OS, DoH probe, atau captive check.
     * Hanya digunakan untuk memfilter query ALLOW agar antarmuka pengguna hanya menampilkan situs web utama yang relevan.
     */
    fun isNoisy(domain: String): Boolean {
        val lower = domain.lowercase().trim().trimEnd('.')
        if (lower.isEmpty()) return true
        if (NOISY_SUFFIXES.any { lower.endsWith(it) }) return true
        if (NOISY_CONTAINS.any { lower.contains(it) }) return true
        if (lower == "wpad" || lower.startsWith("_dns-sd") || lower.startsWith("_tcp.") || lower.startsWith("_udp.")) return true
        return false
    }

    /**
     * Menyederhanakan subdomain ke domain situs web utama.
     * Contoh: "m.youtube.com" -> "youtube.com", "news.detik.com" -> "detik.com"
     */
    fun extractMainDomain(domain: String): String {
        val clean = domain.lowercase().trim().trimEnd('.')
        val parts = clean.split('.')
        if (parts.size <= 2) return clean

        val secondToLast = parts[parts.size - 2]
        val last = parts.last()
        val isTwoPartTld = (last.length == 2 && (secondToLast == "co" || secondToLast == "com" || secondToLast == "org" || secondToLast == "net" || secondToLast == "go" || secondToLast == "ac" || secondToLast == "sch"))

        return if (isTwoPartTld && parts.size >= 3) {
            parts.takeLast(3).joinToString(".")
        } else {
            parts.takeLast(2).joinToString(".")
        }
    }
}
