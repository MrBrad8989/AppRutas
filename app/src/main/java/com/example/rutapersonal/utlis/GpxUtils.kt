package com.example.rutapersonal.utils

import android.content.Context
import android.net.Uri
import com.example.rutapersonal.model.PuntoRuta
import com.example.rutapersonal.model.Ruta
import com.example.rutapersonal.model.Waypoint
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object GpxUtils {

    // --- EXPORTAR ---
    fun escribirGPX(context: Context, ruta: Ruta, puntos: List<PuntoRuta>, waypoints: List<Waypoint>): Uri? {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")

        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        sb.append("<gpx version=\"1.1\" creator=\"GestorRutasApp\" xmlns=\"http://www.topografix.com/GPX/1/1\">\n")

        // Metadata
        sb.append("  <metadata>\n")
        sb.append("    <name>${ruta.nombre}</name>\n")
        sb.append("    <desc>Actividad: ${ruta.actividad}</desc>\n")
        sb.append("  </metadata>\n")

        // Track (La línea)
        sb.append("  <trk>\n")
        sb.append("    <name>${ruta.nombre}</name>\n")
        sb.append("    <trkseg>\n")
        for (p in puntos) {
            sb.append("      <trkpt lat=\"${p.lat}\" lon=\"${p.lng}\">\n")
            sb.append("        <ele>${p.altitud}</ele>\n") // <--- ALTITUD
            sb.append("        <time>${sdf.format(Date(p.timestamp))}</time>\n")
            sb.append("      </trkpt>\n")
        }
        sb.append("    </trkseg>\n")
        sb.append("  </trk>\n")

        // Waypoints
        for (w in waypoints) {
            sb.append("  <wpt lat=\"${w.lat}\" lon=\"${w.lng}\">\n")
            sb.append("    <name>${w.descripcion}</name>\n")
            sb.append("  </wpt>\n")
        }

        sb.append("</gpx>")

        // Guardar fichero en cache para compartirlo
        try {
            val file = File(context.cacheDir, "${ruta.nombre.replace(" ", "_")}.gpx")
            FileOutputStream(file).use { it.write(sb.toString().toByteArray()) }
            return androidx.core.content.FileProvider.getUriForFile(
                context, "${context.packageName}.fileprovider", file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // --- IMPORTAR ---
    // Devuelve un Triple con (NombreRuta, ListaPuntos, ListaWaypoints)
    fun leerGPX(context: Context, uri: Uri): Triple<String, List<PuntoRuta>, List<Waypoint>>? {
        val puntos = mutableListOf<PuntoRuta>()
        val waypoints = mutableListOf<Waypoint>()
        var nombreRuta = "Ruta Importada ${System.currentTimeMillis()}"

        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(inputStream, null)

            var eventType = parser.eventType
            var currentLat = 0.0
            var currentLon = 0.0
            var currentEle = 0.0
            var currentWptName = ""

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tagName = parser.name
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (tagName == "trkpt") {
                            currentLat = parser.getAttributeValue(null, "lat").toDouble()
                            currentLon = parser.getAttributeValue(null, "lon").toDouble()
                        } else if (tagName == "ele") { // Altitud
                            currentEle = parser.nextText().toDoubleOrNull() ?: 0.0
                        } else if (tagName == "wpt") {
                            currentLat = parser.getAttributeValue(null, "lat").toDouble()
                            currentLon = parser.getAttributeValue(null, "lon").toDouble()
                        } else if (tagName == "name") {
                            currentWptName = parser.nextText()
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (tagName == "trkpt") {
                            // Dummy IDs, se generarán al insertar en BD
                            puntos.add(PuntoRuta(rutaId = 0, lat = currentLat, lng = currentLon, altitud = currentEle))
                            currentEle = 0.0
                        } else if (tagName == "wpt") {
                            waypoints.add(Waypoint(rutaId = 0, lat = currentLat, lng = currentLon, descripcion = currentWptName))
                            currentWptName = ""
                        }
                    }
                }
                eventType = parser.next()
            }
            inputStream.close()
            return Triple(nombreRuta, puntos, waypoints)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}