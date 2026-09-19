package net.caaguazu.turismo.ui.perfil

import androidx.compose.runtime.Composable

/**
 * Sin avisos en la primera version de iOS. La razon esta en el `expect`.
 *
 * Es `false` y no un interruptor apagado a proposito: un interruptor que se
 * puede encender y no hace nada es peor que no tenerlo.
 */
actual val avisosDisponibles: Boolean = false

@Composable
actual fun FilaDeAvisos() {
    // Nada. La pantalla de perfil ya pregunta por `avisosDisponibles` antes de
    // llamar aca, asi que esto no deberia dibujarse nunca; existe porque el
    // `expect` lo pide.
}
