package net.caaguazu.turismo.ui

import androidx.compose.runtime.Composable

/**
 * iOS no tiene un gesto de volver del sistema que una app pueda interceptar, y
 * tampoco un "cerrar la app" que se le pueda pedir: cerrar una app la cierra la
 * persona, por el sistema. Asi que aca no hay nada que manejar.
 *
 * La navegacion hacia atras existe igual: es el boton de la cabecera, que ya
 * esta en la ficha, en el perfil, en el articulo y en el recorrido — o sea en
 * todo lo que se abre encima de una seccion.
 */
@Composable
actual fun ManejarVolver(alVolver: () -> Boolean) {
    // Deliberadamente vacio. Ver el comentario de arriba.
}

/** Sin avisos en esta version de iOS, no hay permiso que pedir. */
@Composable
actual fun PedirAvisosAlArrancar() {
    // Deliberadamente vacio.
}
