package net.caaguazu.turismo.ui.mapa

import android.content.Context
import net.caaguazu.turismo.core.Falla
import net.caaguazu.turismo.core.Registro
import net.caaguazu.turismo.core.Resultado
import net.caaguazu.turismo.core.intentar
import java.io.File

/**
 * El mapa base vive dentro del APK y se lee desde disco. No hay servidor de tiles,
 * ni clave de API, ni cuenta: el mapa funciona en modo avion y va a seguir
 * funcionando aunque manana desaparezca cualquier servicio.
 *
 * El archivo se copia de los assets al almacenamiento privado porque PMTiles necesita
 * lectura por posicion sobre un archivo real.
 */
object BaseMapa {

    private const val ETIQUETA = "BaseMapa"
    private const val ORIGEN = "map/caaguazu.pmtiles"
    private const val DESTINO = "caaguazu.pmtiles"
    private const val ESTILO = "map/estilo.json"
    private const val MARCA_RUTA = "__RUTA_PMTILES__"

    /** Centro de Caaguazu y encuadre util, derivados del recorte que lleva el APK. */
    const val LAT_CENTRO = -25.4730
    const val LON_CENTRO = -56.0224
    const val ZOOM_INICIAL = 13.5
    const val ZOOM_MIN = 9.0
    const val ZOOM_MAX = 18.0

    /**
     * Deja el archivo disponible en disco y devuelve su ruta.
     * Si ya esta y el tamano coincide, no vuelve a copiarlo.
     */
    fun asegurarArchivo(contexto: Context): Resultado<File> {
        val destino = File(contexto.filesDir, DESTINO)

        val tamanoOrigen = intentar(ETIQUETA, "medir el mapa embebido") {
            contexto.assets.openFd(ORIGEN).use { it.length }
        }

        if (tamanoOrigen is Resultado.Bien && destino.exists() && destino.length() == tamanoOrigen.valor) {
            Registro.detalle(ETIQUETA, "mapa ya presente (${destino.length()} bytes)")
            return Resultado.Bien(destino)
        }

        val copiado = intentar(ETIQUETA, "copiar el mapa a disco") {
            contexto.assets.open(ORIGEN).use { entrada ->
                destino.outputStream().use { salida -> entrada.copyTo(salida, 64 * 1024) }
            }
            destino
        }

        return when (copiado) {
            is Resultado.Bien -> {
                Registro.info(ETIQUETA, "mapa copiado, ${destino.length()} bytes")
                copiado
            }
            is Resultado.Mal -> {
                // Sin mapa base la pantalla no puede dibujar nada util: se avisa arriba.
                Registro.fallo(ETIQUETA, "no se pudo dejar el mapa en disco")
                Resultado.Mal(Falla.DATOS_INVALIDOS)
            }
        }
    }

    /** Estilo con la ruta real del archivo ya sustituida. */
    fun estilo(contexto: Context, archivo: File): Resultado<String> {
        val leido = intentar(ETIQUETA, "leer $ESTILO") {
            contexto.assets.open(ESTILO).bufferedReader().use { it.readText() }
        }
        return when (leido) {
            is Resultado.Bien -> {
                if (!leido.valor.contains(MARCA_RUTA)) {
                    Registro.fallo(ETIQUETA, "el estilo no tiene la marca $MARCA_RUTA")
                    Resultado.Mal(Falla.DATOS_INVALIDOS)
                } else {
                    // "pmtiles://" exige una URL completa detras, no una ruta pelada
                    // (asi como un pmtiles remoto va "pmtiles://https://..."). Sin el
                    // esquema file://, MapLibre no resuelve el archivo y el mapa
                    // queda con el fondo plano, sin ninguna capa encima.
                    Resultado.Bien(leido.valor.replace(MARCA_RUTA, "file://" + archivo.absolutePath))
                }
            }
            is Resultado.Mal -> Resultado.Mal(Falla.DATOS_INVALIDOS)
        }
    }
}
