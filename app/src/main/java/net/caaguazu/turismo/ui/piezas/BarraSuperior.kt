package net.caaguazu.turismo.ui.piezas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.ui.tema.Letra
import net.caaguazu.turismo.ui.tema.Tono

/**
 * Barra superior: marca a la izquierda, buscador al centro y ajustes a la derecha.
 *
 * Los ajustes viven aca y no en la barra inferior, que queda entera para las
 * secciones que el turista usa todo el tiempo. El icono de la derecha es
 * contextual: abre ajustes desde el resto de la app, y vuelve cuando ya se
 * esta adentro de ajustes o de diagnostico — es el unico boton de esa esquina,
 * asi que no puede quedarse pegado a una sola accion.
 */
@Composable
fun BarraSuperior(
    titulo: () -> String,
    mostrarVolver: Boolean,
    alTocarAccion: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth().background(Tono.papel)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Texto(texto = titulo(), estilo = Letra.tituloTarjeta, color = Tono.tinta)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Glifo(
                    icono = Icono.buscar,
                    descripcion = Textos.t("barra.buscar"),
                    color = Tono.tinta,
                    modifier = Modifier.size(24.dp),
                )
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(Tono.banda, CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = alTocarAccion,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Glifo(
                        icono = if (mostrarVolver) Icono.volver else Icono.ajustes,
                        descripcion = Textos.t(if (mostrarVolver) "accion.volver" else "barra.ajustes"),
                        color = Tono.tinta,
                        modifier = Modifier.size(21.dp),
                    )
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(Tono.linea))
    }
}
