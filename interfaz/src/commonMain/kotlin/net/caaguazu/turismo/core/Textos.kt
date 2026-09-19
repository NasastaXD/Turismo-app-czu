package net.caaguazu.turismo.core

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Todos los textos de interfaz de la app.
 *
 * Ninguna pantalla escribe texto visible: lo pide por clave. Asi cualquier texto se
 * cambia editando un JSON del lado del panel, sin publicar un APK nuevo.
 *
 * No cubre fichas, eventos, recorridos ni articulos: eso es contenido humano y viene
 * de la base de datos.
 *
 * Una clave que falta se muestra marcada de forma inequivoca, para que sea imposible
 * que un hueco sin redactar pase por texto terminado.
 *
 * Los tres archivos de respaldo se leen con `Empaquetado`: en Android salen de
 * los assets del APK y en iOS del bundle, y el codigo de aca no se entera de
 * cual de las dos cosas fue.
 */
object Textos {

    private const val ETIQUETA = "Textos"

    /** Leerlo desde una composicion la suscribe: al refrescar textos, la pantalla se redibuja. */
    private var mapa by mutableStateOf<Map<String, String>>(emptyMap())

    /** El respaldo del APK. Es el piso: nunca se pierde, solo se pisa clave a clave. */
    private var embebidos: Map<String, String> = emptyMap()

    var idioma: String = Idioma.ORIGINAL
        private set

    /**
     * Respaldo embebido, para el primer arranque sin red y para todo lo que el
     * panel todavia no tradujo.
     *
     * Van en tres capas y el orden importa: el castellano es el piso —es el
     * original y esta completo—, encima el idioma elegido, y encima de todo lo
     * que mande el servidor. Asi una clave que falta en ingles sale en
     * castellano en vez de salir marcada entre angulos, que es lo peor de los
     * tres resultados posibles.
     */
    fun cargarEmbebido(codigo: String = Idioma.ORIGINAL) {
        idioma = codigo
        val piso = leerRecurso(Idioma.ORIGINAL)
        val propio = if (codigo == Idioma.ORIGINAL) emptyMap() else leerRecurso(codigo)

        if (piso.isEmpty() && propio.isEmpty()) {
            Bitacora.fallo(ETIQUETA, "sin textos embebidos: la interfaz saldra marcada")
            return
        }
        embebidos = piso + propio
        // Lo que el servidor haya mandado antes no se pierde al cambiar de
        // idioma: se vuelve a pedir enseguida, y hasta que llegue vale el
        // respaldo del idioma nuevo, que es lo correcto.
        mapa = embebidos
        Bitacora.info(
            ETIQUETA,
            "${embebidos.size} textos embebidos para $codigo (${propio.size} propios sobre ${piso.size})",
        )
    }

    private fun leerRecurso(codigo: String): Map<String, String> {
        val ruta = "textos/$codigo.json"
        // Que falte el archivo de un idioma no es un fallo: el panel puede
        // ofrecer uno para el que todavia no viajamos textos de interfaz, y ahi
        // el piso en castellano es la respuesta correcta.
        val leido = Empaquetado.texto(ruta) ?: run {
            Bitacora.detalle(ETIQUETA, "sin respaldo embebido para $codigo")
            return emptyMap()
        }
        return interpretar(leido, ruta) ?: emptyMap()
    }

    /** Un JSON plano de clave a texto. Null si vino roto. */
    private fun interpretar(json: String, origen: String): Map<String, String>? {
        val analizado = intentarCompartido(ETIQUETA, "interpretar textos de $origen") {
            buildMap {
                (Json.parseToJsonElement(json) as? JsonObject)
                    ?.forEach { (clave, valor) -> put(clave, valor.jsonPrimitive.content) }
            }
        }
        return when (analizado) {
            is Resultado.Bien -> analizado.valor
            is Resultado.Mal -> {
                Bitacora.aviso(ETIQUETA, "textos de $origen ilegibles, se conserva lo anterior")
                null
            }
        }
    }

    /**
     * Aplica un juego de textos del servidor sobre el respaldo embebido.
     *
     * Fusiona, no reemplaza, y la diferencia importa: el panel puede tener
     * cargadas solo algunas claves, y reemplazar dejaria sin texto a todas las
     * demas — incluida la atribucion de OpenStreetMap, que es obligatoria por
     * licencia y no puede depender de que alguien se acuerde de cargarla.
     *
     * Un valor vacio tampoco pisa: una clave en blanco en el panel es un
     * descuido, no la intencion de borrar el texto que ya habia.
     */
    fun aplicarMapa(nuevos: Map<String, String>, origen: String) {
        val utiles = nuevos.filterValues { it.isNotBlank() }
        if (utiles.isEmpty()) {
            Bitacora.aviso(ETIQUETA, "textos de $origen sin nada aprovechable, se conserva lo anterior")
            return
        }
        mapa = embebidos + utiles
        Bitacora.info(
            ETIQUETA,
            "${utiles.size} textos de $origen sobre ${embebidos.size} embebidos",
        )
    }

    /**
     * Si ya hay textos con los que dibujar.
     *
     * Lo pregunta el trabajador de avisos, que necesita textos pero no puede
     * volver a cargarlos sin pisar lo que el panel haya mandado.
     */
    val cargados: Boolean get() = mapa.isNotEmpty()

    /** El unico camino por el que un texto llega a la pantalla. */
    fun t(clave: String): String = mapa[clave] ?: marcador(clave)

    private fun marcador(clave: String): String {
        Bitacora.aviso(ETIQUETA, "falta la clave $clave")
        return "‹$clave›"
    }
}
