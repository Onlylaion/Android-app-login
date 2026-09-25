# Conexión MQTT: LogginGym (Android) ↔ Mosquitto (Windows)

Esta guía explica cómo dejar funcionando la conexión MQTT entre la app Android
(emulador) y el broker Mosquitto que corre en esta PC Windows, y qué hacer cada
vez que reiniciás el emulador o la PC.

---

## 1. Qué problema hubo y por qué se resolvió así

El error original (`SocketTimeoutException` al conectar a `10.0.2.2:1883`)
**no era un problema de red**. Se comprobó que:

- Mosquitto escuchaba bien en `0.0.0.0:1883`.
- El emulador SÍ podía llegar por TCP a `10.0.2.2:1883` y a `127.0.0.1:1883`
  (probado con `ping` y `nc` desde dentro del emulador).

La causa real: la librería `org.eclipse.paho.android.service:1.1.1` (sin
mantenimiento desde 2017) crashea la app apenas logra conectar, porque
registra un `BroadcastReceiver` interno sin el flag `RECEIVER_EXPORTED` /
`RECEIVER_NOT_EXPORTED`, obligatorio desde Android 13+ (API 33+). El emulador
Pixel 7 usado corre una build de Android muy nueva (API 37), así que el crash
era 100% reproducible.

**Solución aplicada:**

1. Se reemplazó esa librería por el fork mantenido
   [`com.github.hannesa2:paho.mqtt.android`](https://github.com/hannesa2/paho.mqtt.android)
   (versión `4.5`), que reescribió esa parte usando `WorkManager` en vez del
   `BroadcastReceiver` roto.
2. Se usa `adb reverse` para tunelizar el puerto 1883 del emulador directo al
   `127.0.0.1` de Windows, evitando cualquier duda sobre la red virtual del
   emulador (`10.0.2.2`).

Archivos que se tocaron:
- `settings.gradle.kts` → se agregó el repositorio `https://jitpack.io`.
- `app/build.gradle.kts` → dependencia MQTT actualizada.
- `app/src/main/AndroidManifest.xml` → el `<service>` ahora es
  `info.mqtt.android.service.MqttService`.
- `app/src/main/java/com/example/loggingym/ActividadMenu.java` → import
  actualizado (`info.mqtt.android.service.MqttAndroidClient`) y
  `brokerUri = "tcp://127.0.0.1:1883"`.

---

## 2. Prerrequisitos (una sola vez)

### 2.1 Mosquitto corriendo y escuchando en todas las interfaces

Archivo `C:\Program Files\Mosquitto\mosquitto.conf` debe tener:

```
listener 1883 0.0.0.0
allow_anonymous true
```

Verificar que el servicio esté corriendo:

```powershell
Get-Service Mosquitto
```

Debe decir `Status: Running`. Si no:

```powershell
Start-Service Mosquitto
```

Verificar que escucha en el puerto:

```powershell
netstat -an | Select-String "1883"
```

Deberías ver `0.0.0.0:1883` en estado `LISTENING`.

### 2.2 Regla de firewall (ya creada, solo para referencia)

```powershell
Get-NetFirewallRule -DisplayName "*1883*" | Format-Table DisplayName, Enabled, Direction, Action
```

Si aparece duplicada, no pasa nada — no interfiere. Si quisieras limpiarla:

```powershell
Get-NetFirewallRule -DisplayName "*1883*"
```

y borrar la que sobre con `Remove-NetFirewallRule -Name "<Name>"`.

> Nota: con `adb reverse` (ver más abajo) el firewall de Windows en realidad
> no entra en juego, porque la conexión llega por el túnel ADB, no por la red
> virtual del emulador ni por la LAN.

---

## 3. Cada vez que abrís el proyecto para trabajar (checklist rápido)

Ejecutar en orden, en PowerShell:

### Paso 1 — Confirmar que Mosquitto está corriendo

```powershell
Get-Service Mosquitto
```

### Paso 2 — Levantar el emulador

Abrí el emulador Pixel 7 (API 37) desde Android Studio o con:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe" -avd <nombre_del_avd>
```

(para ver los nombres disponibles: `& "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe" -list-avds`)

### Paso 3 — Confirmar que ADB lo ve

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb devices -l
```

Tiene que aparecer algo como `emulator-5554  device`.

### Paso 4 — ⚠️ Configurar el túnel adb reverse (SE PIERDE AL REINICIAR EL EMULADOR)

Este paso hay que repetirlo cada vez que el emulador se reinicia o se cierra
y se vuelve a abrir (y cada vez que reiniciás el servidor ADB):

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb reverse tcp:1883 tcp:1883
```

Verificar que quedó activo:

```powershell
& $adb reverse --list
```

Debe mostrar: `host-XX tcp:1883 tcp:1883`

### Paso 5 — Compilar e instalar la app

Desde la raíz del proyecto (`C:\Users\Usuario\AndroidStudioProjects\LogginGym`):

```powershell
.\gradlew.bat assembleDebug
& $adb install -r "app\build\outputs\apk\debug\app-debug.apk"
```

O simplemente correr la app con el botón ▶ de Android Studio (hace lo mismo).

### Paso 6 — Abrir la app y navegar a la pantalla del menú

En la pantalla de login, tocá **Confirmar** (no valida usuario/contraseña,
solo navega a `ActividadMenu`, que es donde se dispara la conexión MQTT).

### Paso 7 — Revisar el logcat

```powershell
& $adb logcat -d -s MQTT:D
```

Deberías ver:

```
D MQTT: Conectado al broker
```

---

## 4. Probar que llegan mensajes

Con la app abierta en `ActividadMenu` (ya conectada y suscripta a
`gimnasio/puerta/estado`), publicá un mensaje de prueba desde la PC:

**Opción A — con `mosquitto_pub` (viene instalado con Mosquitto):**

```powershell
& "C:\Program Files\Mosquitto\mosquitto_pub.exe" -h 127.0.0.1 -p 1883 -t "gimnasio/puerta/estado" -m "ABIERTA"
```

**Opción B — con MQTT Explorer:**

Conectate a `192.168.0.237:1883` (o `localhost:1883`), publicá en el tópico
`gimnasio/puerta/estado` el payload que quieras.

Después revisá el logcat de nuevo:

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb logcat -d -s MQTT:D
```

Deberías ver algo como:

```
D MQTT: Llegó: ABIERTA en topic: gimnasio/puerta/estado
```

---

## 5. Troubleshooting

| Síntoma | Causa probable | Qué hacer |
|---|---|---|
| `SocketTimeoutException` conectando | Falta el `adb reverse` (se perdió al reiniciar el emulador) | Repetir el Paso 4 |
| La app se cierra sola ("LogginGym keeps stopping") al ir al menú | Volviste a una versión vieja de la librería Paho, o falta el repo JitPack | Revisar `app/build.gradle.kts` tenga `com.github.hannesa2:paho.mqtt.android:4.5` y `settings.gradle.kts` tenga `https://jitpack.io` |
| `adb devices` no muestra el emulador | El emulador no terminó de bootear, o se cerró | Esperar o volver a abrirlo |
| Build falla bajando `com.github.hannesa2:...` | JitPack momentáneamente caído, o falta conexión a internet | Reintentar `./gradlew.bat assembleDebug --refresh-dependencies` |
| Mensaje publicado no llega al logcat | La app no está conectada (revisar que diga "Conectado al broker" primero) o publicaste en otro tópico | Confirmar tópico exacto: `gimnasio/puerta/estado` |

---

## 6. Cuando conectes el ESP32 real

Estos pasos son solo para el emulador. Cuando uses un dispositivo Android
físico o el ESP32:

- El ESP32 y un teléfono físico en la misma red Wi-Fi deben usar la IP LAN de
  esta PC (`192.168.0.237`), **no** `10.0.2.2` ni `127.0.0.1` (esas dos son
  trucos específicos del emulador).
- En ese caso sí importa el firewall de Windows (Paso 2.2) porque el tráfico
  entra por la red real, no por el túnel ADB.
