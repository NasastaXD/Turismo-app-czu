package net.caaguazu.turismo.core

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.json.Json
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
 */
object Textos {

    private const val ETIQUETA = "Textos"
    private const val EMBEBIDO = "textos/es.json"

    /** Leerlo desde una composicion la suscribe: al refrescar textos, la pantalla se redibuja. */
    private var mapa by mutableStateOf<Map<String, String>>(emptyMap())

    /** El respaldo del APK. Es el piso: nunca se pierde, solo se pisa clave a clave. */
    private var embebidos: Map<String, String> = emptyMap()

    var idioma: String = "es"
        private set

    /** Respaldo embebido, para el primer arranque sin red. */
    fun cargarEmbebido(contexto: Context) {
        val leido = intentar(ETIQUETA, "leer $EMBEBIDO") {
            contexto.assets.open(EMBEBIDO).bufferedReader().use { it.readText() }
        }
        when (leido) {
            is Resultado.Bien -> {
                aplicar(leido.valor, "embebido")
                embebidos = mapa
            }
            is Resultado.Mal -> Registro.fallo(ETIQUETA, "sin textos embebidos: la interfaz saldra marcada")
        }
    }

    /** Reemplaza los textos con los del servidor. Si el JSON viene roto, se conserva lo anterior. */
    fun aplicar(json: String, origen: String) {
        val analizado = intentar(ETIQUETA, "interpretar textos de $origen") {
            Json.parseToJsonElement(json).let { raiz ->
                buildMap {
                    raiz.let { it as? kotlinx.serialization.json.JsonObject }
                        ?.forEach { (clave, valor) -> put(clave, valor.jsonPrimitive.content) }
                }
            }
        }
        when (analizado) {
            is Resultado.Bien -> {
                if (analizado.valor.isEmpty()) {
                    Registro.aviso(ETIQUETA, "textos de $origen vinieron vacios, se conserva lo anterior")
                } else {
                    mapa = analizado.valor
                    Registro.info(ETIQUETA, "cargados ${analizado.valor.size} textos de $origen")
                }
            }
            is Resultado.Mal -> Registro.aviso(ETIQUETA, "textos de $origen ilegibles, se conserva lo anterior")
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
            Registro.aviso(ETIQUETA, "textos de $origen sin nada aprovechable, se conserva lo anterior")
            return
        }
        mapa = embebidos + utiles
        Registro.info(
            ETIQUETA,
            "${utiles.size} textos de $origen sobre ${embebidos.size} embebidos",
        )
    }

    /** El unico camino por el que un texto llega a la pantalla. */
    fun t(clave: String): String = mapa[clave] ?: marcador(clave)

    private fun marcador(clave: String): String {
        Registro.aviso(ETIQUETA, "falta la clave $clave")
        return "‹$clave›"
    }
}
