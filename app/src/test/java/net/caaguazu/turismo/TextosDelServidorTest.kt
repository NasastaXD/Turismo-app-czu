package net.caaguazu.turismo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Como se combinan los textos del servidor con el respaldo del APK.
 *
 * El panel puede tener cargadas solo algunas claves — de hecho, recien
 * instalado tiene cuatro. Si la app reemplazara en vez de fusionar, conectar la
 * API real dejaria sin texto a todo lo demas, incluida la atribucion de
 * OpenStreetMap, que es obligatoria por licencia.
 */
class TextosDelServidorTest {

    /** La misma regla que aplica Textos, aislada para poder probarla. */
    private fun combinar(
        embebidos: Map<String, String>,
        delServidor: Map<String, String>,
    ): Map<String, String> {
        val utiles = delServidor.filterValues { it.isNotBlank() }
        return if (utiles.isEmpty()) embebidos else embebidos + utiles
    }

    private val embebidos = mapOf(
        "app.nombre" to "Caaguazú",
        "mapa.atribucion" to "© OpenStreetMap",
        "nav.inventario" to "‹nav.inventario›",
        "nav.articulos" to "‹nav.articulos›",
    )

    @Test
    fun `un panel a medio cargar no borra lo que ya habia`() {
        // Lo que devuelve el plugin recien instalado.
        val combinado = combinar(embebidos, mapOf("nav.inventario" to "Inventario"))

        assertEquals("El servidor pisa la clave que trae", "Inventario", combinado["nav.inventario"])
        assertEquals(
            "La atribucion es obligatoria por licencia y no puede desaparecer",
            "© OpenStreetMap",
            combinado["mapa.atribucion"],
        )
        assertEquals("El nombre de la app sigue", "Caaguazú", combinado["app.nombre"])
        assertTrue("No se pierde ninguna clave", combinado.keys.containsAll(embebidos.keys))
    }

    @Test
    fun `una clave en blanco es un descuido, no una orden de borrar`() {
        val combinado = combinar(embebidos, mapOf("mapa.atribucion" to "   "))
        assertEquals("© OpenStreetMap", combinado["mapa.atribucion"])
    }

    @Test
    fun `un servidor sin nada util deja el respaldo intacto`() {
        assertEquals(embebidos, combinar(embebidos, emptyMap()))
        assertEquals(embebidos, combinar(embebidos, mapOf("a" to "", "b" to " ")))
    }
}
