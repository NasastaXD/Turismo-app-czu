package net.caaguazu.turismo.core

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

/**
 * Lo que el usuario guarda en su telefono: favoritos y el recorrido que esta
 * armando.
 *
 * Vive solo aca. La primera version de la app no tiene cuenta, asi que no hay
 * donde sincronizarlo — y aunque la hubiera, un recorrido a medio armar es
 * borrador, no contenido del panel.
 *
 * Un archivo JSON y no una base de datos: son dos listas de enteros. Room
 * traeria un procesador de anotaciones y kilobytes para guardar eso.
 *
 * Escribe con `Archivos`, el mismo de la cache, asi que la diferencia entre
 * `java.io.File` y `NSFileManager` queda donde ya estaba resuelta.
 */
object Guardado {

    private const val ETIQUETA = "Guardado"
    private const val FAVORITOS = "favoritos.json"
    private const val RECORRIDO = "recorrido.json"

    private var disco: Archivos? = null

    /** Leerlos desde una composicion la suscribe: el corazon se pinta solo. */
    var favoritos by mutableStateOf<Set<Int>>(emptySet())
        private set

    /**
     * El recorrido es una lista y no un conjunto porque el orden es el
     * recorrido: son las paradas en el orden en que la persona las agrego.
     */
    var recorrido by mutableStateOf<List<Int>>(emptyList())
        private set

    /** Posicion de cada parada, para no buscarla dentro del bucle que la dibuja. */
    val ordenDeParada: Map<Int, Int>
        get() = recorrido.withIndex().associate { (indice, id) -> id to indice + 1 }

    fun iniciar() {
        disco = Archivos(Entorno.carpetaDatos)
        favoritos = leer(FAVORITOS).toSet()
        recorrido = leer(RECORRIDO)
        Bitacora.info(ETIQUETA, "${favoritos.size} favoritos, ${recorrido.size} paradas guardadas")
    }

    fun esFavorito(id: Int) = id in favoritos

    fun alternarFavorito(id: Int) {
        favoritos = if (id in favoritos) favoritos - id else favoritos + id
        escribir(FAVORITOS, favoritos.toList())
    }

    fun enRecorrido(id: Int) = id in recorrido

    fun alternarEnRecorrido(id: Int) {
        recorrido = if (id in recorrido) recorrido - id else recorrido + id
        escribir(RECORRIDO, recorrido)
        Bitacora.detalle(ETIQUETA, "recorrido: ${recorrido.size} paradas")
    }

    fun vaciarRecorrido() {
        recorrido = emptyList()
        escribir(RECORRIDO, recorrido)
    }

    private fun leer(archivo: String): List<Int> {
        val texto = disco?.leer(archivo) ?: return emptyList()
        return when (
            val r = intentarCompartido(ETIQUETA, "leer $archivo") {
                Analizador.decodeFromString(ListSerializer(Int.serializer()), texto)
            }
        ) {
            is Resultado.Bien -> r.valor
            // Un archivo corrupto no puede impedir que la app abra.
            is Resultado.Mal -> emptyList()
        }
    }

    private fun escribir(archivo: String, valores: List<Int>) {
        val destino = disco ?: return
        intentarCompartido(ETIQUETA, "guardar $archivo") {
            destino.escribir(archivo, Analizador.encodeToString(ListSerializer(Int.serializer()), valores))
        }
    }
}
