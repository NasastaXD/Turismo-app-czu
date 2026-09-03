package net.caaguazu.turismo.ui.buscar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.datos.Datos
import net.caaguazu.turismo.ui.PilaBusqueda
import net.caaguazu.turismo.ui.piezas.CabeceraHoja
import net.caaguazu.turismo.ui.piezas.ChipFiltro
import net.caaguazu.turismo.ui.piezas.Estado
import net.caaguazu.turismo.ui.piezas.GrupoFiltro
import net.caaguazu.turismo.ui.piezas.HojaInferior
import net.caaguazu.turismo.ui.piezas.PildoraPrimaria
import net.caaguazu.turismo.ui.piezas.Tirador
import net.caaguazu.turismo.ui.piezas.cargar
import net.caaguazu.turismo.ui.tema.Medida

/** Cuanto puede crecer la hoja antes de tener que hacer scroll adentro. */
private val ALTO_MAXIMO = 520.dp

/**
 * La hoja de filtros.
 *
 * Es la pieza que faltaba y la que cambia como se usa la app: antes filtrar era
 * una fila de chips de etiquetas metida entre el buscador y los resultados, y
 * no habia forma de filtrar por zona ni por precio sin agregar dos filas mas a
 * una pantalla que ya no daba. Sacarlo a una hoja libera la pantalla y deja
 * lugar para los cuatro ejes.
 *
 * Los filtros se aplican al tocarlos, no al confirmar. El boton de abajo cierra
 * la hoja: para cuando alguien lo toca ya vio, detras del velo, cuantos
 * resultados le quedaron.
 */
@Composable
fun BoxScope.HojaFiltros(pila: PilaBusqueda) {
    val (categorias, _) = cargar { Datos.api.categorias() }
    val (zonas, _) = cargar { Datos.api.zonas() }
    val (etiquetas, _) = cargar { Datos.api.etiquetas() }

    HojaInferior(visible = pila.filtrosAbiertos, alCerrar = pila::cerrarFiltros) {
        Box(Modifier.fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.TopCenter) {
            Tirador()
        }

        CabeceraHoja(
            titulo = Textos.t("filtro.titulo"),
            accion = Textos.t("filtro.limpiar").takeIf { !pila.filtros.vacios },
            alTocarAccion = pila::limpiar,
        )

        LazyColumn(
            modifier = Modifier.heightIn(max = ALTO_MAXIMO),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item {
                val lista = (categorias.value as? Estado.Listo)?.valor.orEmpty()
                if (lista.isNotEmpty()) {
                    GrupoFiltro(Textos.t("filtro.categoria")) {
                        FilaDeChips(
                            opciones = lista.map { it.id to it.nombre },
                            elegida = pila.filtros.categoria,
                            alElegir = { id ->
                                pila.filtros = pila.filtros.copy(
                                    categoria = if (pila.filtros.categoria == id) null else id,
                                )
                            },
                        )
                    }
                }
            }

            item {
                val lista = (zonas.value as? Estado.Listo)?.valor.orEmpty()
                if (lista.isNotEmpty()) {
                    GrupoFiltro(Textos.t("filtro.zona")) {
                        FilaDeChips(
                            opciones = lista.map { it.id to it.nombre },
                            elegida = pila.filtros.zona,
                            alElegir = { id ->
                                pila.filtros = pila.filtros.copy(
                                    zona = if (pila.filtros.zona == id) null else id,
                                )
                            },
                        )
                    }
                }
            }

            item {
                val lista = (etiquetas.value as? Estado.Listo)?.valor.orEmpty()
                if (lista.isNotEmpty()) {
                    GrupoFiltro(Textos.t("filtro.etiqueta")) {
                        FilaDeChips(
                            opciones = lista.map { it.id to it.nombre },
                            elegida = pila.filtros.etiqueta,
                            alElegir = { id ->
                                pila.filtros = pila.filtros.copy(
                                    etiqueta = if (pila.filtros.etiqueta == id) null else id,
                                )
                            },
                        )
                    }
                }
            }

            item {
                GrupoFiltro(Textos.t("filtro.precio")) {
                    FilaDeChips(
                        // 0 es gratis y tiene su propia palabra; 1 a 4 se dicen
                        // con los simbolos, que es como ya se leen en la ficha.
                        opciones = listOf(0 to Textos.t("precio.gratis")) +
                            (1..4).map { nivel -> nivel to "$".repeat(nivel) },
                        elegida = pila.filtros.precioMaximo,
                        alElegir = { nivel ->
                            pila.filtros = pila.filtros.copy(
                                precioMaximo = if (pila.filtros.precioMaximo == nivel) null else nivel,
                            )
                        },
                    )
                }
            }
        }

        PildoraPrimaria(
            texto = Textos.t("filtro.aplicar"),
            alTocar = pila::cerrarFiltros,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Medida.margen, vertical = 10.dp),
        )
    }
}

/**
 * Una fila de chips que se corre.
 *
 * Se corre y no se envuelve a proposito: con veinte etiquetas, envolver hace
 * crecer la hoja hasta tapar los resultados que uno esta tratando de mirar.
 */
@Composable
private fun FilaDeChips(
    opciones: List<Pair<Int, String>>,
    elegida: Int?,
    alElegir: (Int) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = Medida.margen),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(opciones, key = { it.first }) { (id, nombre) ->
            ChipFiltro(
                texto = nombre,
                activo = elegida == id,
                alTocar = { alElegir(id) },
            )
        }
    }
}
