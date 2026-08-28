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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.ui.Funciones
import net.caaguazu.turismo.ui.Seccion
import net.caaguazu.turismo.ui.tema.Elevacion
import net.caaguazu.turismo.ui.tema.Letra
import net.caaguazu.turismo.ui.tema.Radio
import net.caaguazu.turismo.ui.tema.Tono

/**
 * Barra de navegacion inferior.
 *
 * La seccion activa se marca rellenando su icono con un disco de contraste, que
 * es la señal mas fuerte que da el sistema sin gastar un color de marca. Las
 * inactivas quedan como glifo suelto, sin etiqueta: cuatro palabras chicas
 * siempre encendidas convertian la barra en un parrafo.
 *
 * La etiqueta si aparece bajo la activa. Sacarlas todas se veia mejor y dejaba
 * al usuario adivinando donde esta parado — para un publico que en buena parte
 * es gente mayor, saber donde uno esta vale mas que la linea limpia.
 *
 * La barra flota: papel, esquinas superiores redondeadas y sombra hacia arriba,
 * sin la linea de 1px que antes la pegaba al contenido.
 */
@Composable
fun BarraInferior(
    seleccionada: () -> Seccion,
    alElegir: (Seccion) -> Unit,
    modifier: Modifier = Modifier,
) {
    val forma = RoundedCornerShape(Radio.hoja)
    Box(
        modifier
            .fillMaxWidth()
            .shadow(Elevacion.flotante, forma, ambientColor = Tono.sombra, spotColor = Tono.sombra)
            .clip(forma)
            .background(Tono.papel),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(top = 12.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Top,
        ) {
            val secciones = Seccion.entries
            if (Funciones.IA_ACTIVA) {
                secciones.take(2).forEach { Boton(it, seleccionada, alElegir) }
                BotonCentral()
                secciones.drop(2).forEach { Boton(it, seleccionada, alElegir) }
            } else {
                secciones.forEach { Boton(it, seleccionada, alElegir) }
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
    val nombre = Textos.t(seccion.clave)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { alElegir(seccion) },
            )
            .padding(horizontal = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (activa) Tono.contraste else Tono.papel),
            contentAlignment = Alignment.Center,
        ) {
            Glifo(
                icono = seccion.icono,
                descripcion = nombre,
                color = if (activa) Tono.sobreContraste else Tono.tintaSuave,
                modifier = Modifier.size(23.dp),
            )
        }
        // La etiqueta solo bajo la activa. El hueco de las demas no se reserva:
        // la barra queda mas baja y la fila sigue alineada porque todas las
        // columnas arrancan arriba.
        if (activa) {
            Texto(
                texto = nombre,
                estilo = Letra.etiquetaNav,
                color = Tono.tinta,
                maxLineas = 1,
                alinear = TextAlign.Center,
            )
        } else {
            Box(Modifier.height(1.dp))
        }
    }
}

@Composable
private fun BotonCentral() {
    Box(
        modifier = Modifier
            .size(72.dp)
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
