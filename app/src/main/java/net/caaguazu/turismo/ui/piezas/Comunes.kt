package net.caaguazu.turismo.ui.piezas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.datos.Imagen
import net.caaguazu.turismo.ui.tema.AnimacionesActivas
import net.caaguazu.turismo.ui.tema.Elevacion
import net.caaguazu.turismo.ui.tema.Letra
import net.caaguazu.turismo.ui.tema.Medida
import net.caaguazu.turismo.ui.tema.Movimiento
import net.caaguazu.turismo.ui.tema.Radio
import net.caaguazu.turismo.ui.tema.Tono

/* ==========================================================================
 * Superficies
 * ======================================================================== */

/**
 * La forma del sistema: una tarjeta muy redondeada que flota sobre el fondo.
 *
 * Todo bloque de contenido pasa por aca. Tenerlo en una sola pieza es lo que
 * hace que cambiar la elevacion o el radio de la app entera sea una linea y no
 * una recorrida por veinte archivos.
 */
@Composable
fun Tarjeta(
    modifier: Modifier = Modifier,
    radio: Dp = Radio.tarjeta,
    elevacion: Dp = Elevacion.tarjeta,
    fondo: Color = Tono.papel,
    alTocar: (() -> Unit)? = null,
    contenido: @Composable BoxScope.() -> Unit,
) {
    val forma = RoundedCornerShape(radio)
    val interaccion = recordarInteraccion()

    Box(
        modifier = modifier
            .shadow(elevacion, forma, ambientColor = Tono.sombra, spotColor = Tono.sombra)
            .clip(forma)
            .background(fondo)
            .then(
                if (alTocar == null) {
                    Modifier
                } else {
                    Modifier
                        .cedeAlTocar(interaccion)
                        .clickable(
                            interactionSource = interaccion,
                            indication = null,
                            onClick = alTocar,
                        )
                },
            ),
    ) {
        contenido()
    }
}

/**
 * Foto de contenido.
 *
 * Va redondeada como todo lo demas. Si la imagen falta o no baja, queda el
 * hueco en color de banda en vez de un blanco que parece un error de dibujo.
 */
@Composable
fun Foto(
    imagen: Imagen?,
    descripcion: String,
    modifier: Modifier = Modifier,
    desaturada: Boolean = false,
    radio: Dp = Radio.media,
) {
    Box(modifier.clip(RoundedCornerShape(radio)).background(Tono.banda)) {
        if (imagen != null) {
            AsyncImage(
                model = imagen.url,
                contentDescription = descripcion,
                contentScale = ContentScale.Crop,
                colorFilter = if (desaturada) FILTRO_DESATURADO else null,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/** Casi gris, no gris del todo: la referencia conserva un resto de color. */
private val FILTRO_DESATURADO =
    ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0.2f) })

/**
 * Foto con velo encima, para cuando lleva texto blanco arriba.
 *
 * El velo por omision es un degradado que carga abajo y deja limpia la parte
 * de arriba: el texto vive abajo, y velar la foto entera por parejo apaga
 * justo lo que se vino a ver. `parejo` vuelve al velo plano para los tiles
 * chicos, donde el degradado no tiene recorrido para notarse.
 *
 * `colorSinFoto` cubre el caso de un tile sin imagen: en vez de un gris muerto
 * queda el color de su categoria, oscurecido para que el texto blanco siga
 * leyendose. Un hueco con identidad es mejor que un hueco.
 */
@Composable
fun FotoConVelo(
    imagen: Imagen?,
    descripcion: String,
    modifier: Modifier = Modifier,
    colorSinFoto: String? = null,
    radio: Dp = Radio.tarjeta,
    parejo: Boolean = false,
) {
    Box(modifier.clip(RoundedCornerShape(radio))) {
        if (imagen == null && !colorSinFoto.isNullOrBlank()) {
            Box(Modifier.fillMaxSize().background(colorDeTexto(colorSinFoto, Tono.tintaSuave)))
        } else {
            Foto(imagen, descripcion, Modifier.fillMaxSize(), desaturada = true, radio = radio)
        }
        if (parejo) {
            Box(Modifier.fillMaxSize().background(Tono.velo))
        } else {
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.45f to Tono.velo,
                        1f to Tono.veloProfundo,
                    ),
                ),
            )
        }
    }
}

/**
 * Interpreta un color que llega del servidor como texto.
 *
 * Si viene mal escrito no se cae: se usa el de respaldo. Un color invalido en
 * una categoria no puede tumbar la pantalla entera.
 */
fun colorDeTexto(hex: String?, respaldo: Color): Color {
    val limpio = hex?.trim()?.removePrefix("#") ?: return respaldo
    if (limpio.length != 6 && limpio.length != 8) return respaldo
    val valor = limpio.toLongOrNull(16) ?: return respaldo
    return if (limpio.length == 6) Color(valor or 0xFF000000L) else Color(valor)
}

/* ==========================================================================
 * Cabeceras
 * ======================================================================== */

/**
 * Cabecera de pantalla: titulo grande a la izquierda, acciones a la derecha.
 *
 * No hay barra: el titulo se apoya en el fondo de la pagina como un parrafo
 * mas. Una barra con el nombre de la app repetido en las cinco pantallas no
 * dice nada, y le come treinta puntos de alto a la unica pantalla que importa.
 */
@Composable
fun CabeceraPantalla(
    titulo: String,
    modifier: Modifier = Modifier,
    acciones: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Medida.margen)
            .padding(top = 8.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Texto(
            texto = titulo,
            estilo = Letra.tituloPantalla,
            color = Tono.tinta,
            maxLineas = 1,
            modifier = Modifier.weight(1f, fill = false),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            content = acciones,
        )
    }
}

/**
 * Encabezado de seccion: titulo a la izquierda, "ver todo" a la derecha.
 *
 * El enlace es texto plano y chico, no un boton: si pesara lo mismo que el
 * titulo competiria con el, y lo que tiene que leerse primero es de que trata
 * la seccion.
 */
@Composable
fun EncabezadoSeccion(
    titulo: String,
    modifier: Modifier = Modifier,
    alVerTodo: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Medida.margen),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Texto(
            texto = titulo,
            estilo = Letra.tituloSeccion,
            color = Tono.tinta,
            maxLineas = 1,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (alVerTodo != null) {
            Texto(
                texto = Textos.t("banda.verTodo"),
                estilo = Letra.enlace,
                color = Tono.tintaSuave,
                maxLineas = 1,
                modifier = Modifier.clickable(onClick = alVerTodo).padding(start = 12.dp),
            )
        }
    }
}

/* ==========================================================================
 * Controles
 * ======================================================================== */

/**
 * Control primario: pildora verde, radio completo.
 *
 * Es el boton que hace la cosa que la pantalla propone, y hay como mucho uno
 * por pantalla. La tinta va en verde oscuro y no en blanco: sobre este verde,
 * el blanco no se lee al sol.
 */
@Composable
fun PildoraPrimaria(
    texto: String,
    alTocar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaccion = recordarInteraccion()
    Box(
        modifier = modifier
            .shadow(
                Elevacion.tarjeta,
                RoundedCornerShape(Radio.completo),
                ambientColor = Tono.sombra,
                spotColor = Tono.sombra,
            )
            .clip(RoundedCornerShape(Radio.completo))
            .background(Tono.primario)
            .cedeAlTocar(interaccion)
            .clickable(interactionSource = interaccion, indication = null, onClick = alTocar)
            .padding(horizontal = 30.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Texto(texto = texto, estilo = Letra.chip, color = Tono.sobrePrimario, maxLineas = 1)
    }
}

/**
 * Control secundario: pildora de relleno suave, sin contorno.
 *
 * El sistema separa por superficie y no por linea. Un contorno de 1px alrededor
 * de cada control secundario llenaba la ficha de rectangulos y competia con el
 * unico boton que si tiene que verse.
 */
@Composable
fun PildoraSuave(
    texto: String,
    alTocar: () -> Unit,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null,
) {
    val interaccion = recordarInteraccion()
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(Radio.completo))
            .background(Tono.campo)
            .cedeAlTocar(interaccion)
            .clickable(interactionSource = interaccion, indication = null, onClick = alTocar)
            .padding(horizontal = 20.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        if (icono != null) {
            Glifo(icono, texto, Tono.tinta, Modifier.size(17.dp))
        }
        Texto(texto = texto, estilo = Letra.chip, color = Tono.tinta, maxLineas = 1)
    }
}

/**
 * Pildora de solo lectura: un dato de contexto, no un control.
 *
 * Es la fila de metadatos de una ficha —donde, cuando, cuanto— con la misma
 * forma que un chip pero sin nada que tocar.
 */
@Composable
fun PildoraMeta(
    texto: String,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null,
    tinta: Color = Tono.tinta,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(Radio.completo))
            .background(Tono.campo)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        if (icono != null) {
            Glifo(icono, texto, tinta, Modifier.size(15.dp))
        }
        Texto(texto = texto, estilo = Letra.chip, color = tinta, maxLineas = 1)
    }
}

/**
 * Buscador: lupa, marcador y campo, sobre el relleno de control.
 *
 * Es un campo, no un boton que abre otra pantalla: la busqueda vive en la
 * misma lista que filtra, para no perder el contexto de categoria o etiqueta
 * ya elegida al escribir.
 */
@Composable
fun CampoBusqueda(
    valor: String,
    alCambiar: (String) -> Unit,
    marcador: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(Radio.completo))
            .background(Tono.campo)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Glifo(Icono.buscar, marcador, Tono.tintaSuave, Modifier.size(18.dp))
        Box(Modifier.weight(1f)) {
            if (valor.isEmpty()) {
                Texto(marcador, Letra.chip, Tono.tintaSuave, maxLineas = 1)
            }
            BasicTextField(
                value = valor,
                onValueChange = alCambiar,
                textStyle = Letra.chip.copy(color = Tono.tinta),
                singleLine = true,
                cursorBrush = SolidColor(Tono.tinta),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * Chip de filtro: relleno suave en reposo, contraste lleno cuando esta activo.
 *
 * Sin contorno en ninguno de los dos estados. El salto de relleno claro a
 * relleno oscuro se ve de lejos; un borde que cambia de color, no.
 */
@Composable
fun ChipFiltro(
    texto: String,
    activo: Boolean,
    alTocar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaccion = recordarInteraccion()
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Radio.completo))
            .background(if (activo) Tono.contraste else Tono.campo)
            .cedeAlTocar(interaccion)
            .clickable(interactionSource = interaccion, indication = null, onClick = alTocar)
            .padding(horizontal = 18.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center,
    ) {
        Texto(
            texto = texto,
            estilo = Letra.chip,
            color = if (activo) Tono.sobreContraste else Tono.tinta,
            maxLineas = 1,
        )
    }
}

/**
 * Interruptor lista/mapa: segmento activo relleno sobre contenedor de control.
 * Es el control que decide como se ve el inventario, no una pantalla aparte.
 */
@Composable
fun InterruptorListaMapa(
    enMapa: () -> Boolean,
    alCambiar: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val mapa = enMapa()
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(Radio.completo))
            .background(Tono.campo)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Segmento(Icono.lista, Textos.t("inv.lista"), activo = !mapa) { alCambiar(false) }
        Segmento(Icono.inventario, Textos.t("inv.mapa"), activo = mapa) { alCambiar(true) }
    }
}

@Composable
private fun Segmento(
    icono: ImageVector,
    descripcion: String,
    activo: Boolean,
    alTocar: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(Radio.completo))
            .background(if (activo) Tono.contraste else Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = alTocar,
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Glifo(
            icono = icono,
            descripcion = descripcion,
            color = if (activo) Tono.sobreContraste else Tono.tintaSuave,
            modifier = Modifier.size(19.dp),
        )
    }
}

/**
 * Boton circular chico de relleno suave: la accion de apoyo de una fila o de
 * una cabecera. Es el mismo cuerpo que un chip, en redondo.
 */
@Composable
fun BotonIcono(
    icono: ImageVector,
    descripcion: String,
    alTocar: () -> Unit,
    modifier: Modifier = Modifier,
    tinta: Color = Tono.tinta,
) {
    val interaccion = recordarInteraccion()
    Box(
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Tono.campo)
            .cedeAlTocar(interaccion)
            .clickable(interactionSource = interaccion, indication = null, onClick = alTocar),
        contentAlignment = Alignment.Center,
    ) {
        Glifo(icono, descripcion, tinta, Modifier.size(20.dp))
    }
}

/**
 * Boton circular que flota sobre una foto o un mapa: volver, compartir.
 * Lleva la elevacion alta porque tiene que despegarse de una imagen, no de un
 * fondo plano.
 */
@Composable
fun BotonFlotante(
    icono: ImageVector,
    descripcion: String,
    alTocar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaccion = recordarInteraccion()
    Box(
        modifier = modifier
            .size(42.dp)
            .shadow(Elevacion.flotante, CircleShape, ambientColor = Tono.sombra, spotColor = Tono.sombra)
            .background(Tono.papel, CircleShape)
            .cedeAlTocar(interaccion)
            .clickable(interactionSource = interaccion, indication = null, onClick = alTocar),
        contentAlignment = Alignment.Center,
    ) {
        Glifo(icono, descripcion, Tono.tinta, Modifier.size(21.dp))
    }
}

/**
 * Corazon de favorito.
 *
 * Sobre una foto va en su circulo de papel, que es lo que lo despega; dentro de
 * una fila va suelto, porque ahi el fondo ya es plano y el circulo solo sumaria
 * un borde mas.
 */
@Composable
fun Corazon(
    marcado: () -> Boolean,
    alTocar: () -> Unit,
    descripcion: String,
    modifier: Modifier = Modifier,
    sobreFoto: Boolean = true,
) {
    val activo = marcado()
    val animar = AnimacionesActivas.current
    val latido = remember { Animatable(1f) }

    // Un latido corto al marcar. Al desmarcar no late: quitar algo no se
    // celebra, y el movimiento tiene que significar algo o no estar.
    LaunchedEffect(activo) {
        if (activo && animar) {
            latido.animateTo(1.25f, Movimiento.toque())
            latido.animateTo(1f, Movimiento.entrada(180))
        }
    }

    val base = modifier.size(if (sobreFoto) 42.dp else 34.dp)
    Box(
        modifier = if (sobreFoto) {
            base
                .shadow(Elevacion.flotante, CircleShape, ambientColor = Tono.sombra, spotColor = Tono.sombra)
                .background(Tono.papel, CircleShape)
                .clickable(onClick = alTocar)
        } else {
            base.clip(CircleShape).clickable(onClick = alTocar)
        },
        contentAlignment = Alignment.Center,
    ) {
        Glifo(
            icono = if (activo) Icono.corazonLleno else Icono.corazon,
            descripcion = descripcion,
            color = if (activo) Tono.acento else Tono.tintaSuave,
            modifier = Modifier.size(20.dp).scale(latido.value),
        )
    }
}

/**
 * Interruptor de encendido/apagado.
 *
 * Se dibuja aca y no se toma de Material porque la app no usa su tema. Es una
 * pista de radio completo con un punto que se corre de lado, y el color solo
 * cambia cuando esta encendido: apagado tiene que leerse como apagado sin
 * depender de distinguir dos verdes.
 */
@Composable
fun Interruptor(
    encendido: Boolean,
    alCambiar: (Boolean) -> Unit,
    descripcion: String,
    modifier: Modifier = Modifier,
) {
    val animar = AnimacionesActivas.current
    val corrimiento by animateDpAsState(
        targetValue = if (encendido) 22.dp else 2.dp,
        animationSpec = if (animar) Movimiento.entrada() else snap(),
        label = "interruptor",
    )

    Box(
        modifier = modifier
            .size(width = 48.dp, height = 28.dp)
            .clip(RoundedCornerShape(Radio.completo))
            .background(if (encendido) Tono.primario else Tono.campo)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { alCambiar(!encendido) },
            )
            .semantics { contentDescription = descripcion },
    ) {
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .offset(x = corrimiento)
                .size(24.dp)
                .background(if (encendido) Tono.sobrePrimario else Tono.papel, CircleShape),
        )
    }
}

/* ==========================================================================
 * Contenido
 * ======================================================================== */

/**
 * Tarjeta de foto del carrusel.
 *
 * Foto redondeada suelta sobre la pagina y el texto debajo, sin caja blanca
 * alrededor. Es lo que hace que la pantalla de inicio se lea como una galeria
 * y no como una grilla de fichas: la foto es el contenido, y una caja que la
 * encierra solo agrega un borde entre la foto y el ojo.
 */
@Composable
fun TarjetaFoto(
    imagen: Imagen?,
    titulo: String,
    modifier: Modifier = Modifier,
    proporcion: Float = 1f,
    encima: String? = null,
    alTocar: () -> Unit,
    esquina: @Composable (BoxScope.() -> Unit)? = null,
) {
    val interaccion = recordarInteraccion()
    Column(
        modifier = modifier
            .cedeAlTocar(interaccion)
            .clickable(interactionSource = interaccion, indication = null, onClick = alTocar),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(proporcion)) {
            Foto(imagen, titulo, Modifier.fillMaxSize(), radio = Radio.tarjeta)
            if (esquina != null) {
                Box(Modifier.align(Alignment.TopEnd).padding(8.dp)) { esquina() }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Texto(titulo, Letra.tituloTarjeta, Tono.tinta, maxLineas = 1)
            if (!encima.isNullOrBlank()) {
                Texto(encima, Letra.fecha, Tono.tintaSuave, maxLineas = 1)
            }
        }
    }
}

/**
 * Tile de foto con la etiqueta encima: la grilla de dos columnas del inicio y
 * de las categorias.
 *
 * La etiqueta va abajo a la izquierda sobre el degradado, que es donde menos
 * tapa la foto y donde el ojo la busca.
 */
@Composable
fun TileEtiquetado(
    imagen: Imagen?,
    etiqueta: String,
    modifier: Modifier = Modifier,
    proporcion: Float = 16f / 11f,
    colorSinFoto: String? = null,
    alTocar: () -> Unit,
) {
    val interaccion = recordarInteraccion()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(proporcion)
            .cedeAlTocar(interaccion)
            .clickable(interactionSource = interaccion, indication = null, onClick = alTocar),
    ) {
        FotoConVelo(
            imagen = imagen,
            descripcion = etiqueta,
            colorSinFoto = colorSinFoto,
            modifier = Modifier.fillMaxSize(),
            radio = Radio.tarjeta,
        )
        Texto(
            texto = etiqueta,
            estilo = Letra.sobreFoto,
            color = Tono.sobreFoto,
            maxLineas = 2,
            modifier = Modifier.align(Alignment.BottomStart).padding(14.dp),
        )
    }
}

/**
 * Atajo de la fila de categorias: foto redondeada con la etiqueta debajo.
 *
 * La referencia usa un glifo por categoria. Aca va la foto: el panel manda el
 * nombre del icono como texto libre y la app no tiene ese vocabulario, asi que
 * mapearlo a mano terminaria en cuatro atajos con el mismo dibujo. La foto es
 * dato real del destino y distingue una categoria de la otra sola.
 */
@Composable
fun AtajoFoto(
    imagen: Imagen?,
    etiqueta: String,
    alTocar: () -> Unit,
    modifier: Modifier = Modifier,
    colorSinFoto: String? = null,
) {
    val interaccion = recordarInteraccion()
    Column(
        modifier = modifier
            .width(76.dp)
            .cedeAlTocar(interaccion)
            .clickable(interactionSource = interaccion, indication = null, onClick = alTocar),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.size(64.dp)) {
            if (imagen == null && !colorSinFoto.isNullOrBlank()) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(Radio.media))
                        .background(colorDeTexto(colorSinFoto, Tono.campo)),
                )
            } else {
                Foto(imagen, etiqueta, Modifier.fillMaxSize(), radio = Radio.media)
            }
        }
        Texto(
            texto = etiqueta,
            estilo = Letra.etiquetaNav,
            color = Tono.tintaSuave,
            maxLineas = 1,
            alinear = TextAlign.Center,
        )
    }
}

/**
 * Fila compacta de lista: miniatura, dos lineas de texto y una accion al final.
 *
 * Reemplaza a la tarjeta de lista con foto de 180: en una pantalla de telefono
 * entraban dos resultados y medio, y buscar entre veinte lugares era hacer
 * scroll siete veces. Con la fila entran seis, que es lo que convierte a una
 * lista en algo que se recorre de un vistazo.
 */
@Composable
fun FilaCompacta(
    imagen: Imagen?,
    titulo: String,
    modifier: Modifier = Modifier,
    detalle: String? = null,
    meta: String? = null,
    colorMeta: Color = Tono.acento,
    alTocar: (() -> Unit)? = null,
    final: @Composable (() -> Unit)? = null,
) {
    Tarjeta(modifier = modifier.fillMaxWidth(), radio = Radio.lista, alTocar = alTocar) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Foto(imagen, titulo, Modifier.size(62.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Texto(titulo, Letra.tituloTarjeta, Tono.tinta, maxLineas = 1)
                if (!detalle.isNullOrBlank()) {
                    Texto(
                        texto = detalle,
                        estilo = Letra.fecha,
                        color = Tono.tintaSuave,
                        maxLineas = 1,
                        conPuntosSuspensivos = false,
                    )
                }
                if (!meta.isNullOrBlank()) {
                    Texto(meta, Letra.etiquetaNav, colorMeta, maxLineas = 1)
                }
            }
            if (final != null) final()
        }
    }
}

/**
 * Rango de precio: los cuatro simbolos siempre presentes, apagados los que no
 * corresponden. Mostrar solo los activos haria que "$" y "$$$$" ocuparan anchos
 * distintos y las tarjetas dejaran de alinearse.
 */
@Composable
fun RangoPrecio(rango: Int?, modifier: Modifier = Modifier) {
    if (rango == null) return

    if (rango <= 0) {
        Texto(
            texto = Textos.t("precio.gratis"),
            estilo = Letra.chip,
            color = Tono.acento,
            modifier = modifier,
        )
        return
    }

    Row(modifier, horizontalArrangement = Arrangement.spacedBy(1.dp)) {
        repeat(4) { i ->
            Texto(
                texto = "$",
                estilo = Letra.precio,
                color = if (i < rango) Tono.tinta else Tono.linea,
            )
        }
    }
}

/**
 * Badge sobre una foto: pildora opaca con una palabra corta.
 *
 * Es donde vive el mango — lo que esta ocurriendo ahora— y el unico lugar
 * donde ese color aparece.
 */
@Composable
fun Badge(
    texto: String,
    modifier: Modifier = Modifier,
    fondo: Color = Tono.destacado,
    tinta: Color = Tono.sobrePrimario,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Radio.completo))
            .background(fondo)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Texto(texto = texto, estilo = Letra.etiquetaNav, color = tinta, maxLineas = 1)
    }
}

/**
 * Barra de accion al pie de una ficha: el dato a la izquierda, el boton a la
 * derecha.
 *
 * Va fija sobre el contenido y no al final del scroll: la accion tiene que
 * estar a mano en cualquier punto de la lectura, no despues de ella.
 */
@Composable
fun BarraAccion(
    textoBoton: String,
    alTocar: () -> Unit,
    modifier: Modifier = Modifier,
    dato: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Tono.papel)
            .padding(horizontal = Medida.margen, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (dato != null) {
            Box(Modifier.weight(1f)) { dato() }
            PildoraPrimaria(texto = textoBoton, alTocar = alTocar)
        } else {
            PildoraPrimaria(texto = textoBoton, alTocar = alTocar, modifier = Modifier.weight(1f))
        }
    }
}

/** Hairline de 1px: el separador de dentro de una tarjeta. */
@Composable
fun Hairline(modifier: Modifier = Modifier) {
    Box(modifier.height(1.dp).background(Tono.linea))
}
