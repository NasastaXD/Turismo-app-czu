package net.caaguazu.turismo.ui.asistente

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import net.caaguazu.turismo.core.Bitacora
import net.caaguazu.turismo.core.Falla
import net.caaguazu.turismo.core.Resultado
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.datos.Datos
import net.caaguazu.turismo.datos.FuenteAsistente
import net.caaguazu.turismo.ui.piezas.BotonIcono
import net.caaguazu.turismo.ui.piezas.CabeceraPantalla
import net.caaguazu.turismo.ui.piezas.Glifo
import net.caaguazu.turismo.ui.piezas.Icono
import net.caaguazu.turismo.ui.piezas.PildoraSuave
import net.caaguazu.turismo.ui.piezas.Texto
import net.caaguazu.turismo.ui.piezas.cedeAlTocar
import net.caaguazu.turismo.ui.piezas.recordarInteraccion
import net.caaguazu.turismo.ui.tema.AnimacionesActivas
import net.caaguazu.turismo.ui.tema.Letra
import net.caaguazu.turismo.ui.tema.Medida
import net.caaguazu.turismo.ui.tema.Radio
import net.caaguazu.turismo.ui.tema.Tono

/**
 * El asistente: una charla que responde dudas con lo publicado en el panel.
 *
 * Todo lo que dice sobre Caaguazu sale de las fichas, articulos y recorridos,
 * y cada respuesta trae debajo las piezas de donde salio, para abrirlas con un
 * toque. Eso es lo que lo separa de un chat cualquiera: quien pregunta puede
 * ver con sus ojos de donde salio un horario antes de ir.
 *
 * La inteligencia vive del lado del servidor (`POST /asistente`): aca no hay
 * claves, ni modelo, ni historial que se pueda adulterar. La app pregunta,
 * muestra y enlaza.
 */

/** Un turno de la charla. */
@Immutable
data class MensajeAsistente(
    val deLaPersona: Boolean,
    val texto: String,
    val fuentes: List<FuenteAsistente> = emptyList(),
)

/**
 * El estado de la charla.
 *
 * Vive en el navegador y no en la pantalla, por lo mismo que los filtros de
 * Buscar: abrir una fuente y volver, o cerrar el asistente y abrirlo de nuevo,
 * no puede borrar lo que se venia hablando.
 *
 * Las fuentes abiertas son una pila propia: una ficha abierta desde una
 * respuesta vuelve a la charla, no al inicio de Buscar. Y desde un recorrido
 * abierto ahi se puede entrar a una de sus paradas sin perder el camino.
 */
class PilaAsistente {

    private companion object { const val ETIQUETA = "Asistente" }

    /** Lo dice el panel. Sin esto en true, el boton no se dibuja. */
    var disponible by mutableStateOf(false)
        private set

    val mensajes = mutableStateListOf<MensajeAsistente>()

    /** Lo escrito y todavia no mandado. Sobrevive a abrir una fuente. */
    var borrador by mutableStateOf("")

    var esperando by mutableStateOf(false)
        private set

    /** El ultimo fallo, si la ultima pregunta no tuvo respuesta. */
    var fallo by mutableStateOf<Falla?>(null)
        private set

    /** Lo que se abrio desde una respuesta, de abajo hacia arriba. */
    private val abiertas = mutableStateListOf<FuenteAsistente>()

    val abierta: FuenteAsistente? get() = abiertas.lastOrNull()

    /** Lo que devolvio el servidor: con esto sigue la misma charla. */
    private var conversacion: String? = null

    /** La pregunta que fallo, para que reintentar no obligue a escribirla de nuevo. */
    private var pendiente: String? = null

    suspend fun consultarDisponible() {
        when (val r = Datos.api.asistente()) {
            is Resultado.Bien -> disponible = r.valor.disponible
            // Un servidor anterior a 0.9.0 responde 404: no hay asistente, y
            // eso es exactamente lo que se muestra — nada.
            is Resultado.Mal -> Bitacora.aviso(ETIQUETA, "sin estado del asistente (${r.falla})")
        }
    }

    suspend fun preguntar(texto: String) {
        val limpio = texto.trim()
        if (limpio.isEmpty() || esperando) return
        mensajes += MensajeAsistente(deLaPersona = true, texto = limpio)
        borrador = ""
        enviar(limpio)
    }

    suspend fun reintentar() {
        val texto = pendiente ?: return
        if (esperando) return
        enviar(texto)
    }

    private suspend fun enviar(texto: String) {
        esperando = true
        fallo = null
        pendiente = texto
        when (val r = Datos.api.preguntar(texto, conversacion)) {
            is Resultado.Bien -> {
                conversacion = r.valor.conversacion.ifBlank { null }
                mensajes += MensajeAsistente(
                    deLaPersona = false,
                    texto = r.valor.respuesta,
                    fuentes = r.valor.fuentes,
                )
                pendiente = null
            }
            is Resultado.Mal -> {
                fallo = r.falla
                Bitacora.aviso(ETIQUETA, "la pregunta no tuvo respuesta (${r.falla})")
                // Puede que lo hayan apagado desde el panel mientras la
                // pantalla estaba abierta: si es asi, el boton se va.
                if (r.falla == Falla.SERVIDOR) consultarDisponible()
            }
        }
        esperando = false
    }

    /** Empezar de cero. El servidor arma otra conversacion con la proxima pregunta. */
    fun nuevaCharla() {
        if (esperando) return
        mensajes.clear()
        conversacion = null
        pendiente = null
        fallo = null
        borrador = ""
    }

    fun abrir(fuente: FuenteAsistente) { abiertas += fuente }

    /** Cierra la ultima fuente abierta. False si ya se esta en la charla. */
    fun volver(): Boolean {
        if (abiertas.isEmpty()) return false
        abiertas.removeAt(abiertas.lastIndex)
        return true
    }
}

/**
 * La pantalla de la charla.
 *
 * Abre sin barra inferior —la pone el armazon— porque la entrada vive abajo y
 * el teclado sube hasta ella: con la barra en el medio, el teclado taparia la
 * barra y la entrada quedaria flotando sobre un hueco.
 */
@Composable
fun PantallaAsistente(
    pila: PilaAsistente,
    alPreguntar: (String) -> Unit,
    alReintentar: () -> Unit,
    alVolver: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lista = rememberLazyListState()
    val animar = AnimacionesActivas.current

    // La ultima pregunta arriba, y debajo lo que vino: la respuesta se lee
    // desde su primera linea. Bajar hasta el final de una respuesta larga
    // obligaria a subir para empezar a leerla, y la pregunta quedaria afuera.
    val ultimaPregunta = pila.mensajes.indexOfLast { it.deLaPersona }
    LaunchedEffect(ultimaPregunta, pila.mensajes.size) {
        if (ultimaPregunta >= 0) {
            if (animar) lista.animateScrollToItem(ultimaPregunta) else lista.scrollToItem(ultimaPregunta)
        }
    }

    Column(modifier.fillMaxSize().background(Tono.fondo)) {
        CabeceraPantalla(Textos.t("asistente.titulo")) {
            // Mientras espera no se puede empezar de cero: la respuesta que
            // esta en camino caeria en la charla nueva.
            if (pila.mensajes.isNotEmpty() && !pila.esperando) {
                BotonIcono(
                    icono = Icono.nueva,
                    descripcion = Textos.t("asistente.nueva"),
                    alTocar = pila::nuevaCharla,
                )
            }
            BotonIcono(
                icono = Icono.volver,
                descripcion = Textos.t("accion.volver"),
                alTocar = alVolver,
            )
        }

        LazyColumn(
            state = lista,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(
                start = Medida.margen,
                end = Medida.margen,
                top = 4.dp,
                bottom = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(Medida.entreTarjetas),
        ) {
            itemsIndexed(pila.mensajes) { _, mensaje ->
                if (mensaje.deLaPersona) {
                    Pregunta(mensaje.texto)
                } else {
                    Respuesta(mensaje, alAbrir = pila::abrir)
                }
            }

            if (pila.esperando) {
                item {
                    Texto(Textos.t("asistente.pensando"), Letra.descripcion, Tono.tintaSuave, maxLineas = 1)
                }
            } else if (pila.fallo != null) {
                item { Fallo(alReintentar) }
            }
        }

        Entrada(
            valor = pila.borrador,
            alCambiar = { pila.borrador = it },
            puedeEnviar = pila.borrador.isNotBlank() && !pila.esperando,
            alEnviar = { alPreguntar(pila.borrador) },
            // Con la charla vacia, lo unico que hay para hacer es escribir: el
            // teclado sube solo y nadie tiene que buscar donde tocar.
            enfocar = pila.mensajes.isEmpty(),
        )
    }
}

/** Lo que escribio la persona: a la derecha, sobre el relleno de control. */
@Composable
private fun Pregunta(texto: String) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
        Box(
            Modifier
                .widthIn(max = 300.dp)
                .clip(RoundedCornerShape(Radio.tarjeta))
                .background(Tono.campo)
                .padding(horizontal = Medida.dentroTarjeta, vertical = 12.dp),
        ) {
            Texto(texto, Letra.descripcion, Tono.tinta)
        }
    }
}

/**
 * Lo que contesto el asistente: suelto sobre el fondo, sin globo. Es lo que
 * se vino a leer, y un globo alrededor le restaria ancho a la linea en un
 * telefono, que es donde menos sobra.
 *
 * Debajo, las fuentes: una pildora por pieza, con el icono de lo que es.
 */
@Composable
private fun Respuesta(mensaje: MensajeAsistente, alAbrir: (FuenteAsistente) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Texto(mensaje.texto, Letra.descripcion, Tono.tinta)

        if (mensaje.fuentes.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Texto(Textos.t("ficha.fuentes"), Letra.enlace, Tono.tintaSuave, maxLineas = 1)
                mensaje.fuentes.forEach { fuente ->
                    PildoraSuave(
                        texto = fuente.titulo,
                        alTocar = { alAbrir(fuente) },
                        icono = iconoDe(fuente),
                    )
                }
            }
        }
    }
}

/** El icono dice que es la pieza antes de leer su nombre. */
private fun iconoDe(fuente: FuenteAsistente) = when {
    fuente.tipo == "articulo" -> Icono.articulos
    fuente.tipo == "recorrido" -> Icono.recorridos
    fuente.tipoItem == "evento" -> Icono.calendario
    else -> Icono.pin
}

@Composable
private fun Fallo(alReintentar: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Texto(Textos.t("estado.error"), Letra.descripcion, Tono.tinta)
        PildoraSuave(texto = Textos.t("estado.reintentar"), alTocar = alReintentar)
    }
}

/**
 * El campo y el boton de mandar.
 *
 * Mandar es la accion de la pantalla, asi que va en el verde, que es el unico
 * de la pantalla. Sin nada escrito el boton queda en relleno de control: un
 * verde que no hace nada al tocarlo seria mentir.
 *
 * El campo crece hasta cuatro lineas. Una pregunta de alguien mayor puede ser
 * larga, y obligarla a una sola linea que se corre de costado es que nadie
 * pueda releer lo que escribio antes de mandarlo.
 */
@Composable
private fun Entrada(
    valor: String,
    alCambiar: (String) -> Unit,
    puedeEnviar: Boolean,
    alEnviar: () -> Unit,
    enfocar: Boolean,
) {
    val foco = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        if (enfocar) foco.requestFocus()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Tono.fondo)
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = Medida.margen, vertical = 10.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(Radio.tarjeta))
                .background(Tono.campo)
                .padding(horizontal = 18.dp, vertical = 14.dp),
        ) {
            if (valor.isEmpty()) {
                Texto(Textos.t("asistente.campo"), Letra.descripcion, Tono.tintaSuave, maxLineas = 1)
            }
            BasicTextField(
                value = valor,
                onValueChange = alCambiar,
                textStyle = Letra.descripcion.copy(color = Tono.tinta),
                maxLines = 4,
                cursorBrush = SolidColor(Tono.tinta),
                modifier = Modifier.fillMaxWidth().focusRequester(foco),
            )
        }

        val interaccion = recordarInteraccion()
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(if (puedeEnviar) Tono.primario else Tono.campo)
                .cedeAlTocar(interaccion)
                .clickable(
                    interactionSource = interaccion,
                    indication = null,
                    enabled = puedeEnviar,
                    onClick = alEnviar,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Glifo(
                icono = Icono.enviar,
                descripcion = Textos.t("asistente.enviar"),
                color = if (puedeEnviar) Tono.sobrePrimario else Tono.tintaSuave,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}
