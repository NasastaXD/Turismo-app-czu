import SwiftUI

/// El puente entre SwiftUI y las pantallas de Compose.
///
/// Es todo el Swift que la app necesita: Compose Multiplatform devuelve un
/// UIViewController y SwiftUI lo envuelve. Si esto crece, algo se hizo mal —
/// las pantallas van en Kotlin, compartidas con Android.
struct VistaCompose: UIViewControllerRepresentable {

    func makeUIViewController(context: Context) -> UIViewController {
        // `PuntoDeEntradaKt` es el nombre que Kotlin/Native le da al archivo
        // PuntoDeEntrada.kt visto desde Swift. El nombre exacto del simbolo lo
        // imprime el paso "Que exporta el framework" del workflow Verificar
        // iOS, leido de la cabecera generada: si no coincide, ese log manda.
        PuntoDeEntradaKt.puntoDeEntrada()
    }

    func updateUIViewController(_ controlador: UIViewController, context: Context) {
        // Nada: el estado vive del lado de Compose.
    }
}
