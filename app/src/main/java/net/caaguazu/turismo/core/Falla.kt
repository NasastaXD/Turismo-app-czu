package net.caaguazu.turismo.core

/**
 * Resultado explicito. Las operaciones que pueden fallar lo devuelven en vez de lanzar,
 * para que quien llama este obligado a contemplar el fallo.
 */
sealed interface Resultado<out T> {
    data class Bien<T>(val valor: T) : Resultado<T>
    data class Mal(val falla: Falla) : Resultado<Nothing>
}

/** El motivo del fallo, en terminos que la interfaz pueda usar para decidir que mostrar. */
enum class Falla {
    SIN_RED,
    SERVIDOR,
    DATOS_INVALIDOS,
    NO_ENCONTRADO,
    SIN_PERMISO,
    SESION_VENCIDA,
    DESCONOCIDA,
}

inline fun <T, R> Resultado<T>.mapear(transformar: (T) -> R): Resultado<R> = when (this) {
    is Resultado.Bien -> Resultado.Bien(transformar(valor))
    is Resultado.Mal -> this
}

fun <T> Resultado<T>.oNulo(): T? = (this as? Resultado.Bien)?.valor

/**
 * Envuelve algo que puede lanzar y lo convierte en resultado, dejando constancia.
 * Es el unico lugar de la app donde se atrapa una excepcion generica.
 */
inline fun <T> intentar(etiqueta: String, que: String, bloque: () -> T): Resultado<T> =
    try {
        Resultado.Bien(bloque())
    } catch (e: Throwable) {
        Registro.fallo(etiqueta, "fallo $que", e)
        Resultado.Mal(Falla.DESCONOCIDA)
    }
