package net.caaguazu.turismo.core

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

/**
 * El idioma en el que se pide y se lee todo.
 *
 * El castellano no es "un idioma mas": es el original, en el que se escribe el
 * contenido. Los demas son una capa encima, y cuando a una pieza le falta un
 * campo traducido el panel sirve el castellano de ese campo suelto. Por eso una
 * ficha puede llegar mitad y mitad, y por eso el contenido trae `traducido`.
 *
 * La lista de idiomas NO va compilada: la manda el panel en `/idiomas`, porque
 * el guarani esta previsto y va a aparecer ahi antes de que salga un APK nuevo.
 * Lo de aca abajo es solo el respaldo para el primer arranque sin red y para un
 * servidor que todavia no tenga ese endpoint.
 */
object Idioma {

    private const val ETIQUETA = "Idioma"

    /** El idioma en el que se escribe el contenido. Nunca falta. */
    const val ORIGINAL = "es"

    /**
     * Nombre de cada idioma en su propio idioma, que es como se escribe un
     * selector: nadie busca "Ingles" en una lista que esta mirando justamente
     * porque no entiende el castellano.
     */
    private val RESPALDO = listOf(
        Disponible(ORIGINAL, "Español"),
        Disponible("en", "English"),
        Disponible("pt", "Português"),
    )

    data class Disponible(val codigo: String, val nombre: String)

    /** Leerlo desde una composicion la suscribe: cambiar de idioma redibuja la app. */
    var actual by mutableStateOf(ORIGINAL)
        private set

    var disponibles by mutableStateOf(RESPALDO)
        private set

    /** Si lo que se ve es el original, no hay traduccion de la que avisar. */
    val enOriginal: Boolean get() = actual == ORIGINAL

    /**
     * Arranca con lo que la persona eligio, y si nunca eligio, con el idioma
     * del telefono — pero solo si lo tenemos. Un telefono en frances abre en
     * castellano, que es el original, y no en un idioma que no existe.
     */
    fun iniciar(contexto: Context) {
        val guardado = Ajustes.idioma
        actual = guardado ?: delSistema()
        Registro.info(ETIQUETA, "idioma inicial: $actual (guardado=$guardado)")
        cargarTextos(contexto)
    }

    fun elegir(contexto: Context, codigo: String) {
        if (codigo == actual) return
        Ajustes.idioma = codigo
        actual = codigo
        Registro.info(ETIQUETA, "idioma cambiado a $codigo")
        cargarTextos(contexto)
    }

    /** La lista que mando el panel. Si viene vacia se conserva el respaldo. */
    fun aplicarDisponibles(lista: List<Disponible>) {
        if (lista.isEmpty()) return
        disponibles = lista
        // Si el idioma elegido dejo de existir del lado del panel, se vuelve al
        // original en vez de seguir pidiendo algo que ya no se sirve.
        if (lista.none { it.codigo == actual }) {
            Registro.aviso(ETIQUETA, "$actual ya no esta en la lista del panel; se vuelve a $ORIGINAL")
            actual = ORIGINAL
        }
    }

    fun nombreDe(codigo: String): String =
        disponibles.firstOrNull { it.codigo == codigo }?.nombre ?: codigo

    private fun cargarTextos(contexto: Context) = Textos.cargarEmbebido(contexto, actual)

    private fun delSistema(): String {
        val delTelefono = runCatching { Locale.getDefault().language }.getOrNull()
        return RESPALDO.firstOrNull { it.codigo == delTelefono }?.codigo ?: ORIGINAL
    }
}
