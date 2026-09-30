package net.caaguazu.turismo.ui.perfil

import androidx.compose.runtime.Composable

/**
 * Si esta plataforma tiene avisos.
 *
 * En Android si: `WorkManager` garantiza que la revision periodica corra
 * sobreviviendo a Doze y al reinicio del telefono. En iOS el equivalente,
 * `BGAppRefreshTask`, es explicitamente *best-effort* —el sistema decide si
 * corre y cuando, segun cuanto se use la app—, asi que en la practica los
 * avisos llegarian tarde o no llegarian.
 *
 * La decision tomada fue sacarlos de la primera version de iOS: sale sin
 * avisos y sin el interruptor, antes que con un interruptor que promete algo
 * que no puede cumplir. Esto es lo que hace que la pantalla de perfil no
 * dibuje una fila que no sirve, en lugar de dibujarla apagada.
 */
expect val avisosDisponibles: Boolean

/**
 * El interruptor de avisos, donde existan.
 *
 * Toda la parte visible es identica a cualquier otra fila de la pantalla; lo
 * que no se puede compartir es el comportamiento —programar el trabajo
 * periodico y pedir el permiso del sistema— y por eso la fila entera vive en
 * cada plataforma.
 */
@Composable
expect fun FilaDeAvisos()
