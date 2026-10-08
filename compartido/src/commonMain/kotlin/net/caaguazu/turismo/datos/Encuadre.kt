package net.caaguazu.turismo.datos

/**
 * El encuadre del mapa, derivado del recorte que viaja embebido.
 *
 * Vive aca porque las dos plataformas dibujan el mismo territorio: si el
 * recorte cambia, este es el unico lugar donde se corrige. Antes estaba
 * duplicado en el lado Android nada mas, y una segunda copia en iOS se habria
 * desviado de la primera sin que nadie se enterara.
 */
object Encuadre {
    const val LAT_CENTRO = -25.4730
    const val LON_CENTRO = -56.0224
    const val ZOOM_INICIAL = 13.5
    const val ZOOM_MIN = 9.0
    const val ZOOM_MAX = 18.0

    /** La marca que el estilo lleva donde va la ruta real del archivo de tiles. */
    const val MARCA_RUTA = "__RUTA_PMTILES__"
}
