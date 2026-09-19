package net.caaguazu.turismo.ui

import androidx.compose.runtime.Composable

/**
 * El gesto de volver del sistema, donde exista.
 *
 * `alVolver` devuelve false cuando ya no queda nada que deshacer, y hay que
 * atenderlo: en Android un manejador activo se come el gesto, asi que ignorar
 * el resultado deja al inicio sin forma de cerrar la app.
 *
 * En iOS no hay un gesto de volver del sistema —lo que hay es el deslizar desde
 * el borde, que pertenece a la navegacion de UIKit y esta app no usa— asi que
 * el `actual` no hace nada. La navegacion hacia atras se hace con el boton de
 * la cabecera, que ya esta en todas las pantallas que se abren encima.
 */
@Composable
expect fun ManejarVolver(alVolver: () -> Boolean)

/**
 * El permiso de avisos, una sola vez al primer arranque.
 *
 * Los avisos arrancan encendidos, pero en Android 13 o mas nuevo eso no
 * alcanza: el sistema exige pedir el permiso. En iOS no se pide nada porque no
 * hay avisos en esta version — la razon esta en `avisosDisponibles`.
 */
@Composable
expect fun PedirAvisosAlArrancar()
