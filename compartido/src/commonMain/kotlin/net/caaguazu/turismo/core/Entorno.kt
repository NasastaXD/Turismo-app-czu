package net.caaguazu.turismo.core

/**
 * Los tres datos que el codigo compartido no puede averiguar solo.
 *
 * Donde guardar lo que dura, donde guardar lo descartable, y con que servidor
 * habla la app. Los tres los sabe cada plataforma por su cuenta y de formas que
 * no se parecen en nada: en Android salen de un `Context` y de un
 * `buildConfigField`, en iOS de `NSSearchPathForDirectoriesInDomains` y del
 * `Info.plist`.
 *
 * Se rellena una vez al arrancar, igual que `Bitacora.destino`, y por la misma
 * razon: un `expect`/`actual` por cada dato obligaria a que el `actual` de
 * Android reciba un `Context`, y entonces el `Context` termina viajando por
 * firmas de codigo compartido hasta llegar aca. Con un punto de enganche, la
 * cascara de cada plataforma lo resuelve a su manera y nadie mas se entera.
 *
 * `lateinit` a proposito: si alguien lee esto antes de que la cascara lo
 * rellene, tiene que fallar fuerte y en el arranque, no devolver una ruta vacia
 * que despues aparece como una cache que no guarda nada.
 */
object Entorno {

    /** Lo que sobrevive: favoritos, el recorrido a medio armar, el mapa base. */
    lateinit var carpetaDatos: String

    /** Lo descartable. La cache de la API vive en una subcarpeta de aca. */
    lateinit var carpetaCache: String

    /** Raiz de la API, con la barra final. Nunca quemada en el codigo. */
    lateinit var urlBase: String

    /**
     * La version que se muestra en el perfil y en diagnostico.
     *
     * Cada plataforma la lleva en su propio sitio —`BuildConfig.VERSION_NAME`
     * en Android, `CFBundleShortVersionString` en iOS— y las dos son cadenas.
     * Que la pantalla las lea del mismo lugar es lo que permite que la pantalla
     * sea una sola.
     */
    lateinit var version: String

    /** Para que la cascara pueda comprobar que lo lleno todo antes de dibujar. */
    val listo: Boolean
        get() = ::carpetaDatos.isInitialized &&
            ::carpetaCache.isInitialized &&
            ::urlBase.isInitialized &&
            ::version.isInitialized
}
