package com.example.rutapersonal.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.rutapersonal.model.Actividad
import com.example.rutapersonal.model.PuntoRuta
import com.example.rutapersonal.model.Ruta
import com.example.rutapersonal.model.Waypoint

@Database(
    entities = [Ruta::class, PuntoRuta::class, Waypoint::class, Actividad::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun rutasDao(): RutasDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gestor_rutas_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}