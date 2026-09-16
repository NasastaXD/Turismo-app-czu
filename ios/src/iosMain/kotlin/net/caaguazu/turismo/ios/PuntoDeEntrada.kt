package net.caaguazu.turismo.ios

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * Lo unico que Xcode necesita llamar.
 *
 * Del lado de Swift son dos lineas dentro de un `UIViewControllerRepresentable`:
 *
 *     import Turismo
 *     TurismoPuntoDeEntradaKt.puntoDeEntrada()
 *
 * El nombre del framework sale de `baseName` en `ios/build.gradle.kts`. El
 * proyecto de Xcode todavia no existe en el repo: generarlo requiere un Mac, y
 * este entorno es Linux.
 */
fun puntoDeEntrada(): UIViewController = ComposeUIViewController {
    PantallaMapaIos()
}
