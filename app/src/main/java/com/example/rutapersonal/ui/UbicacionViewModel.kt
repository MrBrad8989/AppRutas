package com.example.rutapersonal.ui

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rutapersonal.data.AppDatabase
import com.example.rutapersonal.data.RutasRepository
import com.example.rutapersonal.model.Actividad
import com.example.rutapersonal.model.PuntoRuta
import com.example.rutapersonal.model.Ruta
import com.example.rutapersonal.model.Waypoint
import com.example.rutapersonal.utils.GpxUtils // Asegúrate de haber creado este archivo
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.*

class UbicacionViewModel(application: Application) : AndroidViewModel(application) {

    // Inicializamos Repository
    private val repository = RutasRepository(AppDatabase.getDatabase(application).rutasDao())
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    // --- ESTADOS DE UI ---
    private val _ubicacionActual = MutableStateFlow<Location?>(null)
    val ubicacionActual: StateFlow<Location?> = _ubicacionActual

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused

    private val _tiempoTranscurrido = MutableStateFlow(0L)
    val tiempoTranscurrido: StateFlow<Long> = _tiempoTranscurrido

    private val _distanciaAcumulada = MutableStateFlow(0.0)
    val distanciaAcumulada: StateFlow<Double> = _distanciaAcumulada

    private val _velocidadMedia = MutableStateFlow(0.0)
    val velocidadMedia: StateFlow<Double> = _velocidadMedia

    // --- ESTADOS DE ACTIVIDAD ---
    private val _actividades = MutableStateFlow<List<Actividad>>(emptyList())
    val actividades: StateFlow<List<Actividad>> = _actividades

    private val _actividadSeleccionada = MutableStateFlow<Actividad?>(null)
    val actividadSeleccionada: StateFlow<Actividad?> = _actividadSeleccionada

    // Listas para pintar en tiempo real
    private val _puntosRutaActual = MutableStateFlow<List<PuntoRuta>>(emptyList())
    val puntosRutaActual: StateFlow<List<PuntoRuta>> = _puntosRutaActual

    private val _waypointsActuales = MutableStateFlow<List<Waypoint>>(emptyList())
    val waypointsActuales: StateFlow<List<Waypoint>> = _waypointsActuales

    // Historial
    private val _todasLasRutas = MutableStateFlow<List<Ruta>>(emptyList())
    val todasLasRutas: StateFlow<List<Ruta>> = _todasLasRutas

    // Visualización de ruta guardada
    private val _rutaSeleccionadaPuntos = MutableStateFlow<List<PuntoRuta>>(emptyList())
    val rutaSeleccionadaPuntos: StateFlow<List<PuntoRuta>> = _rutaSeleccionadaPuntos
    private val _rutaSeleccionadaWaypoints = MutableStateFlow<List<Waypoint>>(emptyList())
    val rutaSeleccionadaWaypoints: StateFlow<List<Waypoint>> = _rutaSeleccionadaWaypoints

    // Variables internas de control
    private var rutaIdActual: Long? = null
    private var recordingJob: Job? = null
    private var timerJob: Job? = null
    private var ultimoPunto: PuntoRuta? = null

    init {
        // Cargar historial al iniciar
        viewModelScope.launch {
            repository.obtenerTodasLasRutas().collect { _todasLasRutas.value = it }
        }

        // Cargar Actividades
        viewModelScope.launch {
            repository.obtenerActividades().collect { lista ->
                if (lista.isEmpty()) {
                    crearActividadesPorDefecto()
                } else {
                    _actividades.value = lista
                    if (_actividadSeleccionada.value == null) {
                        _actividadSeleccionada.value = lista.first()
                    }
                }
            }
        }
    }

    private suspend fun crearActividadesPorDefecto() {
        val defaults = listOf(
            Actividad("Andar", "directions_walk"),
            Actividad("Correr", "directions_run"),
            Actividad("Bici", "directions_bike")
        )
        defaults.forEach { repository.insertarActividad(it) }
    }

    fun seleccionarActividad(actividad: Actividad) {
        _actividadSeleccionada.value = actividad
    }

    // --- CONTROL DE GRABACIÓN ---

    fun togglePausa() {
        if (_isRecording.value) {
            _isPaused.value = !_isPaused.value
        }
    }

    @SuppressLint("MissingPermission")
    fun iniciarGrabacion() {
        if (_isRecording.value) return

        val actNombre = _actividadSeleccionada.value?.nombre ?: "Andar"

        viewModelScope.launch {
            val nuevaRuta = Ruta(
                nombre = "Ruta ${System.currentTimeMillis()}",
                actividad = actNombre,
                duracion = 0
            )
            rutaIdActual = repository.insertarRuta(nuevaRuta)

            _isRecording.value = true
            _isPaused.value = false
            _distanciaAcumulada.value = 0.0
            _tiempoTranscurrido.value = 0L
            _velocidadMedia.value = 0.0
            _puntosRutaActual.value = emptyList()
            _waypointsActuales.value = emptyList()
            ultimoPunto = null

            timerJob = launch {
                val startTime = System.currentTimeMillis()
                var pausedTime = 0L
                var lastPauseStart = 0L

                while (_isRecording.value) {
                    if (_isPaused.value) {
                        if (lastPauseStart == 0L) lastPauseStart = System.currentTimeMillis()
                    } else {
                        if (lastPauseStart != 0L) {
                            pausedTime += System.currentTimeMillis() - lastPauseStart
                            lastPauseStart = 0L
                        }
                        val tiempoActual = System.currentTimeMillis() - startTime - pausedTime
                        _tiempoTranscurrido.value = tiempoActual

                        if (tiempoActual > 1000) {
                            val distKm = _distanciaAcumulada.value / 1000.0
                            val horas = tiempoActual / 3600000.0
                            if (horas > 0) _velocidadMedia.value = distKm / horas
                        }
                    }
                    delay(1000)
                }
            }

            recordingJob = launch {
                while (_isRecording.value) {
                    if (!_isPaused.value) {
                        obtenerUbicacionYGuardar()
                    }
                    delay(10_000)
                }
            }
        }
    }

    fun detenerGrabacion(guardar: Boolean) {
        _isRecording.value = false
        _isPaused.value = false
        recordingJob?.cancel()
        timerJob?.cancel()

        viewModelScope.launch {
            val rId = rutaIdActual ?: return@launch
            if (guardar) {
                repository.actualizarRuta(
                    rId,
                    _distanciaAcumulada.value,
                    _tiempoTranscurrido.value,
                    _velocidadMedia.value
                )
            } else {
                repository.eliminarRuta(rId)
            }
            rutaIdActual = null
            _puntosRutaActual.value = emptyList()
            _waypointsActuales.value = emptyList()
            _velocidadMedia.value = 0.0
        }
    }

    // --- LÓGICA GPS ---

    @SuppressLint("MissingPermission")
    fun iniciarSeguimientoGPS() {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { _ubicacionActual.value = it }
    }

    @SuppressLint("MissingPermission")
    private suspend fun obtenerUbicacionYGuardar() {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                location?.let { loc ->
                    _ubicacionActual.value = loc
                    rutaIdActual?.let { rId ->
                        guardarPuntoEnBd(rId, loc)
                    }
                }
            }
    }

    private fun guardarPuntoEnBd(rutaId: Long, location: Location) {
        viewModelScope.launch {
            // Guardamos altitud
            val nuevoPunto = PuntoRuta(
                rutaId = rutaId,
                lat = location.latitude,
                lng = location.longitude,
                altitud = location.altitude
            )
            repository.insertarPuntoRuta(nuevoPunto)

            val lista = _puntosRutaActual.value.toMutableList()
            lista.add(nuevoPunto)
            _puntosRutaActual.value = lista

            ultimoPunto?.let { anterior ->
                val dist = calcularDistanciaHaversine(anterior, nuevoPunto)
                _distanciaAcumulada.value += dist
            }
            ultimoPunto = nuevoPunto
        }
    }

    // --- WAYPOINTS ---

    fun anadirWaypoint(descripcion: String, fotoUri: String?) {
        val loc = _ubicacionActual.value ?: return
        val rId = rutaIdActual ?: return
        viewModelScope.launch {
            val wp = Waypoint(rutaId = rId, lat = loc.latitude, lng = loc.longitude, descripcion = descripcion, fotoPath = fotoUri)
            repository.insertarWaypoint(wp)
            val lista = _waypointsActuales.value.toMutableList()
            lista.add(wp)
            _waypointsActuales.value = lista
        }
    }

    // NUEVO: Borrar Waypoint
    fun borrarWaypoint(wpId: Long) {
        viewModelScope.launch { repository.eliminarWaypoint(wpId) }
    }

    // NUEVO: Editar Waypoint
    fun editarWaypoint(wpId: Long, desc: String, foto: String?) {
        viewModelScope.launch { repository.actualizarWaypoint(wpId, desc, foto) }
    }

    fun crearUriImagen(): Uri {
        val imageFile = File(getApplication<Application>().getExternalFilesDir("Pictures"), "foto_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(getApplication(), "${getApplication<Application>().packageName}.fileprovider", imageFile)
    }

    // --- VISUALIZACIÓN Y GESTIÓN ---

    fun cargarRutaParaVisualizar(rutaId: Long) {
        viewModelScope.launch {
            repository.obtenerPuntosDeRuta(rutaId).collect { _rutaSeleccionadaPuntos.value = it }
        }
        viewModelScope.launch {
            repository.obtenerWaypointsDeRuta(rutaId).collect { _rutaSeleccionadaWaypoints.value = it }
        }
    }

    fun actualizarNombreRuta(rutaId: Long, nuevoNombre: String) {
        viewModelScope.launch { repository.actualizarNombreRuta(rutaId, nuevoNombre) }
    }

    // --- GPX (IMPORTAR / EXPORTAR) ---

    fun exportarRutaGPX(context: Context, ruta: Ruta) {
        viewModelScope.launch {
            // Usamos los puntos cargados en memoria de la ruta visualizada
            val puntos = _rutaSeleccionadaPuntos.value
            val waypoints = _rutaSeleccionadaWaypoints.value

            if (puntos.isNotEmpty()) {
                val uri = GpxUtils.escribirGPX(context, ruta, puntos, waypoints)
                if (uri != null) {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, uri)
                        type = "application/gpx+xml"
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Exportar GPX"))
                }
            }
        }
    }

    fun importarRutaGPX(context: Context, uri: Uri) {
        viewModelScope.launch {
            val datos = GpxUtils.leerGPX(context, uri)
            if (datos != null) {
                val (nombre, puntos, waypoints) = datos

                // 1. Crear Ruta Importada
                val nuevaRuta = Ruta(nombre = nombre, actividad = "Importada")
                val rId = repository.insertarRuta(nuevaRuta)

                // 2. Insertar Puntos y Recalcular Distancia Total
                var distancia = 0.0
                var ultimo: PuntoRuta? = null

                puntos.forEach { p ->
                    val pConId = p.copy(rutaId = rId)
                    repository.insertarPuntoRuta(pConId)

                    if (ultimo != null) {
                        distancia += calcularDistanciaHaversine(ultimo!!, pConId)
                    }
                    ultimo = pConId
                }

                // 3. Insertar Waypoints
                waypoints.forEach { w ->
                    repository.insertarWaypoint(w.copy(rutaId = rId))
                }

                // 4. Actualizar totales en BD
                repository.actualizarRuta(rId, distancia, 0L, 0.0)
            }
        }
    }

    // --- UTILIDAD MATEMÁTICA ---
    private fun calcularDistanciaHaversine(p1: PuntoRuta, p2: PuntoRuta): Double {
        val R = 6371000.0
        val lat1 = Math.toRadians(p1.lat)
        val lat2 = Math.toRadians(p2.lat)
        val dLat = Math.toRadians(p2.lat - p1.lat)
        val dLng = Math.toRadians(p2.lng - p1.lng)
        val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }
}