package net.caaguazu.turismo.core

import android.content.Context

/**
 * Ajustes que sobreviven al cierre de la app.
 *
 * Vive aca la memoria de los avisos, que es lo que evita notificar dos veces lo
 * mismo.
 *
 * Todo con SharedPreferences y nada mas: son unas pocas claves y un conjunto de
 * enteros. Una base de datos para esto seria maquinaria de sobra.
 */
object Ajustes {

    private const val ETIQUETA = "Ajustes"
    private const val ARCHIVO = "ajustes"
    private const val CLAVE_IDIOMA = "idioma"
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

    private lateinit var preferencias: android.content.SharedPreferences

    fun iniciar(contexto: Context) {
        preferencias = contexto.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)
    }

    /* ---------------------------------------------------------------------
     * Avisos
     * ------------------------------------------------------------------- */

    /** Si la app revisa y notifica. Arranca encendido; se apaga solo si se niega el permiso. */
    /**
     * El idioma elegido a mano. Null significa que nadie eligio todavia, que no
     * es lo mismo que haber elegido castellano: sin elegir se sigue al telefono,
     * y elegido se respeta aunque el telefono diga otra cosa.
     */
    var idioma: String?
        get() = runCatching { preferencias.getString(CLAVE_IDIOMA, null) }.getOrNull()
        set(valor) {
            runCatching { preferencias.edit().putString(CLAVE_IDIOMA, valor).apply() }
        }

    var avisosActivos: Boolean
        get() = runCatching { preferencias.getBoolean(CLAVE_AVISOS, true) }.getOrDefault(true)
        set(valor) {
            runCatching { preferencias.edit().putBoolean(CLAVE_AVISOS, valor).apply() }
            Registro.info(ETIQUETA, "avisos ${if (valor) "encendidos" else "apagados"}")
        }

    /**
     * Si el interruptor ya quedo fijado alguna vez, a mano o por el resultado
     * del permiso. Antes de eso, `avisosActivos` es solo el valor por defecto:
     * hace falta pedir el permiso de sistema una vez para que sea real.
     */
    fun avisosDecididos(): Boolean = runCatching { preferencias.contains(CLAVE_AVISOS) }.getOrDefault(false)

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
