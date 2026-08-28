package net.caaguazu.turismo.ui.piezas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.ui.Funciones
import net.caaguazu.turismo.ui.Seccion
import net.caaguazu.turismo.ui.tema.Elevacion
import net.caaguazu.turismo.ui.tema.Letra
import net.caaguazu.turismo.ui.tema.Tono

/**
 * Barra de navegacion inferior.
 *
 * El estado activo se marca solo con peso y opacidad del icono: el sistema reserva el
 * acento para metadatos y prohibe pildoras de fondo en la navegacion.
 *
 * El boton central es la IA, que llega al final del desarrollo. Mientras este apagado
 * la barra reparte las cuatro secciones sin dejar un hueco donde deberia estar.
 */
@Composable
fun BarraInferior(
    seleccionada: () -> Seccion,
    alElegir: (Seccion) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxWidth().background(Tono.papel)) {
        Column {
            Box(Modifier.fillMaxWidth().height(1.dp).background(Tono.linea))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(top = 10.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Top,
            ) {
                val sinIa = Seccion.entries
                if (Funciones.IA_ACTIVA) {
                    sinIa.take(2).forEach { Boton(it, seleccionada, alElegir) }
                    BotonCentral()
                    sinIa.drop(2).forEach { Boton(it, seleccionada, alElegir) }
                } else {
                    sinIa.forEach { Boton(it, seleccionada, alElegir) }
                }
            }
        }
    }
}

@Composable
private fun Boton(
    seccion: Seccion,
    seleccionada: () -> Seccion,
    alElegir: (Seccion) -> Unit,
) {
    // Lectura diferida: solo este boton se redibuja al cambiar de seccion, no la barra.
    val activa = seleccionada() == seccion

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { alElegir(seccion) },
            )
            .padding(horizontal = 4.dp)
            .alpha(if (activa) 1f else 0.45f),
    ) {
        Glifo(
            icono = seccion.icono,
            descripcion = Textos.t(seccion.clave),
            color = Tono.tinta,
            modifier = Modifier.size(26.dp),
        )
        Texto(
            texto = Textos.t(seccion.clave),
            estilo = Letra.etiquetaNav,
            color = Tono.tinta,
            maxLineas = 1,
            alinear = TextAlign.Center,
        )
        // El guion bajo la seccion activa. La opacidad sola no alcanza para
        // decir donde esta uno: es una diferencia que se pierde a plena luz.
        Box(
            Modifier
                .padding(top = 2.dp)
                .size(width = 16.dp, height = 3.dp)
                .background(
                    if (activa) Tono.contraste else Color.Transparent,
                    CircleShape,
                ),
        )
    }
}

@Composable
private fun BotonCentral() {
    Box(
        modifier = Modifier
            .size(88.dp)
            .shadow(
                Elevacion.flotante,
                CircleShape,
                ambientColor = Tono.sombra,
                spotColor = Tono.sombra,
            )
            .background(Tono.contraste, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Texto(texto = Textos.t("nav.ia"), estilo = Letra.chip, color = Tono.sobreContraste)
    }
}
