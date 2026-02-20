# 1. Gestor de Rutas

[cite_start]**Módulo:** PMDM - Programación Multimedia y Dispositivos Móviles
[cite_start]**Curso:** 2025-2026 
[cite_start]**Centro:** IES Juan Bosco
[cite_start]**Alumno/s:** Remus Sabou
[cite_start]**Fecha:** 20/02/2026

## 2. Descripción
Gestor de Rutas es una aplicación para Android diseñada para amantes del deporte al aire libre. Resuelve el problema de trazar y guardar rutas personales, permitiendo registrar paseos, carreras o rutas en bicicleta. Está dirigida a cualquier usuario que desee llevar un registro de sus recorridos. Las funcionalidades principales incluyen el seguimiento GPS en tiempo real, creación de waypoints con fotografías y exportación/importación de recorridos en formato GPX. [cite_start]Utiliza Jetpack Compose para la interfaz, Room para la base de datos local y OSMDroid para la visualización de mapas. [cite: 279, 280, 281, 282, 283]

## 3. Características
La aplicación ha sido desarrollada utilizando un enfoque moderno con las siguientes tecnologías:
* **UI:** Jetpack Compose (Material Design 3).
* **Mapas:** OSMDroid para representar la ruta dibujando polilíneas y marcadores.
* **Almacenamiento Local:** Room Database (con flujos reactivos mediante `Flow`) para guardar el historial de las rutas, puntos geográficos y waypoints.
* **Ubicación:** `FusedLocationProviderClient` de Google Play Services para obtener latitud, longitud y altitud.
* [cite_start]**Gestión de archivos:** Generación y lectura de archivos XML nativos para soportar el formato GPX. [cite: 285]

## 4. Funcionalidades implementadas
* Grabación de rutas en segundo plano con cálculo de distancia, duración y velocidad media.
* Adición de Waypoints durante la ruta interactuando con la cámara del dispositivo para tomar fotos geolocalizadas.
* Historial completo de rutas guardadas, con opciones de visualización detallada, renombrado y borrado.
* Importación y Exportación de las rutas en formato estandarizado GPX (`.gpx`).
* Compartición de la información del recorrido a través de otras apps instaladas. 

## 5. Funcionalidades NO implementadas (trabajo futuro)
* Sincronización en la nube de los recorridos.

## 6. Video de demostración
[cite_start]**Enlace al video:** [Pega aquí el enlace de YouTube/Drive/GitHub Releases] 

## 7. Permisos solicitados
[cite_start]Para poder funcionar correctamente, la aplicación declara en su `AndroidManifest.xml` los siguientes permisos: 
* [cite_start]`android.permission.INTERNET`: Necesario para cargar los "tiles" del mapa de OSMDroid. 
* `android.permission.ACCESS_FINE_LOCATION`: Para obtener las coordenadas exactas del GPS necesarias para el trazado de la ruta.
* `android.permission.ACCESS_COARSE_LOCATION`: Para obtener la ubicación general cuando no hay alta precisión.
* [cite_start]`android.permission.CAMERA`: Para tomar fotografías y asociarlas a los Waypoints creados. 

## 8. Autor
* [cite_start]**Remus Sabou** - [https://github.com/MrBrad8989] - [remussabou02@gmail.com] 
* [cite_start]**Última actualización:** 20/02/2026
* [cite_start]**Estado del proyecto:** Completado - Listo para producción 
