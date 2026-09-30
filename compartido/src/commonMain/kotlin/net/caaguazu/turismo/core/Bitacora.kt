package net.caaguazu.turismo.core

/**
 * Por donde el codigo compartido deja constancia.
 *
 * `Registro`, el de la app Android, no puede vivir aca: escribe a Logcat y a un
 * archivo rotativo con un pool de hilos, instala el capturador global de
 * excepciones y se exporta desde la pantalla de diagnostico. Nada de eso existe
 * en iOS, y portarlo entero no hacia falta para que `Http` y `Cache` puedan
 * anotar lo que les pasa.
 *
 * Asi que esto es un unico punto de enganche: una propiedad de funcion que cada
 * plataforma rellena al arrancar. En Android la rellena `App` apuntando al
 * `Registro` que ya existe —mismos archivos, misma exportacion, nada cambia—; en
 * iOS, con NSLog.
 *
 * Es una propiedad y no una interfaz a proposito: una interfaz con una sola
 * implementacion por plataforma es justo la capa ceremonial que el proyecto
 * prohibe. Sin rellenar, esto no escribe nada y no falla: que la app Android
 * olvide engancharlo se nota como un registro incompleto, nunca como una caida.
 */
object Bitacora {

    enum class Nivel { DETALLE, INFO, AVISO, FALLO }

    /**
     * Lo llama cada plataforma una sola vez al arrancar. No hay sincronizacion
     * porque se escribe antes de que nada lea, en el arranque y desde un solo
     * hilo.
     */
    var destino: ((Nivel, String, String, Throwable?) -> Unit)? = null

    fun detalle(etiqueta: String, mensaje: String) = anotar(Nivel.DETALLE, etiqueta, mensaje, null)
    fun info(etiqueta: String, mensaje: String) = anotar(Nivel.INFO, etiqueta, mensaje, null)
    fun aviso(etiqueta: String, mensaje: String) = anotar(Nivel.AVISO, etiqueta, mensaje, null)
    fun fallo(etiqueta: String, mensaje: String, causa: Throwable? = null) =
        anotar(Nivel.FALLO, etiqueta, mensaje, causa)

    private fun anotar(nivel: Nivel, etiqueta: String, mensaje: String, causa: Throwable?) {
        // Que un registro falle no puede tumbar lo que se estaba registrando.
        runCatching { destino?.invoke(nivel, etiqueta, mensaje, causa) }
    }
}

/**
 * Lo mismo que `intentar` de la app, para el codigo compartido: envuelve algo
 * que puede lanzar y lo convierte en resultado, dejando constancia.
 */
inline fun <T> intentarCompartido(
    etiqueta: String,
    que: String,
    bloque: () -> T,
): Resultado<T> =
    try {
        Resultado.Bien(bloque())
    } catch (e: Throwable) {
        Bitacora.fallo(etiqueta, "fallo $que", e)
        Resultado.Mal(Falla.DESCONOCIDA)
    }
