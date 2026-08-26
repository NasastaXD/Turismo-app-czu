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
import net.caaguazu.turismo.ui.inventario.Inventario
import net.caaguazu.turismo.ui.perfil.PantallaDiagnostico
import net.caaguazu.turismo.ui.perfil.PantallaPerfil
import net.caaguazu.turismo.ui.piezas.BarraInferior
import net.caaguazu.turismo.ui.piezas.BarraSuperior
import net.caaguazu.turismo.ui.tema.Tono

/**
 * Armazon de la app: barra superior, contenido y barra inferior.
 *
 * Las secciones todavia sin construir se ven como huecos marcados, no como
 * pantallas vacias que aparentan estar terminadas.
 */
@Composable
fun Aplicacion() {
    val navegador = remember { Navegador() }

    BackHandler(enabled = true) { navegador.volver() }

    Column(Modifier.fillMaxSize().background(Tono.papel)) {
        // La ficha lleva su propia cabecera sobre la foto: la barra general
        // taparia el titulo justo donde tiene que leerse.
        val conBarra = !navegador.enFicha()

        if (conBarra) {
            BarraSuperior(
                titulo = { Textos.t("app.nombre") },
                alTocarPerfil = navegador::abrirPerfil,
            )
        }

        Box(Modifier.weight(1f)) {
            when {
                navegador.diagnosticoAbierto -> PantallaDiagnostico()
                navegador.perfilAbierto -> PantallaPerfil(navegador::abrirDiagnostico)
                else -> when (navegador.seccion) {
                    Seccion.PRINCIPAL -> SeccionPendiente("nav.principal")
                    Seccion.INVENTARIO -> Inventario(navegador.inventario)
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

private fun Navegador.enFicha(): Boolean =
    !perfilAbierto && !diagnosticoAbierto &&
        seccion == Seccion.INVENTARIO &&
        inventario.actual is net.caaguazu.turismo.ui.inventario.RutaInv.Ficha
