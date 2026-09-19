package net.caaguazu.turismo.core

/**
 * Milisegundos de epoca a `AAAAMMDDTHHMMSSZ`, el formato de fecha de un `.ics`.
 *
 * Lo usa iOS, que no tiene una intencion de sistema para agendar y lo resuelve
 * armando un archivo de calendario. Vive aca, en codigo compartido, por una
 * razon practica: es aritmetica pura y facil de equivocar —los años bisiestos,
 * el mes que se pasa de largo, el resto negativo antes de 1970— y aca se puede
 * probar en la JVM en un segundo, mientras que en `iosMain` la unica forma de
 * comprobarla seria una vuelta entera de CI en un Mac.
 *
 * Sin `NSDateFormatter` ni `SimpleDateFormat` a proposito: los dos toman la
 * zona y el calendario del telefono. En un telefono con calendario budista, un
 * formateador escribe el año 2569 y el `.ics` sale con una fecha que ningun
 * calendario puede leer. La cuenta de dias desde 1970 no depende de nada de eso.
 */
fun enFormatoIcs(milisegundos: Long): String {
    // El resto de una division con negativos en Kotlin sale negativo, asi que
    // una fecha anterior a 1970 daria una hora negativa. Se normaliza: un dia
    // menos y el resto hacia arriba.
    var dias = milisegundos / MS_POR_DIA
    var resto = milisegundos % MS_POR_DIA
    if (resto < 0) {
        resto += MS_POR_DIA
        dias -= 1
    }

    val segundosDelDia = (resto / 1000L).toInt()
    val hora = segundosDelDia / 3600
    val minuto = (segundosDelDia % 3600) / 60
    val segundo = segundosDelDia % 60

    var anio = 1970
    var restantes = dias
    if (restantes >= 0) {
        while (true) {
            val largo = if (bisiesto(anio)) 366L else 365L
            if (restantes < largo) break
            restantes -= largo
            anio += 1
        }
    } else {
        while (restantes < 0) {
            anio -= 1
            restantes += if (bisiesto(anio)) 366L else 365L
        }
    }

    val largos = largosDeMes(anio)
    var mes = 0
    while (restantes >= largos[mes]) {
        restantes -= largos[mes]
        mes += 1
    }
    val dia = restantes.toInt() + 1

    return buildString(16) {
        append(cuatro(anio))
        append(dos(mes + 1))
        append(dos(dia))
        append('T')
        append(dos(hora))
        append(dos(minuto))
        append(dos(segundo))
        append('Z')
    }
}

private const val MS_POR_DIA = 86_400_000L

private fun bisiesto(anio: Int) = (anio % 4 == 0 && anio % 100 != 0) || anio % 400 == 0

private fun largosDeMes(anio: Int) = intArrayOf(
    31, if (bisiesto(anio)) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31,
)

private fun dos(valor: Int) = if (valor < 10) "0$valor" else valor.toString()

private fun cuatro(valor: Int) = valor.toString().padStart(4, '0')
