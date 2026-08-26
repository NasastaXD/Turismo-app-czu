package net.caaguazu.turismo.ui.mapa

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.ui.piezas.Texto
import net.caaguazu.turismo.ui.tema.Letra
import net.caaguazu.turismo.ui.tema.Sans
import net.caaguazu.turismo.ui.tema.Tono

/**
 * Atribucion de OpenStreetMap.
 *
 * La licencia ODbL obliga a mostrarla de forma visible sobre el mapa. No es decoracion
 * ni es opcional: si se quita, la app deja de cumplir la licencia de los datos.
 */
@Composable
fun AtribucionMapa(modifier: Modifier = Modifier) {
    Texto(
        texto = Textos.t("mapa.atribucion"),
        estilo = Letra.etiquetaNav.copy(fontSize = 11.sp),
        color = Tono.tintaSuave,
        modifier = modifier
            .background(Color.White.copy(alpha = 0.82f), RoundedCornerShape(999.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

/** Estado de error del mapa: nunca una pantalla en blanco sin explicacion. */
@Composable
fun MapaNoDisponible(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Texto(
            texto = Textos.t("mapa.error.titulo"),
            estilo = Letra.tituloTarjeta,
            color = Tono.tinta,
            alinear = TextAlign.Center,
        )
        Texto(
            texto = Textos.t("mapa.error.detalle"),
            estilo = Letra.descripcion.copy(fontFamily = Sans),
            color = Tono.tintaSuave,
            alinear = TextAlign.Center,
        )
    }
}
