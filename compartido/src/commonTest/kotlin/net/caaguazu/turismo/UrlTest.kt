package net.caaguazu.turismo

import net.caaguazu.turismo.core.codificarParaUrl
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * La codificacion de URL tiene que dar exactamente lo mismo que daba
 * `java.net.URLEncoder` en la version de Android.
 *
 * No es purismo: **la URL es la clave de la cache**. Un solo caracter
 * codificado distinto y la busqueda de "Caaguazú" que ya esta guardada en los
 * telefonos deja de encontrarse, y cada teléfono con la app vuelve a bajar todo
 * una vez. Los valores esperados de aca estan calculados con las reglas de
 * `URLEncoder` —no reservados tal cual, espacio como `+`, el resto en
 * porcentaje sobre UTF-8— y son los que hay que conservar.
 */
class UrlTest {

    @Test
    fun `los acentos van en porcentaje sobre sus bytes utf8`() {
        assertEquals("Caaguaz%C3%BA", codificarParaUrl("Caaguazú"))
        assertEquals("%C3%B1andut%C3%AD", codificarParaUrl("ñandutí"))
    }

    @Test
    fun `el espacio va como mas y no como porcentaje veinte`() {
        // Esto es lo que hacia URLEncoder. Cambiarlo a %20 seria mas moderno y
        // dejaria sin efecto toda la cache ya guardada.
        assertEquals("salto+cristal", codificarParaUrl("salto cristal"))
    }

    @Test
    fun `lo que separa una consulta se escapa`() {
        assertEquals("a%2Fb%3Fc%3Dd%26e", codificarParaUrl("a/b?c=d&e"))
        assertEquals("100%25", codificarParaUrl("100%"))
        assertEquals("%2B", codificarParaUrl("+"))
    }

    @Test
    fun `los no reservados pasan tal cual`() {
        assertEquals("*-._", codificarParaUrl("*-._"))
        assertEquals("Caaguazu123", codificarParaUrl("Caaguazu123"))
        assertEquals("", codificarParaUrl(""))
    }

    @Test
    fun `un byte alto no sale con signo`() {
        // El fallo clasico: Byte es con signo en Kotlin, asi que sin enmascarar
        // a 0xFF cualquier caracter de 0x80 arriba —o sea todo acento— saldria
        // como "%-3D" en lugar de "%C3".
        val codificado = codificarParaUrl("áéíóúñÑ")
        assertEquals(false, codificado.contains('-'), "salio un byte con signo: $codificado")
        assertEquals("%C3%A1%C3%A9%C3%AD%C3%B3%C3%BA%C3%B1%C3%91", codificado)
    }
}
