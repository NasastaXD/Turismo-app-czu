package net.caaguazu.turismo

import net.caaguazu.turismo.core.enFormatoIcs
import net.caaguazu.turismo.core.isoEnMilisegundos
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * El parser de fechas del panel.
 *
 * De esto dependen dos cosas que tienen que dar lo mismo en los dos telefonos:
 * si el boton de agendar se dibuja, y si un evento entra en la ventana de dos
 * dias en la que corresponde avisar. Un parser equivocado no se ve como un
 * error: se ve como un aviso que nunca llego, o como una fiesta agendada con
 * seis horas de diferencia.
 *
 * Los milisegundos esperados estan calculados aparte, no con esta funcion.
 *
 * Los nombres de las pruebas de `commonTest` **no llevan coma ni punto**:
 * Kotlin/Native los rechaza —"Name contains illegal characters"— aunque la JVM
 * los acepte sin chistar. Se descubre recien al compilar para iOS, que es una
 * vuelta de CI en un Mac; no vale la pena gastarla dos veces por lo mismo.
 */
class FechaIsoTest {

    @Test
    fun `las tres formas que manda el panel dan el mismo instante`() {
        val esperado = 1_789_077_600_000L
        assertEquals(esperado, isoEnMilisegundos("2026-09-10T22:00:00+00:00"))
        assertEquals(esperado, isoEnMilisegundos("2026-09-10T22:00:00Z"))
        // Sin zona se lee como UTC, que es como el panel las guarda.
        assertEquals(esperado, isoEnMilisegundos("2026-09-10T22:00:00"))
    }

    @Test
    fun `el desplazamiento se resta y no se suma`() {
        // 22:00 en Paraguay (UTC-3) es mas tarde en UTC, no mas temprano. Si el
        // signo estuviera invertido, esto daria 1789066800000 y todo evento
        // quedaria corrido seis horas.
        assertEquals(1_789_088_400_000L, isoEnMilisegundos("2026-09-10T22:00:00-03:00"))
        assertEquals(1_789_088_400_000L, isoEnMilisegundos("2026-09-10T22:00:00-0300"))
    }

    @Test
    fun `un bisiesto y un año divisible por cuatrocientos`() {
        assertEquals(1_709_209_845_000L, isoEnMilisegundos("2024-02-29T12:30:45Z"))
        assertEquals(951_782_400_000L, isoEnMilisegundos("2000-02-29T00:00:00Z"))
    }

    @Test
    fun `la fraccion de segundo se acepta y se descarta`() {
        // La app no la usa. Rechazar la fecha entera por unos milisegundos
        // seria perder el evento por nada.
        assertEquals(1_789_077_600_000L, isoEnMilisegundos("2026-09-10T22:00:00.500Z"))
        assertEquals(1_789_077_600_000L, isoEnMilisegundos("2026-09-10T22:00:00,5+00:00"))
    }

    @Test
    fun `lo que no es una fecha devuelve null y no una fecha inventada`() {
        assertNull(isoEnMilisegundos(null))
        assertNull(isoEnMilisegundos(""))
        assertNull(isoEnMilisegundos("   "))
        assertNull(isoEnMilisegundos("proximamente"))
        assertNull(isoEnMilisegundos("2026-09-10"))
        assertNull(isoEnMilisegundos("10/09/2026 22:00"))
        assertNull(isoEnMilisegundos("2026-09-10T22:00"))
    }

    @Test
    fun `estricto como lo era con isLenient false`() {
        // Un 31 de febrero no se corre al primero de marzo: no es una fecha.
        assertNull(isoEnMilisegundos("2026-02-31T00:00:00Z"))
        assertNull(isoEnMilisegundos("2026-13-01T00:00:00Z"))
        assertNull(isoEnMilisegundos("2026-00-01T00:00:00Z"))
        assertNull(isoEnMilisegundos("2026-09-00T00:00:00Z"))
        assertNull(isoEnMilisegundos("2026-09-10T24:00:00Z"))
        assertNull(isoEnMilisegundos("2026-09-10T22:60:00Z"))
        // 2026 no es bisiesto.
        assertNull(isoEnMilisegundos("2026-02-29T00:00:00Z"))
    }

    @Test
    fun `una zona que no existe no pasa`() {
        assertNull(isoEnMilisegundos("2026-09-10T22:00:00+99:00"))
        assertNull(isoEnMilisegundos("2026-09-10T22:00:00ARG"))
        assertNull(isoEnMilisegundos("2026-09-10T22:00:00+3:00"))
    }

    @Test
    fun `leer y volver a escribir da la misma fecha`() {
        // La vuelta completa: el parser y el formateador del `.ics` son inversos
        // uno del otro, asi que si los dos estan bien, esto cierra. Y si uno se
        // rompe, esto lo dice sin que haga falta otro juego de valores.
        for (texto in listOf(
            "2026-09-10T22:00:00Z",
            "1999-12-31T23:59:59Z",
            "2024-02-29T00:00:00Z",
            "2100-03-01T12:00:00Z",
            "1970-01-01T00:00:00Z",
        )) {
            val ms = isoEnMilisegundos(texto)
            assertEquals(
                texto.replace("-", "").replace(":", ""),
                enFormatoIcs(ms!!),
                "no cerro la vuelta para $texto",
            )
        }
    }
}
