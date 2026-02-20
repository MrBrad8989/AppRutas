package com.example.rutapersonal.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import com.example.rutapersonal.model.PuntoRuta
import com.example.rutapersonal.model.Waypoint
import android.location.Location
import android.graphics.Color
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@Composable
fun MapaRuta(
    ubicacionActual: Location?,
    puntosRuta: List<PuntoRuta>,
    waypoints: List<Waypoint>
) {
    AndroidView(
        factory = { ctx ->
            Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                controller.setZoom(18.0)
            }
        },
        update = { mapView ->
            mapView.overlays.clear()

            // 1. Dibujar Ruta (Polilínea Azul)
            if (puntosRuta.isNotEmpty()) {
                val polyline = Polyline()
                polyline.setPoints(puntosRuta.map { GeoPoint(it.lat, it.lng) })
                polyline.outlinePaint.color = Color.BLUE
                polyline.outlinePaint.strokeWidth = 10f
                mapView.overlays.add(polyline)
            }

            // 2. Dibujar Waypoints (Marcadores)
            waypoints.forEach { wp ->
                val marker = Marker(mapView)
                marker.position = GeoPoint(wp.lat, wp.lng)
                marker.title = wp.descripcion
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                mapView.overlays.add(marker)
            }

            // 3. Ubicación Actual y Centrado
            ubicacionActual?.let {
                val yo = GeoPoint(it.latitude, it.longitude)
                val marker = Marker(mapView)
                marker.position = yo
                marker.title = "Yo"
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                mapView.overlays.add(marker)

                // Centrar mapa si estamos grabando o iniciando
                mapView.controller.animateTo(yo)
            }

            mapView.invalidate()
        }
    )
}