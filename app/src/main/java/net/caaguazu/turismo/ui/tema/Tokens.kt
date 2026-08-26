package net.caaguazu.turismo.ui.tema

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Tokens del sistema "Alpine Editorial".
 *
 * La firma del sistema es la tension entre esquina viva y radio completo: toda
 * superficie de contenido va a radio 0, todo control va a radio 999. No se rompe.
 */
object Tono {
    val papel = Color(0xFFFFFFFF)      // fondo base y tarjeta de lista
    val banda = Color(0xFFF4F3F1)      // banda de seccion alterna, gris calido
    val superficie = Color(0xFFF5F5F5) // cuerpo de texto de tarjeta de carrusel
    val tinta = Color(0xFF1D1D1F)      // titulos y texto principal
    val tintaSuave = Color(0xFF55555A) // descripciones y texto secundario
    val linea = Color(0xFFE6E4E1)      // hairlines de 1px
    val acento = Color(0xFFE9503F)     // fechas, badges, iconos de accion
    val negro = Color(0xFF000000)      // navegacion, FAB, filtros, toggles
    val velo = Color(0x73000000)       // scrim sobre foto en tiles de menu
}

object Medida {
    val margen = 16.dp
    val entreTarjetas = 6.dp
    val dentroTarjeta = 16.dp
    val tituloACarrusel = 20.dp
    val bandaArriba = 32.dp
    val bandaAbajo = 40.dp

    /** El ancho de tarjeta es 43% del viewport: la tercera queda cortada por el borde. */
    const val FRACCION_TARJETA = 0.43f
}

object Radio {
    val vivo = 0.dp        // media, tarjetas de carrusel, tiles de menu
    val lista = 8.dp       // tarjetas de lista
    val completo = 999.dp  // chips, buscador, filtros, toggles, FAB
}
