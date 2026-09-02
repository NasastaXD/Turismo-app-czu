package net.caaguazu.turismo.datos

import net.caaguazu.turismo.core.Textos

/**
 * El nombre para mostrar de una categoria, zona o etiqueta.
 *
 * No se traducen con la ficha, y es a proposito: "Sitio Natural" o "con niños"
 * no son contenido de un lugar sino del sistema, y los comparten cientos de
 * fichas. Traducirlos por ficha seria traducir lo mismo una vez por cada lugar,
 * con el resultado previsible de que terminen dichos de cuatro maneras.
 *
 * Viajan por `/strings/{idioma}`, el mismo canal que los textos de la interfaz,
 * y se buscan por su slug. Si el panel todavia no los cargo —hoy no— queda el
 * nombre que vino en el dato, que es el castellano. Nunca un hueco.
 */
fun Categoria.nombreVisible(): String = Textos.opcional(slug) ?: nombre

fun Termino.nombreVisible(): String = Textos.opcional(slug) ?: nombre

fun Etiqueta.nombreVisible(): String = Textos.opcional(slug) ?: nombre

fun Zona.nombreVisible(): String = Textos.opcional(slug) ?: nombre
