package net.caaguazu.turismo.core

/**
 * Enganche para programar y cancelar la revision periodica de avisos.
 *
 * Existe para que :interfaz no dependa de WorkManager ni de :app. El
 * `Vigilante` vive en :app —es un `CoroutineWorker`, o sea algo que solo
 * Android tiene— y el interruptor de la pantalla de perfil vive aca. En lugar
 * de invertir la dependencia o de mover WorkManager a este modulo, :app rellena
 * estas dos funciones al arrancar.
 *
 * Es el mismo patron que `Bitacora.destino` y por la misma razon: dos
 * propiedades de funcion en vez de una interfaz con una sola implementacion.
 *
 * Sin rellenar, el interruptor cambia la preferencia y no programa nada. Eso es
 * un error de arranque de :app, no un estado valido.
 */
object TrabajoDeAvisos {

    var programar: (() -> Unit)? = null
    var cancelar: (() -> Unit)? = null
}
