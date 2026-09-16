package net.caaguazu.turismo.core

/**
 * Envuelve algo que puede lanzar y lo convierte en resultado, dejando constancia.
 * Es el unico lugar de la app donde se atrapa una excepcion generica.
 *
 * Se queda en :app —y no con `Resultado`, en :compartido— porque necesita `Registro`,
 * que escribe a Logcat y a un archivo: nada de eso existe en iOS.
 */
inline fun <T> intentar(etiqueta: String, que: String, bloque: () -> T): Resultado<T> =
    try {
        Resultado.Bien(bloque())
    } catch (e: Throwable) {
        Registro.fallo(etiqueta, "fallo $que", e)
        Resultado.Mal(Falla.DESCONOCIDA)
    }
