package com.example.rutapersonal.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "actividad")
data class Actividad(
    @PrimaryKey val nombre: String, // Ej: "Correr", "Ciclismo"
    val icono: String // Guardaremos el nombre del icono de Material Icons
)


@Entity(
    tableName = "ruta",
    foreignKeys = [ForeignKey(
        entity = Actividad::class,
        parentColumns = ["nombre"],
        childColumns = ["actividad"],
        onDelete = ForeignKey.SET_DEFAULT
    )]
)
data class Ruta(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val actividad: String = "Andar",
    val distancia: Double = 0.0,
    val duracion: Long = 0L,
    val velocidadMedia: Double = 0.0
)

@Entity(
    tableName = "punto_ruta",
    foreignKeys = [ForeignKey(entity = Ruta::class, parentColumns = ["id"], childColumns = ["rutaId"], onDelete = ForeignKey.CASCADE)]
)
data class PuntoRuta(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rutaId: Long,
    val lat: Double,
    val lng: Double,
    val altitud: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "waypoint",
    foreignKeys = [ForeignKey(entity = Ruta::class, parentColumns = ["id"], childColumns = ["rutaId"], onDelete = ForeignKey.CASCADE)]
)
data class Waypoint(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rutaId: Long,
    val lat: Double,
    val lng: Double,
    val descripcion: String,
    val fotoPath: String? = null
)