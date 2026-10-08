package net.caaguazu.turismo.ui.mapa

import net.caaguazu.turismo.core.Resultado
import net.caaguazu.turismo.datos.Encuadre

/**
 * El mapa base vive dentro de la app y se lee desde disco. No hay servidor de
 * tiles, ni clave de API, ni cuenta: el archivo de 2 MB viaja empaquetado y el
 * mapa funciona en modo avion y va a seguir funcionando aunque manana
 * desaparezca cualquier servicio.
 *
 * El encuadre es el mismo en las dos plataformas porque es el mismo territorio.
 * Los valores viven en `:compartido` y se reexportan con estos nombres para no
 * tocar a las pantallas que ya los usan.
 */
object BaseMapa {

    const val LAT_CENTRO = Encuadre.LAT_CENTRO
    const val LON_CENTRO = Encuadre.LON_CENTRO
    const val ZOOM_INICIAL = Encuadre.ZOOM_INICIAL
    const val ZOOM_MIN = Encuadre.ZOOM_MIN
    const val ZOOM_MAX = Encuadre.ZOOM_MAX
}

/**
 * El estilo con las rutas reales ya sustituidas, listo para MapLibre.
 *
 * Es `suspend` porque de los dos lados hay disco de por medio, y en Android la
 * primera vez son 2 MB saliendo de los assets: hacerlo en el hilo que dibuja se
 * siente como un tiron al abrir el mapa. Cada `actual` se encarga de irse del
 * hilo principal.
 *
 * Devuelve `Resultado` y no lanza: sin mapa base la pantalla tiene que poder
 * decidir que mostrar, que es el aviso de "mapa no disponible" y no una
 * pantalla en blanco.
 *
 * Lo que cambia entre plataformas no es el estilo, es de donde sale el archivo:
 *
 * - En **Android** hay que copiar el `.pmtiles` de los assets al almacenamiento
 *   privado, porque dentro del APK no es un archivo y PMTiles necesita lectura
 *   por posicion sobre uno real.
 * - En **iOS** el bundle ya son archivos, asi que no hay copia.
 */
expect suspend fun estiloDelMapa(): Resultado<String>
