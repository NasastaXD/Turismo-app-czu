package net.caaguazu.turismo.core

/**
 * Salidas hacia la app de mapas del telefono.
 *
 * Se delega en vez de incrustar navegacion propia porque quien toca "como llego"
 * quiere llegar, y para eso su app de siempre tiene voz, trafico y transporte
 * publico. Competir con eso no aporta nada al turista.
 *
 * Todo lo de aca es compartido: armar el enlace es armar una cadena. Lo unico
 * que cambia entre plataformas es abrirlo, y eso lo hace `Sistema`.
 */
object MapasExternos {

    private const val ETIQUETA = "Mapas"

    /**
     * Google Maps corta en nueve paradas intermedias, y en solo tres si el
     * enlace termina abriendose en un navegador. Un recorrido mas largo se
     * manda por tramos.
     */
    const val MAX_PARADAS_INTERMEDIAS = 9

    /**
     * Un punto: enlace universal, lo abre cualquier app de mapas instalada.
     *
     * Primero `geo:`, que es el esquema que entienden las apps de mapas de las
     * dos plataformas, y si nadie lo atiende, el enlace web. El respaldo importa
     * mas en iOS, donde un telefono sin Google Maps ni Waze igual tiene Safari.
     */
    fun abrirPunto(lat: Double, lng: Double, nombre: String): Boolean {
        val etiqueta = codificarParaUrl(
            nombre.ifBlank { "$lat,$lng" },
            espacioComoMas = false,
        )
        return Sistema.abrirUrl("geo:$lat,$lng?q=$lat,$lng($etiqueta)") ||
            Sistema.abrirUrl("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
    }

    /**
     * El enlace que ya viene calculado del panel (`google_maps`). Preferirlo
     * sobre armar uno propio importa: puede ser el que pego el promotor a
     * mano, mas preciso que un pin armado solo con lat/lng.
     */
    fun abrirEnlace(url: String): Boolean = Sistema.abrirUrl(url)

    /**
     * Un recorrido completo. Devuelve false si no cabe: quien llama decide si
     * lo parte en tramos.
     */
    fun abrirRecorrido(
        puntos: List<Pair<Double, Double>>,
        aPie: Boolean = true,
    ): Boolean {
        if (puntos.size < 2) return false
        if (puntos.size - 2 > MAX_PARADAS_INTERMEDIAS) {
            Bitacora.aviso(ETIQUETA, "recorrido de ${puntos.size} paradas: no entra en un solo enlace")
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
        return Sistema.abrirUrl(url)
    }
}
