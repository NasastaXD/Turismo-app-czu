package net.caaguazu.turismo.ui.inventario

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import net.caaguazu.turismo.core.Calendario
import net.caaguazu.turismo.core.Guardado
import net.caaguazu.turismo.core.HtmlSencillo
import net.caaguazu.turismo.core.MapasExternos
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.datos.Datos
import net.caaguazu.turismo.datos.Ficha
import net.caaguazu.turismo.datos.ResumenArticulo
import net.caaguazu.turismo.ui.piezas.BotonFlotante
import net.caaguazu.turismo.ui.piezas.Cargador
import net.caaguazu.turismo.ui.piezas.Foto
import net.caaguazu.turismo.ui.piezas.FotoConVelo
import net.caaguazu.turismo.ui.piezas.Hairline
import net.caaguazu.turismo.ui.piezas.Icono
import net.caaguazu.turismo.ui.piezas.PildoraContorno
import net.caaguazu.turismo.ui.piezas.PildoraPrimaria
import net.caaguazu.turismo.ui.piezas.RangoPrecio
import net.caaguazu.turismo.ui.piezas.Tarjeta
import net.caaguazu.turismo.ui.piezas.Texto
import net.caaguazu.turismo.ui.piezas.cargar
import net.caaguazu.turismo.ui.tema.Letra
import net.caaguazu.turismo.ui.tema.Medida
import net.caaguazu.turismo.ui.tema.Radio
import net.caaguazu.turismo.ui.tema.Tono

/**
 * La ficha de un atractivo.
 *
 * El orden lo fija la referencia visual: foto con el nombre encima, salir al
 * mapa arriba, informacion en el medio, y agregar al recorrido fijo abajo — la
 * accion que se quiere a mano en cualquier punto del scroll.
 *
 * Donde la referencia tenia etiquetas van los articulos relacionados.
 */
@Composable
fun PantallaFicha(id: Int, alVolver: () -> Unit, modifier: Modifier = Modifier) {
    val (estado, reintentar) = cargar(id) { Datos.api.ficha(id) }

    Box(modifier.fillMaxSize().background(Tono.fondo)) {
        Cargador(estado = estado.value, reintentar = reintentar) { ficha ->
            Contenido(ficha, alVolver)
        }
        // El boton de volver vive fuera del contenido: tiene que existir aunque
        // la ficha no haya cargado.
        BotonFlotante(
            icono = Icono.volver,
            descripcion = Textos.t("accion.volver"),
            alTocar = alVolver,
            modifier = Modifier.statusBarsPadding().padding(12.dp),
        )
    }
}

@Composable
private fun Contenido(ficha: Ficha, alVolver: () -> Unit) {
    val contexto = LocalContext.current
    val enRecorrido = Guardado.enRecorrido(ficha.id)

    // Interpretar el HTML es trabajo real: se hace una vez por ficha y no en
    // cada recomposicion del scroll.
    val cuerpo = remember(ficha.id) { HtmlSencillo.bloques(ficha.articuloHtml) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(bottom = 96.dp)) {

            item {
                Box(Modifier.fillMaxWidth().aspectRatio(4f / 3f)) {
                    FotoConVelo(ficha.portada, ficha.titulo, Modifier.fillMaxSize())
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(Medida.margen),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Texto(
                            texto = ficha.titulo,
                            estilo = Letra.tituloPagina,
                            color = Color.White,
                            maxLineas = 3,
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            ficha.zona?.let {
                                Texto(it.nombre, Letra.chip, Color.White.copy(alpha = 0.85f), maxLineas = 1)
                            }
                            RangoPrecio(ficha.practicos.rangoPrecio)
                        }
                    }
                }
            }

            // Salir al mapa y agendar: arriba, como en la referencia.
            item {
                val enlaceMapa = ficha.googleMaps
                val coordenadas = ficha.coordenadas
                val hayMapa = enlaceMapa != null || coordenadas != null
                // Agendar solo tiene sentido en un evento con fecha legible. En
                // un sitio no hay nada que poner en el calendario.
                val seAgenda = ficha.tipoItem == "evento" &&
                    Calendario.sePuedeAgendar(ficha.fechas?.inicio)

                if (hayMapa || seAgenda) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(Medida.margen),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        if (hayMapa) {
                            PildoraContorno(
                                texto = Textos.t("ficha.mapa"),
                                icono = Icono.inventario,
                                alTocar = {
                                    // El enlace del panel puede ser uno pegado a
                                    // mano, mas preciso que un pin armado solo
                                    // con lat/lng: se prefiere si viene.
                                    if (enlaceMapa != null) {
                                        MapasExternos.abrirEnlace(contexto, enlaceMapa)
                                    } else if (coordenadas != null) {
                                        MapasExternos.abrirPunto(
                                            contexto, coordenadas.lat, coordenadas.lng, ficha.titulo,
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (seAgenda) {
                            PildoraContorno(
                                texto = Textos.t("ficha.agendar"),
                                icono = Icono.calendario,
                                alTocar = {
                                    Calendario.agendar(
                                        contexto = contexto,
                                        titulo = ficha.titulo,
                                        inicioIso = ficha.fechas?.inicio,
                                        finIso = ficha.fechas?.fin,
                                        lugar = ficha.zona?.nombre,
                                        descripcion = ficha.gancho,
                                    )
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            if (ficha.gancho.isNotBlank()) {
                item {
                    Texto(
                        texto = ficha.gancho,
                        estilo = Letra.descripcion,
                        color = Tono.tintaSuave,
                        modifier = Modifier.padding(horizontal = Medida.margen, vertical = 4.dp),
                    )
                }
            }

            items(cuerpo) { bloque ->
                val texto = when (bloque) {
                    is HtmlSencillo.Bloque.Parrafo -> bloque.texto
                    is HtmlSencillo.Bloque.Subtitulo -> bloque.texto
                    is HtmlSencillo.Bloque.Punto -> bloque.texto
                    is HtmlSencillo.Bloque.Cita -> bloque.texto
                    is HtmlSencillo.Bloque.Figura -> null
                }
                if (texto != null) {
                    BasicText(
                        text = texto,
                        style = Letra.descripcion.copy(color = Tono.tintaSuave),
                        modifier = Modifier.padding(horizontal = Medida.margen, vertical = 4.dp),
                    )
                }
            }

            val practicos = datosPracticos(ficha)
            if (practicos.isNotEmpty()) {
                item { Seccion(Textos.t("ficha.info")) }
                item {
                    // Los datos van juntos en una tarjeta y no sueltos sobre el
                    // fondo: son un bloque que se lee de una, no cinco cosas.
                    Tarjeta(Modifier.fillMaxWidth().padding(horizontal = Medida.margen)) {
                        Column(Modifier.padding(vertical = 6.dp)) {
                            practicos.forEachIndexed { indice, (clave, valor) ->
                                if (indice > 0) {
                                    Hairline(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = Medida.dentroTarjeta),
                                    )
                                }
                                Dato(Textos.t(clave), valor)
                            }
                        }
                    }
                }
            }

            if (ficha.galeria.isNotEmpty()) {
                item { Seccion(Textos.t("ficha.galeria")) }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = Medida.margen),
                        horizontalArrangement = Arrangement.spacedBy(Medida.entreTarjetas),
                    ) {
                        items(ficha.galeria) { imagen ->
                            Foto(imagen, ficha.titulo, Modifier.width(240.dp).height(180.dp))
                        }
                    }
                }
            }

            // Donde la referencia tenia etiquetas.
            if (ficha.articulosRelacionados.isNotEmpty()) {
                item { Seccion(Textos.t("ficha.relacionados")) }
                items(ficha.articulosRelacionados) { articulo -> FilaRelacionado(articulo) }
            }

            if (ficha.fuentes.isNotBlank()) {
                item { Seccion(Textos.t("ficha.fuentes")) }
                item {
                    Texto(
                        texto = ficha.fuentes,
                        estilo = Letra.descripcion,
                        color = Tono.tintaSuave,
                        modifier = Modifier.padding(horizontal = Medida.margen),
                    )
                }
            }

            ficha.autor?.let { autor ->
                item {
                    Column(Modifier.padding(Medida.margen)) {
                        Hairline(Modifier.fillMaxWidth())
                        Row(
                            Modifier.padding(top = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Texto(Textos.t("ficha.autor"), Letra.chip, Tono.tintaSuave, maxLineas = 1)
                            Texto(autor.nombre, Letra.chip, Tono.tinta, maxLineas = 1)
                        }
                    }
                }
            }
        }

        // Accion principal fija: se quiere a mano en cualquier punto del scroll.
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Tono.papel)
                .padding(Medida.margen),
        ) {
            PildoraPrimaria(
                texto = Textos.t(if (enRecorrido) "ficha.quitar" else "ficha.agregar"),
                alTocar = { Guardado.alternarEnRecorrido(ficha.id) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Solo los campos que tienen algo: no se reservan huecos vacios. */
private fun datosPracticos(ficha: Ficha): List<Pair<String, String>> = buildList {
    fun mas(clave: String, valor: String) {
        if (valor.isNotBlank()) add(clave to valor)
    }
    mas("ficha.horario", ficha.practicos.horario)
    mas("ficha.costo", ficha.practicos.costo)
    mas("ficha.contacto", ficha.practicos.contacto)
    mas("ficha.camino", ficha.acceso.estadoCamino)
    mas("ficha.acceso", ficha.acceso.accesibilidad)
}

@Composable
private fun Seccion(titulo: String) {
    Texto(
        texto = titulo,
        estilo = Letra.tituloSeccion,
        color = Tono.tinta,
        modifier = Modifier.padding(
            start = Medida.margen, end = Medida.margen,
            top = Medida.bandaArriba, bottom = 12.dp,
        ),
    )
}

@Composable
private fun Dato(etiqueta: String, valor: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Medida.dentroTarjeta, vertical = 10.dp),
    ) {
        Texto(etiqueta, Letra.chip, Tono.tintaSuave, maxLineas = 1)
        Texto(valor, Letra.descripcion, Tono.tinta)
    }
}

@Composable
private fun FilaRelacionado(articulo: ResumenArticulo) {
    Tarjeta(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Medida.margen, vertical = 5.dp),
        radio = Radio.lista,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Foto(articulo.portada, articulo.titulo, Modifier.size(64.dp))
            Texto(articulo.titulo, Letra.tituloTarjeta, Tono.tinta, maxLineas = 2)
        }
    }
}

