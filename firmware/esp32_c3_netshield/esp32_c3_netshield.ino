/*
 * NetShield IoT — ESP32-C3 SuperMini DNS Filtering Engine & API Gateway
 * Board: ESP32-C3 SuperMini (RISC-V 160MHz, 4MB Flash)
 *
 * Mode: Pure Station Mode (STA) — Terhubung langsung ke Wi-Fi Rumah ALMERE
 */

#include <WiFi.h>
#include <WiFiUdp.h>
#include <WebServer.h>
#include <WebSocketsServer.h>
#include <ArduinoJson.h>

// ==========================================
// 1. WI-FI CONFIGURATION (PURE STATION MODE)
// ==========================================
const char* WIFI_SSID     = "New york";
const char* WIFI_PASSWORD = "rongawi123";

// Upstream Public DNS (Cloudflare)
IPAddress upstreamDns(1, 1, 1, 1);
const uint16_t DNS_PORT = 53;
const int LED_PIN = 8; // Onboard LED pada ESP32-C3 SuperMini (Active LOW)

// ==========================================
// 2. NETWORK & SERVER INSTANCES
// ==========================================
WiFiUDP udpDns;
WebServer restServer(80);
WebSocketsServer wsServer(81);

// ==========================================
// 3. IN-MEMORY DATA STRUCTURES
// ==========================================

struct BlockRule {
  String id;
  String domain;
  String category;
  String source;
  unsigned long createdAt;
};

const int MAX_RULES = 150;
BlockRule blocklist[MAX_RULES];
int blocklistCount = 0;

struct DeviceRecord {
  String id;
  String ip;
  int requests;
  int blocked;
  unsigned long lastActivity;
};

const int MAX_DEVICES = 30;
DeviceRecord devices[MAX_DEVICES];
int deviceCount = 0;

struct RecentEvent {
  String id;
  unsigned long timestamp;
  String clientIp;
  String domain;
  String action;
  String category;
};

const int MAX_RECENT = 25;
RecentEvent recentEvents[MAX_RECENT];
int recentCount = 0;

// Statistik Kumulatif
unsigned long totalDnsRequests = 0;
unsigned long blockedDnsRequests = 0;
unsigned long allowedDnsRequests = 0;
unsigned long bootTimestamp = 0;
unsigned long lastSyncTime = 0;

// ==========================================
// 4. HELPER FUNCTIONS
// ==========================================

void addDefaultRules() {
  blocklist[0] = {"blk-1", "doubleclick.net", "Ads & Trackers", "NetShield Feed", millis()};
  blocklist[1] = {"blk-2", "googleads.g.doubleclick.net", "Ads & Trackers", "NetShield Feed", millis()};
  blocklist[2] = {"blk-3", "pagead2.googlesyndication.com", "Ads & Trackers", "NetShield Feed", millis()};
  blocklist[3] = {"blk-4", "adservice.google.com", "Ads & Trackers", "NetShield Feed", millis()};
  blocklist[4] = {"blk-5", "crypto-miner-pool.xyz", "Malware", "Threat Intel", millis()};
  blocklist[5] = {"blk-6", "slot-gacor-online88.top", "Gambling", "Manual Entry", millis()};
  blocklist[6] = {"blk-7", "secure-login-bank-verify.cc", "Phishing", "Threat Intel", millis()};
  blocklistCount = 7;
}

int findBlockRule(const String& domain) {
  for (int i = 0; i < blocklistCount; i++) {
    if (domain.equalsIgnoreCase(blocklist[i].domain) ||
        domain.endsWith("." + blocklist[i].domain)) {
      return i;
    }
  }
  return -1;
}

void trackDevice(const String& ipStr, bool isBlocked) {
  for (int i = 0; i < deviceCount; i++) {
    if (devices[i].ip == ipStr) {
      devices[i].requests++;
      if (isBlocked) devices[i].blocked++;
      devices[i].lastActivity = millis();
      return;
    }
  }
  if (deviceCount < MAX_DEVICES) {
    devices[deviceCount].id = "dev-" + String(deviceCount + 1);
    devices[deviceCount].ip = ipStr;
    devices[deviceCount].requests = 1;
    devices[deviceCount].blocked = isBlocked ? 1 : 0;
    devices[deviceCount].lastActivity = millis();
    deviceCount++;
  }
}

void recordRecentEvent(const String& clientIp, const String& domain, const String& action, const String& category) {
  if (recentCount < MAX_RECENT) {
    recentCount++;
  }
  for (int i = recentCount - 1; i > 0; i--) {
    recentEvents[i] = recentEvents[i - 1];
  }
  recentEvents[0].id = "evt-" + String(millis()) + "-" + String(random(100, 999));
  recentEvents[0].timestamp = millis();
  recentEvents[0].clientIp = clientIp;
  recentEvents[0].domain = domain;
  recentEvents[0].action = action;
  recentEvents[0].category = category;
}

void broadcastWebSocketEvent(const String& clientIp, const String& domain, const String& action, const String& category) {
  JsonDocument doc;
  doc["id"] = "evt-" + String(millis()) + "-" + String(random(100, 999));
  doc["timestamp"] = millis();
  doc["clientIp"] = clientIp;
  doc["domain"] = domain;
  doc["queryType"] = "A";
  doc["action"] = action;
  doc["category"] = category;
  doc["source"] = "ESP32 Sinkhole";

  String jsonString;
  serializeJson(doc, jsonString);
  wsServer.broadcastTXT(jsonString);
}

// ==========================================
// 5. DNS PARSING & SINKHOLE LOGIC
// ==========================================

String parseDnsQuery(uint8_t* buffer, int length) {
  if (length < 12) return "";
  int pos = 12;
  String domain = "";

  while (pos < length && buffer[pos] != 0) {
    int len = buffer[pos++];
    if (pos + len > length) return "";
    for (int i = 0; i < len; i++) {
      domain += (char)buffer[pos++];
    }
    if (buffer[pos] != 0) {
      domain += ".";
    }
  }
  domain.toLowerCase();
  return domain;
}

void sendDnsAnswer(IPAddress clientIp, uint16_t clientPort, uint8_t* queryBuffer, int queryLength, IPAddress resolvedIp) {
  uint8_t response[512];
  if (queryLength > 512 - 16) return;

  memcpy(response, queryBuffer, queryLength);

  response[2] = 0x81;
  response[3] = 0x80;
  response[6] = 0x00;
  response[7] = 0x01;

  int pos = queryLength;

  response[pos++] = 0xC0;
  response[pos++] = 0x0C;
  response[pos++] = 0x00;
  response[pos++] = 0x01;
  response[pos++] = 0x00;
  response[pos++] = 0x01;
  response[pos++] = 0x00;
  response[pos++] = 0x00;
  response[pos++] = 0x00;
  response[pos++] = 0x3C;
  response[pos++] = 0x00;
  response[pos++] = 0x04;
  response[pos++] = resolvedIp[0];
  response[pos++] = resolvedIp[1];
  response[pos++] = resolvedIp[2];
  response[pos++] = resolvedIp[3];

  udpDns.beginPacket(clientIp, clientPort);
  udpDns.write(response, pos);
  udpDns.endPacket();
}

void sendSinkholeResponse(IPAddress clientIp, uint16_t clientPort, uint8_t* queryBuffer, int queryLength) {
  sendDnsAnswer(clientIp, clientPort, queryBuffer, queryLength, IPAddress(0, 0, 0, 0));
}

void forwardToUpstreamDns(IPAddress clientIp, uint16_t clientPort, uint8_t* queryBuffer, int queryLength) {
  WiFiUDP upstreamUdp;
  upstreamUdp.begin(0);
  upstreamUdp.beginPacket(upstreamDns, 53);
  upstreamUdp.write(queryBuffer, queryLength);
  upstreamUdp.endPacket();

  bool answered = false;
  unsigned long startWait = millis();
  while (millis() - startWait < 80) {
    int respSize = upstreamUdp.parsePacket();
    if (respSize > 0) {
      uint8_t respBuffer[512];
      int bytesRead = upstreamUdp.read(respBuffer, sizeof(respBuffer));
      if (bytesRead > 0) {
        udpDns.beginPacket(clientIp, clientPort);
        udpDns.write(respBuffer, bytesRead);
        udpDns.endPacket();
        answered = true;
      }
      break;
    }
    delay(2);
  }
  upstreamUdp.stop();

  // Fallback jika router tanpa internet/WAN: kembalikan IP gateway router
  // sehingga DNS query klien tidak time-out dan event ALLOW tetap tercatat instan!
  if (!answered) {
    IPAddress fallbackIp = WiFi.gatewayIP();
    if (fallbackIp == IPAddress(0, 0, 0, 0)) {
      fallbackIp = WiFi.localIP();
    }
    sendDnsAnswer(clientIp, clientPort, queryBuffer, queryLength, fallbackIp);
  }
}

void handleDnsPacket() {
  int packetSize = udpDns.parsePacket();
  if (packetSize <= 0) return;

  uint8_t packetBuffer[512];
  int len = udpDns.read(packetBuffer, sizeof(packetBuffer));
  if (len <= 0) return;

  IPAddress clientIp = udpDns.remoteIP();
  uint16_t clientPort = udpDns.remotePort();
  String domain = parseDnsQuery(packetBuffer, len);

  if (domain.length() == 0) return;

  totalDnsRequests++;
  String clientIpStr = clientIp.toString();

  int matchIndex = findBlockRule(domain);
  if (matchIndex != -1) {
    blockedDnsRequests++;
    String category = blocklist[matchIndex].category;
    sendSinkholeResponse(clientIp, clientPort, packetBuffer, len);
    trackDevice(clientIpStr, true);
    recordRecentEvent(clientIpStr, domain, "BLOCK", category);
    broadcastWebSocketEvent(clientIpStr, domain, "BLOCK", category);
    Serial.printf("[BLOCKED] %s from %s (%s)\n", domain.c_str(), clientIpStr.c_str(), category.c_str());
  } else {
    allowedDnsRequests++;
    forwardToUpstreamDns(clientIp, clientPort, packetBuffer, len);
    trackDevice(clientIpStr, false);
    recordRecentEvent(clientIpStr, domain, "ALLOW", "General");
    broadcastWebSocketEvent(clientIpStr, domain, "ALLOW", "General");
    Serial.printf("[ALLOWED] %s from %s\n", domain.c_str(), clientIpStr.c_str());
  }
}

// ==========================================
// 6. REST API ROUTES
// ==========================================

void setupRestRoutes() {
  restServer.enableCORS(true);

  // 0. Root Check & Simple Health
  restServer.on("/", HTTP_GET, []() {
    restServer.send(200, "text/plain", "NetShield IoT Gateway Active");
  });

  restServer.on("/health", HTTP_GET, []() {
    JsonDocument doc;
    doc["status"] = "ok";
    doc["timestamp"] = millis();
    String res;
    serializeJson(doc, res);
    restServer.send(200, "application/json", res);
  });

  // 1. Health Check
  restServer.on("/api/v1/health", HTTP_GET, []() {
    JsonDocument doc;
    doc["status"] = "ok";
    doc["timestamp"] = millis();
    String res;
    serializeJson(doc, res);
    restServer.send(200, "application/json", res);
  });

  // 2. System Status
  restServer.on("/api/v1/status", HTTP_GET, []() {
    JsonDocument doc;
    doc["online"] = true;
    doc["ipAddress"] = WiFi.localIP().toString();
    doc["dnsEngineStatus"] = "ACTIVE";
    doc["uptime"] = (millis() - bootTimestamp) / 1000;
    doc["lastSync"] = lastSyncTime;
    doc["freeHeap"] = ESP.getFreeHeap();
    String res;
    serializeJson(doc, res);
    restServer.send(200, "application/json", res);
  });

  // 3. Cumulative Stats
  restServer.on("/api/v1/stats", HTTP_GET, []() {
    JsonDocument doc;
    doc["totalRequests"] = totalDnsRequests;
    doc["blockedRequests"] = blockedDnsRequests;
    doc["allowedRequests"] = allowedDnsRequests;
    doc["uniqueDevices"] = deviceCount;
    String res;
    serializeJson(doc, res);
    restServer.send(200, "application/json", res);
  });

  // 4. Traffic Analytics
  restServer.on("/api/v1/traffic", HTTP_GET, []() {
    JsonDocument doc;
    JsonArray arr = doc.to<JsonArray>();

    unsigned long now = millis();
    JsonObject p1 = arr.add<JsonObject>();
    p1["timestamp"] = now;
    p1["label"] = "Now";
    p1["totalQueries"] = totalDnsRequests;
    p1["blockedQueries"] = blockedDnsRequests;

    String res;
    serializeJson(doc, res);
    restServer.send(200, "application/json", res);
  });

  // 5. Recent Traffic Events
  restServer.on("/api/v1/traffic/recent", HTTP_GET, []() {
    JsonDocument doc;
    JsonArray arr = doc.to<JsonArray>();

    for (int i = 0; i < recentCount; i++) {
      JsonObject item = arr.add<JsonObject>();
      item["id"] = recentEvents[i].id;
      item["timestamp"] = recentEvents[i].timestamp;
      item["clientIp"] = recentEvents[i].clientIp;
      item["domain"] = recentEvents[i].domain;
      item["queryType"] = "A";
      item["action"] = recentEvents[i].action;
      item["category"] = recentEvents[i].category;
      item["source"] = "ESP32 Core";
    }

    String res;
    serializeJson(doc, res);
    restServer.send(200, "application/json", res);
  });

  // 6. Blocklist Fetch
  restServer.on("/api/v1/blocklist", HTTP_GET, []() {
    JsonDocument doc;
    JsonArray arr = doc.to<JsonArray>();

    for (int i = 0; i < blocklistCount; i++) {
      JsonObject item = arr.add<JsonObject>();
      item["id"] = blocklist[i].id;
      item["domain"] = blocklist[i].domain;
      item["category"] = blocklist[i].category;
      item["source"] = blocklist[i].source;
      item["createdAt"] = blocklist[i].createdAt;
      item["enabled"] = true;
    }

    String res;
    serializeJson(doc, res);
    restServer.send(200, "application/json", res);
  });

  // 7. Add Blocklist Item
  restServer.on("/api/v1/blocklist", HTTP_POST, []() {
    if (!restServer.hasArg("plain")) {
      restServer.send(400, "application/json", "{\"error\":\"Missing body\"}");
      return;
    }
    JsonDocument doc;
    DeserializationError err = deserializeJson(doc, restServer.arg("plain"));
    if (err) {
      restServer.send(400, "application/json", "{\"error\":\"Invalid JSON\"}");
      return;
    }

    String domain = doc["domain"] | "";
    String category = doc["category"] | "Custom";
    String source = doc["source"] | "Manual Entry";

    if (domain.length() == 0 || blocklistCount >= MAX_RULES) {
      restServer.send(400, "application/json", "{\"error\":\"Cannot add domain\"}");
      return;
    }

    String newId = "blk-" + String(millis());
    blocklist[blocklistCount] = {newId, domain, category, source, millis()};
    blocklistCount++;

    JsonDocument resp;
    resp["id"] = newId;
    resp["domain"] = domain;
    resp["category"] = category;
    resp["source"] = source;
    resp["createdAt"] = millis();
    resp["enabled"] = true;

    String resStr;
    serializeJson(resp, resStr);
    restServer.send(201, "application/json", resStr);
  });

  // 8. Sync Blocklist
  restServer.on("/api/v1/blocklist/sync", HTTP_POST, []() {
    lastSyncTime = millis();
    JsonDocument doc;
    doc["success"] = true;
    doc["syncedRulesCount"] = blocklistCount;
    doc["timestamp"] = lastSyncTime;
    String res;
    serializeJson(doc, res);
    restServer.send(200, "application/json", res);
  });

  // 9. Devices Observed
  restServer.on("/api/v1/devices", HTTP_GET, []() {
    JsonDocument doc;
    JsonArray arr = doc.to<JsonArray>();

    for (int i = 0; i < deviceCount; i++) {
      JsonObject item = arr.add<JsonObject>();
      item["id"] = devices[i].id;
      item["ipAddress"] = devices[i].ip;
      item["macAddress"] = "ESP32-Observed";
      item["requestCount"] = devices[i].requests;
      item["blockedCount"] = devices[i].blocked;
      item["lastActivity"] = devices[i].lastActivity;
    }

    String res;
    serializeJson(doc, res);
    restServer.send(200, "application/json", res);
  });
}

// ==========================================
// 7. SETUP & MAIN LOOP
// ==========================================

void setup() {
  pinMode(LED_PIN, OUTPUT);
  digitalWrite(LED_PIN, HIGH); // LED mati saat awal (Active LOW)

  Serial.begin(115200);
  delay(1500);

  Serial.println("\n==========================================");
  Serial.println("   NetShield IoT — ESP32-C3 SuperMini    ");
  Serial.println("==========================================");

  addDefaultRules();
  bootTimestamp = millis();

  // Pure Station Mode (STA): Konek ke Wi-Fi Hotspot New york
  WiFi.mode(WIFI_STA);
  WiFi.setAutoReconnect(true);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.printf("Menghubungkan ke Wi-Fi '%s' ", WIFI_SSID);

  int attempts = 0;
  while (WiFi.status() != WL_CONNECTED && attempts < 40) {
    delay(250);
    digitalWrite(LED_PIN, !digitalRead(LED_PIN)); // Blink LED saat mencoba konek
    Serial.print(".");
    attempts++;
  }

  if (WiFi.status() == WL_CONNECTED) {
    digitalWrite(LED_PIN, LOW); // LED Solid ON jika berhasil terhubung
    Serial.printf("\n[OK] Berhasil Terhubung ke Wi-Fi '%s'!\n", WIFI_SSID);
    Serial.print("[IP ESP32] -> ");
    Serial.println(WiFi.localIP());
    Serial.print("[Gateway]  -> ");
    Serial.println(WiFi.gatewayIP());
    Serial.println("Masukkan IP ini di menu Settings aplikasi NetShield IoT!");
  } else {
    Serial.println("\n[INFO] Menunggu Wi-Fi siap di background...");
  }

  // Mulai DNS Server UDP di Port 53
  if (udpDns.begin(DNS_PORT)) {
    Serial.printf("[OK] DNS Sinkhole aktif di port %d\n", DNS_PORT);
  }

  // Mulai WebServer REST API di Port 80
  setupRestRoutes();
  restServer.begin();
  Serial.println("[OK] REST API WebServer aktif di port 80");

  // Mulai WebSockets Server di Port 81
  wsServer.begin();
  Serial.println("[OK] WebSocket Server aktif di port 81 (/ws)");
  Serial.println("==========================================\n");
}

void loop() {
  if (Serial.available()) {
    while (Serial.available()) Serial.read();
    Serial.printf("[IP ESP32] -> %s\n", WiFi.localIP().toString().c_str());
  }

  if (WiFi.status() == WL_CONNECTED) {
    digitalWrite(LED_PIN, LOW); // Tetap menyala jika terkoneksi
    handleDnsPacket();
    restServer.handleClient();
    wsServer.loop();

    static unsigned long lastIpPrint = 0;
    if (millis() - lastIpPrint > 4000) {
      lastIpPrint = millis();
      Serial.printf("[IP ESP32] -> %s\n", WiFi.localIP().toString().c_str());
    }
  } else {
    // Kedip lambat jika belum/terputus dari Wi-Fi
    static unsigned long lastBlink = 0;
    if (millis() - lastBlink > 800) {
      lastBlink = millis();
      digitalWrite(LED_PIN, !digitalRead(LED_PIN));
    }
  }
}
