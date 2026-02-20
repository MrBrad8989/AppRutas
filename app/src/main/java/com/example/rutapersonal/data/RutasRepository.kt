package com.example.rutapersonal.data

import com.example.rutapersonal.model.Actividad
import com.example.rutapersonal.model.PuntoRuta
import com.example.rutapersonal.model.Ruta
import com.example.rutapersonal.model.Waypoint
import kotlinx.coroutines.flow.Flow

class RutasRepository(private val dao: RutasDao) {


    // WAYPOINTS
    suspend fun eliminarWaypoint(id: Long) = dao.eliminarWaypoint(id)

    suspend fun actualizarWaypoint(id: Long, desc: String, foto: String?) = dao.actualizarWaypoint(id, desc, foto)

    // ACTIVIDADES
    suspend fun insertarActividad(actividad: Actividad) {
        dao.insertarActividad(actividad)
    }

    fun obtenerActividades(): Flow<List<Actividad>> {
        return dao.obtenerActividades()
    }

    // --- Insertar datos (Creación) ---
    suspend fun insertarRuta(ruta: Ruta): Long {
        return dao.insertarRuta(ruta)
    }

    suspend fun insertarPuntoRuta(punto: PuntoRuta) {
        dao.insertarPuntoRuta(punto)
    }

    suspend fun insertarWaypoint(waypoint: Waypoint) {
        dao.insertarWaypoint(waypoint)
    }

    // --- Actualizar y Modificar ---

    // 1. Guardar métricas finales al detener (Distancia, Duración y Velocidad Media)
    suspend fun actualizarRuta(id: Long, distancia: Double, duracion: Long, velocidad: Double) {
        dao.actualizarMetricasRuta(id, distancia, duracion, velocidad)
    }

    // 2. Renombrar una ruta existente
    suspend fun actualizarNombreRuta(id: Long, nuevoNombre: String) {
        dao.actualizarNombreRuta(id, nuevoNombre)
    }

    // 3. Eliminar (Descartar) una ruta
    suspend fun eliminarRuta(id: Long) {
        dao.eliminarRuta(id)
    }

    // --- Consultas (Lectura) ---

    fun obtenerTodasLasRutas(): Flow<List<Ruta>> {
        return dao.obtenerTodasLasRutas()
    }

    fun obtenerPuntosDeRuta(rutaId: Long): Flow<List<PuntoRuta>> {
        return dao.obtenerPuntosDeRuta(rutaId)
    }

    fun obtenerWaypointsDeRuta(rutaId: Long): Flow<List<Waypoint>> {
        return dao.obtenerWaypointsDeRuta(rutaId)
    }
}