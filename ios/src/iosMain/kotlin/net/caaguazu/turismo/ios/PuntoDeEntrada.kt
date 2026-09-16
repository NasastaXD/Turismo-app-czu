package net.caaguazu.turismo.ios

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * Lo unico que Xcode necesita llamar.
 *
 * Del lado de Swift son dos lineas dentro de un `UIViewControllerRepresentable`
 * —esta escrito en `ios/xcode/VistaCompose.swift`—:
 *
 *     import Turismo
 *     PuntoDeEntradaKt.puntoDeEntrada()
 *
 * En Objective-C la clase se llama `TurismoPuntoDeEntradaKt`: Kotlin/Native le
 * pone delante el `baseName` del framework. Swift se come ese prefijo porque
 * coincide con el nombre del modulo que se importa, y por eso los dos nombres
 * no son iguales. El paso "Que exporta el framework" del workflow imprime los
 * dos leidos del encabezado generado, que es la unica fuente que manda.
 *
 * El proyecto de Xcode todavia no existe en el repo: generarlo requiere un Mac,
 * y este entorno es Linux.
 */
fun puntoDeEntrada(): UIViewController = ComposeUIViewController {
    PantallaMapaIos()
}
