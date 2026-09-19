package net.caaguazu.turismo.core

/**
 * Resultado explicito. Las operaciones que pueden fallar lo devuelven en vez de lanzar,
 * para que quien llama este obligado a contemplar el fallo.
 *
 * Vive aca y no en :app porque es el idioma de fallos de todo el proyecto, y el lado
 * de iOS tiene los mismos fallos que atender. El paquete no cambio: para :app esto
 * sigue estando donde estaba.
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
