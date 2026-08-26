package net.caaguazu.turismo.ui.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.caaguazu.turismo.BuildConfig
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.ui.piezas.Glifo
import net.caaguazu.turismo.ui.piezas.Hairline
import net.caaguazu.turismo.ui.piezas.Icono
import net.caaguazu.turismo.ui.piezas.Texto
import net.caaguazu.turismo.ui.tema.Letra
import net.caaguazu.turismo.ui.tema.Medida
import net.caaguazu.turismo.ui.tema.Tono

/**
 * Perfil.
 *
 * La primera version no tiene cuenta, asi que aca solo hay ajustes del telefono.
 * El diagnostico se abre tocando siete veces la version: no es una funcion de
 * producto, es la forma de que alguien pueda mandarme el registro cuando algo
 * falle en su telefono.
 */
@Composable
fun PantallaPerfil(alAbrirDiagnostico: () -> Unit, modifier: Modifier = Modifier) {
    var toques by remember { mutableIntStateOf(0) }

    Column(modifier.fillMaxSize().background(Tono.papel)) {
        Fila(Textos.t("perfil.idioma"))
        Fila(Textos.t("perfil.acerca"))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    toques++
                    if (toques >= 7) {
                        toques = 0
                        alAbrirDiagnostico()
                    }
                }
                .padding(Medida.margen),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Texto(Textos.t("diag.version"), Letra.chip, Tono.tintaSuave, maxLineas = 1)
            Texto(BuildConfig.VERSION_NAME, Letra.chip, Tono.tinta, maxLineas = 1)
        }
    }
}

@Composable
private fun Fila(texto: String) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Medida.margen),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Texto(texto, Letra.tituloTarjeta, Tono.tinta, maxLineas = 1)
            Glifo(Icono.volver, texto, Tono.tintaSuave, Modifier.size(18.dp))
        }
        Hairline(Modifier.fillMaxWidth().padding(horizontal = Medida.margen))
    }
}
