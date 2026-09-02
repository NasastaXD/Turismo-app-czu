package net.caaguazu.turismo.datos

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNames

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
     * El panel ya la manda, pero como `imagen` y no como `portada` — el pedido
     * de §6bis se resolvio con otro nombre de campo. Se acepta cualquiera de
     * los dos por si el nombre cambia de nuevo; si algun dia falta del todo,
     * la pantalla cae a la foto del primer atractivo de la categoria.
     */
    @JsonNames("imagen") val portada: Imagen? = null,
    val total: Int = 0,
)

/**
 * Una etiqueta del catalogo de `/etiquetas`.
 *
 * Solo trae las que ya etiquetan algo (`hide_empty`): a diferencia de
 * Categoria, que muestra todo, una etiqueta sin uso es ruido en un selector
 * de chips y no parte de la estructura.
 */
@Immutable
@Serializable
data class Etiqueta(
    val id: Int,
    val slug: String = "",
    val nombre: String = "",
    val total: Int = 0,
)

@Immutable
@Serializable
data class Zona(
    val id: Int,
    val slug: String = "",
    val nombre: String = "",
    val padre: Int? = null,
    val total: Int = 0,
)

@Immutable
@Serializable
data class Coordenadas(val lat: Double, val lng: Double)

/**
 * Fechas de un evento. Solo viene poblado cuando `tipoItem == "evento"`: una
 * ficha de sitio no tiene cuando pasa.
 */
@Immutable
@Serializable
data class Fechas(
    val inicio: String? = null,
    val fin: String? = null,
    @SerialName("en_curso") val enCurso: Boolean = false,
    val terminado: Boolean = false,
)

/** Elemento de lista: solo lo que pinta una tarjeta. */
@Immutable
@Serializable
data class ItemInventario(
    val id: Int,
    @SerialName("tipo_item") val tipoItem: String = "sitio",
    val titulo: String = "",
    val gancho: String = "",
    val categoria: Termino? = null,
    val zona: Termino? = null,
    val etiquetas: List<Termino> = emptyList(),
    val coordenadas: Coordenadas? = null,
    val portada: Imagen? = null,
    @SerialName("rango_precio") val rangoPrecio: Int? = null,
    @SerialName("horario_resumen") val horarioResumen: String = "",
    /** Solo si `tipoItem == "evento"`: cuando pasa. */
    val fechas: Fechas? = null,
    @SerialName("google_maps") val googleMaps: String? = null,
    /**
     * En que idioma vienen los textos de esta pieza, ya resuelto: si se pidio
     * uno que el panel no tiene, aca dice el original.
     */
    val idioma: String = "es",
    /**
     * True solo si TODOS los campos traducibles de esta pieza estaban
     * traducidos. La caida al original es campo por campo, asi que false
     * significa que alguno vino en castellano y lo que se ve es una mezcla.
     * Con el original pedido viene siempre false: no hay nada que traducir.
     */
    val traducido: Boolean = false,
    val actualizado: String? = null,
)

@Immutable
@Serializable
data class Practicos(
    val horario: String = "",
    val costo: String = "",
    @SerialName("rango_precio") val rangoPrecio: Int? = null,
    val contacto: String = "",
)

@Immutable
@Serializable
data class Acceso(
    @SerialName("estado_camino") val estadoCamino: String = "",
    val accesibilidad: String = "",
)

@Immutable
@Serializable
data class Autor(val id: Int, val nombre: String = "")

/**
 * Firma de un articulo. No es una cuenta necesariamente: `cuenta` solo viene
 * cuando quien firma es una cuenta del sistema, y una nota puede llevar mas
 * de una firma.
 */
@Immutable
@Serializable
data class AutorArticulo(
    val nombre: String = "",
    val cuenta: Int? = null,
)

@Immutable
@Serializable
data class ResumenArticulo(
    val id: Int,
    val titulo: String = "",
    val antetitulo: String = "",
    val subtitulo: String = "",
    val entradilla: String = "",
    val portada: Imagen? = null,
    val autores: List<AutorArticulo> = emptyList(),
    val publicado: String? = null,
    val etiquetas: List<Termino> = emptyList(),
    /**
     * En que idioma vienen los textos de esta pieza, ya resuelto: si se pidio
     * uno que el panel no tiene, aca dice el original.
     */
    val idioma: String = "es",
    /**
     * True solo si TODOS los campos traducibles de esta pieza estaban
     * traducidos. La caida al original es campo por campo, asi que false
     * significa que alguno vino en castellano y lo que se ve es una mezcla.
     * Con el original pedido viene siempre false: no hay nada que traducir.
     */
    val traducido: Boolean = false,
)

@Immutable
@Serializable
data class Ficha(
    val id: Int,
    @SerialName("tipo_item") val tipoItem: String = "sitio",
    val titulo: String = "",
    val gancho: String = "",
    val categoria: Termino? = null,
    val zona: Termino? = null,
    val etiquetas: List<Termino> = emptyList(),
    val coordenadas: Coordenadas? = null,
    val portada: Imagen? = null,
    val galeria: List<Imagen> = emptyList(),
    val video: String? = null,
    val practicos: Practicos = Practicos(),
    val acceso: Acceso = Acceso(),
    /** Solo si `tipoItem == "evento"`: cuando pasa. */
    val fechas: Fechas? = null,
    @SerialName("google_maps") val googleMaps: String? = null,
    /**
     * El cuerpo de la ficha. El servidor lo manda como `descripcion`; el
     * contrato original preveia `articulo_html`, y se acepta cualquiera de
     * los dos por si vuelve a cambiar. Sin esto la ficha se veia sin cuerpo,
     * sin galeria de texto y sin "leer mas": el campo llegaba, pero con un
     * nombre que el modelo no sabia leer.
     */
    @SerialName("articulo_html") @JsonNames("descripcion") val articuloHtml: String = "",
    @SerialName("articulos_relacionados") val articulosRelacionados: List<ResumenArticulo> = emptyList(),
    val fuentes: String = "",
    val autor: Autor? = null,
    /**
     * En que idioma vienen los textos de esta pieza, ya resuelto: si se pidio
     * uno que el panel no tiene, aca dice el original.
     */
    val idioma: String = "es",
    /**
     * True solo si TODOS los campos traducibles de esta pieza estaban
     * traducidos. La caida al original es campo por campo, asi que false
     * significa que alguno vino en castellano y lo que se ve es una mezcla.
     * Con el original pedido viene siempre false: no hay nada que traducir.
     */
    val traducido: Boolean = false,
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

/**
 * Solo para `origen: "evento_legado"` — el CPT viejo, que ya no se carga pero
 * sigue sirviendose. Un evento nuevo es una `Ficha` con `tipoItem == "evento"`:
 * viaja por `/inventario`, no por este modelo.
 */
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
    /**
     * En que idioma vienen los textos de esta pieza, ya resuelto: si se pidio
     * uno que el panel no tiene, aca dice el original.
     */
    val idioma: String = "es",
    /**
     * True solo si TODOS los campos traducibles de esta pieza estaban
     * traducidos. La caida al original es campo por campo, asi que false
     * significa que alguno vino en castellano y lo que se ve es una mezcla.
     * Con el original pedido viene siempre false: no hay nada que traducir.
     */
    val traducido: Boolean = false,
    val actualizado: String? = null,
)

@Immutable
@Serializable
data class Articulo(
    val id: Int,
    val titulo: String = "",
    val antetitulo: String = "",
    val subtitulo: String = "",
    val entradilla: String = "",
    val portada: Imagen? = null,
    val autores: List<AutorArticulo> = emptyList(),
    val publicado: String? = null,
    @SerialName("cuerpo_html") val cuerpoHtml: String = "",
    val categoria: Termino? = null,
    val etiquetas: List<Termino> = emptyList(),
    val fuentes: List<String> = emptyList(),
    val relacionados: List<ResumenArticulo> = emptyList(),
    /**
     * En que idioma vienen los textos de esta pieza, ya resuelto: si se pidio
     * uno que el panel no tiene, aca dice el original.
     */
    val idioma: String = "es",
    /**
     * True solo si TODOS los campos traducibles de esta pieza estaban
     * traducidos. La caida al original es campo por campo, asi que false
     * significa que alguno vino en castellano y lo que se ve es una mezcla.
     * Con el original pedido viene siempre false: no hay nada que traducir.
     */
    val traducido: Boolean = false,
    val actualizado: String? = null,
)

/** El audio o video de un recorrido entero o de una parada puntual. */
@Immutable
@Serializable
data class MedioRecorrido(
    val tipo: String = "audio",
    val url: String,
    val titulo: String = "",
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
    @SerialName("google_maps") val googleMaps: String? = null,
    val costo: String = "",
    val horario: String? = null,
    val inicio: String? = null,
    val fin: String? = null,
    val texto: String = "",
    val medio: MedioRecorrido? = null,
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

/** Los costos de las paradas son texto libre: no se suman, se listan. */
@Immutable
@Serializable
data class CostoTotal(
    @SerialName("hay_pago") val hayPago: Boolean = false,
    val detalle: List<String> = emptyList(),
)

/**
 * Si dos paradas son eventos que no se solapan, el recorrido no se hace en
 * una sola salida. La forma de cada conflicto no esta cerrada en el contrato
 * todavia, asi que se guarda tal cual llega en vez de asumirle una forma.
 */
@Immutable
@Serializable
data class CompatibilidadFechas(
    val compatible: Boolean = true,
    val conflictos: List<JsonElement> = emptyList(),
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
    val medios: List<MedioRecorrido> = emptyList(),
    val articulos: List<ResumenArticulo> = emptyList(),
    @SerialName("costo_total") val costoTotal: CostoTotal? = null,
    val fechas: CompatibilidadFechas? = null,
    @SerialName("google_maps") val googleMaps: String? = null,
    val historia: Historia? = null,
    @SerialName("articulo_html") val articuloHtml: String = "",
    /**
     * En que idioma vienen los textos de esta pieza, ya resuelto: si se pidio
     * uno que el panel no tiene, aca dice el original.
     */
    val idioma: String = "es",
    /**
     * True solo si TODOS los campos traducibles de esta pieza estaban
     * traducidos. La caida al original es campo por campo, asi que false
     * significa que alguno vino en castellano y lo que se ve es una mezcla.
     * Con el original pedido viene siempre false: no hay nada que traducir.
     */
    val traducido: Boolean = false,
)

/**
 * Un idioma que el panel sirve. `nombre` viene en su propio idioma, que es como
 * se escribe un selector: nadie busca "Ingles" en una lista que esta mirando
 * justamente porque no entiende el castellano.
 */
@Immutable
@Serializable
data class IdiomaDisponible(
    val codigo: String,
    val nombre: String = "",
    val original: Boolean = false,
)

/** Lo que devuelve `/idiomas`. La lista no va compilada en la app. */
@Immutable
@Serializable
data class Idiomas(
    val original: String = "es",
    val idiomas: List<IdiomaDisponible> = emptyList(),
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
