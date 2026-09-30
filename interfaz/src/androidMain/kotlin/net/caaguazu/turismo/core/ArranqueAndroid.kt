package net.caaguazu.turismo.core

import android.content.Context
import java.io.File

/**
 * El unico sitio por el que un `Context` de Android entra al modulo compartido.
 *
 * No tiene contraparte `expect`: es a proposito. La cascara de cada plataforma
 * arranca lo suyo con la informacion que solo ella tiene —aca un `Context`, en
 * iOS el bundle— y a partir de ahi nadie mas necesita ninguna de las dos cosas.
 * Un `expect fun iniciar(contexto: Context)` habria obligado a que el lado iOS
 * declare un tipo `Context` que no existe, o a que el `Context` viaje por
 * firmas de codigo compartido.
 *
 * Lo llama `App.onCreate` una sola vez, antes de que se dibuje nada.
 */
object ArranqueAndroid {

    /**
     * El contexto de la aplicacion, que vive tanto como el proceso. No hay nada
     * que filtrar: no es el de una Activity.
     */
    internal lateinit var contexto: Context
        private set

    fun iniciar(contexto: Context, urlBase: String, version: String) {
        this.contexto = contexto.applicationContext

        Entorno.carpetaDatos = contexto.filesDir.absolutePath
        Entorno.carpetaCache = File(contexto.cacheDir, "api").absolutePath
        Entorno.urlBase = urlBase
        Entorno.version = version
    }
}
