package net.caaguazu.turismo.core

/**
 * El registro, como lo ve la pantalla de diagnostico.
 *
 * `Registro` —el de Android— escribe a Logcat y a un archivo rotativo con un
 * pool de hilos, y no cruza: es el mismo motivo por el que existe `Bitacora`.
 * Pero la pantalla que lo muestra si cruza, y necesita tres cosas de el: el
 * texto completo, borrarlo, y nada mas.
 *
 * Asi que van por enganche, igual que `Bitacora.destino`. En Android lo rellena
 * `App` apuntando al `Registro` de siempre; en iOS queda un anillo en memoria,
 * que es menos que un archivo pero es lo que hay sin construir un registro a
 * disco entero, y alcanza para ver que paso en esta corrida.
 *
 * Sin rellenar, la pantalla se dibuja vacia y el estado vacio ya esta escrito:
 * no hay forma de que esto se vea como una caida.
 */
object RegistroVisible {

    /** Todo el registro, en el orden en que se escribio. */
    var texto: (() -> String)? = null

    /** Vaciarlo. Lo ofrece la pantalla para poder reproducir un fallo limpio. */
    var borrar: (() -> Unit)? = null

    fun leer(): String = texto?.invoke().orEmpty()
}
