<div align="center">

# 🧭 Gestor de Rutas

**App Android para registrar, guardar y compartir rutas al aire libre (andar, correr y bici).**

![Plataforma](https://img.shields.io/badge/Plataforma-Android-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)
![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)
![Estado](https://img.shields.io/badge/Estado-Completado-success)

</div>

---

## 📌 Información del proyecto

| Campo | Valor |
|---|---|
| **Módulo** | PMDM · Programación Multimedia y Dispositivos Móviles |
| **Curso** | 2025-2026 |
| **Centro** | IES Juan Bosco |
| **Fecha** | 20/02/2026 |

---

## ✨ Descripción

**Gestor de Rutas** es una aplicación Android orientada a personas que practican deporte al aire libre y quieren llevar un registro fiable de sus recorridos.

Permite:
- 📍 Seguir rutas con GPS en tiempo real.
- 🧷 Añadir waypoints durante la actividad.
- 📸 Asociar fotos geolocalizadas a los waypoints.
- 💾 Guardar el historial localmente.
- 🔄 Importar y exportar rutas en formato **GPX**.
- 📤 Compartir recorridos con otras aplicaciones.

---

## 🚀 Funcionalidades implementadas

- **Grabación de rutas en segundo plano** con cálculo de:
  - distancia total,
  - duración,
  - velocidad media.
- **Creación de waypoints** durante el recorrido.
- **Captura de fotografías** para enriquecer cada waypoint.
- **Historial completo de rutas** con:
  - visualización detallada,
  - renombrado,
  - borrado.
- **Importación y exportación GPX** (`.gpx`) para interoperabilidad.
- **Compartición de información** del recorrido vía apps externas.

---

## 🧱 Stack tecnológico

### Interfaz y arquitectura
- **Jetpack Compose** (Material 3)
- **ViewModel + StateFlow/Flow**

### Ubicación y mapas
- **FusedLocationProviderClient** (Google Play Services)
- **OSMDroid** para visualización de mapa, marcadores y polilíneas

### Datos y almacenamiento
- **Room Database** para rutas, puntos geográficos y waypoints
- **XML nativo** para lectura/escritura de archivos GPX

### Otras librerías
- **Coil** para carga de imágenes
- **Firebase Analytics**

---

## 📱 Requisitos y entorno

- **Android Studio** (recomendado: versión estable reciente)
- **JDK 11**
- **SDK de Android:**
  - `minSdk = 24`
  - `targetSdk = 35`
  - `compileSdk = 35`

---

## ⚙️ Instalación y ejecución

1. Clona el repositorio:
   ```bash
   git clone <URL_DEL_REPO>
   cd AppRutas
   ```
2. Abre el proyecto en **Android Studio**.
3. Sincroniza Gradle.
4. Ejecuta la app en emulador o dispositivo físico con GPS.

> ✅ Recomendación: para probar seguimiento real, usa un dispositivo físico.

---

## 🔐 Permisos solicitados

La app declara los siguientes permisos en `AndroidManifest.xml`:

- `android.permission.INTERNET` → cargar tiles del mapa.
- `android.permission.ACCESS_FINE_LOCATION` → ubicación precisa para el trazado.
- `android.permission.ACCESS_COARSE_LOCATION` → ubicación aproximada.
- `android.permission.CAMERA` → captura de fotos para waypoints.

Además, la cámara está marcada como opcional:
- `android.hardware.camera` con `required="false"`.

---

## 🗂️ Estructura principal del proyecto

```text
app/src/main/java/com/example/rutapersonal/
├── data/               # Room: DAO, DB y repositorio
├── model/              # Entidades y modelos de dominio
├── ui/                 # Composables y ViewModels
└── utlis/              # Utilidades (GPX)
```

---

## 🎥 Demo

- **Vídeo de demostración:** https://youtube.com/shorts/8H_34uFe3oQ?feature=share

---

## 🛣️ Trabajo futuro

- ☁️ Sincronización en la nube de recorridos.
- 🧭 Navegación asistida sobre rutas importadas.
- 📊 Métricas avanzadas por sesión (ritmo por tramo, desnivel, etc.).

---

<div align="center">

Hecho con ❤️ en Kotlin + Compose.

</div>
