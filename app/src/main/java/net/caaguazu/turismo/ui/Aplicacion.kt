package net.caaguazu.turismo.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import net.caaguazu.turismo.ui.articulos.Articulos
import net.caaguazu.turismo.ui.inventario.Inventario
import net.caaguazu.turismo.ui.inventario.RutaInv
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
 * Ya no hay barra superior. Cada pantalla abre con su propio titulo grande
 * alineado a la izquierda, que es de donde sale el aire del sistema: una barra
 * con el nombre de la app repetido arriba de las cinco pantallas gastaba alto
 * para decir algo que nadie necesita leer cinco veces.
 *
 * El armazon solo decide una cosa: si el contenido arranca debajo de la barra
 * de estado o corre por debajo de ella. Las pantallas con foto o mapa a sangre
 * corren por debajo — la imagen llega hasta arriba de todo y los botones
 * flotan encima.
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

    ConMovimientoDelSistema(recordarAnimacionesActivas()) {
        Column(Modifier.fillMaxSize().background(Tono.fondo)) {
            val aSangre = navegador.enPantallaASangre()

            Box(
                Modifier
                    .weight(1f)
                    .then(if (aSangre) Modifier else Modifier.statusBarsPadding()),
            ) {
                Cruce(navegador.caraActual()) { _ ->
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
                                alVerFicha = { id ->
                                    navegador.ir(Seccion.INVENTARIO)
                                    navegador.inventario.ir(RutaInv.Ficha(id))
                                },
                                alVerCategoria = { categoria ->
                                    navegador.ir(Seccion.INVENTARIO)
                                    navegador.inventario.ir(RutaInv.Lista(categoria))
                                },
                                alVerInventario = { navegador.ir(Seccion.INVENTARIO) },
                                alVerArticulos = { navegador.ir(Seccion.ARTICULOS) },
                                alVerRecorridos = { navegador.ir(Seccion.RECORRIDOS) },
                                alAbrirPerfil = navegador::abrirPerfil,
                            )

                            Seccion.INVENTARIO -> Inventario(
                                pila = navegador.inventario,
                                alAbrirPerfil = navegador::abrirPerfil,
                            )

                            Seccion.ARTICULOS -> Articulos(
                                pila = navegador.articulos,
                                alAbrirPerfil = navegador::abrirPerfil,
                            )

                            Seccion.RECORRIDOS -> Recorridos(
                                pila = navegador.recorridos,
                                alAbrirPerfil = navegador::abrirPerfil,
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
            }

            BarraInferior(
                seleccionada = { navegador.seccion },
                alElegir = navegador::ir,
            )
        }
    }
}

/** Las pantallas cuya foto o mapa llega hasta arriba de todo. */
private fun Navegador.enPantallaASangre(): Boolean {
    if (perfilAbierto || diagnosticoAbierto) return false
    return when (seccion) {
        Seccion.INVENTARIO -> inventario.actual is RutaInv.Ficha
        Seccion.ARTICULOS -> articulos.abierto != null
        Seccion.RECORRIDOS -> recorridos.abierto != null
        Seccion.PRINCIPAL -> false
    }
}
