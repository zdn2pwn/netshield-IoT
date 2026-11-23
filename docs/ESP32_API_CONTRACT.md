# NetShield IoT — ESP32 Firmware API Specification & Contract

This document provides the canonical technical specification for the REST API and WebSocket interface between the **NetShield IoT Android Dashboard** and the **ESP32 DNS Filtering Engine** firmware.

---

## 1. Network Topology & Transport Architecture

- **Protocol**: HTTP/1.1 & RFC 6455 WebSocket
- **Default REST Base URL**: `http://192.168.1.10:80`
- **Default WebSocket URL**: `ws://192.168.1.10:81/ws`
- **Network Scope**: Trusted Local Area Network (Home LAN / Lab IoT testbed).
- **Content-Type**: `application/json; charset=utf-8`

---

## 2. REST Endpoints Specification

### 2.1 Health Check
- **Endpoint**: `GET /api/v1/health`
- **Description**: Lightweight heartbeat used by the Android app to test connectivity.
- **Response**: `200 OK`
```json
{
  "status": "ok",
  "timestamp": 1732381200
}
```

---

### 2.2 System & Hardware Status
- **Endpoint**: `GET /api/v1/status`
- **Description**: Returns hardware health, DNS engine operational mode, and uptime.
- **Response**: `200 OK`
```json
{
  "online": true,
  "ipAddress": "192.168.1.10",
  "dnsEngineStatus": "ACTIVE",
  "uptime": 142850,
  "lastSync": 1732381140,
  "freeHeap": 196608
}
```
- **Field Definitions**:
  - `online` (boolean): `true` when engine is operational.
  - `dnsEngineStatus` (string): `"ACTIVE"`, `"PAUSED"`, or `"DEGRADED"`.
  - `uptime` (integer): Total uptime in seconds.
  - `lastSync` (integer): Epoch timestamp (seconds or milliseconds) of last rule sync.
  - `freeHeap` (integer): ESP32 free heap memory in bytes.

---

### 2.3 Dashboard Cumulative Statistics
- **Endpoint**: `GET /api/v1/stats`
- **Description**: High-level counters for dashboard statistics cards.
- **Response**: `200 OK`
```json
{
  "totalRequests": 24850,
  "blockedRequests": 4120,
  "allowedRequests": 20730,
  "uniqueDevices": 6
}
```

---

### 2.4 Traffic Analytics (Hourly 24-Hour Bins)
- **Endpoint**: `GET /api/v1/traffic`
- **Description**: Chronological timeseries datapoints for rendering the dashboard traffic chart.
- **Response**: `200 OK`
```json
[
  {
    "timestamp": 1732309200,
    "label": "04:00",
    "totalQueries": 280,
    "blockedQueries": 24
  },
  {
    "timestamp": 1732323600,
    "label": "08:00",
    "totalQueries": 1450,
    "blockedQueries": 190
  },
  {
    "timestamp": 1732338000,
    "label": "12:00",
    "totalQueries": 2890,
    "blockedQueries": 480
  },
  {
    "timestamp": 1732352400,
    "label": "16:00",
    "totalQueries": 3120,
    "blockedQueries": 560
  },
  {
    "timestamp": 1732366800,
    "label": "20:00",
    "totalQueries": 4450,
    "blockedQueries": 890
  },
  {
    "timestamp": 1732381200,
    "label": "24:00",
    "totalQueries": 3980,
    "blockedQueries": 680
  }
]
```

---

### 2.5 Recent Historical DNS Traffic
- **Endpoint**: `GET /api/v1/traffic/recent?limit=20`
- **Description**: Returns recent buffered DNS queries from ESP32 circular memory buffer.
- **Response**: `200 OK`
```json
[
  {
    "id": "evt-10928",
    "timestamp": 1732381195,
    "clientIp": "192.168.1.105",
    "domain": "tracker.adservice.google.com",
    "queryType": "A",
    "action": "BLOCK",
    "category": "Ads & Trackers",
    "source": "Threat Intel Feed"
  },
  {
    "id": "evt-10927",
    "timestamp": 1732381180,
    "clientIp": "192.168.1.101",
    "domain": "api.github.com",
    "queryType": "HTTPS",
    "action": "ALLOW",
    "category": "Development",
    "source": "Upstream DNS"
  }
]
```

---

### 2.6 Domain Blocklist Management

#### A. Fetch All Blocked Domains
- **Endpoint**: `GET /api/v1/blocklist`
- **Response**: `200 OK`
```json
[
  {
    "id": "blk-1",
    "domain": "crypto-miner-pool.xyz",
    "category": "Malware",
    "source": "Threat Intel Feed",
    "createdAt": 1732035600,
    "enabled": true
  },
  {
    "id": "blk-2",
    "domain": "slot-gacor-online88.top",
    "category": "Gambling",
    "source": "Manual Entry",
    "createdAt": 1732122000,
    "enabled": true
  }
]
```

#### B. Add Domain to Blocklist
- **Endpoint**: `POST /api/v1/blocklist`
- **Request Body**:
```json
{
  "domain": "malicious-phish.net",
  "category": "Phishing",
  "source": "Manual Entry"
}
```
- **Response**: `201 Created` or `200 OK`
```json
{
  "id": "blk-9281a4b1",
  "domain": "malicious-phish.net",
  "category": "Phishing",
  "source": "Manual Entry",
  "createdAt": 1732381220,
  "enabled": true
}
```

#### C. Remove Domain from Blocklist
- **Endpoint**: `DELETE /api/v1/blocklist/{id}`
- **Response**: `200 OK`
```json
{
  "success": true,
  "message": "Domain removed from blocklist"
}
```

#### D. Synchronize Blocklist Rules with ESP32 Storage
- **Endpoint**: `POST /api/v1/blocklist/sync`
- **Response**: `200 OK`
```json
{
  "success": true,
  "syncedRulesCount": 14250,
  "timestamp": 1732381250
}
```

---

### 2.7 Observed Devices
- **Endpoint**: `GET /api/v1/devices`
- **Description**: Returns all client IP addresses observed by the DNS engine.
- **Response**: `200 OK`
```json
[
  {
    "id": "dev-101",
    "ipAddress": "192.168.1.101",
    "macAddress": "E4:5F:01:2D:8A:10",
    "requestCount": 8420,
    "blockedCount": 1340,
    "lastActivity": 1732381180
  },
  {
    "id": "dev-105",
    "ipAddress": "192.168.1.105",
    "macAddress": "3C:28:6D:71:B2:44",
    "requestCount": 6890,
    "blockedCount": 1820,
    "lastActivity": 1732381195
  }
]
```

---

## 3. Real-Time WebSocket Streaming

- **URI**: `ws://192.168.1.10:81/ws`
- **Direction**: ESP32 Server $\rightarrow$ Android Client
- **Format**: JSON text frames

### Packet Format:
```json
{
  "id": "ws-evt-4019",
  "timestamp": 1732381205000,
  "clientIp": "192.168.1.105",
  "domain": "tracking.analytics-network.com",
  "queryType": "A",
  "action": "BLOCK",
  "category": "Ads & Trackers",
  "source": "Threat Intel Feed"
}
```

### Action Values:
- `"BLOCK"`: The DNS request matched a blocklist rule and was sinkholed (e.g. 0.0.0.0 returned).
- `"ALLOW"`: The DNS request was permitted and forwarded to upstream DNS.

---

## 4. ESP32 Firmware Implementation Reference (C++ / ArduinoJson)

```cpp
#include <WiFi.h>
#include <WebServer.h>
#include <WebSocketsServer.h>
#include <ArduinoJson.h>

WebServer server(80);
WebSocketsServer webSocket = WebSocketsServer(81);

void setupApiRoutes() {
    server.on("/api/v1/health", HTTP_GET, []() {
        StaticJsonDocument<128> doc;
        doc["status"] = "ok";
        doc["timestamp"] = millis() / 1000;
        String response;
        serializeJson(doc, response);
        server.send(200, "application/json", response);
    });
    
    server.begin();
    webSocket.begin();
}

void broadcastDnsEvent(const char* clientIp, const char* domain, const char* action, const char* category) {
    StaticJsonDocument<256> doc;
    doc["id"] = String("evt-") + String(millis());
    doc["timestamp"] = millis();
    doc["clientIp"] = clientIp;
    doc["domain"] = domain;
    doc["queryType"] = "A";
    doc["action"] = action;
    doc["category"] = category;
    doc["source"] = "ESP32 Core";
    
    String jsonStr;
    serializeJson(doc, jsonStr);
    webSocket.broadcastTXT(jsonStr);
}
```
