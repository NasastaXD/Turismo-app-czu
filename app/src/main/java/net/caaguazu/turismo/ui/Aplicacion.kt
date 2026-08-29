package net.caaguazu.turismo.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import net.caaguazu.turismo.core.Ajustes
import net.caaguazu.turismo.core.Avisos
import net.caaguazu.turismo.core.Vigilante
import net.caaguazu.turismo.ui.articulos.Articulos
import net.caaguazu.turismo.ui.buscar.Buscar
import net.caaguazu.turismo.ui.perfil.PantallaDiagnostico
import net.caaguazu.turismo.ui.perfil.PantallaPerfil
import net.caaguazu.turismo.ui.piezas.BarraInferior
import net.caaguazu.turismo.ui.piezas.ConMovimientoDelSistema
import net.caaguazu.turismo.ui.piezas.Cruce
import net.caaguazu.turismo.ui.principal.Principal
import net.caaguazu.turismo.ui.recorridos.Recorridos
import net.caaguazu.turismo.ui.tema.Tono
import net.caaguazu.turismo.ui.tema.recordarAnimacionesActivas

/**
 * Armazon de la app: contenido y barra inferior.
 *
 * No decide nada mas. No hay barra superior —cada pantalla abre con su propio
 * titulo— y tampoco pone el hueco de la barra de estado: eso lo resuelve cada
 * cabecera, porque las pantallas que van a sangre —la ficha, el mapa, el
 * articulo— necesitan llegar hasta arriba de todo y un padding puesto aca se lo
 * impedia a todas por igual.
 */
@Composable
fun Aplicacion() {
    val navegador = remember { Navegador() }

    // La app sigue el ajuste del telefono.
    //
    // Va en SideEffect y no suelto en el cuerpo: esta composicion LEE colores de
    // Tono, y escribir en composicion un estado que la misma composicion lee es
    // una escritura hacia atras — Compose la invalida y vuelve a componer. El
    // SideEffect corre despues de que la composicion cerro, que es cuando
    // escribir es seguro.
    //
    // El valor inicial ya quedo puesto en App.onCreate, asi que esto solo actua
    // cuando la persona cambia el modo con la app abierta y no hay un cuadro
    // dibujado con el tema equivocado.
    val oscuro = isSystemInDarkTheme()
    SideEffect { Tono.oscuro = oscuro }

    BackHandler(enabled = true) { navegador.volver() }

    PedirAvisosAlArrancar()

    ConMovimientoDelSistema(recordarAnimacionesActivas()) {
        Column(Modifier.fillMaxSize().background(Tono.fondo)) {
            Box(Modifier.weight(1f)) {
                Cruce(navegador.caraActual()) { _ ->
                    when {
                        navegador.diagnosticoAbierto -> PantallaDiagnostico(alVolver = navegador::volver)
                        navegador.perfilAbierto -> PantallaPerfil(
                            alVolver = navegador::volver,
                            alAbrirDiagnostico = navegador::abrirDiagnostico,
                        )
                        else -> when (navegador.seccion) {
                            Seccion.INICIO -> Principal(
                                alBuscar = { navegador.ir(Seccion.BUSCAR) },
                                alBuscarCategoria = navegador::buscarPorCategoria,
                                alAbrirFicha = navegador::abrirFicha,
                                alVerArticulo = { id ->
                                    navegador.ir(Seccion.ARTICULOS)
                                    navegador.articulos.abrir(id)
                                },
                                alVerRecorrido = { id ->
                                    navegador.ir(Seccion.RECORRIDOS)
                                    navegador.recorridos.abrir(id)
                                },
                                alVerArticulos = { navegador.ir(Seccion.ARTICULOS) },
                                alVerRecorridos = { navegador.ir(Seccion.RECORRIDOS) },
                                alAbrirPerfil = navegador::abrirPerfil,
                            )

                            Seccion.BUSCAR -> Buscar(
                                pila = navegador.busqueda,
                                alAbrirPerfil = navegador::abrirPerfil,
                            )

                            Seccion.ARTICULOS -> Articulos(
                                pila = navegador.articulos,
                                alAbrirPerfil = navegador::abrirPerfil,
                            )

                            Seccion.RECORRIDOS -> Recorridos(
                                pila = navegador.recorridos,
                                alAbrirPerfil = navegador::abrirPerfil,
                                alAbrirFicha = navegador::abrirFicha,
                            )
                        }
                    }
                }
            }

            BarraInferior(
                seleccionada = { navegador.seccion },
                alElegir = navegador::ir,
            )
        }
    }
}

/**
 * Los avisos arrancan encendidos, pero en Android 13 o mas nuevo eso no alcanza:
 * el sistema exige pedir el permiso una vez. Se pide al primer arranque nomas
 * —`avisosDecididos()` evita repetirlo despues— y si se niega, el interruptor
 * cae solo a apagado en vez de quedar prendido sin avisar nunca.
 */
@Composable
private fun PedirAvisosAlArrancar() {
    val contexto = LocalContext.current

    val pedirPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { concedido ->
        Ajustes.avisosActivos = concedido
        if (concedido) Vigilante.programar(contexto) else Vigilante.cancelar(contexto)
    }

    LaunchedEffect(Unit) {
        if (Ajustes.avisosDecididos()) return@LaunchedEffect
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !Avisos.hayPermiso(contexto)) {
            pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            Ajustes.avisosActivos = true
        }
    }
}
