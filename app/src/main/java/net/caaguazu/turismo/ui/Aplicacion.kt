package net.caaguazu.turismo.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.ui.mapa.MapaCaaguazu
import net.caaguazu.turismo.ui.piezas.BarraInferior
import net.caaguazu.turismo.ui.piezas.BarraSuperior
import net.caaguazu.turismo.ui.tema.Tono

/**
 * Armazon de la app: barra superior, contenido y barra inferior.
 *
 * El contenido se elige por seccion. Las secciones todavia sin construir se ven como
 * huecos marcados, no como pantallas vacias.
 */
@Composable
fun Aplicacion() {
    val navegador = remember { Navegador() }

    BackHandler(enabled = true) { navegador.volver() }

    Column(Modifier.fillMaxSize().background(Tono.papel)) {
        BarraSuperior(
            titulo = { Textos.t("app.nombre") },
            alTocarPerfil = navegador::abrirPerfil,
        )

        Box(Modifier.weight(1f)) {
            if (navegador.perfilAbierto) {
                SeccionPendiente("perfil.titulo")
            } else {
                when (navegador.seccion) {
                    Seccion.PRINCIPAL -> SeccionPendiente("nav.principal")
                    Seccion.INVENTARIO -> MapaCaaguazu()
                    Seccion.ARTICULOS -> SeccionPendiente("nav.articulos")
                    Seccion.RECORRIDOS -> SeccionPendiente("nav.recorridos")
                }
            }
        }

        BarraInferior(
            seleccionada = { navegador.seccion },
            alElegir = navegador::ir,
        )
    }
}
