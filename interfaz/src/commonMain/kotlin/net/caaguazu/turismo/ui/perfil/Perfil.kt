package net.caaguazu.turismo.ui.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.caaguazu.turismo.core.Ajustes
import net.caaguazu.turismo.core.Entorno
import net.caaguazu.turismo.core.Idioma
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.ui.piezas.BotonIcono
import net.caaguazu.turismo.ui.piezas.CabeceraHoja
import net.caaguazu.turismo.ui.piezas.CabeceraPantalla
import net.caaguazu.turismo.ui.piezas.Glifo
import net.caaguazu.turismo.ui.piezas.Hairline
import net.caaguazu.turismo.ui.piezas.HojaInferior
import net.caaguazu.turismo.ui.piezas.Icono
import net.caaguazu.turismo.ui.piezas.Interruptor
import net.caaguazu.turismo.ui.piezas.Tarjeta
import net.caaguazu.turismo.ui.piezas.Texto
import net.caaguazu.turismo.ui.piezas.Tirador
import net.caaguazu.turismo.ui.tema.Letra
import net.caaguazu.turismo.ui.tema.Medida
import net.caaguazu.turismo.ui.tema.Tono

/**
 * Perfil.
 *
 * La primera version no tiene cuenta, asi que aca solo hay ajustes del telefono,
 * agrupados en tarjetas por tema como en la referencia.
 *
 * El diagnostico se abre tocando siete veces la version: no es una funcion de
 * producto, es la forma de que alguien pueda mandarme el registro cuando algo
 * falle en su telefono.
 */
@Composable
fun PantallaPerfil(
    alVolver: () -> Unit,
    alAbrirDiagnostico: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var toques by remember { mutableIntStateOf(0) }
    var eligiendoIdioma by remember { mutableStateOf(false) }

    Box(modifier.fillMaxSize().background(Tono.fondo)) {
        Column(Modifier.fillMaxSize()) {
            // La cabecera va fuera de la lista: dentro habria que compensar el
            // margen lateral de la lista con un padding negativo, que Compose no
            // admite y que ademas seria una forma rara de decir "esto no es un
            // item mas".
            CabeceraPantalla(Textos.t("barra.ajustes")) {
                BotonIcono(
                    icono = Icono.volver,
                    descripcion = Textos.t("accion.volver"),
                    alTocar = alVolver,
                )
            }

            LazyColumn(
                contentPadding = PaddingValues(
                    start = Medida.margen,
                    end = Medida.margen,
                    bottom = Medida.colaDeLista,
                ),
                verticalArrangement = Arrangement.spacedBy(Medida.entreTarjetas),
            ) {
                item { Grupo(Textos.t("perfil.general")) }
                item {
                    Tarjeta(Modifier.fillMaxWidth()) {
                        Column {
                            FilaIdioma { eligiendoIdioma = true }
                            // El hairline va con la fila, no antes: donde no
                            // hay avisos —iOS, por ahora— una linea sola
                            // debajo del idioma seria un separador que no
                            // separa nada.
                            if (avisosDisponibles) {
                                Hairline(
                                    Modifier.fillMaxWidth().padding(horizontal = Medida.dentroTarjeta),
                                )
                                FilaDeAvisos()
                            }
                        }
                    }
                }

                item { Grupo(Textos.t("perfil.acercaDe")) }
                item {
                    Tarjeta(Modifier.fillMaxWidth()) {
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
                                .padding(Medida.dentroTarjeta),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Texto(Textos.t("diag.version"), Letra.chip, Tono.tintaSuave, maxLineas = 1)
                            Texto(Entorno.version, Letra.chip, Tono.tinta, maxLineas = 1)
                        }
                    }
                }
            }
        }

        HojaInferior(visible = eligiendoIdioma, alCerrar = { eligiendoIdioma = false }) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) { Tirador() }
            CabeceraHoja(titulo = Textos.t("perfil.idioma"))

            // La lista sale del panel, no del APK: el guarani esta previsto y va
            // a aparecer ahi antes de que salga una version nueva de la app.
            Idioma.disponibles.forEach { disponible ->
                FilaIdiomaDisponible(
                    nombre = disponible.nombre,
                    elegido = disponible.codigo == Idioma.actual,
                    alTocar = {
                        // Lo embebido queda cambiado aca mismo. Lo que el panel
                        // tenga traducido lo trae `Aplicacion`, que reacciona al
                        // idioma: pedirlo desde esta hoja no funcionaba, porque
                        // cambiar de idioma cruza la cara y destruye la hoja
                        // —con su alcance— antes de que el pedido vuelva.
                        Idioma.elegir(disponible.codigo)
                        eligiendoIdioma = false
                    },
                )
            }
        }
    }
}

/** El idioma elegido, con su nombre en su propio idioma. */
@Composable
private fun FilaIdioma(alTocar: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = alTocar).padding(Medida.dentroTarjeta),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Texto(Textos.t("perfil.idioma"), Letra.tituloTarjeta, Tono.tinta, maxLineas = 1)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Texto(Idioma.nombreDe(Idioma.actual), Letra.chip, Tono.tintaSuave, maxLineas = 1)
            Glifo(Icono.chevron, Textos.t("perfil.idioma"), Tono.tintaSuave, Modifier.size(17.dp))
        }
    }
}

@Composable
private fun FilaIdiomaDisponible(nombre: String, elegido: Boolean, alTocar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = alTocar)
            .padding(horizontal = Medida.margen, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Texto(
            texto = nombre,
            estilo = Letra.tituloTarjeta,
            color = if (elegido) Tono.tinta else Tono.tintaSuave,
            maxLineas = 1,
        )
        if (elegido) {
            Glifo(Icono.tilde, nombre, Tono.primario, Modifier.size(20.dp))
        }
    }
}

/** Titulo de grupo: va fuera de la tarjeta, como en la referencia. */
@Composable
private fun Grupo(texto: String) {
    Texto(
        texto = texto,
        estilo = Letra.enlace,
        color = Tono.tintaSuave,
        maxLineas = 1,
        modifier = Modifier.padding(start = 6.dp, top = 10.dp, bottom = 0.dp),
    )
}
