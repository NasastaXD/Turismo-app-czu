package net.caaguazu.turismo.core

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * La version de la JVM. Es el mismo HttpURLConnection de antes de cruzar a
 * codigo compartido: sin cliente HTTP externo, porque alcanza para GET y POST
 * de JSON con ETag.
 */
internal actual suspend fun pedirHttp(
    url: String,
    etag: String?,
    esperaConexionMs: Int,
    esperaLecturaMs: Int,
): Resultado<RespuestaHttp> {
    var conexion: HttpURLConnection? = null
    return try {
        conexion = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = esperaConexionMs
            readTimeout = esperaLecturaMs
            setRequestProperty("Accept", "application/json")
            if (etag != null) setRequestProperty("If-None-Match", etag)
        }

        val codigo = conexion.responseCode
        val cuerpo = when {
            codigo == Http.NO_MODIFICADO -> ""
            codigo in 200..299 -> conexion.inputStream.bufferedReader().use { it.readText() }
            else -> conexion.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
        }
        Resultado.Bien(RespuestaHttp(codigo, cuerpo, conexion.getHeaderField("ETag")))
    } catch (e: UnknownHostException) {
        Bitacora.aviso(Http.ETIQUETA, "sin resolucion de nombre para $url")
        Resultado.Mal(Falla.SIN_RED)
    } catch (e: SSLException) {
        Bitacora.fallo(Http.ETIQUETA, "fallo de TLS en $url", e)
        Resultado.Mal(Falla.SIN_RED)
    } catch (e: IOException) {
        Bitacora.aviso(Http.ETIQUETA, "fallo de red en $url: ${e.message}")
        Resultado.Mal(Falla.SIN_RED)
    } catch (e: Throwable) {
        Bitacora.fallo(Http.ETIQUETA, "fallo inesperado pidiendo $url", e)
        Resultado.Mal(Falla.DESCONOCIDA)
    } finally {
        conexion?.disconnect()
    }
}
