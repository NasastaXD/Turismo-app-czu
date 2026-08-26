package net.caaguazu.turismo.core

import android.content.Context
import android.util.Log
import net.caaguazu.turismo.BuildConfig
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Registro propio de la app.
 *
 * En depuracion escribe a Logcat. En release escribe a un archivo rotativo dentro del
 * almacenamiento privado, que la pantalla de diagnostico puede exportar.
 *
 * Las etiquetas se pasan como texto literal a proposito: R8 renombra las clases, asi
 * que derivarlas del nombre de clase dejaria el registro inservible justo en release,
 * que es cuando hace falta.
 */
object Registro {

    private const val ETIQUETA_GLOBAL = "Caaguazu"
    private const val ARCHIVO = "registro.txt"
    private const val ARCHIVO_PREVIO = "registro-previo.txt"
    private const val LIMITE_BYTES = 512L * 1024L

    private val reloj = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US)

    @Volatile private var destino: File? = null
    private val candado = Any()

    /** Se llama una sola vez, al arrancar la aplicacion. */
    fun iniciar(contexto: Context) {
        synchronized(candado) {
            if (destino != null) return
            destino = runCatching {
                File(contexto.filesDir, ARCHIVO).also { it.parentFile?.mkdirs() }
            }.getOrNull()
        }
        capturarCaidas()
        info("Registro", "iniciado v${BuildConfig.VERSION_NAME} (${BuildConfig.BUILD_TYPE})")
    }

    fun detalle(etiqueta: String, mensaje: String) {
        if (BuildConfig.DEBUG) Log.d(ETIQUETA_GLOBAL, "$etiqueta: $mensaje")
        escribir('D', etiqueta, mensaje)
    }

    fun info(etiqueta: String, mensaje: String) {
        if (BuildConfig.DEBUG) Log.i(ETIQUETA_GLOBAL, "$etiqueta: $mensaje")
        escribir('I', etiqueta, mensaje)
    }

    fun aviso(etiqueta: String, mensaje: String) {
        if (BuildConfig.DEBUG) Log.w(ETIQUETA_GLOBAL, "$etiqueta: $mensaje")
        escribir('W', etiqueta, mensaje)
    }

    fun fallo(etiqueta: String, mensaje: String, causa: Throwable? = null) {
        if (BuildConfig.DEBUG) Log.e(ETIQUETA_GLOBAL, "$etiqueta: $mensaje", causa)
        escribir('E', etiqueta, if (causa == null) mensaje else "$mensaje | ${causa.stackTraceToString()}")
    }

    /** Contenido completo del registro, para la pantalla de diagnostico. */
    fun leerTodo(): String {
        val actual = destino ?: return ""
        return runCatching {
            val previo = File(actual.parentFile, ARCHIVO_PREVIO)
            buildString {
                if (previo.exists()) append(previo.readText())
                if (actual.exists()) append(actual.readText())
            }
        }.getOrElse { "" }
    }

    fun borrar() {
        val actual = destino ?: return
        runCatching {
            actual.delete()
            File(actual.parentFile, ARCHIVO_PREVIO).delete()
        }
    }

    private fun escribir(nivel: Char, etiqueta: String, mensaje: String) {
        val archivo = destino ?: return
        synchronized(candado) {
            runCatching {
                if (archivo.length() > LIMITE_BYTES) {
                    val previo = File(archivo.parentFile, ARCHIVO_PREVIO)
                    previo.delete()
                    archivo.renameTo(previo)
                }
                archivo.appendText("${reloj.format(Date())} $nivel/$etiqueta: $mensaje\n")
            }
        }
    }

    /** Deja constancia por escrito antes de que la app muera. */
    private fun capturarCaidas() {
        val anterior = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { hilo, error ->
            escribir('E', "Caida", "hilo=${hilo.name} | ${error.stackTraceToString()}")
            anterior?.uncaughtException(hilo, error)
        }
    }
}
