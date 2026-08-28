package net.caaguazu.turismo.datos

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelos del contrato czu-app/v1, calcados de la implementacion del plugin.
 *
 * Un solo modelo por entidad: no hay cadena DTO -> dominio -> interfaz, porque
 * seria copiar campos de un objeto a otro sin ganar nada.
 *
 * Casi todo es anulable a proposito. El servidor devuelve null en cuanto un dato
 * falta —una ficha sin coordenadas, una categoria sin marcador, un evento sin
 * foto— y una pantalla que asuma lo contrario se cae con contenido real.
 */

@Immutable
@Serializable
data class Pagina<T>(
    val items: List<T> = emptyList(),
    val total: Int = 0,
    val pagina: Int = 1,
    @SerialName("por_pagina") val porPagina: Int = 20,
) {
    /** Sin `total` habria que pedir una pagina de mas para descubrir el final. */
    fun hayMas(): Boolean = pagina * porPagina < total
}

@Immutable
@Serializable
data class Imagen(
    val url: String,
    val w: Int = 0,
    val h: Int = 0,
    val credito: String = "",
    val alt: String = "",
) {
    /** Relacion de aspecto real, para reservar el hueco antes de que baje la foto. */
    fun proporcion(): Float = if (w > 0 && h > 0) w.toFloat() / h else 1f
}

@Immutable
@Serializable
data class Termino(
    val id: Int,
    val slug: String = "",
    val nombre: String = "",
    val color: String? = null,
)

@Immutable
@Serializable
data class Categoria(
    val id: Int,
    val slug: String = "",
    val nombre: String = "",
    val padre: Int? = null,
    val icono: String = "",
    val color: String = "",
    /** PNG pre-renderizado del pin. Es para el mapa: no sirve de fondo. */
    val marker: String? = null,
    /**
     * Foto de fondo del tile de categoria.
     *
     * Todavia no existe en el contrato — esta pedida al panel. Mientras no
     * llegue viene null y la pantalla cae a la foto del primer atractivo de la
     * categoria, que es contenido real y no una imagen de archivo.
     */
    val portada: Imagen? = null,
    val total: Int = 0,
)

@Immutable
@Serializable
data class Coordenadas(val lat: Double, val lng: Double)

/** Elemento de lista: solo lo que pinta una tarjeta. */
@Immutable
@Serializable
data class ItemInventario(
    val id: Int,
    val tipo: String = "destino",
    val titulo: String = "",
    val categoria: Termino? = null,
    val coordenadas: Coordenadas? = null,
    val portada: Imagen? = null,
    @SerialName("rango_precio") val rangoPrecio: Int? = null,
    @SerialName("horario_resumen") val horarioResumen: String = "",
    val actualizado: String? = null,
)

@Immutable
@Serializable
data class Practicos(
    val horario: String = "",
    val costo: String = "",
    @SerialName("rango_precio") val rangoPrecio: Int? = null,
    val duracion: String = "",
    val servicios: String = "",
    val temporada: String = "",
    val contacto: String = "",
)

@Immutable
@Serializable
data class Acceso(
    @SerialName("como_llegar") val comoLlegar: String = "",
    val referencia: String = "",
    @SerialName("estado_camino") val estadoCamino: String = "",
)

@Immutable
@Serializable
data class Autor(val id: Int, val nombre: String = "")

@Immutable
@Serializable
data class ResumenArticulo(
    val id: Int,
    val titulo: String = "",
    val bajada: String = "",
    val portada: Imagen? = null,
    val autor: Autor? = null,
    val publicado: String? = null,
)

@Immutable
@Serializable
data class Ficha(
    val id: Int,
    val tipo: String = "destino",
    val titulo: String = "",
    val categoria: Termino? = null,
    val etiquetas: List<Termino> = emptyList(),
    val coordenadas: Coordenadas? = null,
    val portada: Imagen? = null,
    val galeria: List<Imagen> = emptyList(),
    val video: String? = null,
    val practicos: Practicos = Practicos(),
    val acceso: Acceso = Acceso(),
    val descripcion: String = "",
    @SerialName("articulos_relacionados") val articulosRelacionados: List<ResumenArticulo> = emptyList(),
    val fuentes: String = "",
    val autor: Autor? = null,
    val actualizado: String? = null,
)

/** Payload minimo a proposito: el mapa carga todos los pines de una sola vez. */
@Immutable
@Serializable
data class Marcador(
    val id: Int,
    val tipo: String,
    val lat: Double,
    val lng: Double,
    val categoria: Int? = null,
)

@Immutable
@Serializable
data class Lugar(
    @SerialName("ref_tipo") val refTipo: String? = null,
    @SerialName("ref_id") val refId: Int? = null,
    val nombre: String? = null,
    val lat: Double = 0.0,
    val lng: Double = 0.0,
)

@Immutable
@Serializable
data class Evento(
    val id: Int,
    val tipo: String = "evento",
    val titulo: String = "",
    val inicio: String? = null,
    val fin: String? = null,
    val lugar: Lugar? = null,
    val costo: String = "",
    val categoria: Termino? = null,
    val portada: Imagen? = null,
    val resumen: String = "",
    @SerialName("articulo_html") val articuloHtml: String = "",
    val autor: Autor? = null,
    val actualizado: String? = null,
)

@Immutable
@Serializable
data class Articulo(
    val id: Int,
    val titulo: String = "",
    val bajada: String = "",
    val portada: Imagen? = null,
    val autor: Autor? = null,
    val publicado: String? = null,
    @SerialName("cuerpo_html") val cuerpoHtml: String = "",
    @SerialName("pie_portada") val piePortada: String = "",
    val categoria: Termino? = null,
    val relacionados: List<ResumenArticulo> = emptyList(),
    val actualizado: String? = null,
)

/**
 * Parada de un recorrido, con el sitio ya resuelto para no pedir una ficha por
 * parada.
 *
 * `disponible` en false significa que el lugar se despublico despues de que
 * alguien lo guardo. El servidor la informa en vez de omitirla, y la app tiene
 * que mostrarla como perdida: si desapareciera sin avisar, el recorrido guardado
 * cambiaria solo y nadie entenderia por que.
 */
@Immutable
@Serializable
data class Parada(
    val orden: Int,
    @SerialName("ref_tipo") val refTipo: String,
    @SerialName("ref_id") val refId: Int,
    val disponible: Boolean = true,
    val titulo: String = "",
    val portada: Imagen? = null,
    val categoria: Termino? = null,
    val coordenadas: Coordenadas? = null,
    val costo: String = "",
    val horario: String? = null,
    val inicio: String? = null,
    val fin: String? = null,
    val nota: String = "",
)

@Immutable
@Serializable
data class Historia(
    val introduccion: String = "",
    val correlacion: String = "",
    val personas: List<String> = emptyList(),
    val curiosidades: List<String> = emptyList(),
    @SerialName("articulos_ref") val articulosRef: List<Int> = emptyList(),
)

@Immutable
@Serializable
data class Recorrido(
    val id: Int,
    val tipo: String = "prehecho",
    val titulo: String = "",
    val resumen: String = "",
    val portada: Imagen? = null,
    @SerialName("duracion_estimada") val duracionEstimada: String = "",
    @SerialName("cantidad_paradas") val cantidadParadas: Int = 0,
    val paradas: List<Parada> = emptyList(),
    @SerialName("costo_total") val costoTotal: String? = null,
    val historia: Historia? = null,
    @SerialName("articulo_html") val articuloHtml: String = "",
)

/** Entrada del manifiesto de medios: imagen o animacion. */
@Immutable
@Serializable
data class Medio(
    val tipo: String = "imagen",
    val url: String,
    val alt: String = "",
    val formato: String? = null,
    val w: Int = 0,
    val h: Int = 0,
)

@Immutable
@Serializable
data class Delta(
    val desde: String? = null,
    val hasta: String? = null,
    val cambiados: Map<String, List<Int>> = emptyMap(),
    val eliminados: Map<String, List<Int>> = emptyMap(),
    /** Si viene en true, el delta no alcanza y hay que recargar todo de cero. */
    val completo: Boolean = false,
)

/** Cuerpo uniforme de todo 4xx y 5xx. */
@Immutable
@Serializable
data class ErrorApi(val error: DetalleError = DetalleError())

@Immutable
@Serializable
data class DetalleError(
    val codigo: String = "",
    val mensaje: String = "",
)
