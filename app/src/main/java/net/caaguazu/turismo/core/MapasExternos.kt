package net.caaguazu.turismo.core

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Salidas hacia la app de mapas del telefono.
 *
 * Se delega en vez de incrustar navegacion propia porque quien toca "como llego"
 * quiere llegar, y para eso su app de siempre tiene voz, trafico y transporte
 * publico. Competir con eso no aporta nada al turista.
 */
object MapasExternos {

    private const val ETIQUETA = "Mapas"

    /**
     * Google Maps corta en nueve paradas intermedias, y en solo tres si el
     * enlace termina abriendose en un navegador. Un recorrido mas largo se
     * manda por tramos.
     */
    const val MAX_PARADAS_INTERMEDIAS = 9

    /** Un punto: enlace universal, lo abre cualquier app de mapas instalada. */
    fun abrirPunto(contexto: Context, lat: Double, lng: Double, nombre: String): Boolean {
        val etiqueta = Uri.encode(nombre.ifBlank { "$lat,$lng" })
        return abrir(contexto, Uri.parse("geo:$lat,$lng?q=$lat,$lng($etiqueta)")) ||
            abrir(contexto, Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng"))
    }

    /**
     * El enlace que ya viene calculado del panel (`google_maps`). Preferirlo
     * sobre armar uno propio importa: puede ser el que pego el promotor a
     * mano, mas preciso que un pin armado solo con lat/lng.
     */
    fun abrirEnlace(contexto: Context, url: String): Boolean = abrir(contexto, Uri.parse(url))

    /**
     * Un recorrido completo. Devuelve false si no cabe: quien llama decide si
     * lo parte en tramos.
     */
    fun abrirRecorrido(
        contexto: Context,
        puntos: List<Pair<Double, Double>>,
        aPie: Boolean = true,
    ): Boolean {
        if (puntos.size < 2) return false
        if (puntos.size - 2 > MAX_PARADAS_INTERMEDIAS) {
            Registro.aviso(ETIQUETA, "recorrido de ${puntos.size} paradas: no entra en un solo enlace")
            return false
        }

        val origen = puntos.first()
        val destino = puntos.last()
        val medio = puntos.drop(1).dropLast(1)

        val url = buildString {
            append("https://www.google.com/maps/dir/?api=1")
            append("&origin=${origen.first},${origen.second}")
            append("&destination=${destino.first},${destino.second}")
            if (medio.isNotEmpty()) {
                append("&waypoints=")
                append(medio.joinToString("|") { "${it.first},${it.second}" })
            }
            append("&travelmode=").append(if (aPie) "walking" else "driving")
        }
        return abrir(contexto, Uri.parse(url))
    }

    private fun abrir(contexto: Context, uri: Uri): Boolean {
        val intencion = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            contexto.startActivity(intencion)
            true
        } catch (e: Throwable) {
            // Un telefono sin ninguna app de mapas es raro pero posible.
            Registro.aviso(ETIQUETA, "no hay app que abra $uri")
            false
        }
    }
}
