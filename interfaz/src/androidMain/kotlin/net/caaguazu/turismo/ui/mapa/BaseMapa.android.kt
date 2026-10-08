package net.caaguazu.turismo.ui.mapa

import kotlinx.coroutines.withContext
import net.caaguazu.turismo.core.ArranqueAndroid
import net.caaguazu.turismo.core.Bitacora
import net.caaguazu.turismo.core.Entorno
import net.caaguazu.turismo.core.Falla
import net.caaguazu.turismo.core.Resultado
import net.caaguazu.turismo.core.despachadorIo
import net.caaguazu.turismo.core.intentarCompartido
import net.caaguazu.turismo.datos.Encuadre
import java.io.File

private const val ETIQUETA = "BaseMapa"
private const val ORIGEN = "map/caaguazu.pmtiles"
private const val DESTINO = "caaguazu.pmtiles"
private const val ESTILO = "map/estilo.json"

/**
 * En Android el `.pmtiles` hay que copiarlo a disco: dentro del APK no es un
 * archivo, y PMTiles necesita lectura por posicion sobre uno real. Si ya esta y
 * el tamaño coincide, no se vuelve a copiar.
 */
actual suspend fun estiloDelMapa(): Resultado<String> = withContext(despachadorIo) {
    when (val archivo = asegurarArchivo()) {
        is Resultado.Mal -> archivo
        is Resultado.Bien -> leerEstilo(archivo.valor)
    }
}

private fun asegurarArchivo(): Resultado<File> {
    val assets = ArranqueAndroid.contexto.assets
    val destino = File(Entorno.carpetaDatos, DESTINO)

    val tamanoOrigen = intentarCompartido(ETIQUETA, "medir el mapa embebido") {
        assets.openFd(ORIGEN).use { it.length }
    }

    if (tamanoOrigen is Resultado.Bien && destino.exists() && destino.length() == tamanoOrigen.valor) {
        Bitacora.detalle(ETIQUETA, "mapa ya presente (${destino.length()} bytes)")
        return Resultado.Bien(destino)
    }

    val copiado = intentarCompartido(ETIQUETA, "copiar el mapa a disco") {
        assets.open(ORIGEN).use { entrada ->
            destino.outputStream().use { salida -> entrada.copyTo(salida, 64 * 1024) }
        }
        destino
    }

    return when (copiado) {
        is Resultado.Bien -> {
            Bitacora.info(ETIQUETA, "mapa copiado, ${destino.length()} bytes")
            copiado
        }
        is Resultado.Mal -> {
            // Sin mapa base la pantalla no puede dibujar nada util: se avisa arriba.
            Bitacora.fallo(ETIQUETA, "no se pudo dejar el mapa en disco")
            Resultado.Mal(Falla.DATOS_INVALIDOS)
        }
    }
}

private fun leerEstilo(archivo: File): Resultado<String> {
    val leido = intentarCompartido(ETIQUETA, "leer $ESTILO") {
        ArranqueAndroid.contexto.assets.open(ESTILO).bufferedReader().use { it.readText() }
    }
    return when (leido) {
        is Resultado.Mal -> Resultado.Mal(Falla.DATOS_INVALIDOS)
        is Resultado.Bien -> {
            if (!leido.valor.contains(Encuadre.MARCA_RUTA)) {
                Bitacora.fallo(ETIQUETA, "el estilo no tiene la marca ${Encuadre.MARCA_RUTA}")
                Resultado.Mal(Falla.DATOS_INVALIDOS)
            } else {
                // "pmtiles://" exige una URL completa detras, no una ruta pelada
                // (asi como un pmtiles remoto va "pmtiles://https://..."). Sin el
                // esquema file://, MapLibre no resuelve el archivo y el mapa
                // queda con el fondo plano, sin ninguna capa encima.
                Resultado.Bien(
                    leido.valor.replace(Encuadre.MARCA_RUTA, "file://" + archivo.absolutePath),
                )
            }
        }
    }
}
