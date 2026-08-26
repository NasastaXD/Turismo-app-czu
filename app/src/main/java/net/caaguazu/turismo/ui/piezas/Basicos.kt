package net.caaguazu.turismo.ui.piezas

import androidx.compose.foundation.Image
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

/**
 * Texto e iconos de la app.
 *
 * Se apoyan en foundation y no en Material3 porque el diseno es propio de punta a
 * punta: el tema de Material no se usa en ninguna pantalla, y arrastrarlo solo
 * sumaria peso al APK.
 */
@Composable
fun Texto(
    texto: String,
    estilo: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    maxLineas: Int = Int.MAX_VALUE,
    alinear: TextAlign? = null,
) {
    BasicText(
        text = texto,
        modifier = modifier,
        style = estilo.copy(color = color, textAlign = alinear ?: estilo.textAlign),
        maxLines = maxLineas,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun Glifo(
    icono: ImageVector,
    descripcion: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = rememberVectorPainter(icono),
        contentDescription = descripcion,
        colorFilter = ColorFilter.tint(color),
        modifier = modifier,
    )
}
