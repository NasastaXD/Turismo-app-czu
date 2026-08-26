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
import net.caaguazu.turismo.ui.articulos.Articulos
import net.caaguazu.turismo.ui.inventario.Inventario
import net.caaguazu.turismo.ui.inventario.RutaInv
import net.caaguazu.turismo.ui.recorridos.Recorridos
import net.caaguazu.turismo.ui.perfil.PantallaDiagnostico
import net.caaguazu.turismo.ui.principal.Principal
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
                    Seccion.PRINCIPAL -> Principal(
                        alVerArticulo = { id ->
                            navegador.ir(Seccion.ARTICULOS)
                            navegador.articulos.abrir(id)
                        },
                        alVerRecorrido = { id ->
                            navegador.ir(Seccion.RECORRIDOS)
                            navegador.recorridos.abrir(id)
                        },
                        alVerInventario = { navegador.ir(Seccion.INVENTARIO) },
                        alVerArticulos = { navegador.ir(Seccion.ARTICULOS) },
                        alVerRecorridos = { navegador.ir(Seccion.RECORRIDOS) },
                    )
                    Seccion.INVENTARIO -> Inventario(navegador.inventario)
                    Seccion.ARTICULOS -> Articulos(navegador.articulos)
                    Seccion.RECORRIDOS -> Recorridos(
                        pila = navegador.recorridos,
                        alAbrirFicha = { id ->
                            // Abrir una ficha desde un recorrido lleva al
                            // inventario, que es donde vive esa pantalla.
                            navegador.ir(Seccion.INVENTARIO)
                            navegador.inventario.ir(RutaInv.Ficha(id))
                        },
                    )
                }
            }
        }

        BarraInferior(
            seleccionada = { navegador.seccion },
            alElegir = navegador::ir,
        )
    }
}

/** Las pantallas que llevan su propia cabecera sobre la foto o el mapa. */
private fun Navegador.enFicha(): Boolean {
    if (perfilAbierto || diagnosticoAbierto) return false
    return when (seccion) {
        Seccion.INVENTARIO -> inventario.actual is RutaInv.Ficha
        Seccion.ARTICULOS -> articulos.abierto != null
        Seccion.RECORRIDOS -> recorridos.abierto != null
        Seccion.PRINCIPAL -> false
    }
}
