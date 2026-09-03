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
    fun cargarEmbebido(contexto: Context, codigo: String = Idioma.ORIGINAL) {
        idioma = codigo
        val piso = leerAsset(contexto, Idioma.ORIGINAL)
        val propio = if (codigo == Idioma.ORIGINAL) emptyMap() else leerAsset(contexto, codigo)

        if (piso.isEmpty() && propio.isEmpty()) {
            Registro.fallo(ETIQUETA, "sin textos embebidos: la interfaz saldra marcada")
            return
        }
        embebidos = piso + propio
        mapa = embebidos
        Registro.info(
            ETIQUETA,
            "${embebidos.size} textos embebidos para $codigo (${propio.size} propios sobre ${piso.size})",
        )
    }

    private fun leerAsset(contexto: Context, codigo: String): Map<String, String> {
        val ruta = "textos/$codigo.json"
        val leido = intentar(ETIQUETA, "leer $ruta") {
            contexto.assets.open(ruta).bufferedReader().use { it.readText() }
        }
        return when (leido) {
            is Resultado.Bien -> interpretar(leido.valor, ruta) ?: emptyMap()
            // Que falte el archivo de un idioma no es un fallo: el panel puede
            // ofrecer uno para el que todavia no viajamos textos de interfaz, y
            // ahi el piso en castellano es la respuesta correcta.
            is Resultado.Mal -> emptyMap()
        }
    }

    /** Un JSON plano de clave a texto. Null si vino roto. */
    private fun interpretar(json: String, origen: String): Map<String, String>? {
        val analizado = intentar(ETIQUETA, "interpretar textos de $origen") {
            Json.parseToJsonElement(json).let { raiz ->
                buildMap {
                    raiz.let { it as? kotlinx.serialization.json.JsonObject }
                        ?.forEach { (clave, valor) -> put(clave, valor.jsonPrimitive.content) }
                }
            }
        }
        return when (analizado) {
            is Resultado.Bien -> analizado.valor
            is Resultado.Mal -> {
                Registro.aviso(ETIQUETA, "textos de $origen ilegibles, se conserva lo anterior")
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
