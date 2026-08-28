package net.caaguazu.turismo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.ui.piezas.Texto
import net.caaguazu.turismo.ui.tema.Letra
import net.caaguazu.turismo.ui.tema.Tono

/**
 * Hueco de una seccion todavia sin construir.
 *
 * Muestra la clave de texto entre marcas angulares a proposito: asi es imposible
 * confundir un hueco pendiente con una pantalla terminada.
 */
@Composable
fun SeccionPendiente(clave: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize().background(Tono.fondo),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(40.dp),
        ) {
            Texto(
                texto = Textos.t(clave),
                estilo = Letra.tituloPantalla,
                color = Tono.tinta,
                alinear = TextAlign.Center,
            )
            Texto(
                texto = Textos.t("estado.pendiente"),
                estilo = Letra.descripcion,
                color = Tono.tintaSuave,
                alinear = TextAlign.Center,
            )
        }
    }
}
