package net.caaguazu.turismo

import net.caaguazu.turismo.datos.FuenteAsistente
import net.caaguazu.turismo.ui.Filtros
import net.caaguazu.turismo.ui.Navegador
import net.caaguazu.turismo.ui.PilaBusqueda
import net.caaguazu.turismo.ui.RutaBusqueda
import net.caaguazu.turismo.ui.Seccion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * El gesto de volver dentro de Buscar.
 *
 * Buscar es una sola pantalla que cambia de cara, asi que filtrar no empuja
 * nada a la pila. Sin deshacer la busqueda a mano, volver se saltaba la
 * seccion entera y tiraba al inicio: se perdia de un toque el contexto que la
 * pantalla unica venia justamente a conservar.
 */
class NavegacionTest {

    @Test
    fun `volver deshace la busqueda antes de dejar la seccion`() {
        val pila = PilaBusqueda()
        pila.filtros = Filtros(categoria = 66)
        pila.consulta = "ykua"
        assertTrue("deberia estar buscando", pila.buscando)

        assertTrue("volver deberia consumir el gesto", pila.volver())
        assertFalse("la busqueda deberia haber quedado limpia", pila.buscando)
        assertEquals("", pila.consulta)
        assertEquals(Filtros(), pila.filtros)

        // Ya sin nada que deshacer, el gesto pasa de largo y lo toma el
        // armazon, que es lo que lleva al inicio.
        assertFalse("sin busqueda, volver no deberia consumir", pila.volver())
    }

    @Test
    fun `volver sale del mapa antes de tocar la busqueda`() {
        val pila = PilaBusqueda()
        pila.filtros = Filtros(etiqueta = 70)
        pila.enMapa = true

        assertTrue(pila.volver())
        assertFalse("deberia haber salido del mapa", pila.enMapa)
        assertTrue("el filtro no se toca todavia", pila.buscando)

        assertTrue(pila.volver())
        assertFalse("recien ahora se limpia el filtro", pila.buscando)
    }

    /** El orden importa: primero la hoja, despues el pin, despues la pila. */
    @Test
    fun `volver respeta el orden de las capas`() {
        val pila = PilaBusqueda()
        pila.ir(RutaBusqueda.Ficha(260))
        pila.seleccionEnMapa = 260
        pila.abrirFiltros()

        assertTrue(pila.volver())
        assertFalse("primero cierra la hoja", pila.filtrosAbiertos)

        assertTrue(pila.volver())
        assertEquals("despues suelta el pin", null, pila.seleccionEnMapa)

        assertTrue(pila.volver())
        assertTrue("por ultimo cierra la ficha", pila.actual is RutaBusqueda.Explorar)
    }

    /**
     * Una fuente abierta desde una respuesta vuelve a la charla, no a Buscar:
     * es de donde se vino. Y la charla se cierra recien despues, volviendo a
     * la seccion que estaba debajo, sin moverla.
     */
    @Test
    fun `volver desde una fuente regresa a la charla y despues a la seccion`() {
        val nav = Navegador()
        nav.ir(Seccion.ARTICULOS)
        nav.abrirAsistente()
        nav.asistente.abrir(FuenteAsistente(tipo = "recorrido", id = 5))
        nav.asistente.abrir(FuenteAsistente(tipo = "ficha", id = 12))

        assertTrue(nav.volver())
        assertEquals("primero cierra la parada", 5, nav.asistente.abierta?.id)

        assertTrue(nav.volver())
        assertEquals("despues el recorrido", null, nav.asistente.abierta)
        assertTrue("la charla sigue abierta", nav.asistenteAbierto)

        assertTrue(nav.volver())
        assertFalse("recien ahora se cierra el asistente", nav.asistenteAbierto)
        assertEquals("la seccion de debajo no se toco", Seccion.ARTICULOS, nav.seccion)
    }

    /** Cerrar el asistente no borra la charla: abrirlo de nuevo la encuentra. */
    @Test
    fun `cerrar el asistente conserva lo que se venia hablando`() {
        val nav = Navegador()
        nav.abrirAsistente()
        nav.asistente.borrador = "y manana"

        assertTrue(nav.volver())
        nav.abrirAsistente()
        assertEquals("y manana", nav.asistente.borrador)
    }
}
