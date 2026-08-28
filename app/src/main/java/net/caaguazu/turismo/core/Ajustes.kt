package net.caaguazu.turismo.core

import android.content.Context
import net.caaguazu.turismo.BuildConfig

/**
 * Ajustes que sobreviven al cierre de la app.
 *
 * Hoy solo uno, y existe por una razon concreta: durante la prueba cerrada hace
 * falta poder mirar la app con los datos de ejemplo y con los del panel sin
 * compilar dos veces. Cambiarlo desde el telefono evita una vuelta entera de
 * compilar, publicar, descargar e instalar por cada comparacion.
 */
object Ajustes {

    private const val ETIQUETA = "Ajustes"
    private const val ARCHIVO = "ajustes"
    private const val CLAVE_ORIGEN = "origen"

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
}
