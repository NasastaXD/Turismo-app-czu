package net.caaguazu.turismo.ui.piezas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.datos.Imagen
import net.caaguazu.turismo.ui.tema.AnimacionesActivas
import net.caaguazu.turismo.ui.tema.Elevacion
import net.caaguazu.turismo.ui.tema.Letra
import net.caaguazu.turismo.ui.tema.Movimiento
import net.caaguazu.turismo.ui.tema.Radio
import net.caaguazu.turismo.ui.tema.Tono

/**
 * La forma del sistema: una tarjeta redondeada que flota sobre el fondo.
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
                // La desaturacion se reserva para los tiles de menu, donde va
                // texto encima. En una tarjeta la foto va con su color natural.
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
) {
    Box(modifier.clip(RoundedCornerShape(radio))) {
        if (imagen == null && !colorSinFoto.isNullOrBlank()) {
            Box(Modifier.fillMaxSize().background(colorDeTexto(colorSinFoto, Tono.tintaSuave)))
        } else {
            Foto(imagen, descripcion, Modifier.fillMaxSize(), desaturada = true, radio = radio)
        }
        Box(Modifier.fillMaxSize().background(Tono.velo))
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
            .padding(horizontal = 28.dp, vertical = 15.dp),
        contentAlignment = Alignment.Center,
    ) {
        Texto(texto = texto, estilo = Letra.chip, color = Tono.sobrePrimario, maxLineas = 1)
    }
}

/** Control secundario: contorno fino sobre el fondo de la pantalla. */
@Composable
fun PildoraContorno(
    texto: String,
    alTocar: () -> Unit,
    modifier: Modifier = Modifier,
    icono: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {
    val interaccion = recordarInteraccion()
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(Radio.completo))
            .background(Tono.papel)
            .border(1.dp, Tono.linea, RoundedCornerShape(Radio.completo))
            .cedeAlTocar(interaccion)
            .clickable(interactionSource = interaccion, indication = null, onClick = alTocar)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (icono != null) {
            Glifo(icono, texto, Tono.tinta, Modifier.size(18.dp))
        }
        Texto(texto = texto, estilo = Letra.chip, color = Tono.tinta, maxLineas = 1)
    }
}

/**
 * Chip de filtro: pildora que se rellena de contraste cuando esta activa.
 * Es el patron de la referencia para elegir entre pocas opciones visibles.
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
            .background(if (activo) Tono.contraste else Tono.papel)
            .then(
                if (activo) {
                    Modifier
                } else {
                    Modifier.border(1.dp, Tono.linea, RoundedCornerShape(Radio.completo))
                },
            )
            .cedeAlTocar(interaccion)
            .clickable(interactionSource = interaccion, indication = null, onClick = alTocar)
            .padding(horizontal = 18.dp, vertical = 10.dp),
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
 * Interruptor lista/mapa: segmento activo relleno sobre contenedor de banda.
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
            .background(Tono.banda)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Segmento(Icono.lista, Textos.t("inv.lista"), activo = !mapa) { alCambiar(false) }
        Segmento(Icono.inventario, Textos.t("inv.mapa"), activo = mapa) { alCambiar(true) }
    }
}

@Composable
private fun Segmento(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
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
            .padding(horizontal = 18.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Glifo(
            icono = icono,
            descripcion = descripcion,
            color = if (activo) Tono.sobreContraste else Tono.tintaSuave,
            modifier = Modifier.size(20.dp),
        )
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
                estilo = Letra.chip,
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
 * Icono de accion de una tarjeta de lista: circulo de 56, borde de 1.5 en
 * acento, glifo en acento, fondo transparente.
 *
 * Solo aparecen los disponibles: una tarjeta sin telefono no deja el aire de
 * un boton que no existe.
 */
@Composable
fun IconoAccion(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    descripcion: String,
    alTocar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .border(1.5.dp, Tono.acento, CircleShape)
            .clickable(onClick = alTocar),
        contentAlignment = Alignment.Center,
    ) {
        Glifo(icono, descripcion, Tono.acento, Modifier.size(22.dp))
    }
}

/**
 * Corazon de favorito: circulo de papel de 44 sobre la esquina de la media,
 * con el glifo en acento.
 */
@Composable
fun Corazon(
    marcado: () -> Boolean,
    alTocar: () -> Unit,
    descripcion: String,
    modifier: Modifier = Modifier,
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

    Box(
        modifier = modifier
            .size(44.dp)
            .shadow(Elevacion.tarjeta, CircleShape, ambientColor = Tono.sombra, spotColor = Tono.sombra)
            .background(Tono.papel, CircleShape)
            .clickable(onClick = alTocar),
        contentAlignment = Alignment.Center,
    ) {
        Glifo(
            icono = if (activo) Icono.corazonLleno else Icono.corazon,
            descripcion = descripcion,
            color = Tono.acento,
            modifier = Modifier.size(20.dp).scale(latido.value),
        )
    }
}

/**
 * Boton circular que flota sobre una foto o un mapa: volver, compartir.
 * Lleva la elevacion alta porque tiene que despegarse de una imagen, no de un
 * fondo plano.
 */
@Composable
fun BotonFlotante(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    descripcion: String,
    alTocar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .shadow(Elevacion.flotante, CircleShape, ambientColor = Tono.sombra, spotColor = Tono.sombra)
            .background(Tono.papel, CircleShape)
            .clickable(onClick = alTocar),
        contentAlignment = Alignment.Center,
    ) {
        Glifo(icono, descripcion, Tono.tinta, Modifier.size(22.dp))
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
            .background(if (encendido) Tono.primario else Tono.linea)
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

/** Hairline de 1px: el separador de dentro de una tarjeta. */
@Composable
fun Hairline(modifier: Modifier = Modifier) {
    Box(modifier.height(1.dp).background(Tono.linea))
}
