package net.caaguazu.turismo.ui.piezas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import net.caaguazu.turismo.ui.tema.AnimacionesActivas
import net.caaguazu.turismo.ui.tema.Movimiento
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Piezas de movimiento.
 *
 * Todas preguntan primero si el telefono tiene las animaciones activas, y si no,
 * dibujan el estado final sin transicion. Ninguna pantalla debe animar a mano:
 * asi el movimiento de la app se cambia en un solo lugar.
 */

/** Aparicion de contenido: se funde y sube apenas, como si se acomodara. */
@Composable
fun Aparece(
    visible: Boolean,
    modifier: Modifier = Modifier,
    contenido: @Composable () -> Unit,
) {
    if (!AnimacionesActivas.current) {
        if (visible) contenido()
        return
    }
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(Movimiento.entrada()) +
            slideInVertically(Movimiento.entrada()) { alto -> alto / 12 },
        exit = fadeOut(Movimiento.cruce()),
    ) {
        contenido()
    }
}

/** Cambio entre dos cosas del mismo nivel. Sin desplazamiento: solo se cruzan. */
@Composable
fun <T> Cruce(
    objetivo: T,
    modifier: Modifier = Modifier,
    contenido: @Composable (T) -> Unit,
) {
    if (!AnimacionesActivas.current) {
        contenido(objetivo)
        return
    }
    Crossfade(
        targetState = objetivo,
        modifier = modifier,
        animationSpec = Movimiento.cruce(),
        label = "cruce",
    ) { estado ->
        contenido(estado)
    }
}

/**
 * Respuesta al dedo: la superficie cede un poco al presionarla.
 *
 * Es el unico movimiento que no explica nada — confirma que el toque llego,
 * que en pantallas grandes y dedos poco precisos importa mas de lo que parece.
 */
@Composable
fun Modifier.cedeAlTocar(
    interaccion: MutableInteractionSource,
    hasta: Float = 0.97f,
): Modifier {
    if (!AnimacionesActivas.current) return this
    val presionado by interaccion.collectIsPressedAsState()
    val escala by animateFloatAsState(
        targetValue = if (presionado) hasta else 1f,
        animationSpec = Movimiento.toque(),
        label = "cede",
    )
    return this.scale(escala)
}

/** Recuerda una fuente de interaccion, para no crear una por recomposicion. */
@Composable
fun recordarInteraccion(): MutableInteractionSource = remember { MutableInteractionSource() }

/** Provee el estado de las animaciones del sistema a todo el arbol. */
@Composable
fun ConMovimientoDelSistema(activas: Boolean, contenido: @Composable () -> Unit) {
    CompositionLocalProvider(AnimacionesActivas provides activas, content = contenido)
}
