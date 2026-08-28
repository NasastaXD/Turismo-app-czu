package net.caaguazu.turismo.ui.piezas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import net.caaguazu.turismo.ui.tema.AnimacionesActivas
import net.caaguazu.turismo.ui.tema.Elevacion
import net.caaguazu.turismo.ui.tema.Letra
import net.caaguazu.turismo.ui.tema.Medida
import net.caaguazu.turismo.ui.tema.Movimiento
import net.caaguazu.turismo.ui.tema.Radio
import net.caaguazu.turismo.ui.tema.Tono

/**
 * Hoja que sube desde abajo.
 *
 * Es la pieza que cambia la forma de la app en esta version: los filtros y el
 * lugar tocado en el mapa dejan de ser pantallas a las que se va y vuelve, y
 * pasan a ser una capa que se levanta sobre lo que ya estaba. La diferencia no
 * es estetica — quien filtra quiere ver como cambia lo que tiene detras, y en
 * una pantalla aparte eso es imposible.
 *
 * El velo de atras cierra al tocarlo. Es el gesto que la gente ya prueba sola
 * antes de buscar un boton de cerrar.
 */
@Composable
fun BoxScope.HojaInferior(
    visible: Boolean,
    alCerrar: () -> Unit,
    modifier: Modifier = Modifier,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    val animar = AnimacionesActivas.current

    if (visible) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Tono.velo)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = alCerrar,
                ),
        )
    }

    val forma = RoundedCornerShape(topStart = Radio.hoja, topEnd = Radio.hoja)
    val cuerpo: @Composable () -> Unit = {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .shadow(Elevacion.flotante, forma, ambientColor = Tono.sombra, spotColor = Tono.sombra)
                .clip(forma)
                .background(Tono.papel)
                // La hoja no propaga el toque: tocar dentro no puede cerrarla.
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                )
                .navigationBarsPadding()
                .padding(bottom = Medida.entreTarjetas),
            content = contenido,
        )
    }

    Box(Modifier.align(Alignment.BottomCenter)) {
        if (animar) {
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(Movimiento.entrada()) { alto -> alto } +
                    fadeIn(Movimiento.entrada()),
                exit = slideOutVertically(Movimiento.cruce()) { alto -> alto } +
                    fadeOut(Movimiento.cruce()),
            ) {
                cuerpo()
            }
        } else if (visible) {
            cuerpo()
        }
    }
}

/**
 * El tirador de la hoja: la barrita de arriba que dice que esto se arrastra.
 *
 * No arrastra todavia —la hoja se cierra por el velo o por su boton— pero la
 * marca va igual, porque es lo que hace que se lea como una hoja y no como una
 * tarjeta pegada abajo.
 */
@Composable
fun Tirador(modifier: Modifier = Modifier) {
    Box(
        modifier
            .padding(top = 10.dp, bottom = 4.dp)
            .size(width = 40.dp, height = 4.dp)
            .clip(RoundedCornerShape(Radio.completo))
            .background(Tono.linea),
    )
}

/**
 * Cabecera de una hoja: titulo a la izquierda y una accion de texto a la
 * derecha —limpiar, cancelar—, como el "CLEAR ALL" de la referencia.
 */
@Composable
fun CabeceraHoja(
    titulo: String,
    modifier: Modifier = Modifier,
    accion: String? = null,
    alTocarAccion: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Medida.margen)
            .padding(top = 6.dp, bottom = Medida.entreTarjetas),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Texto(titulo, Letra.tituloSeccion, Tono.tinta, maxLineas = 1)
        if (accion != null && alTocarAccion != null) {
            Texto(
                texto = accion,
                estilo = Letra.enlace,
                color = Tono.tintaSuave,
                maxLineas = 1,
                modifier = Modifier.clickable(onClick = alTocarAccion).padding(start = 12.dp),
            )
        }
    }
}

/**
 * Interruptor de dos o mas caras: pildora contenedora y el segmento elegido
 * relleno de contraste.
 *
 * Reemplaza a los chips que hacian de pestañas. Un chip dice "esto se puede
 * apagar"; un segmento dice "una de estas dos, siempre", que es lo que pasa
 * cuando se elige entre mis recorridos y los del equipo.
 */
@Composable
fun SegmentoPildora(
    opciones: List<String>,
    elegida: Int,
    alElegir: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(Radio.completo))
            .background(Tono.campo)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        opciones.forEachIndexed { indice, texto ->
            val activo = indice == elegida
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(Radio.completo))
                    .background(if (activo) Tono.contraste else Tono.campo)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { alElegir(indice) },
                    )
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Texto(
                    texto = texto,
                    estilo = Letra.chip,
                    color = if (activo) Tono.sobreContraste else Tono.tintaSuave,
                    maxLineas = 1,
                )
            }
        }
    }
}

/**
 * Barra flotante sobre un mapa: la busqueda y los controles viajan encima del
 * lienzo en vez de empujarlo hacia abajo.
 *
 * Es lo que permite que el mapa ocupe la pantalla entera. Antes el mapa era el
 * ultimo tercio de una columna con cabecera, buscador, chips y contador; en un
 * telefono eso dejaba un mapa del tamaño de una estampilla.
 */
@Composable
fun BoxScope.BarraFlotanteMapa(
    modifier: Modifier = Modifier,
    contenido: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = Medida.margen, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        content = contenido,
    )
}
