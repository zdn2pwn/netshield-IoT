package com.mistxyt.netshield_iot.core.util

object DomainFilterHelper {

    private val NOISY_SUFFIXES = listOf(
        ".arpa",
        ".local",
        ".aaplimg.com",
        ".apple-dns.net"
    )

    private val NOISY_CONTAINS = listOf(
        "mask.icloud",
        "mask-h2.icloud",
        "metrics.icloud",
        "gateway.icloud",
        "dns.google",
        "cloudflare-dns",
        "one.one.one.one",
        "dns.quad9",
        "connectivitycheck.gstatic",
        "captive.apple.com",
        "msftconnecttest",
        "clients1.google.com",
        "clients3.google.com",
        "clients4.google.com",
        "time.apple.com",
        "configuration.apple.com"
    )

    /**
     * Mengembalikan true jika domain merupakan telemetri internal OS, DoH probe, atau captive check.
     * Hanya digunakan untuk memfilter query ALLOW agar antarmuka pengguna hanya menampilkan situs web yang relevan.
     */
    fun isNoisy(domain: String): Boolean {
        val lower = domain.lowercase().trim()
        if (NOISY_SUFFIXES.any { lower.endsWith(it) }) return true
        if (NOISY_CONTAINS.any { lower.contains(it) }) return true
        if (lower == "wpad" || lower.startsWith("_dns-sd")) return true
        return false
    }
}
