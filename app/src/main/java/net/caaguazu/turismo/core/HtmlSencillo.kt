package net.caaguazu.turismo.core

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

/**
 * Convierte el HTML que manda WordPress en bloques tipografiados.
 *
 * No es un motor de HTML ni pretende serlo. Es el subconjunto que produce el
 * editor —parrafos, subtitulos, listas, negrita, cursiva, enlaces e imagenes— y
 * nada mas. Lo que no reconoce se degrada a texto, nunca se pierde.
 *
 * Se hace asi y no con un WebView por dos motivos: un WebView pesa, tarda en
 * arrancar y trae su propio motor de scroll; y el articulo tiene que verse con
 * la tipografia de la app, no con la que decida el HTML.
 */
object HtmlSencillo {

    private const val ETIQUETA = "Html"

    sealed interface Bloque {
        data class Parrafo(val texto: AnnotatedString) : Bloque
        data class Subtitulo(val texto: AnnotatedString) : Bloque
        data class Punto(val texto: AnnotatedString) : Bloque
        data class Cita(val texto: AnnotatedString) : Bloque
        data class Figura(val url: String, val pie: String) : Bloque
    }

    private val BLOQUES = Regex(
        "<(p|h[1-6]|li|blockquote|figure|img)\\b[^>]*>(.*?)</\\1>|<img\\b[^>]*>",
        setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
    )
    private val ATRIBUTO_SRC = Regex("""src\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE)
    private val ATRIBUTO_ALT = Regex("""alt\s*=\s*["']([^"']*)["']""", RegexOption.IGNORE_CASE)
    private val FUERTE = Regex("<(strong|b)\\b[^>]*>(.*?)</\\1>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
    private val CURSIVA = Regex("<(em|i)\\b[^>]*>(.*?)</\\1>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
    private val ETIQUETAS = Regex("<[^>]+>")

    fun bloques(html: String): List<Bloque> {
        if (html.isBlank()) return emptyList()

        val limpio = html
            .replace(Regex("<(script|style)\\b.*?</\\1>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)), "")

        val salida = mutableListOf<Bloque>()

        BLOQUES.findAll(limpio).forEach { encontrado ->
            val entero = encontrado.value
            val etiqueta = encontrado.groupValues[1].lowercase()
            val dentro = encontrado.groupValues.getOrElse(2) { "" }

            when {
                entero.startsWith("<img", ignoreCase = true) || etiqueta == "img" -> {
                    val url = ATRIBUTO_SRC.find(entero)?.groupValues?.get(1)
                    if (url != null) salida += Bloque.Figura(url, ATRIBUTO_ALT.find(entero)?.groupValues?.get(1).orEmpty())
                }
                etiqueta == "figure" -> {
                    val url = ATRIBUTO_SRC.find(dentro)?.groupValues?.get(1)
                    val pie = texto(dentro.substringAfter("</img>", "").ifBlank { dentro })
                    if (url != null) salida += Bloque.Figura(url, pie.text.trim())
                }
                etiqueta.startsWith("h") -> conTexto(dentro) { salida += Bloque.Subtitulo(it) }
                etiqueta == "li" -> conTexto(dentro) { salida += Bloque.Punto(it) }
                etiqueta == "blockquote" -> conTexto(dentro) { salida += Bloque.Cita(it) }
                else -> conTexto(dentro) { salida += Bloque.Parrafo(it) }
            }
        }

        // El editor puede devolver texto suelto sin envolver en <p>.
        if (salida.isEmpty()) {
            val suelto = texto(limpio)
            if (suelto.isNotBlank()) {
                Registro.detalle(ETIQUETA, "cuerpo sin bloques reconocibles, se muestra como parrafo")
                salida += Bloque.Parrafo(suelto)
            }
        }

        return salida
    }

    private inline fun conTexto(bruto: String, agregar: (AnnotatedString) -> Unit) {
        val t = texto(bruto)
        if (t.isNotBlank()) agregar(t)
    }

    /** Texto con negrita y cursiva conservadas; el resto de etiquetas se cae. */
    private fun texto(bruto: String): AnnotatedString {
        val marcas = mutableListOf<Triple<Int, Int, SpanStyle>>()

        // Se resuelven sobre el bruto y se recalculan al quitar las etiquetas,
        // porque quitar primero perderia donde empezaba y terminaba cada marca.
        var trabajo = bruto
        trabajo = aplicar(trabajo, FUERTE, SpanStyle(fontWeight = FontWeight.Bold), marcas)
        trabajo = aplicar(trabajo, CURSIVA, SpanStyle(fontStyle = FontStyle.Italic), marcas)

        val plano = entidades(ETIQUETAS.replace(trabajo, "")).trim()

        return AnnotatedString.Builder().apply {
            append(plano)
            marcas.forEach { (inicio, fin, estilo) ->
                if (inicio in 0..plano.length && fin in inicio..plano.length) {
                    addStyle(estilo, inicio, fin)
                }
            }
        }.toAnnotatedString()
    }

    /**
     * Reemplaza la etiqueta por su contenido y anota donde quedo, midiendo sobre
     * el texto ya sin etiquetas para que los indices sean los del texto final.
     */
    private fun aplicar(
        fuente: String,
        patron: Regex,
        estilo: SpanStyle,
        marcas: MutableList<Triple<Int, Int, SpanStyle>>,
    ): String {
        var resultado = fuente
        while (true) {
            val encontrado = patron.find(resultado) ?: break
            val contenido = encontrado.groupValues[2]
            val antes = ETIQUETAS.replace(resultado.substring(0, encontrado.range.first), "")
            val inicio = entidades(antes).trimStart().length
            val largo = entidades(ETIQUETAS.replace(contenido, "")).length
            marcas += Triple(inicio, inicio + largo, estilo)
            resultado = resultado.replaceRange(encontrado.range, contenido)
        }
        return resultado
    }

    private fun entidades(texto: String): String = texto
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#039;", "'")
        .replace("&hellip;", "…")
        .replace("&mdash;", "—")
        .replace("&ndash;", "–")
        .replace("&laquo;", "«")
        .replace("&raquo;", "»")
        .replace(Regex("\\s+"), " ")
}
