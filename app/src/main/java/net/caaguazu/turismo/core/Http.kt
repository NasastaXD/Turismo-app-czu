package net.caaguazu.turismo.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * Cliente HTTP de la app.
 *
 * Sin libreria de red: para GET y POST de JSON con ETag, lo que trae Android
 * alcanza, y una dependencia menos son menos kilobytes y una superficie menos que
 * mantener.
 *
 * Toda respuesta pasa por la cache de disco. Eso es lo que hace que la app siga
 * mostrando contenido cuando no hay senal, que en un distrito de 942 km es la
 * situacion normal y no la excepcion.
 */
class Http(private val cache: Cache) {

    private companion object {
        const val ETIQUETA = "Http"
        const val ESPERA_CONEXION = 10_000
        const val ESPERA_LECTURA = 20_000

        /** Un corte breve de senal no deberia tumbar la pantalla si un segundo intento la resuelve. */
        const val ESPERA_REINTENTO_MS = 600L
    }

    /**
     * Cuerpo de una respuesta y de donde salio, que la interfaz necesita saber
     * para poder avisar que lo que se ve puede estar desactualizado.
     */
    data class Cuerpo(val texto: String, val deCache: Boolean)

    suspend fun obtener(url: String): Resultado<Cuerpo> = withContext(Dispatchers.IO) {
        val guardado = cache.leer(url)

        var intento = pedir(url, guardado?.etag)
        if (intento is Resultado.Mal) {
            // La mayoria de los fallos que se ven en el telefono son un corte
            // breve, no estar realmente sin senal. Un segundo intento corto es
            // mas barato que mostrar un error que un segundo mas tarde se hubiera
            // resuelto solo.
            delay(ESPERA_REINTENTO_MS)
            intento = pedir(url, guardado?.etag)
        }

        when (intento) {
            is Resultado.Bien -> {
                val respuesta = intento.valor
                when {
                    // 304: lo guardado sigue vigente y no viajo ni un byte de cuerpo.
                    respuesta.codigo == HttpURLConnection.HTTP_NOT_MODIFIED && guardado != null -> {
                        Registro.detalle(ETIQUETA, "304 $url")
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
                    Registro.aviso(ETIQUETA, "sin red, se usa la copia guardada de $url")
                    Resultado.Bien(Cuerpo(guardado.cuerpo, deCache = true))
                } else {
                    intento
                }
            }
        }
    }

    private data class Respuesta(val codigo: Int, val cuerpo: String, val etag: String?)

    private fun pedir(url: String, etag: String?): Resultado<Respuesta> {
        var conexion: HttpURLConnection? = null
        return try {
            conexion = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = ESPERA_CONEXION
                readTimeout = ESPERA_LECTURA
                setRequestProperty("Accept", "application/json")
                if (etag != null) setRequestProperty("If-None-Match", etag)
            }

            val codigo = conexion.responseCode
            val cuerpo = when {
                codigo == HttpURLConnection.HTTP_NOT_MODIFIED -> ""
                codigo in 200..299 -> conexion.inputStream.bufferedReader().use { it.readText() }
                else -> conexion.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }
            Resultado.Bien(Respuesta(codigo, cuerpo, conexion.getHeaderField("ETag")))
        } catch (e: UnknownHostException) {
            Registro.aviso(ETIQUETA, "sin resolucion de nombre para $url")
            Resultado.Mal(Falla.SIN_RED)
        } catch (e: SSLException) {
            Registro.fallo(ETIQUETA, "fallo de TLS en $url", e)
            Resultado.Mal(Falla.SIN_RED)
        } catch (e: IOException) {
            Registro.aviso(ETIQUETA, "fallo de red en $url: ${e.message}")
            Resultado.Mal(Falla.SIN_RED)
        } catch (e: Throwable) {
            Registro.fallo(ETIQUETA, "fallo inesperado pidiendo $url", e)
            Resultado.Mal(Falla.DESCONOCIDA)
        } finally {
            conexion?.disconnect()
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
        Registro.aviso(ETIQUETA, "$codigo en $url -> $falla")
        return falla
    }
}
