package net.caaguazu.turismo.core

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

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
 *
 * Este objeto **solo recuerda que idioma esta elegido**. Cargar los textos que
 * le corresponden es de `Aplicacion`, que tiene un `LaunchedEffect(actual)`
 * para eso. Antes lo hacia aca, y para poder hacerlo tenia que guardarse un
 * `Context`: sacarlo dejo un unico dueño de "los textos siguen al idioma", que
 * es lo que evita que un cambio de idioma deje la interfaz en un idioma y el
 * contenido en otro.
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
    fun iniciar() {
        val guardado = Ajustes.idioma
        actual = guardado ?: delSistema()
        Bitacora.info(ETIQUETA, "idioma inicial: $actual (guardado=$guardado)")
    }

    fun elegir(codigo: String) {
        if (codigo == actual) return
        Ajustes.idioma = codigo
        actual = codigo
        Bitacora.info(ETIQUETA, "idioma cambiado a $codigo")
    }

    /** La lista que mando el panel. Si viene vacia se conserva el respaldo. */
    fun aplicarDisponibles(lista: List<Disponible>) {
        if (lista.isEmpty()) return
        disponibles = lista
        // Si el idioma elegido dejo de existir del lado del panel, se vuelve al
        // original en vez de seguir pidiendo algo que ya no se sirve.
        if (lista.none { it.codigo == actual }) {
            Bitacora.aviso(ETIQUETA, "$actual ya no esta en la lista del panel; se vuelve a $ORIGINAL")
            // Mover `actual` no alcanza. Hay que soltar tambien la eleccion
            // guardada —si no, el proximo arranque vuelve a pedir el idioma que
            // el panel ya no sirve— y los textos los recarga solo el efecto de
            // `Aplicacion`, que mira justamente `actual`.
            Ajustes.idioma = null
            actual = ORIGINAL
        }
    }

    fun nombreDe(codigo: String): String =
        disponibles.firstOrNull { it.codigo == codigo }?.nombre ?: codigo

    private fun delSistema(): String {
        val delTelefono = Sistema.idiomaDelTelefono
        return RESPALDO.firstOrNull { it.codigo == delTelefono }?.codigo ?: ORIGINAL
    }
}
