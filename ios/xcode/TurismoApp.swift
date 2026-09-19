import SwiftUI

/// El arranque de la app.
///
/// `ignoresSafeArea` es a proposito: el mapa ocupa la pantalla entera y los
/// controles flotan encima, igual que en Android. Compose maneja sus propios
/// margenes de seguridad.
@main
struct TurismoApp: App {
    var body: some Scene {
        WindowGroup {
            VistaCompose()
                .ignoresSafeArea()
        }
    }
}
