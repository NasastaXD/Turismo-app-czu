package net.caaguazu.turismo.ui.piezas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.datos.Imagen
import net.caaguazu.turismo.ui.tema.Letra
import net.caaguazu.turismo.ui.tema.Medida
import net.caaguazu.turismo.ui.tema.Radio
import net.caaguazu.turismo.ui.tema.Tono

/**
 * La tarjeta grande: una foto a todo el ancho con el texto encima, abajo.
 *
 * Es la unidad de contenido de esta version. La anterior repartia todo en
 * carruseles de tarjetas chicas, y el resultado era que ninguna foto se veia:
 * cuatro rieles horizontales de miniaturas es un indice disfrazado de galeria.
 * Una foto grande por vez muestra el lugar, que es lo unico que hace que
 * alguien quiera ir.
 */
@Composable
fun TarjetaGrande(
    imagen: Imagen?,
    titulo: String,
    modifier: Modifier = Modifier,
    encima: String? = null,
    proporcion: Float = 3f / 2f,
    alTocar: () -> Unit,
    esquina: @Composable (() -> Unit)? = null,
) {
    val interaccion = recordarInteraccion()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(proporcion)
            .cedeAlTocar(interaccion)
            .clickable(interactionSource = interaccion, indication = null, onClick = alTocar),
    ) {
        FotoConVelo(imagen, titulo, Modifier.fillMaxSize(), radio = Radio.tarjeta)

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(Medida.dentroTarjeta),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            if (!encima.isNullOrBlank()) {
                Texto(encima, Letra.etiquetaNav, Tono.sobreFoto, maxLineas = 1)
            }
            Texto(titulo, Letra.tituloSeccion, Tono.sobreFoto, maxLineas = 2)
        }

        if (esquina != null) {
            Box(Modifier.align(Alignment.TopEnd).padding(10.dp)) { esquina() }
        }
    }
}

/**
 * Banda promocional: foto ancha, titulo grande encima y una pildora de contorno
 * claro que invita a entrar.
 *
 * Es la unica pieza del sistema con contorno, y se justifica: va sobre una foto,
 * donde un relleno solido taparia la imagen y un texto suelto no se leeria como
 * algo que se toca.
 */
@Composable
fun BandaPromocional(
    imagen: Imagen?,
    titulo: String,
    textoAccion: String,
    alTocar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaccion = recordarInteraccion()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .cedeAlTocar(interaccion)
            .clickable(interactionSource = interaccion, indication = null, onClick = alTocar),
    ) {
        FotoConVelo(imagen, titulo, Modifier.fillMaxSize(), radio = Radio.tarjeta, parejo = true)
        Column(
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Texto(
                texto = titulo,
                estilo = Letra.tituloPantalla,
                color = Tono.sobreFoto,
                maxLineas = 2,
                alinear = TextAlign.Center,
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(Radio.completo))
                    .border(1.5.dp, Tono.sobreFoto, RoundedCornerShape(Radio.completo))
                    .padding(horizontal = 22.dp, vertical = 10.dp),
            ) {
                Texto(textoAccion, Letra.chip, Tono.sobreFoto, maxLineas = 1)
            }
        }
    }
}

/**
 * Entrada a la busqueda: parece un campo pero no escribe, lleva a la pantalla
 * donde si se escribe.
 *
 * El inicio no filtra nada, asi que un campo de verdad ahi seria un control que
 * promete algo que la pantalla no puede cumplir.
 */
@Composable
fun EntradaBusqueda(
    marcador: String,
    alTocar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaccion = recordarInteraccion()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radio.completo))
            .background(Tono.papel)
            .cedeAlTocar(interaccion)
            .clickable(interactionSource = interaccion, indication = null, onClick = alTocar)
            .padding(horizontal = 18.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Glifo(Icono.buscar, marcador, Tono.tintaSuave, Modifier.size(18.dp))
        Texto(marcador, Letra.chip, Tono.tintaSuave, maxLineas = 1)
    }
}

/**
 * Boton de filtros con el contador de cuantos hay puestos.
 *
 * El numero importa: con la hoja cerrada, no hay forma de saber cuantos filtros
 * estan actuando sobre lo que se ve, y una lista corta sin explicacion se lee
 * como que no hay nada cargado.
 */
@Composable
fun BotonFiltros(
    cantidad: Int,
    descripcion: String,
    alTocar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaccion = recordarInteraccion()
    val activo = cantidad > 0
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(Radio.completo))
            .background(if (activo) Tono.contraste else Tono.campo)
            .cedeAlTocar(interaccion)
            .clickable(interactionSource = interaccion, indication = null, onClick = alTocar)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Glifo(
            icono = Icono.filtros,
            descripcion = descripcion,
            color = if (activo) Tono.sobreContraste else Tono.tinta,
            modifier = Modifier.size(18.dp),
        )
        if (activo) {
            Texto(cantidad.toString(), Letra.chip, Tono.sobreContraste, maxLineas = 1)
        }
    }
}

/**
 * Grupo de una hoja de filtros: un titulo y debajo lo que se elige.
 *
 * El titulo va fuera del contenido y en chico, como en la referencia: lo que
 * tiene que verse son las opciones, no como se llama el grupo.
 */
@Composable
fun GrupoFiltro(
    titulo: String,
    modifier: Modifier = Modifier,
    contenido: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(bottom = Medida.entreTarjetas),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Texto(
            texto = titulo,
            estilo = Letra.enlace,
            color = Tono.tintaSuave,
            maxLineas = 1,
            modifier = Modifier.padding(horizontal = Medida.margen),
        )
        contenido()
    }
}

/**
 * Tarjeta del lugar tocado en el mapa.
 *
 * Es la unica forma de que el mapa sirva para algo: un pin sin nombre obliga a
 * salir del mapa para saber que era. Aparece sobre el lienzo, no en lugar de el.
 */
@Composable
fun TarjetaDeMapa(
    imagen: Imagen?,
    titulo: String,
    detalle: String?,
    alAbrir: () -> Unit,
    alCerrar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Tarjeta(modifier = modifier.fillMaxWidth(), alTocar = alAbrir) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Foto(imagen, titulo, Modifier.size(64.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Texto(titulo, Letra.tituloTarjeta, Tono.tinta, maxLineas = 2)
                if (!detalle.isNullOrBlank()) {
                    Texto(
                        texto = detalle,
                        estilo = Letra.fecha,
                        color = Tono.tintaSuave,
                        maxLineas = 1,
                        conPuntosSuspensivos = false,
                    )
                }
            }
            BotonIcono(
                icono = Icono.quitar,
                descripcion = Textos.t("accion.volver"),
                tinta = Tono.tintaSuave,
                alTocar = alCerrar,
            )
        }
    }
}
