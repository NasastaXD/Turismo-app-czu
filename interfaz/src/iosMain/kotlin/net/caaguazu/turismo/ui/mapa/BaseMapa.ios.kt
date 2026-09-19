package net.caaguazu.turismo.ui.mapa

import kotlinx.cinterop.ExperimentalForeignApi
import net.caaguazu.turismo.core.Bitacora
import net.caaguazu.turismo.core.Falla
import net.caaguazu.turismo.core.Resultado
import net.caaguazu.turismo.datos.Encuadre
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfFile

private const val ETIQUETA = "BaseMapaIos"
private const val NOMBRE_TILES = "caaguazu"
private const val TIPO_TILES = "pmtiles"
private const val CARPETA = "map"
private const val NOMBRE_ESTILO = "estilo"
private const val TIPO_ESTILO = "json"

/**
 * El prefijo con el que el estilo referencia los glifos del lado Android.
 * `asset://` es un esquema que solo entiende MapLibre en Android; en iOS hay
 * que dejar una ruta del bundle. Se reemplaza aca y no con una segunda marca en
 * `estilo.json` a proposito: tocar ese archivo obligaria a cambiar tambien el
 * lado Android, cuyo renderizado no se puede comprobar en este entorno. Cuando
 * haya con que verificarlo, lo limpio es una marca `__RUTA_GLIFOS__` simetrica
 * a la de los tiles.
 */
private const val PREFIJO_GLIFOS_ANDROID = "asset://map/"

/**
 * En iOS el bundle ya son archivos reales, asi que PMTiles puede hacer lectura
 * por posicion ahi mismo y no hay ninguna copia que hacer. Es mas simple que en
 * Android por esa unica razon.
 *
 * `suspend` sin cambiar de hilo: son unos kilobytes del estilo y una busqueda en
 * el bundle. Lo que en Android cuesta —los 2 MB del .pmtiles— aca no pasa.
 */
@OptIn(ExperimentalForeignApi::class)
actual suspend fun estiloDelMapa(): Resultado<String> {
    val bundle = NSBundle.mainBundle

    val rutaTiles = bundle.pathForResource(NOMBRE_TILES, TIPO_TILES, CARPETA)
        ?: return sinRecurso("$NOMBRE_TILES.$TIPO_TILES")
    val rutaEstilo = bundle.pathForResource(NOMBRE_ESTILO, TIPO_ESTILO, CARPETA)
        ?: return sinRecurso("$NOMBRE_ESTILO.$TIPO_ESTILO")

    val crudo = leerTexto(rutaEstilo) ?: run {
        Bitacora.fallo(ETIQUETA, "no se pudo leer el estilo del bundle")
        return Resultado.Mal(Falla.DATOS_INVALIDOS)
    }

    // Sin la marca, sustituir no haria nada y el mapa quedaria con el fondo
    // plano y ninguna capa encima: es un fallo, no un estilo sin tiles.
    if (!crudo.contains(Encuadre.MARCA_RUTA)) {
        Bitacora.fallo(ETIQUETA, "el estilo no tiene la marca ${Encuadre.MARCA_RUTA}")
        return Resultado.Mal(Falla.DATOS_INVALIDOS)
    }

    // "pmtiles://" exige una URL completa detras, no una ruta pelada. Es la
    // misma regla que en Android y la razon por la que ahi faltaba el esquema y
    // el mapa no cargaba.
    val carpetaMapa = rutaTiles.substringBeforeLast('/')
    val conTiles = crudo.replace(Encuadre.MARCA_RUTA, "file://$rutaTiles")
    val conGlifos = conTiles.replace(PREFIJO_GLIFOS_ANDROID, "file://$carpetaMapa/")

    return Resultado.Bien(conGlifos)
}

private fun sinRecurso(nombre: String): Resultado<String> {
    Bitacora.fallo(ETIQUETA, "falta $nombre en el bundle: la carpeta map/ no viajo")
    return Resultado.Mal(Falla.NO_ENCONTRADO)
}

/**
 * Lee un archivo del bundle como texto.
 *
 * Via NSData y no con `stringWithContentsOfFile`, que esta deprecado y cuyo
 * mapeo a Kotlin obliga a manejar punteros para el NSError.
 */
@OptIn(ExperimentalForeignApi::class)
private fun leerTexto(ruta: String): String? {
    val datos: NSData = NSData.dataWithContentsOfFile(ruta) ?: return null
    return NSString.create(data = datos, encoding = NSUTF8StringEncoding) as String?
}
