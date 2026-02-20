package com.example.rutapersonal.data

import androidx.room.*
import com.example.rutapersonal.model.Actividad // Importar
import com.example.rutapersonal.model.PuntoRuta
import com.example.rutapersonal.model.Ruta
import com.example.rutapersonal.model.Waypoint
import kotlinx.coroutines.flow.Flow

@Dao
interface RutasDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarActividad(actividad: Actividad)

    @Query("SELECT * FROM actividad")
    fun obtenerActividades(): Flow<List<Actividad>>

    @Query("DELETE FROM waypoint WHERE id = :waypointId")
    suspend fun eliminarWaypoint(waypointId: Long)

    @Query("UPDATE waypoint SET descripcion = :nuevaDesc, fotoPath = :nuevaFoto WHERE id = :waypointId")
    suspend fun actualizarWaypoint(waypointId: Long, nuevaDesc: String, nuevaFoto: String?)

    @Insert
    suspend fun insertarRuta(ruta: Ruta): Long

    @Insert
    suspend fun insertarPuntoRuta(punto: PuntoRuta)

    @Insert
    suspend fun insertarWaypoint(waypoint: Waypoint)

    @Query("UPDATE ruta SET distancia = :distancia, duracion = :duracion, velocidadMedia = :velocidad WHERE id = :rutaId")
    suspend fun actualizarMetricasRuta(rutaId: Long, distancia: Double, duracion: Long, velocidad: Double)

    @Query("UPDATE ruta SET nombre = :nuevoNombre WHERE id = :rutaId")
    suspend fun actualizarNombreRuta(rutaId: Long, nuevoNombre: String)

    @Query("DELETE FROM ruta WHERE id = :rutaId")
    suspend fun eliminarRuta(rutaId: Long)

    @Query("SELECT * FROM ruta ORDER BY id DESC")
    fun obtenerTodasLasRutas(): Flow<List<Ruta>>

    @Query("SELECT * FROM punto_ruta WHERE rutaId = :rutaId ORDER BY timestamp ASC")
    fun obtenerPuntosDeRuta(rutaId: Long): Flow<List<PuntoRuta>>

    @Query("SELECT * FROM waypoint WHERE rutaId = :rutaId")
    fun obtenerWaypointsDeRuta(rutaId: Long): Flow<List<Waypoint>>
}