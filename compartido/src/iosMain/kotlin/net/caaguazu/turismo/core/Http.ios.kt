package net.caaguazu.turismo.core

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSError
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSURLErrorCannotConnectToHost
import platform.Foundation.NSURLErrorCannotFindHost
import platform.Foundation.NSURLErrorNetworkConnectionLost
import platform.Foundation.NSURLErrorNotConnectedToInternet
import platform.Foundation.NSURLErrorSecureConnectionFailed
import platform.Foundation.NSURLErrorTimedOut
import platform.Foundation.NSURLSession
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataTaskWithRequest
import platform.Foundation.setHTTPMethod
import platform.Foundation.setValue
import kotlin.coroutines.resume

/**
 * La version de iOS, sobre NSURLSession.
 *
 * NSURLSession ya es asincrono, asi que no hay nada que sacar de un hilo: lo
 * unico que hace falta es puentear su callback a una funcion suspend, y para
 * eso esta `suspendCancellableCoroutine`. Si quien llama se cancela —una
 * pantalla que se cierra mientras carga— la tarea se cancela con ella, en lugar
 * de seguir descargando para nadie.
 *
 * Esto es la alternativa a sumar Ktor. Son estas treinta lineas contra una
 * dependencia mas, en un proyecto que las cuenta: el ETag, el reintento y la
 * caida a la copia guardada quedaron compartidos, asi que lo unico duplicado es
 * abrir la conexion y leer la respuesta.
 */
internal actual suspend fun pedirHttp(
    url: String,
    etag: String?,
    esperaConexionMs: Int,
    esperaLecturaMs: Int,
): Resultado<RespuestaHttp> = suspendCancellableCoroutine { continuacion ->

    val destino = NSURL(string = url)
    val solicitud = NSMutableURLRequest.requestWithURL(destino)
    solicitud.setHTTPMethod("GET")
    solicitud.setValue("application/json", forHTTPHeaderField = "Accept")
    if (etag != null) solicitud.setValue(etag, forHTTPHeaderField = "If-None-Match")

    // NSURLSession tiene un solo timeout por pedido, no uno de conexion y otro
    // de lectura como HttpURLConnection. Se usa el de lectura, que es el mayor:
    // usar el de conexion cortaria descargas legitimamente lentas.
    //
    // Es una propiedad y no un setter: `setTimeoutInterval(...)` no resuelve.
    solicitud.timeoutInterval = esperaLecturaMs / 1000.0

    val tarea = NSURLSession.sharedSession.dataTaskWithRequest(solicitud) { datos, respuesta, error ->
        if (!continuacion.isActive) return@dataTaskWithRequest

        if (error != null) {
            continuacion.resume(fallaDeNSError(error, url))
            return@dataTaskWithRequest
        }

        val http = respuesta as? NSHTTPURLResponse
        if (http == null) {
            Bitacora.fallo(Http.ETIQUETA, "respuesta sin cabecera HTTP en $url")
            continuacion.resume(Resultado.Mal(Falla.DESCONOCIDA))
            return@dataTaskWithRequest
        }

        val codigo = http.statusCode.toInt()
        // En un 304 no viaja cuerpo, igual que del lado Android.
        val cuerpo = if (codigo == Http.NO_MODIFICADO || datos == null) {
            ""
        } else {
            (NSString.create(data = datos, encoding = NSUTF8StringEncoding) as String?).orEmpty()
        }

        // valueForHTTPHeaderField y no allHeaderFields: las cabeceras HTTP no
        // distinguen mayusculas pero ese diccionario si, asi que buscar "ETag"
        // a mano se perderia un "Etag".
        val etagRecibido = http.valueForHTTPHeaderField("ETag")

        continuacion.resume(Resultado.Bien(RespuestaHttp(codigo, cuerpo, etagRecibido)))
    }

    continuacion.invokeOnCancellation { tarea.cancel() }
    tarea.resume()
}

/**
 * Los codigos de NSURLSession que son "no hay red", para que quien llama caiga
 * a la copia guardada igual que en Android.
 */
private fun fallaDeNSError(error: NSError, url: String): Resultado<RespuestaHttp> {
    val sinRed = error.code in listOf(
        NSURLErrorNotConnectedToInternet,
        NSURLErrorCannotFindHost,
        NSURLErrorCannotConnectToHost,
        NSURLErrorNetworkConnectionLost,
        NSURLErrorTimedOut,
        NSURLErrorSecureConnectionFailed,
    )

    return if (sinRed) {
        Bitacora.aviso(Http.ETIQUETA, "fallo de red en $url: ${error.localizedDescription}")
        Resultado.Mal(Falla.SIN_RED)
    } else {
        Bitacora.fallo(Http.ETIQUETA, "fallo inesperado pidiendo $url: ${error.localizedDescription}")
        Resultado.Mal(Falla.DESCONOCIDA)
    }
}
