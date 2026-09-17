package net.caaguazu.turismo.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * El unico acceso a la red.
 *
 * Todo lo que decide algo vive aca y se escribe una sola vez: el 304 contra la
 * copia guardada, el reintento corto, y la caida a la copia guardada sin senal —
 * mostrando contenido cuando no hay senal, que en un distrito de 942 km es la
 * situacion normal y no la excepcion.
 *
 * Lo que cambia por plataforma es solo `pedirHttp`: abrir la conexion, leer el
 * cuerpo y sacar el ETag. HttpURLConnection no existe en iOS y NSURLSession no
 * existe en Android, pero son unas treinta lineas cada uno y ninguna de ellas
 * decide nada.
 */
class Http(private val cache: Cache) {

    internal companion object {
        const val ETIQUETA = "Http"
        const val ESPERA_CONEXION_MS = 10_000
        const val ESPERA_LECTURA_MS = 20_000

        /** Un corte breve de senal no deberia tumbar la pantalla si un segundo intento la resuelve. */
        const val ESPERA_REINTENTO_MS = 600L

        const val NO_MODIFICADO = 304
    }

    /**
     * Cuerpo de una respuesta y de donde salio, que la interfaz necesita saber
     * para poder avisar que lo que se ve puede estar desactualizado.
     */
    data class Cuerpo(val texto: String, val deCache: Boolean)

    // Dispatchers.IO envuelve todo y no solo el pedido: la cache tambien toca
    // disco, y leerla en el hilo que dibuja se siente como tirones.
    suspend fun obtener(url: String): Resultado<Cuerpo> = withContext(Dispatchers.IO) {
        val guardado = cache.leer(url)

        var intento = pedirHttp(url, guardado?.etag, ESPERA_CONEXION_MS, ESPERA_LECTURA_MS)
        if (intento is Resultado.Mal) {
            // La mayoria de los fallos que se ven en el telefono son un corte
            // breve, no estar realmente sin senal. Un segundo intento corto es
            // mas barato que mostrar un error que un segundo mas tarde se hubiera
            // resuelto solo.
            delay(ESPERA_REINTENTO_MS)
            intento = pedirHttp(url, guardado?.etag, ESPERA_CONEXION_MS, ESPERA_LECTURA_MS)
        }

        when (intento) {
            is Resultado.Bien -> {
                val respuesta = intento.valor
                when {
                    // 304: lo guardado sigue vigente y no viajo ni un byte de cuerpo.
                    respuesta.codigo == NO_MODIFICADO && guardado != null -> {
                        Bitacora.detalle(ETIQUETA, "304 $url")
                        Resultado.Bien(Cuerpo(guardado.cuerpo, deCache = true))
                    }
                    respuesta.codigo in 200..299 -> {
                        cache.guardar(url, respuesta.cuerpo, respuesta.etag)
                        Resultado.Bien(Cuerpo(respuesta.cuerpo, deCache = false))
                    }
                    else -> Resultado.Mal(fallaDe(respuesta.codigo, url))
                }
            }

            is Resultado.Mal -> {
                // Sin red: lo viejo es mejor que una pantalla de error.
                if (guardado != null) {
                    Bitacora.aviso(ETIQUETA, "sin red, se usa la copia guardada de $url")
                    Resultado.Bien(Cuerpo(guardado.cuerpo, deCache = true))
                } else {
                    intento
                }
            }
        }
    }

    /** El codigo HTTP se traduce a algo con lo que la interfaz pueda decidir. */
    private fun fallaDe(codigo: Int, url: String): Falla {
        val falla = when (codigo) {
            401 -> Falla.SESION_VENCIDA
            403 -> Falla.SIN_PERMISO
            404 -> Falla.NO_ENCONTRADO
            in 500..599 -> Falla.SERVIDOR
            else -> Falla.DESCONOCIDA
        }
        Bitacora.aviso(ETIQUETA, "$codigo en $url -> $falla")
        return falla
    }
}

/** Lo que devuelve un pedido crudo, antes de que nadie decida nada con el. */
internal data class RespuestaHttp(val codigo: Int, val cuerpo: String, val etag: String?)

/**
 * Un GET de JSON, una vez por plataforma.
 *
 * Devuelve `Resultado.Mal(SIN_RED)` para todo lo que sea un problema de
 * conexion —nombre que no resuelve, TLS, timeout— porque quien llama lo trata
 * igual: cae a la copia guardada. Un codigo HTTP de error no es un fallo aca:
 * viaja como `Bien` con su codigo, y es `Http` quien decide que significa.
 */
internal expect suspend fun pedirHttp(
    url: String,
    etag: String?,
    esperaConexionMs: Int,
    esperaLecturaMs: Int,
): Resultado<RespuestaHttp>
