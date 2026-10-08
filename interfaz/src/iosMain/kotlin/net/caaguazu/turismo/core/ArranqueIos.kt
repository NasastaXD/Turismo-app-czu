package net.caaguazu.turismo.core

import platform.Foundation.NSBundle
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.UIKit.UIViewController

/**
 * Lo que la cascara de iOS rellena antes de dibujar.
 *
 * Contraparte de `ArranqueAndroid`, y como esa, sin `expect`: cada plataforma
 * arranca con lo que solo ella sabe. Aca son las carpetas del sandbox y el
 * `Info.plist`.
 *
 * Tambien guarda el controlador raiz, y no es un detalle: presentar una hoja
 * de compartir en iOS exige un controlador desde el que presentarla. La
 * alternativa era buscarlo en las escenas conectadas cada vez —API que cambio
 * dos veces y que devuelve null en los momentos menos convenientes— cuando el
 * punto de entrada lo tiene en la mano.
 */
object ArranqueIos {

    private const val CLAVE_URL_BASE = "URL_BASE"
    private const val CLAVE_VERSION = "CFBundleShortVersionString"

    /**
     * Desde donde se presenta la hoja de compartir. Lo pone `puntoDeEntrada`
     * con el controlador que le entrega a Swift.
     */
    var raiz: UIViewController? = null

    fun iniciar() {
        Entorno.carpetaDatos = carpetaDeDocumentos()
        Entorno.carpetaCache = carpetaDeCache()
        // Con la barra final siempre, que es lo que espera quien concatena la
        // ruta del endpoint. El Info.plist la puede traer o no.
        Entorno.urlBase = delPlist(CLAVE_URL_BASE).orEmpty().let {
            if (it.isBlank() || it.endsWith("/")) it else "$it/"
        }
        Entorno.version = delPlist(CLAVE_VERSION).orEmpty()

        if (Entorno.urlBase.isBlank()) {
            // Sin URL base no hay contenido, solo el mapa embebido. Queda
            // anotado en lugar de caerse: es un error de empaquetado —falta la
            // clave en el Info.plist— y la app tiene que poder decirlo.
            Bitacora.fallo("ArranqueIos", "falta $CLAVE_URL_BASE en el Info.plist")
        }
    }

    /**
     * Documentos para lo que dura, Caches para lo descartable. Es la separacion
     * que iOS espera: el sistema puede vaciar Caches cuando le hace falta
     * espacio, y es justo lo que la cache de la API tolera.
     *
     * Las dos van con argumentos nombrados y sin un helper que reciba el tipo
     * del directorio: ese tipo es un typealias de las bindings y escribirlo en
     * una firma es justo el detalle que cambia entre versiones de Kotlin.
     */
    private fun carpetaDeDocumentos(): String {
        val carpetas = NSSearchPathForDirectoriesInDomains(
            directory = NSDocumentDirectory,
            domainMask = NSUserDomainMask,
            expandTilde = true,
        )
        return carpetas.firstOrNull() as? String ?: "/tmp"
    }

    private fun carpetaDeCache(): String {
        val carpetas = NSSearchPathForDirectoriesInDomains(
            directory = NSCachesDirectory,
            domainMask = NSUserDomainMask,
            expandTilde = true,
        )
        val raiz = carpetas.firstOrNull() as? String ?: "/tmp"
        return "$raiz/api"
    }

    private fun delPlist(clave: String): String? =
        NSBundle.mainBundle.objectForInfoDictionaryKey(clave) as? String
}
