package net.caaguazu.turismo.ui.perfil

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import net.caaguazu.turismo.BuildConfig
import net.caaguazu.turismo.core.Ajustes
import net.caaguazu.turismo.core.Avisos
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.core.Vigilante
import net.caaguazu.turismo.ui.piezas.Glifo
import net.caaguazu.turismo.ui.piezas.Hairline
import net.caaguazu.turismo.ui.piezas.Icono
import net.caaguazu.turismo.ui.piezas.Interruptor
import net.caaguazu.turismo.ui.piezas.Tarjeta
import net.caaguazu.turismo.ui.piezas.Texto
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
fun PantallaPerfil(alAbrirDiagnostico: () -> Unit, modifier: Modifier = Modifier) {
    var toques by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(Tono.fondo),
        contentPadding = PaddingValues(Medida.margen),
        verticalArrangement = Arrangement.spacedBy(Medida.entreTarjetas),
    ) {
        item { Grupo(Textos.t("perfil.general")) }
        item {
            Tarjeta(Modifier.fillMaxWidth()) {
                Column {
                    Fila(Textos.t("perfil.idioma"))
                    Hairline(Modifier.fillMaxWidth().padding(horizontal = Medida.dentroTarjeta))
                    FilaAvisos()
                }
            }
        }

        item { Grupo(Textos.t("perfil.acercaDe")) }
        item {
            Tarjeta(Modifier.fillMaxWidth()) {
                Column {
                    Fila(Textos.t("perfil.acerca"))
                    Hairline(Modifier.fillMaxWidth().padding(horizontal = Medida.dentroTarjeta))
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
                        Texto(BuildConfig.VERSION_NAME, Letra.chip, Tono.tinta, maxLineas = 1)
                    }
                }
            }
        }
    }
}

/**
 * El interruptor de avisos.
 *
 * Encenderlo pide el permiso del sistema en Android 13 o mas nuevo. Si la
 * persona lo niega, el interruptor vuelve solo a apagado en vez de quedar
 * encendido sin avisar nunca — que es la forma mas segura de que alguien crea
 * que la app esta rota.
 */
@Composable
private fun FilaAvisos() {
    val contexto = LocalContext.current
    var encendido by remember { mutableStateOf(Ajustes.avisosActivos) }

    fun encender() {
        Ajustes.avisosActivos = true
        encendido = true
        Vigilante.programar(contexto)
    }

    fun apagar() {
        Ajustes.avisosActivos = false
        encendido = false
        // Se olvida lo anotado: volver a encender empieza limpio y no dispara
        // un aviso por cada cosa publicada mientras estuvo apagado.
        Ajustes.olvidarAvisados()
        Vigilante.cancelar(contexto)
    }

    val pedirPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { concedido -> if (concedido) encender() else apagar() }

    Row(
        modifier = Modifier.fillMaxWidth().padding(Medida.dentroTarjeta),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.weight(1f)) {
            Texto(Textos.t("perfil.avisos"), Letra.tituloTarjeta, Tono.tinta, maxLineas = 1)
            Texto(Textos.t("perfil.avisosDetalle"), Letra.chip, Tono.tintaSuave, maxLineas = 2)
        }
        Interruptor(
            encendido = encendido,
            descripcion = Textos.t("perfil.avisos"),
            alCambiar = { quiere ->
                if (!quiere) {
                    apagar()
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    !Avisos.hayPermiso(contexto)
                ) {
                    pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    encender()
                }
            },
        )
    }
}

/** Titulo de grupo: va fuera de la tarjeta, como en la referencia. */
@Composable
private fun Grupo(texto: String) {
    Texto(
        texto = texto,
        estilo = Letra.chip,
        color = Tono.tintaSuave,
        maxLineas = 1,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 2.dp),
    )
}

@Composable
private fun Fila(texto: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(Medida.dentroTarjeta),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Texto(texto, Letra.tituloTarjeta, Tono.tinta, maxLineas = 1)
        Glifo(Icono.chevron, texto, Tono.tintaSuave, Modifier.size(18.dp))
    }
}
