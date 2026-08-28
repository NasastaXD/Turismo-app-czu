package net.caaguazu.turismo.ui.articulos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import net.caaguazu.turismo.core.HtmlSencillo
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.datos.Articulo
import net.caaguazu.turismo.datos.Datos
import net.caaguazu.turismo.datos.Imagen
import net.caaguazu.turismo.ui.piezas.BotonFlotante
import net.caaguazu.turismo.ui.piezas.Cargador
import net.caaguazu.turismo.ui.piezas.Foto
import net.caaguazu.turismo.ui.piezas.Hairline
import net.caaguazu.turismo.ui.piezas.Icono
import net.caaguazu.turismo.ui.piezas.Tarjeta
import net.caaguazu.turismo.ui.piezas.Texto
import net.caaguazu.turismo.ui.piezas.cargar
import net.caaguazu.turismo.ui.tema.Letra
import net.caaguazu.turismo.ui.tema.Medida
import net.caaguazu.turismo.ui.tema.Radio
import net.caaguazu.turismo.ui.tema.Tono

/**
 * El articulo abierto.
 *
 * Composicion tomada de la referencia del diario: titular, bajada, firma y
 * despues el cuerpo. El cuerpo va sobre la superficie de papel, que en modo
 * oscuro es la tarjeta oscura: leer un texto largo pide un fondo parejo, no el
 * fondo de la app.
 */
@Composable
fun PantallaArticulo(id: Int, alVolver: () -> Unit, modifier: Modifier = Modifier) {
    val (estado, reintentar) = cargar(id) { Datos.api.articulo(id) }

    Box(modifier.fillMaxSize().background(Tono.papel)) {
        Cargador(estado = estado.value, reintentar = reintentar) { articulo ->
            Cuerpo(articulo)
        }
        BotonFlotante(
            icono = Icono.volver,
            descripcion = Textos.t("accion.volver"),
            alTocar = alVolver,
            modifier = Modifier.statusBarsPadding().padding(12.dp),
        )
    }
}

@Composable
private fun Cuerpo(articulo: Articulo) {
    // Interpretar el HTML es trabajo real: se hace una vez por articulo y no en
    // cada recomposicion del scroll.
    val bloques = remember(articulo.id) { HtmlSencillo.bloques(articulo.cuerpoHtml) }

    LazyColumn(contentPadding = PaddingValues(top = 64.dp, bottom = 48.dp)) {

        item {
            Column(
                modifier = Modifier.padding(horizontal = Medida.margen),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Texto(
                    texto = articulo.titulo,
                    estilo = Letra.titularArticulo,
                    color = Tono.tinta,
                )
                if (articulo.entradilla.isNotBlank()) {
                    Texto(
                        texto = articulo.entradilla,
                        estilo = Letra.bajadaArticulo,
                        color = Tono.tintaSuave,
                    )
                }
                Firma(nombreAutores(articulo.autores), articulo.publicado)
            }
        }

        item {
            Column(Modifier.padding(top = 24.dp)) {
                Foto(
                    imagen = articulo.portada,
                    descripcion = articulo.titulo,
                    modifier = Modifier.fillMaxWidth().aspectRatio(3f / 2f),
                )
                val credito = articulo.portada?.credito
                if (!credito.isNullOrBlank()) {
                    Texto(
                        texto = credito,
                        estilo = Letra.etiquetaNav,
                        color = Tono.tintaSuave,
                        modifier = Modifier.padding(horizontal = Medida.margen, vertical = 8.dp),
                    )
                }
            }
        }

        items(bloques) { bloque -> BloqueDeTexto(bloque) }

        if (articulo.relacionados.isNotEmpty()) {
            item {
                Column(Modifier.padding(top = Medida.bandaArriba)) {
                    Hairline(Modifier.fillMaxWidth().padding(horizontal = Medida.margen))
                    Texto(
                        texto = Textos.t("ficha.relacionados"),
                        estilo = Letra.tituloSeccion,
                        color = Tono.tinta,
                        modifier = Modifier.padding(Medida.margen),
                    )
                }
            }
            items(articulo.relacionados) { relacionado ->
                Tarjeta(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Medida.margen, vertical = 5.dp),
                    radio = Radio.lista,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Foto(
                            relacionado.portada,
                            relacionado.titulo,
                            Modifier.width(96.dp).aspectRatio(1f),
                        )
                        Texto(relacionado.titulo, Letra.titularTarjeta, Tono.tinta, maxLineas = 3)
                    }
                }
            }
        }
    }
}

@Composable
private fun BloqueDeTexto(bloque: HtmlSencillo.Bloque) {
    when (bloque) {
        is HtmlSencillo.Bloque.Parrafo -> Parrafo(bloque.texto, Letra.cuerpoArticulo, Tono.tinta)

        is HtmlSencillo.Bloque.Subtitulo -> Parrafo(
            bloque.texto,
            Letra.titularTarjeta,
            Tono.tinta,
            arriba = 26.dp,
        )

        is HtmlSencillo.Bloque.Punto -> Row(
            modifier = Modifier.padding(start = Medida.margen, end = Medida.margen, top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Texto("•", Letra.cuerpoArticulo, Tono.tintaSuave)
            TextoRico(bloque.texto, Letra.cuerpoArticulo, Tono.tinta)
        }

        is HtmlSencillo.Bloque.Cita -> Box(
            Modifier
                .padding(horizontal = Medida.margen, vertical = 16.dp)
                .fillMaxWidth(),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(Modifier.width(3.dp).background(Tono.acento))
                TextoRico(bloque.texto, Letra.bajadaArticulo, Tono.tinta)
            }
        }

        is HtmlSencillo.Bloque.Figura -> Column(Modifier.padding(vertical = 16.dp)) {
            Foto(
                imagen = Imagen(url = bloque.url),
                descripcion = bloque.pie,
                modifier = Modifier.fillMaxWidth().aspectRatio(3f / 2f),
            )
            if (bloque.pie.isNotBlank()) {
                Texto(
                    texto = bloque.pie,
                    estilo = Letra.etiquetaNav,
                    color = Tono.tintaSuave,
                    modifier = Modifier.padding(horizontal = Medida.margen, vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun Parrafo(
    texto: AnnotatedString,
    estilo: androidx.compose.ui.text.TextStyle,
    color: Color,
    arriba: androidx.compose.ui.unit.Dp = 14.dp,
) {
    Box(Modifier.padding(start = Medida.margen, end = Medida.margen, top = arriba)) {
        TextoRico(texto, estilo, color)
    }
}

/** Igual que Texto, pero conserva las marcas de negrita y cursiva del HTML. */
@Composable
private fun TextoRico(
    texto: AnnotatedString,
    estilo: androidx.compose.ui.text.TextStyle,
    color: Color,
) {
    androidx.compose.foundation.text.BasicText(
        text = texto,
        style = estilo.copy(color = color),
    )
}
