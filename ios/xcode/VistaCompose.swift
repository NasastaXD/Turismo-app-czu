import SwiftUI

// Sin esta linea el compilador no ve nada de Kotlin y dice
// "cannot find 'PuntoDeEntradaKt' in scope", que suena a que falta el
// framework cuando lo que falta es importarlo.
import TurismoKit

/// El puente entre SwiftUI y las pantallas de Compose.
///
/// Es todo el Swift que la app necesita: Compose Multiplatform devuelve un
/// UIViewController y SwiftUI lo envuelve. Si esto crece, algo se hizo mal —
/// las pantallas van en Kotlin, compartidas con Android.
struct VistaCompose: UIViewControllerRepresentable {

    func makeUIViewController(context: Context) -> UIViewController {
        // `PuntoDeEntradaKt` es el nombre que Kotlin/Native le da al archivo
        // PuntoDeEntrada.kt visto desde Swift. En Objective-C la clase lleva
        // delante el nombre del framework —TurismoKitPuntoDeEntradaKt— y Swift
        // se come ese prefijo porque coincide con el modulo importado. El paso
        // "Que exporta el framework" del workflow Verificar iOS imprime los dos
        // leidos de la cabecera generada: si no coinciden, ese log manda.
        PuntoDeEntradaKt.puntoDeEntrada()
    }

    func updateUIViewController(_ controlador: UIViewController, context: Context) {
        // Nada: el estado vive del lado de Compose.
    }
}
