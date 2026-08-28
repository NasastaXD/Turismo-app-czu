package net.caaguazu.turismo.ui.piezas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.datos.Imagen
import net.caaguazu.turismo.ui.tema.AnimacionesActivas
import net.caaguazu.turismo.ui.tema.Letra
import net.caaguazu.turismo.ui.tema.Movimiento
import net.caaguazu.turismo.ui.tema.Radio
import net.caaguazu.turismo.ui.tema.Tono

/**
 * Foto de contenido.
 *
 * Radio 0 siempre: el sistema no redondea medios ni tarjetas de contenido. Si la
 * imagen falta o no baja, queda el hueco en color de banda en vez de un blanco
 * que parece un error de dibujo.
 */
@Composable
fun Foto(
    imagen: Imagen?,
    descripcion: String,
    modifier: Modifier = Modifier,
    desaturada: Boolean = false,
) {
    Box(modifier.background(Tono.banda)) {
        if (imagen != null) {
            AsyncImage(
                model = imagen.url,
                contentDescription = descripcion,
                contentScale = ContentScale.Crop,
                // El sistema reserva la desaturacion para los tiles de menu. En
                // tarjetas la foto va tal cual, con su saturacion natural.
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
) {
    Box(modifier) {
        if (imagen == null && !colorSinFoto.isNullOrBlank()) {
            Box(Modifier.fillMaxSize().background(colorDeTexto(colorSinFoto, Tono.tintaSuave)))
        } else {
            Foto(imagen, descripcion, Modifier.fillMaxSize(), desaturada = true)
        }
        // Velo uniforme, no degradado: el sistema lo especifica plano.
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

/** Control primario: pildora negra, radio completo. */
@Composable
fun PildoraNegra(
    texto: String,
    alTocar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Radio.completo))
            .background(Tono.negro)
            .clickable(onClick = alTocar)
            .padding(horizontal = 26.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Texto(texto = texto, estilo = Letra.chip, color = Color.White, maxLineas = 1)
    }
}

/** Control secundario: contorno fino sobre papel. */
@Composable
fun PildoraContorno(
    texto: String,
    alTocar: () -> Unit,
    modifier: Modifier = Modifier,
    icono: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(Radio.completo))
            .background(Tono.papel)
            .clickable(onClick = alTocar)
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
 * Interruptor lista/mapa: segmento activo en blanco sobre contenedor negro.
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
            .background(Tono.negro)
            .padding(3.dp),
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
            .background(if (activo) Tono.papel else Color.Transparent)
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
            color = if (activo) Tono.tinta else Color.White,
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
 * Icono de accion de una tarjeta de lista: circulo de 56, borde de 1.5 en
 * acento, glifo en acento, fondo transparente.
 *
 * El sistema es explicito en que solo aparecen los disponibles y en que el
 * bloque no reserva huecos vacios: una tarjeta sin telefono no deja el aire de
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
 * Corazon de favorito: circulo blanco de 44 sobre la esquina superior izquierda
 * de la media, con el glifo en acento y sin relleno.
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

/** Hairline de 1px: el unico separador del sistema. */
@Composable
fun Hairline(modifier: Modifier = Modifier) {
    Box(modifier.height(1.dp).background(Tono.linea))
}

/**
 * Campo de busqueda: pildora de radio completo sobre banda, con el glifo de
 * lupa a la izquierda y el marcador reemplazando al texto cuando esta vacio.
 *
 * Quien llama es responsable de esperar antes de usar el valor para pedir a la
 * API: escribir letra por letra no tiene que disparar un pedido por letra.
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
            .background(Tono.banda)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
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
