package net.caaguazu.turismo

import net.caaguazu.turismo.core.enFormatoIcs
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * La fecha del `.ics` que iOS entrega al calendario.
 *
 * Es aritmetica hecha a mano —sin formateador, para que el calendario del
 * telefono no le cambie el año— y por eso se prueba. Cada caso de aca es uno de
 * los que se equivocan solos: el año bisiesto, el que parece bisiesto y no lo
 * es, el 29 de febrero, el dia siguiente a un 29 de febrero, y una fecha
 * anterior a 1970, donde el resto de la division sale negativo.
 *
 * Los valores esperados estan calculados aparte, no leidos de esta misma
 * funcion: si los sacara de ella, la prueba diria que la funcion hace lo que
 * hace.
 */
class FechaIcsTest {

    @Test
    fun `el comienzo de la epoca`() {
        assertEquals("19700101T000000Z", enFormatoIcs(0L))
    }

    @Test
    fun `una fecha con hora y minuto y segundo`() {
        assertEquals("20010909T014640Z", enFormatoIcs(1_000_000_000_000L))
    }

    @Test
    fun `el 29 de febrero de un año divisible por 400`() {
        // 2000 es bisiesto, aunque termine en dos ceros: la regla del 400.
        assertEquals("20000229T000000Z", enFormatoIcs(951_782_400_000L))
    }

    @Test
    fun `el 29 de febrero de un bisiesto normal`() {
        assertEquals("20040229T000000Z", enFormatoIcs(1_078_012_800_000L))
    }

    @Test
    fun `el primero de marzo de un bisiesto`() {
        // El que se corre un dia si el largo de febrero se calculo mal.
        assertEquals("20200301T000000Z", enFormatoIcs(1_583_020_800_000L))
    }

    @Test
    fun `un año divisible por cien que no es bisiesto`() {
        // 2100 NO es bisiesto. Si la regla del 400 estuviera mal escrita, esto
        // saldria como 21001231.
        assertEquals("21000101T000000Z", enFormatoIcs(4_102_444_800_000L))
    }

    @Test
    fun `una fecha anterior a 1970`() {
        // El caso del resto negativo. Sin normalizarlo, la hora sale en
        // negativo y el dia se va al año anterior.
        assertEquals("19691231T000000Z", enFormatoIcs(-86_400_000L))
    }

    @Test
    fun `un comienzo de año reciente`() {
        assertEquals("20260101T000000Z", enFormatoIcs(1_767_225_600_000L))
    }

    @Test
    fun `siempre dieciseis caracteres`() {
        // El formato es de largo fijo. Un mes o un dia sin el cero adelante
        // rompe el archivo y el calendario lo rechaza sin decir por que.
        for (ms in listOf(0L, 1_000_000_000_000L, 1_767_225_600_000L, -86_400_000L)) {
            assertEquals(16, enFormatoIcs(ms).length, "largo distinto para $ms")
        }
    }
}
