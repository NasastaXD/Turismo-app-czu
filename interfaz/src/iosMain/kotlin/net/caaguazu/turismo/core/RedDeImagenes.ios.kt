package net.caaguazu.turismo.core

import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.SingletonImageLoader
import coil3.network.NetworkClient
import coil3.network.NetworkFetcher
import coil3.network.NetworkHeaders
import coil3.network.NetworkRequest
import coil3.network.NetworkResponse
import coil3.network.NetworkResponseBody
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import okio.Buffer
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionConfiguration
import platform.Foundation.dataTaskWithRequest
import platform.Foundation.setHTTPMethod
import platform.Foundation.setValue
import platform.Foundation.timeIntervalSince1970
import platform.posix.memcpy
import kotlin.coroutines.resume

private const val ETIQUETA = "Imagenes"

/**
 * El motor de red de Coil en iOS, sobre NSURLSession.
 *
 * Coil 3 es multiplataforma pero no trae motor de red propio: en Android usa
 * OkHttp y para iOS lo esperado seria Ktor con su motor Darwin, o sea **dos
 * dependencias nuevas** para bajar fotos. Este archivo las reemplaza, y la
 * razon de que sea posible es que la interfaz que hay que implementar es un
 * solo metodo — leido del artefacto, no supuesto.
 *
 * Es el mismo criterio que llevo a resolver `Http` con NSURLSession en vez de
 * Ktor: cincuenta lineas propias contra una dependencia mas, en un proyecto
 * cuya regla explicita es no agregar una dependencia que se pueda evitar.
 *
 * La cache de imagenes en disco la sigue manejando Coil, que ya la trae: esto
 * es solo el transporte.
 */
@OptIn(ExperimentalForeignApi::class)
internal class RedDeImagenes : NetworkClient {

    override suspend fun <T> executeRequest(
        request: NetworkRequest,
        block: suspend (response: NetworkResponse) -> T,
    ): T {
        val inicio = ahoraMs()
        val respuesta = pedir(request)
        val fin = ahoraMs()

        // El cuerpo se entrega como un buffer de okio, que es lo que Coil sabe
        // leer. Okio ya viene con Coil, asi que no suma nada.
        val cuerpo = Buffer().apply { write(respuesta.bytes) }

        return block(
            NetworkResponse(
                code = respuesta.codigo,
                requestMillis = inicio,
                responseMillis = fin,
                headers = respuesta.cabeceras,
                body = NetworkResponseBody(cuerpo),
            ),
        ).also { cuerpo.close() }
    }

    private class Respuesta(
        val codigo: Int,
        val bytes: ByteArray,
        val cabeceras: NetworkHeaders,
    )

    private suspend fun pedir(request: NetworkRequest): Respuesta =
        suspendCancellableCoroutine { continuacion ->
            val destino = NSURL.URLWithString(request.url)
            if (destino == null) {
                // Una URL que iOS no entiende no es un fallo de red: se
                // responde 400 para que Coil la descarte como cualquier otra
                // respuesta mala, en vez de lanzar desde dentro de la corrutina
                // —que en Kotlin/Native termina el proceso, no la corrutina—.
                Bitacora.aviso(ETIQUETA, "URL que iOS no entiende: ${request.url}")
                continuacion.resume(Respuesta(400, ByteArray(0), NetworkHeaders.EMPTY))
                return@suspendCancellableCoroutine
            }

            val pedido = NSMutableURLRequest.requestWithURL(destino)
            pedido.setHTTPMethod(request.method)
            request.headers.asMap().forEach { (clave, valores) ->
                valores.forEach { valor -> pedido.setValue(valor, forHTTPHeaderField = clave) }
            }

            val tarea = sesionDeImagenes.dataTaskWithRequest(pedido) { datos, respuesta, error ->
                if (error != null) {
                    Bitacora.detalle(ETIQUETA, "fallo al bajar ${request.url}: ${error.localizedDescription}")
                    // 0 es "no hubo respuesta". Coil lo trata como fallo y deja
                    // el hueco de la imagen, que es lo correcto sin red.
                    continuacion.resume(Respuesta(0, ByteArray(0), NetworkHeaders.EMPTY))
                    return@dataTaskWithRequest
                }
                val http = respuesta as? NSHTTPURLResponse
                continuacion.resume(
                    Respuesta(
                        codigo = http?.statusCode?.toInt() ?: 0,
                        bytes = datos?.comoBytes() ?: ByteArray(0),
                        cabeceras = http.comoCabeceras(),
                    ),
                )
            }

            // Si quien pidio la imagen se va —una lista que se desplaza rapido—
            // la descarga se corta en lugar de seguir para nadie.
            continuacion.invokeOnCancellation { tarea.cancel() }
            tarea.resume()
        }
}

/**
 * Una sola sesion para las imagenes, separada de la de la API.
 *
 * Separada a proposito: las fotos son mas pesadas y mas tolerantes a la espera
 * que una respuesta JSON, y compartir la sesion significaria compartir los
 * timeouts. Que una foto lenta no haga esperar a la ficha entera.
 */
private val sesionDeImagenes: NSURLSession by lazy {
    val configuracion = NSURLSessionConfiguration.defaultSessionConfiguration
    configuracion.timeoutIntervalForRequest = 20.0
    configuracion.timeoutIntervalForResource = 90.0
    NSURLSession.sessionWithConfiguration(configuracion)
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.comoBytes(): ByteArray {
    val largo = length.toInt()
    if (largo == 0) return ByteArray(0)
    val destino = ByteArray(largo)
    // Hay que fijar el arreglo para que el recolector no lo mueva mientras
    // memcpy escribe adentro.
    destino.usePinned { fijado -> memcpy(fijado.addressOf(0), bytes, length) }
    return destino
}

private fun NSHTTPURLResponse?.comoCabeceras(): NetworkHeaders {
    if (this == null) return NetworkHeaders.EMPTY
    val constructor = NetworkHeaders.Builder()
    allHeaderFields.forEach { (clave, valor) ->
        val nombre = clave as? String ?: return@forEach
        val contenido = valor as? String ?: return@forEach
        constructor[nombre] = contenido
    }
    return constructor.build()
}

private fun ahoraMs(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()

/**
 * Deja el motor puesto antes de que se dibuje la primera imagen.
 *
 * `setSafe` y no `setUnsafe`: si el cargador ya existiera, reemplazarlo a mitad
 * de camino tiraria la cache en memoria. Lo llama `puntoDeEntrada` antes de
 * componer nada, y por eso es publica: la cascara vive en otro modulo.
 */
@OptIn(ExperimentalCoilApi::class)
fun configurarImagenes() {
    SingletonImageLoader.setSafe { contexto ->
        ImageLoader.Builder(contexto)
            .components { add(NetworkFetcher.Factory(networkClient = { RedDeImagenes() })) }
            .build()
    }
}
