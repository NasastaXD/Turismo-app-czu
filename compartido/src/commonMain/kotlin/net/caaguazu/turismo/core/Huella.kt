package net.caaguazu.turismo.core

/**
 * La huella de una URL, para usarla de nombre de archivo en la cache.
 *
 * Antes esto era un SHA-256 de `java.security.MessageDigest`, que no existe en
 * Kotlin/Native. Se reemplaza por FNV-1a propio en lugar de por un expect/actual
 * con dos criptografias distintas, porque aca no hace falta criptografia: el
 * nombre de un archivo de cache no protege nada y nunca cruza de un telefono a
 * otro, asi que tampoco necesita coincidir entre plataformas.
 *
 * Lo que si hace falta es que dos URL distintas no caigan en el mismo archivo,
 * porque eso serviria una respuesta por otra. Por eso son dos pasadas con
 * semillas distintas —128 bits en total, no 64—: con un solo FNV de 64 bits la
 * probabilidad de choque es baja pero no despreciable, y el modo de falla seria
 * silencioso y muy raro de diagnosticar.
 *
 * Costo de haber cambiado el algoritmo: al actualizar, la cache que ya estaba
 * en disco queda inalcanzable y se descarga de nuevo, una sola vez. Sobre algo
 * que el proyecto define como descartable por definicion, es aceptable.
 */
fun huellaDeUrl(url: String): String {
    val bytes = url.encodeToByteArray()
    return hex(fnv1a(bytes, BASE_A)) + hex(fnv1a(bytes, BASE_B))
}

// Las dos semillas: la primera es el offset basis estandar de FNV-1a de 64
// bits; la segunda es arbitraria y solo tiene que ser distinta de la primera.
//
// Van escritas como Long con signo y no como `0x...uL.toLong()`, que no es
// constante de compilacion y no compila en un `const val`. El hexadecimal
// queda al lado para que se pueda reconocer de donde salen.
private const val BASE_A = -3750763034362895579L // 0xcbf29ce484222325
private const val BASE_B = -7046029254386353131L // 0x9e3779b97f4a7c15, proporcion aurea
private const val PRIMO = 0x100000001B3L

private fun fnv1a(bytes: ByteArray, base: Long): Long {
    var huella = base
    for (byte in bytes) {
        huella = huella xor (byte.toLong() and 0xFF)
        huella *= PRIMO
    }
    return mezclar(huella)
}

/**
 * El final de splitmix64, para que cambiar un byte cambie toda la huella.
 *
 * Hace falta: FNV-1a solo no difunde: recorre los bytes en orden y el ultimo
 * multiplicador no vuelve sobre lo ya mezclado, asi que dos URL que difieren
 * en el ultimo caracter salen con el principio identico. Lo encontro una
 * prueba de este mismo archivo, no una sospecha. Ademas de quedar prolijo, un
 * hash que difunde bien reparte mejor y deja menos margen de colision.
 */
private fun mezclar(valor: Long): Long {
    var x = valor
    x = x xor (x ushr 30)
    x *= -4658895280553007687L // 0xbf58476d1ce4e5b9
    x = x xor (x ushr 27)
    x *= -7723592293110705685L // 0x94d049bb133111eb
    return x xor (x ushr 31)
}

/** 16 digitos hexadecimales, siempre: un nombre de archivo de largo fijo. */
private fun hex(valor: Long): String {
    val digitos = "0123456789abcdef"
    val salida = StringBuilder(16)
    for (desplazamiento in 60 downTo 0 step 4) {
        salida.append(digitos[((valor ushr desplazamiento) and 0xF).toInt()])
    }
    return salida.toString()
}
