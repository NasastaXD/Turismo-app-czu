package net.caaguazu.turismo.ui

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
import net.caaguazu.turismo.core.Idioma
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.datos.Datos
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

    // `volver` devuelve false cuando ya no queda nada que deshacer, y ese caso
    // hay que atenderlo: en Android un BackHandler activo se come el gesto, asi
    // que ignorar el resultado dejaba al inicio sin forma de cerrar la app — el
    // gesto de volver no hacia absolutamente nada, para siempre.
    ManejarVolver(navegador::volver)

    // Los textos se cargan desde aca y no desde la hoja que cambia el idioma:
    // el idioma es parte de la cara que cruza, asi que ese gesto destruye la
    // hoja —y con ella su alcance— antes de que el pedido termine.
    //
    // Primero el respaldo empaquetado del idioma nuevo y encima lo del panel.
    // Los dos en el mismo efecto y en este orden: un solo dueño de "los textos
    // siguen al idioma actual" es lo que evita que la interfaz quede en un
    // idioma y el contenido en otro.
    LaunchedEffect(Idioma.actual) {
        Textos.cargarEmbebido(Idioma.actual)
        Datos.refrescarTextos()
    }

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
