package net.caaguazu.turismo.core

import android.content.Context
import net.caaguazu.turismo.BuildConfig

/**
 * Ajustes que sobreviven al cierre de la app.
 *
 * Dos cosas viven aca: el origen de los datos —que existe porque durante la
 * prueba cerrada hace falta comparar mocks y panel sin compilar dos veces— y la
 * memoria de los avisos, que es lo que evita notificar dos veces lo mismo.
 *
 * Todo con SharedPreferences y nada mas: son unas pocas claves y un conjunto de
 * enteros. Una base de datos para esto seria maquinaria de sobra.
 */
object Ajustes {

    private const val ETIQUETA = "Ajustes"
    private const val ARCHIVO = "ajustes"
    private const val CLAVE_ORIGEN = "origen"
    private const val CLAVE_AVISOS = "avisos"
    private const val CLAVE_ARTICULOS_VISTOS = "articulos_vistos"
    private const val CLAVE_EVENTOS_AVISADOS = "eventos_avisados"

    /**
     * Tope de ids recordados por conjunto.
     *
     * Sin tope, la lista de "ya avisado" crece para siempre. Se conservan los
     * ultimos: un articulo que salio hace dos años no va a volver a aparecer
     * como nuevo, y si apareciera, un aviso de mas es mejor que un archivo de
     * preferencias que crece sin limite.
     */
    private const val MAX_RECORDADOS = 300

    enum class Origen { MOCKS, PANEL }

    private lateinit var preferencias: android.content.SharedPreferences

    fun iniciar(contexto: Context) {
        preferencias = contexto.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)
    }

    /** De donde salen los datos. Por defecto, lo que decidio la compilacion. */
    var origen: Origen
        get() {
            val guardado = runCatching { preferencias.getString(CLAVE_ORIGEN, null) }.getOrNull()
            return runCatching { Origen.valueOf(guardado!!) }.getOrElse {
                if (BuildConfig.USAR_MOCKS) Origen.MOCKS else Origen.PANEL
            }
        }
        set(valor) {
            runCatching { preferencias.edit().putString(CLAVE_ORIGEN, valor.name).apply() }
            Registro.info(ETIQUETA, "origen de datos cambiado a $valor")
        }

    /** La otra opcion, que es lo que ofrece el boton de cambiar. */
    fun contraria(): Origen = if (origen == Origen.MOCKS) Origen.PANEL else Origen.MOCKS

    /** Como se llama cada origen. Es un dato tecnico, no texto de producto. */
    fun nombre(o: Origen): String = when (o) {
        Origen.MOCKS -> "mocks"
        Origen.PANEL -> BuildConfig.URL_BASE
    }

    /* ---------------------------------------------------------------------
     * Avisos
     * ------------------------------------------------------------------- */

    /**
     * Si la app revisa y notifica. Arranca apagado a proposito: notificar sin
     * que nadie lo haya pedido es la clase de cosa que termina con la app
     * silenciada entera.
     */
    var avisosActivos: Boolean
        get() = runCatching { preferencias.getBoolean(CLAVE_AVISOS, false) }.getOrDefault(false)
        set(valor) {
            runCatching { preferencias.edit().putBoolean(CLAVE_AVISOS, valor).apply() }
            Registro.info(ETIQUETA, "avisos ${if (valor) "encendidos" else "apagados"}")
        }

    /** Articulos de los que ya se aviso, o que ya existian al encender los avisos. */
    var articulosVistos: Set<Int>
        get() = leerIds(CLAVE_ARTICULOS_VISTOS)
        set(valor) = guardarIds(CLAVE_ARTICULOS_VISTOS, valor)

    /** Eventos de los que ya se aviso que se venian. */
    var eventosAvisados: Set<Int>
        get() = leerIds(CLAVE_EVENTOS_AVISADOS)
        set(valor) = guardarIds(CLAVE_EVENTOS_AVISADOS, valor)

    /** Al apagar los avisos se olvida lo anotado: volver a encenderlos empieza limpio. */
    fun olvidarAvisados() {
        runCatching {
            preferencias.edit()
                .remove(CLAVE_ARTICULOS_VISTOS)
                .remove(CLAVE_EVENTOS_AVISADOS)
                .apply()
        }
    }

    private fun leerIds(clave: String): Set<Int> =
        runCatching {
            preferencias.getStringSet(clave, emptySet())
                .orEmpty()
                .mapNotNull(String::toIntOrNull)
                .toSet()
        }.getOrDefault(emptySet())

    private fun guardarIds(clave: String, ids: Set<Int>) {
        // Los mas altos son los mas recientes: los ids del panel son
        // autoincrementales, asi que ordenar por id ordena por antiguedad.
        val recortado = ids.sortedDescending().take(MAX_RECORDADOS)
        runCatching {
            preferencias.edit()
                .putStringSet(clave, recortado.map(Int::toString).toSet())
                .apply()
        }
    }
}
