package net.caaguazu.turismo.core

/**
 * Codificacion de un valor para meterlo en una consulta.
 *
 * Propio y no `java.net.URLEncoder`, que no existe en iOS. Y compartido y no
 * uno por plataforma, porque **la URL es la clave de la cache**: si Android
 * codificara un acento de una forma y iOS de otra, las dos apps guardarian la
 * misma busqueda en dos archivos distintos. Eso no rompe nada visible, pero
 * duplica el disco y hace que un fallo de cache se vea solo en un telefono.
 *
 * Es `application/x-www-form-urlencoded`, igual que `URLEncoder`: los no
 * reservados van tal cual, el espacio va como `+`, y el resto en porcentaje
 * sobre sus bytes UTF-8. El espacio como `+` y no como `%20` no es un detalle
 * de estilo: es lo que hacia la version de Android, y cambiarlo invalidaria de
 * golpe toda la cache ya guardada en los telefonos que tienen la app.
 *
 * `espacioComoMas = false` existe para un caso concreto y distinto: la etiqueta
 * de un enlace `geo:`, que no es una consulta sino texto que la app de mapas
 * muestra al lado del pin. Ahi un `+` se ve como un `+`, asi que el espacio va
 * en porcentaje.
 */
fun codificarParaUrl(valor: String, espacioComoMas: Boolean = true): String {
    val salida = StringBuilder(valor.length)
    for (byte in valor.encodeToByteArray()) {
        val caracter = byte.toInt().toChar()
        when {
            caracter in 'A'..'Z' || caracter in 'a'..'z' || caracter in '0'..'9' -> salida.append(caracter)
            caracter == '-' || caracter == '_' || caracter == '.' || caracter == '*' -> salida.append(caracter)
            caracter == ' ' && espacioComoMas -> salida.append('+')
            else -> {
                // `and 0xFF` porque Byte es con signo: sin eso, cualquier byte
                // de 0x80 arriba —o sea todo acento— saldria negativo y el hex
                // con un signo menos adelante.
                val crudo = byte.toInt() and 0xFF
                salida.append('%')
                salida.append(HEX[crudo shr 4])
                salida.append(HEX[crudo and 0x0F])
            }
        }
    }
    return salida.toString()
}

private const val HEX = "0123456789ABCDEF"
