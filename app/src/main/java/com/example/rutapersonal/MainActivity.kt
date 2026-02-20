package com.example.rutapersonal

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rutapersonal.model.Ruta
import com.example.rutapersonal.model.Waypoint
import com.example.rutapersonal.ui.MapaRuta
import com.example.rutapersonal.ui.UbicacionViewModel
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppGestorRutas()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppGestorRutas() {
    val vm: UbicacionViewModel = viewModel()
    var tabIndex by remember { mutableStateOf(0) } // 0: Grabar, 1: Historial
    val titles = listOf("Grabar Ruta", "Mis Rutas")

    // Permisos
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        vm.iniciarSeguimientoGPS()
    }
    LaunchedEffect(Unit) {
        launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA))
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                titles.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = { Icon(if (index == 0) Icons.Default.Map else Icons.Default.List, contentDescription = null) },
                        label = { Text(title) },
                        selected = tabIndex == index,
                        onClick = { tabIndex = index }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (tabIndex == 0) {
                PantallaGrabacion(vm)
            } else {
                PantallaHistorial(vm)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaGrabacion(vm: UbicacionViewModel) {
    val ubicacion by vm.ubicacionActual.collectAsState()
    val isRecording by vm.isRecording.collectAsState()
    val isPaused by vm.isPaused.collectAsState()
    val distancia by vm.distanciaAcumulada.collectAsState()
    val tiempo by vm.tiempoTranscurrido.collectAsState()
    val velocidad by vm.velocidadMedia.collectAsState()
    val puntos by vm.puntosRutaActual.collectAsState()
    val waypoints by vm.waypointsActuales.collectAsState()

    // Estados para Actividades
    val actividades by vm.actividades.collectAsState()
    val actividadSeleccionada by vm.actividadSeleccionada.collectAsState()

    var showStopDialog by remember { mutableStateOf(false) }
    var showWaypointDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // MAPA
        MapaRuta(ubicacion, puntos, waypoints)

        // --- PANEL SUPERIOR (Métricas + Selector) ---
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. SELECTOR DE ACTIVIDAD (Solo visible si NO estamos grabando)
            if (!isRecording && actividades.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        actividades.forEach { act ->
                            val isSelected = actividadSeleccionada?.nombre == act.nombre
                            FilterChip(
                                selected = isSelected,
                                onClick = { vm.seleccionarActividad(act) },
                                label = { Text(act.nombre) },
                                leadingIcon = {
                                    Icon(
                                        obtenerIconoPorNombre(act.icono),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // 2. PANEL DE MÉTRICAS (Solo visible al grabar)
            if (isRecording) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            MetricItem("Distancia", "${String.format("%.0f", distancia)} m")
                            MetricItem("Tiempo", formatearTiempo(tiempo))
                            MetricItem("Velocidad", "${String.format("%.1f", velocidad)} km/h")
                        }
                        if (isPaused) {
                            Text(
                                "PAUSA",
                                color = Color.Red,
                                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }

        // BOTONES FLOTANTES
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            if (isRecording) {
                // Botón Waypoint
                SmallFloatingActionButton(
                    onClick = { showWaypointDialog = true },
                    containerColor = MaterialTheme.colorScheme.tertiary
                ) {
                    Icon(Icons.Default.AddLocation, "Waypoint")
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Botón Pausa/Reanudar
                FloatingActionButton(
                    onClick = { vm.togglePausa() },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause, "Pausa")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Botón Principal (Play / Stop)
            FloatingActionButton(
                onClick = {
                    if (isRecording) showStopDialog = true else vm.iniciarGrabacion()
                },
                containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            ) {
                Icon(if (isRecording) Icons.Default.Stop else Icons.Default.PlayArrow, "Acción")
            }
        }
    }

    // DIÁLOGOS
    if (showStopDialog) {
        AlertDialog(
            onDismissRequest = { showStopDialog = false },
            title = { Text("Finalizar ruta") },
            text = { Text("¿Deseas guardar la ruta registrada o descartarla?") },
            confirmButton = {
                Button(onClick = {
                    vm.detenerGrabacion(guardar = true)
                    showStopDialog = false
                }) { Text("Guardar") }
            },
            dismissButton = {
                Button(
                    onClick = {
                        vm.detenerGrabacion(guardar = false)
                        showStopDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Descartar") }
            }
        )
    }

    if (showWaypointDialog) {
        DialogoWaypoint(
            onDismiss = { showWaypointDialog = false },
            onConfirm = { desc, uri -> vm.anadirWaypoint(desc, uri) },
            vm = vm
        )
    }
}

@Composable
fun DialogoWaypoint(onDismiss: () -> Unit, onConfirm: (String, String?) -> Unit, vm: UbicacionViewModel) {
    var descripcion by remember { mutableStateOf("") }
    var fotoUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (!success) fotoUri = null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Waypoint") },
        text = {
            Column {
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    val uri = vm.crearUriImagen()
                    fotoUri = uri
                    cameraLauncher.launch(uri)
                }) {
                    Text(if (fotoUri == null) "Tomar Foto" else "Foto tomada")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(descripcion, fotoUri?.toString())
                onDismiss()
            }) { Text("Guardar") }
        }
    )
}

@Composable
fun PantallaHistorial(vm: UbicacionViewModel) {
    val rutas by vm.todasLasRutas.collectAsState()
    var rutaSeleccionada by remember { mutableStateOf<Ruta?>(null) }

    // Estado para renombrar (Lista)
    var rutaAEditar by remember { mutableStateOf<Ruta?>(null) }

    val context = LocalContext.current

    // Launcher para importar GPX
    val gpxImportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) vm.importarRutaGPX(context, uri)
    }

    if (rutaSeleccionada == null) {
        // --- LISTA DE RUTAS ---
        Scaffold(
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { gpxImportLauncher.launch("*/*") }, // Abrir explorador de archivos
                    icon = { Icon(Icons.Default.Upload, null) },
                    text = { Text("Importar GPX") }
                )
            }
        ) { padding ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                item {
                    Text("Mis Rutas Guardadas", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(rutas) { ruta ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                vm.cargarRutaParaVisualizar(ruta.id)
                                rutaSeleccionada = ruta
                            },
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icono Actividad
                            Icon(
                                imageVector = obtenerIconoPorNombre(obtenerIconoString(ruta.actividad)),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp).padding(end = 16.dp)
                            )

                            // Info Central
                            Column(modifier = Modifier.weight(1f)) {
                                Text(ruta.nombre, style = MaterialTheme.typography.titleMedium)
                                Text(ruta.actividad, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row {
                                    Text("Dist: ${String.format("%.0f", ruta.distancia)} m", style = MaterialTheme.typography.bodySmall)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Vel: ${String.format("%.1f", ruta.velocidadMedia)} km/h", style = MaterialTheme.typography.bodySmall)
                                }
                            }

                            // Botones Acción
                            Row {
                                IconButton(onClick = { rutaAEditar = ruta }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Renombrar")
                                }
                                IconButton(onClick = { compartirRuta(context, ruta) }) {
                                    Icon(Icons.Default.Share, contentDescription = "Compartir")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Diálogo Renombrar
        if (rutaAEditar != null) {
            DialogoRenombrar(
                ruta = rutaAEditar!!,
                onDismiss = { rutaAEditar = null },
                onConfirm = { nuevoNombre ->
                    vm.actualizarNombreRuta(rutaAEditar!!.id, nuevoNombre)
                    rutaAEditar = null
                }
            )
        }

    } else {
        // --- DETALLE RUTA COMPLETO (Con Mapa, Waypoints y Exportación) ---
        DetalleRutaScreen(vm, rutaSeleccionada!!) {
            rutaSeleccionada = null
        }
    }
}

// Nueva pantalla de detalle refactorizada para soportar la lista de waypoints y botones extra
@Composable
fun DetalleRutaScreen(vm: UbicacionViewModel, ruta: Ruta, onBack: () -> Unit) {
    val puntos by vm.rutaSeleccionadaPuntos.collectAsState()
    val waypoints by vm.rutaSeleccionadaWaypoints.collectAsState()
    val context = LocalContext.current

    // Estado para editar waypoint
    var waypointAEditar by remember { mutableStateOf<Waypoint?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            // 1. MAPA (Ocupa la parte superior, peso 1)
            Box(modifier = Modifier.weight(1f)) {
                MapaRuta(null, puntos, waypoints)
            }

            // 2. LISTA DE WAYPOINTS (Editable)
            if (waypoints.isNotEmpty()) {
                Surface(tonalElevation = 2.dp) {
                    Column {
                        Text("Waypoints (Editar)", modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.titleMedium)
                        LazyColumn(modifier = Modifier.height(150.dp).fillMaxWidth()) {
                            items(waypoints) { wp ->
                                ListItem(
                                    headlineContent = { Text(wp.descripcion) },
                                    leadingContent = { Icon(Icons.Default.Place, null) },
                                    trailingContent = {
                                        Row {
                                            IconButton(onClick = { waypointAEditar = wp }) {
                                                Icon(Icons.Default.Edit, "Editar")
                                            }
                                            IconButton(onClick = { vm.borrarWaypoint(wp.id) }) {
                                                Icon(Icons.Default.Delete, "Borrar", tint = Color.Red)
                                            }
                                        }
                                    }
                                )
                                Divider()
                            }
                        }
                    }
                }
            }

            // 3. PANEL DE INFO Y ACCIONES (Parte inferior)
            Card(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = obtenerIconoPorNombre(obtenerIconoString(ruta.actividad)),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(ruta.nombre, style = MaterialTheme.typography.titleLarge)
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MetricItem("Distancia", "${String.format("%.0f", ruta.distancia)} m")
                        MetricItem("Tiempo", formatearTiempo(ruta.duracion))
                        MetricItem("Vel. Media", "${String.format("%.1f", ruta.velocidadMedia)} km/h")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // BOTONES DE ACCIÓN (Compartir Texto y Exportar GPX)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        OutlinedButton(onClick = { compartirRuta(context, ruta) }) {
                            Icon(Icons.Default.Share, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Compartir Info")
                        }
                        Button(onClick = { vm.exportarRutaGPX(context, ruta) }) {
                            Icon(Icons.Default.Upload, null) // Upload como exportar
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Exportar GPX")
                        }
                    }
                }
            }
        }

        // Botón Volver Flotante
        SmallFloatingActionButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Icon(Icons.Default.ArrowBack, "Volver")
        }
    }

    // Diálogo Editar Waypoint
    if (waypointAEditar != null) {
        DialogoEditarWaypoint(
            wp = waypointAEditar!!,
            onDismiss = { waypointAEditar = null },
            onConfirm = { desc ->
                vm.editarWaypoint(waypointAEditar!!.id, desc, waypointAEditar!!.fotoPath)
                waypointAEditar = null
            }
        )
    }
}

@Composable
fun DialogoRenombrar(ruta: Ruta, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var texto by remember { mutableStateOf(ruta.nombre) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Renombrar Ruta") },
        text = {
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                label = { Text("Nombre") }
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(texto) }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun DialogoEditarWaypoint(wp: Waypoint, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var desc by remember { mutableStateOf(wp.descripcion) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Waypoint") },
        text = {
            OutlinedTextField(
                value = desc,
                onValueChange = { desc = it },
                label = { Text("Descripción") }
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(desc) }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

fun compartirRuta(context: Context, ruta: Ruta) {
    val textoCompartir = """
        ¡Mira mi ruta de ${ruta.actividad}: ${ruta.nombre}!
        Distancia: ${String.format("%.0f", ruta.distancia)} m
        Duración: ${formatearTiempo(ruta.duracion)}
        Velocidad Media: ${String.format("%.1f", ruta.velocidadMedia)} km/h
    """.trimIndent()

    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, textoCompartir)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Compartir ruta vía...")
    context.startActivity(shareIntent)
}

@Composable
fun MetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium)
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

fun formatearTiempo(ms: Long): String {
    val seg = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
    val min = TimeUnit.MILLISECONDS.toMinutes(ms)
    return String.format("%02d:%02d", min, seg)
}

// --- FUNCIONES AUXILIARES DE ICONOS ---
fun obtenerIconoPorNombre(nombre: String): ImageVector {
    return when (nombre) {
        "directions_run" -> Icons.Default.DirectionsRun
        "directions_bike" -> Icons.Default.DirectionsBike
        "directions_walk" -> Icons.Default.DirectionsWalk
        else -> Icons.Default.Place
    }
}

fun obtenerIconoString(nombreActividad: String): String {
    return when (nombreActividad) {
        "Correr" -> "directions_run"
        "Bici" -> "directions_bike"
        "Andar" -> "directions_walk"
        else -> "place"
    }
}