package net.caaguazu.turismo.core

/**
 * Una fecha ISO 8601 del panel a milisegundos de epoca. Null si no se entiende.
 *
 * Propio y no `SimpleDateFormat`, que no existe en iOS, ni `NSDateFormatter`,
 * que no existe en Android. Y compartido, no uno por plataforma, porque de esta
 * funcion dependen dos decisiones que tienen que dar lo mismo en los dos
 * telefonos: si el boton de agendar se dibuja, y si un evento cae dentro de la
 * ventana de dos dias en la que corresponde avisar. Dos parsers en paralelo
 * serian dos formas de equivocarse, y la equivocacion se veria como un aviso
 * que llega en un telefono y no en el otro.
 *
 * Se aceptan las tres formas que devuelve el panel, en este orden:
 *
 *   `2026-09-10T22:00:00+00:00`   con desplazamiento, con o sin dos puntos
 *   `2026-09-10T22:00:00Z`        en UTC explicito
 *   `2026-09-10T22:00:00`         sin zona
 *
 * **Una fecha sin zona se lee como UTC**, que es como el panel las guarda.
 * Interpretarla en la zona del telefono correria el evento varias horas segun
 * donde este parado el turista, que es justo a quien no hay que confundirle la
 * hora de una fiesta patronal.
 *
 * Estricto a proposito, como lo era con `isLenient = false`: un mes 13 o un 31
 * de febrero no se corren al mes siguiente, devuelven null. Una fecha inventada
 * es peor que ninguna — con null, el boton no se dibuja; con una fecha corrida,
 * alguien llega un dia tarde.
 */
fun isoEnMilisegundos(iso: String?): Long? {
    val texto = iso?.trim().orEmpty()
    // Lo mas corto que puede ser valido: "2026-09-10T22:00:00", 19 caracteres.
    if (texto.length < 19) return null

    val anio = texto.numero(0, 4) ?: return null
    if (texto[4] != '-') return null
    val mes = texto.numero(5, 7) ?: return null
    if (texto[7] != '-') return null
    val dia = texto.numero(8, 10) ?: return null
    // El panel manda 'T'; una 't' minuscula o un espacio son variantes legales
    // del formato y no vale rechazarlas por eso.
    if (texto[10] != 'T' && texto[10] != 't' && texto[10] != ' ') return null
    val hora = texto.numero(11, 13) ?: return null
    if (texto[13] != ':') return null
    val minuto = texto.numero(14, 16) ?: return null
    if (texto[16] != ':') return null
    val segundo = texto.numero(17, 19) ?: return null

    if (mes !in 1..12) return null
    if (dia !in 1..diasDelMes(anio, mes)) return null
    // 24:00 no se acepta, ni el segundo 60 de un salto: el panel no los manda y
    // aceptarlos obligaria a decidir a que instante corresponden.
    if (hora !in 0..23 || minuto !in 0..59 || segundo !in 0..59) return null

    var resto = texto.substring(19)

    // Fraccion de segundo: se acepta y se descarta. La app no la usa para nada
    // y rechazar la fecha entera por unos milisegundos seria perder el evento.
    if (resto.startsWith(".") || resto.startsWith(",")) {
        val fin = resto.indexOfFirst { it !in '0'..'9' && it != '.' && it != ',' }
        resto = if (fin < 0) "" else resto.substring(fin)
    }

    val desplazamientoMs = when {
        resto.isEmpty() -> 0L
        resto == "Z" || resto == "z" -> 0L
        else -> desplazamiento(resto) ?: return null
    }

    val dias = diasDesdeEpoca(anio, mes, dia)
    val segundos = dias * 86_400L + hora * 3600L + minuto * 60L + segundo
    // El desplazamiento se RESTA: "22:00-03:00" es mas tarde en UTC, no mas
    // temprano. Invertir este signo es el error clasico de todo esto, y se
    // veria como un evento corrido seis horas en Paraguay.
    return (segundos - desplazamientoMs / 1000L) * 1000L
}

/** `+00:00`, `-0300`, `+03`. Null si no es ninguna de las tres. */
private fun desplazamiento(crudo: String): Long? {
    val signo = when (crudo.firstOrNull()) {
        '+' -> 1L
        '-' -> -1L
        else -> return null
    }
    val cuerpo = crudo.substring(1).replace(":", "")
    val horas: Int
    val minutos: Int
    when (cuerpo.length) {
        2 -> {
            horas = cuerpo.numero(0, 2) ?: return null
            minutos = 0
        }
        4 -> {
            horas = cuerpo.numero(0, 2) ?: return null
            minutos = cuerpo.numero(2, 4) ?: return null
        }
        else -> return null
    }
    if (horas > 18 || minutos > 59) return null
    return signo * (horas * 3600L + minutos * 60L) * 1000L
}

/**
 * Dias desde 1970-01-01, por el algoritmo de calendario civil de Hinnant.
 *
 * Corre el año para que marzo sea el primer mes, y asi el dia extra del
 * bisiesto cae siempre al final y no hay que tratarlo aparte. Es el mismo
 * algoritmo que usa la biblioteca estandar de C++ y de varias mas: no se
 * escribio a ojo.
 */
private fun diasDesdeEpoca(anio: Int, mes: Int, dia: Int): Long {
    val a = if (mes <= 2) anio - 1 else anio
    val era = (if (a >= 0) a else a - 399) / 400
    val anioDeEra = a - era * 400
    val diaDeAnio = (153 * (mes + (if (mes > 2) -3 else 9)) + 2) / 5 + dia - 1
    val diaDeEra = anioDeEra * 365 + anioDeEra / 4 - anioDeEra / 100 + diaDeAnio
    return era.toLong() * 146_097L + diaDeEra.toLong() - 719_468L
}

private fun diasDelMes(anio: Int, mes: Int): Int = when (mes) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    2 -> if ((anio % 4 == 0 && anio % 100 != 0) || anio % 400 == 0) 29 else 28
    else -> 0
}

/** Un tramo de digitos a numero. Null si hay algo que no es un digito. */
private fun String.numero(desde: Int, hasta: Int): Int? {
    if (hasta > length) return null
    var valor = 0
    for (i in desde until hasta) {
        val c = this[i]
        if (c !in '0'..'9') return null
        valor = valor * 10 + (c - '0')
    }
    return valor
}
