package net.caaguazu.turismo.ui.inventario

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.caaguazu.turismo.core.Calendario
import net.caaguazu.turismo.core.Guardado
import net.caaguazu.turismo.core.Idioma
import net.caaguazu.turismo.core.HtmlSencillo
import net.caaguazu.turismo.core.MapasExternos
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.datos.Datos
import net.caaguazu.turismo.datos.Ficha
import net.caaguazu.turismo.datos.nombreVisible
import net.caaguazu.turismo.datos.ResumenArticulo
import net.caaguazu.turismo.ui.articulos.fechaCorta
import net.caaguazu.turismo.ui.piezas.Badge
import net.caaguazu.turismo.ui.piezas.BarraAccion
import net.caaguazu.turismo.ui.piezas.BotonFlotante
import net.caaguazu.turismo.ui.piezas.Cargador
import net.caaguazu.turismo.ui.piezas.Corazon
import net.caaguazu.turismo.ui.piezas.Estado
import net.caaguazu.turismo.ui.piezas.FilaCompacta
import net.caaguazu.turismo.ui.piezas.Foto
import net.caaguazu.turismo.ui.piezas.Glifo
import net.caaguazu.turismo.ui.piezas.Hairline
import net.caaguazu.turismo.ui.piezas.Icono
import net.caaguazu.turismo.ui.piezas.PildoraMeta
import net.caaguazu.turismo.ui.piezas.PildoraSuave
import net.caaguazu.turismo.ui.piezas.RangoPrecio
import net.caaguazu.turismo.ui.piezas.Tarjeta
import net.caaguazu.turismo.ui.piezas.Texto
import net.caaguazu.turismo.ui.piezas.cargar
import net.caaguazu.turismo.ui.tema.Letra
import net.caaguazu.turismo.ui.tema.Medida
import net.caaguazu.turismo.ui.tema.Radio
import net.caaguazu.turismo.ui.tema.Tono

/** Cuantas lineas del cuerpo se ven antes de tener que pedir el resto. */
private const val LINEAS_PLEGADAS = 4

/** Cuatro lineas de descripcion a lo ancho de un telefono, aproximado. */
private const val LARGO_DE_CUATRO_LINEAS = 220

/** Cuanto de la foto tapa la hoja al arrancar. */
private val SOLAPE = 32.dp

/**
 * La ficha de un lugar.
 *
 * La foto queda fija al fondo, a sangre y hasta arriba de todo, y el contenido
 * sube por encima como una hoja de esquinas redondeadas que la tapa. Es la
 * forma de la referencia y no es decorativa: al desplazar, la foto se queda y
 * la hoja avanza, asi que el lugar sigue presente mientras se lee sobre el, en
 * vez de desaparecer al primer gesto.
 *
 * El titulo va en la hoja, en tinta, no sobre la foto en blanco. Un titular
 * blanco encima de una imagen depende de que la imagen sea oscura justo ahi, y
 * las fotos del destino no se eligen pensando en eso.
 */
@Composable
fun PantallaFicha(id: Int, alVolver: () -> Unit, modifier: Modifier = Modifier) {
    val (estado, reintentar) = cargar(id) { Datos.api.ficha(id) }

    Box(modifier.fillMaxSize().background(Tono.fondo)) {
        Cargador(estado = estado.value, reintentar = reintentar) { ficha ->
            Contenido(ficha)
        }

        // Volver y favorito viven fuera del contenido, y tienen que dibujarse
        // DESPUES de el.
        //
        // No es una preferencia de orden: la lista que trae la hoja ocupa la
        // pantalla entera, foto incluida, asi que cualquier control que quede
        // por debajo deja de recibir el toque. El corazon estaba dentro de la
        // foto y no se podia tocar — se veia, cambiaba de estado nunca.
        BotonFlotante(
            icono = Icono.volver,
            descripcion = Textos.t("accion.volver"),
            alTocar = alVolver,
            modifier = Modifier.statusBarsPadding().padding(Medida.entreTarjetas),
        )
        (estado.value as? Estado.Listo)?.valor?.let { ficha ->
            Corazon(
                marcado = { Guardado.esFavorito(ficha.id) },
                alTocar = { Guardado.alternarFavorito(ficha.id) },
                descripcion = ficha.titulo,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(Medida.entreTarjetas),
            )
        }
    }
}

@Composable
private fun Contenido(ficha: Ficha) {
    val contexto = LocalContext.current
    val enRecorrido = Guardado.enRecorrido(ficha.id)
    var desplegado by remember(ficha.id) { mutableStateOf(false) }

    // Interpretar el HTML es trabajo real: se hace una vez por ficha y no en
    // cada recomposicion del scroll.
    val cuerpo = remember(ficha.id) { HtmlSencillo.bloques(ficha.articuloHtml) }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val altoFoto: Dp = maxWidth * 3f / 4f

        // La foto no esta dentro de la lista: se dibuja detras y no se mueve.
        Box(Modifier.fillMaxWidth().height(altoFoto)) {
            Foto(ficha.portada, ficha.titulo, Modifier.fillMaxSize(), radio = Radio.ninguno)
            if (ficha.fechas?.enCurso == true) {
                Badge(
                    texto = Textos.t("evento.enCurso"),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = Medida.margen, bottom = SOLAPE + 14.dp),
                )
            }
        }

        LazyColumn(contentPadding = PaddingValues(bottom = 112.dp)) {

            // El hueco que deja ver la foto. Es un item de la lista para que se
            // desplace con ella: la hoja sube y la foto se queda.
            item { Box(Modifier.height(altoFoto - SOLAPE)) }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = Radio.hoja, topEnd = Radio.hoja))
                        .background(Tono.fondo)
                        .padding(top = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Texto(
                        texto = ficha.titulo,
                        estilo = Letra.tituloPagina,
                        color = Tono.tinta,
                        maxLineas = 3,
                        modifier = Modifier.padding(horizontal = Medida.margen),
                    )
                    ficha.zona?.let { zona ->
                        Row(
                            modifier = Modifier.padding(horizontal = Medida.margen),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                        ) {
                            Glifo(Icono.pin, zona.nombre, Tono.acento, Modifier.size(16.dp))
                            Texto(zona.nombre, Letra.fecha, Tono.acento, maxLineas = 1)
                        }
                    }

                    // Los metadatos como pildoras: cada dato en su capsula, en
                    // una fila que se corre. Es mas legible que una linea de
                    // textos separados por puntos, sobre todo cuando alguno
                    // falta.
                    val metadatos = metadatosDe(ficha)
                    if (metadatos.isNotEmpty()) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = Medida.margen),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(metadatos) { dato -> PildoraMeta(dato) }
                        }
                    }

                    // La caida al original es campo por campo, asi que una ficha
                    // puede venir con el titulo traducido y el cuerpo no. Se
                    // avisa en vez de esconderla: a medio traducir sigue
                    // teniendo la foto, el mapa, el horario y el precio, que es
                    // la mayor parte de para que se abre.
                    if (!ficha.traducido && ficha.idioma != Idioma.ORIGINAL) {
                        Texto(
                            texto = Textos.t("ficha.parcial"),
                            estilo = Letra.fecha,
                            color = Tono.tintaSuave,
                            modifier = Modifier.padding(horizontal = Medida.margen),
                        )
                    }
                }
            }

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
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Tono.fondo)
                            .padding(horizontal = Medida.margen)
                            .padding(top = Medida.entreTarjetas),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        if (hayMapa) {
                            PildoraSuave(
                                texto = Textos.t("ficha.mapa"),
                                icono = Icono.pin,
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
                            PildoraSuave(
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
                        color = Tono.tinta,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Tono.fondo)
                            .padding(horizontal = Medida.margen)
                            .padding(top = Medida.entreTarjetas),
                    )
                }
            }

            // El cuerpo arranca plegado. Una ficha con quince parrafos empujaba
            // la galeria y los relacionados fuera de todo alcance razonable;
            // quien quiere leerlo lo abre, y quien vino a ver donde queda no
            // tiene que pasarlo de largo.
            if (cuerpo.isNotEmpty()) {
                val visibles = if (desplegado) cuerpo else cuerpo.take(1)
                items(visibles) { bloque ->
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
                            maxLines = if (desplegado) Int.MAX_VALUE else LINEAS_PLEGADAS,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Tono.fondo)
                                .padding(horizontal = Medida.margen, vertical = 6.dp),
                        )
                    }
                }
                // El enlace solo si hay algo mas que abrir. Un "leer mas" sobre
                // un cuerpo de dos lineas no lleva a ningun lado y hace dudar
                // de que el resto de la ficha este completa.
                if (!desplegado && hayMasQueLeer(cuerpo)) {
                    item {
                        Box(Modifier.fillMaxWidth().background(Tono.fondo)) {
                            Texto(
                                texto = Textos.t("ficha.leerMas"),
                                estilo = Letra.enlace,
                                color = Tono.tinta,
                                modifier = Modifier
                                    .padding(horizontal = Medida.margen)
                                    .clickable { desplegado = true }
                                    .padding(vertical = 6.dp),
                            )
                        }
                    }
                }
            }

            val practicos = datosPracticos(ficha)
            if (practicos.isNotEmpty()) {
                item { Seccion(Textos.t("ficha.info")) }
                item {
                    // Los datos van juntos en una tarjeta y no sueltos sobre el
                    // fondo: son un bloque que se lee de una, no cinco cosas.
                    Box(Modifier.fillMaxWidth().background(Tono.fondo)) {
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
            }

            if (ficha.galeria.isNotEmpty()) {
                item { Seccion(Textos.t("ficha.galeria")) }
                item {
                    Box(Modifier.fillMaxWidth().background(Tono.fondo)) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = Medida.margen),
                            horizontalArrangement = Arrangement.spacedBy(Medida.entreTarjetas),
                        ) {
                            items(ficha.galeria) { imagen ->
                                Foto(
                                    imagen = imagen,
                                    descripcion = ficha.titulo,
                                    modifier = Modifier.width(260.dp).height(190.dp),
                                    radio = Radio.tarjeta,
                                )
                            }
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Tono.fondo)
                            .padding(horizontal = Medida.margen),
                    )
                }
            }

            item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(Tono.fondo)
                        .padding(Medida.margen),
                ) {
                    ficha.autor?.let { autor ->
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

        // El precio a la izquierda y la accion a la derecha: el dato que se
        // busca de un vistazo y lo que se hace con el, en la misma linea.
        BarraAccion(
            textoBoton = Textos.t(if (enRecorrido) "ficha.quitar" else "ficha.agregar"),
            alTocar = { Guardado.alternarEnRecorrido(ficha.id) },
            modifier = Modifier.align(Alignment.BottomCenter),
            dato = ficha.practicos.rangoPrecio?.let { { RangoPrecio(it) } },
        )
    }
}

/**
 * Si el cuerpo plegado esconde algo.
 *
 * Con mas de un bloque, seguro. Con uno solo hay que estimarlo por largo: saber
 * cuantas lineas ocupa un texto exige medirlo, y medir para decidir si dibujar
 * un enlace es mas maquinaria de la que vale.
 */
private fun hayMasQueLeer(cuerpo: List<HtmlSencillo.Bloque>): Boolean {
    if (cuerpo.size > 1) return true
    return when (val unico = cuerpo.firstOrNull()) {
        is HtmlSencillo.Bloque.Parrafo -> unico.texto.length > LARGO_DE_CUATRO_LINEAS
        is HtmlSencillo.Bloque.Punto -> unico.texto.length > LARGO_DE_CUATRO_LINEAS
        is HtmlSencillo.Bloque.Cita -> unico.texto.length > LARGO_DE_CUATRO_LINEAS
        else -> false
    }
}

/** Los metadatos que tienen algo que decir, en el orden en que se preguntan. */
private fun metadatosDe(ficha: Ficha): List<String> = buildList {
    fechaCorta(ficha.fechas?.inicio)?.let { add(it) }
    ficha.categoria?.nombreVisible()?.takeIf { it.isNotBlank() }?.let { add(it) }
    ficha.etiquetas.forEach { etiqueta ->
        etiqueta.nombreVisible().takeIf { it.isNotBlank() }?.let { add(it) }
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
    Box(Modifier.fillMaxWidth().background(Tono.fondo)) {
        Texto(
            texto = titulo,
            estilo = Letra.tituloSeccion,
            color = Tono.tinta,
            modifier = Modifier.padding(
                start = Medida.margen, end = Medida.margen,
                top = Medida.entreSecciones, bottom = 12.dp,
            ),
        )
    }
}

@Composable
private fun Dato(etiqueta: String, valor: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Medida.dentroTarjeta, vertical = 10.dp),
    ) {
        Texto(etiqueta, Letra.etiquetaNav, Tono.tintaSuave, maxLineas = 1)
        Texto(valor, Letra.descripcion, Tono.tinta)
    }
}

@Composable
private fun FilaRelacionado(articulo: ResumenArticulo) {
    Box(Modifier.fillMaxWidth().background(Tono.fondo)) {
        FilaCompacta(
            imagen = articulo.portada,
            titulo = articulo.titulo,
            detalle = articulo.entradilla.ifBlank { null },
            modifier = Modifier.padding(horizontal = Medida.margen, vertical = 5.dp),
            // Todavia no lleva a ningun lado: abrir un articulo desde una ficha
            // cruza dos secciones y esa navegacion no existe. Dejar la fila
            // tocable sin hacer nada seria peor que no ofrecerlo.
            alTocar = null,
        )
    }
}
