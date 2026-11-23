package com.mistxyt.netshield_iot.data.remote.websocket

import com.google.gson.Gson
import com.mistxyt.netshield_iot.core.util.DateTimeHelper
import com.mistxyt.netshield_iot.data.remote.dto.DnsEventDto
import com.mistxyt.netshield_iot.domain.model.DnsAction
import com.mistxyt.netshield_iot.domain.model.DnsEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

enum class WebSocketState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    RECONNECTING
}

class NetShieldWebSocketClient(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var webSocket: WebSocket? = null
    private val isRunning = AtomicBoolean(false)
    private var reconnectAttempt = 0
    private var currentUrl: String = ""

    private val _connectionState = MutableStateFlow(WebSocketState.DISCONNECTED)
    val connectionState: StateFlow<WebSocketState> = _connectionState.asStateFlow()

    private val _incomingEvents = MutableSharedFlow<DnsEvent>(extraBufferCapacity = 64)
    val incomingEvents: SharedFlow<DnsEvent> = _incomingEvents.asSharedFlow()

    private var reconnectJob: Job? = null

    fun connect(ip: String, port: Int) {
        val cleanIp = com.mistxyt.netshield_iot.data.remote.NetworkModule.sanitizeIp(ip).ifBlank { "127.0.0.1" }
        val effectivePort = if (port in 1..65535) port else 81
        val url = "ws://$cleanIp:$effectivePort/ws"
        if (currentUrl == url && _connectionState.value == WebSocketState.CONNECTED) {
            return
        }
        currentUrl = url
        isRunning.set(true)
        reconnectAttempt = 0
        reconnectJob?.cancel()
        initiateConnection()
    }

    private fun initiateConnection() {
        if (!isRunning.get()) return

        _connectionState.value = if (reconnectAttempt > 0) WebSocketState.RECONNECTING else WebSocketState.CONNECTING

        try {
            webSocket?.close(1000, "Reconnecting")
        } catch (_: Exception) {}

        val request = Request.Builder()
            .url(currentUrl)
            .build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                reconnectAttempt = 0
                _connectionState.value = WebSocketState.CONNECTED
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                parseAndEmitEvent(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                _connectionState.value = WebSocketState.DISCONNECTED
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _connectionState.value = WebSocketState.DISCONNECTED
                scheduleReconnect()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _connectionState.value = WebSocketState.DISCONNECTED
                scheduleReconnect()
            }
        })
    }

    private fun parseAndEmitEvent(json: String) {
        try {
            val dto = gson.fromJson(json, DnsEventDto::class.java)
            if (dto != null && !dto.domain.isNullOrBlank()) {
                val event = DnsEvent(
                    id = dto.id ?: "evt-${System.currentTimeMillis()}-${(100..999).random()}",
                    timestamp = DateTimeHelper.resolveValidEpochMs(dto.timestamp),
                    clientIp = dto.clientIp ?: "192.168.1.1",
                    domain = dto.domain,
                    queryType = dto.queryType ?: "A",
                    action = DnsAction.fromString(dto.action),
                    category = dto.category ?: "General",
                    source = dto.source ?: "ESP32 Core"
                )
                scope.launch {
                    _incomingEvents.emit(event)
                }
            }
        } catch (_: Exception) {
            // Ignore malformed packet safely
        }
    }

    private fun scheduleReconnect() {
        if (!isRunning.get()) return

        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            reconnectAttempt++
            // Exponential backoff: 1s, 2s, 4s, 8s, max 30s
            val delayMs = ((1L shl (reconnectAttempt.coerceAtMost(5) - 1)) * 1000L).coerceIn(1000L, 30000L)
            _connectionState.value = WebSocketState.RECONNECTING
            delay(delayMs)
            if (isRunning.get()) {
                initiateConnection()
            }
        }
    }

    fun disconnect() {
        isRunning.set(false)
        reconnectJob?.cancel()
        try {
            webSocket?.close(1000, "Client disconnect")
        } catch (_: Exception) {}
        webSocket = null
        _connectionState.value = WebSocketState.DISCONNECTED
    }
}
