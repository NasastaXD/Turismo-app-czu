import XCTest

/// Recorre la app y saca las capturas para la ficha de la App Store.
///
/// La corre el workflow "Capturas para la tienda", una vez por idioma, sobre un
/// iPhone 16 Pro: su pantalla da 1206 x 2622, que es la medida que App Store
/// Connect exige ("iPhone con Dynamic Island, pantalla mediana"). Las capturas
/// salen de `XCUIScreen` y no de `simctl io screenshot` desde afuera: la del
/// host a veces fotografia el escritorio de SpringBoard en lugar de la app, y
/// la de adentro del test es siempre la app.
///
/// **Se toca por coordenadas, no por etiquetas.** Las pantallas son Compose, y
/// lo que el arbol de accesibilidad expone depende de la version de Compose
/// Multiplatform y del idioma; una coordenada normalizada no. Las posiciones
/// salen de medir una captura real de la app (docs/imagenes) y de las medidas
/// de `BarraInferior`: cinco botones repartidos con `SpaceEvenly`, el del medio
/// mas grande. Si la barra cambia de forma, esto se corrige aca.
///
/// Cada pantalla se fotografia **desde un arranque nuevo**. iOS no tiene gesto
/// de volver del sistema y el boton de volver de cada pantalla esta en otro
/// lugar; relanzar cuesta diez segundos y no depende de nada.
///
/// No hay ni un `XCTAssert` que corte el recorrido: una pantalla que no se pudo
/// abrir deja una captura equivocada, que se ve al mirarlas, y las demas salen
/// igual. Una captura de mas no cuesta nada; una corrida entera perdida si.
final class CapturasTiendaUITests: XCTestCase {

    /// Barra inferior, con el asistente encendido: cinco lugares.
    private enum Barra {
        static let alto: CGFloat = 0.8925
        static let inicio = CGVector(dx: 0.115, dy: alto)
        static let buscar = CGVector(dx: 0.296, dy: alto)
        static let asistente = CGVector(dx: 0.500, dy: 0.9075)
        static let articulos = CGVector(dx: 0.704, dy: alto)
        static let recorridos = CGVector(dx: 0.886, dy: alto)
    }

    /// La primera foto del mosaico de lugares del inicio.
    private let primerLugar = CGVector(dx: 0.27, dy: 0.485)
    /// El boton del mapa, a la izquierda del de ajustes, en la cabecera de buscar.
    private let botonMapa = CGVector(dx: 0.775, dy: 0.104)

    override func setUpWithError() throws {
        continueAfterFailure = true
    }

    func testCapturasParaLaTienda() throws {
        // El workflow pasa TEST_RUNNER_IDIOMA; xcodebuild le saca el prefijo
        // antes de dejarlo en el entorno del test.
        let idioma = ProcessInfo.processInfo.environment["IDIOMA"] ?? "es"
        let espera = TimeInterval(ProcessInfo.processInfo.environment["ESPERA"] ?? "") ?? 12

        // El idioma del telefono y no el de la app: la app sigue al telefono
        // mientras nadie elija otro en Ajustes, y una instalacion nueva no
        // eligio nada. Es exactamente lo que va a ver quien la baje.
        let region = ["es": "es_PY", "en": "en_US", "pt": "pt_BR"][idioma] ?? "es_PY"
        let lengua = idioma == "pt" ? "pt-BR" : idioma
        let argumentos = ["-AppleLanguages", "(\(lengua))", "-AppleLocale", region]

        // 1. El inicio, tal como abre.
        var app = abrir(argumentos, espera: espera)
        fotografiar("01-inicio")

        // 2 y 3. Una ficha: arriba, y despues de subir la hoja sobre la foto.
        tocar(app, primerLugar, espera: espera / 2)
        fotografiar("02-ficha")
        // Un arrastre corto y lento, no `swipeUp()`. El deslizamiento entero
        // lanzaba la hoja hasta arriba y su contenido quedaba debajo de la
        // barra de estado, pisandose con la hora (primera corrida, 6/11/2026).
        // Asi la hoja sube lo justo para mostrar la informacion y para.
        app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.78))
            .press(
                forDuration: 0.1,
                thenDragTo: app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.56)),
                withVelocity: .slow,
                thenHoldForDuration: 0.5
            )
        Thread.sleep(forTimeInterval: 2)
        fotografiar("03-ficha-abajo")

        // 4. Buscar, con el mosaico de categorias.
        app = abrir(argumentos, espera: espera)
        tocar(app, Barra.buscar, espera: espera / 2)
        fotografiar("04-buscar")

        // 5. El mapa, que es la misma busqueda dibujada sobre el lienzo. Tarda
        // mas que el resto: hay que leer el .pmtiles y que MapLibre dibuje.
        tocar(app, botonMapa, espera: espera)
        fotografiar("05-mapa")

        // 6 y 7. Articulos y recorridos.
        app = abrir(argumentos, espera: espera)
        tocar(app, Barra.articulos, espera: espera / 2)
        fotografiar("06-articulos")

        app = abrir(argumentos, espera: espera)
        tocar(app, Barra.recorridos, espera: espera / 2)
        fotografiar("07-recorridos")

        // 8. El asistente. Vacio a proposito: una pregunta escrita aca seria
        // texto de producto, y eso lo escribe una persona.
        app = abrir(argumentos, espera: espera)
        tocar(app, Barra.asistente, espera: 3)
        // La primera vez que aparece el teclado en un simulador nuevo, iOS
        // muestra su presentacion de "deslizar para escribir", en el idioma del
        // sistema y tapando media pantalla. Paso en la primera corrida. Es del
        // sistema, no de la app, asi que se cierra si esta.
        let continuar = app.buttons.matching(
            NSPredicate(format: "label IN %@", ["Continue", "Continuar"])
        ).firstMatch
        if continuar.waitForExistence(timeout: 2) {
            continuar.tap()
            Thread.sleep(forTimeInterval: 1)
        }
        fotografiar("08-asistente")

        app.terminate()
    }

    /// Lanza la app de cero y espera a que el contenido haya bajado.
    private func abrir(_ argumentos: [String], espera: TimeInterval) -> XCUIApplication {
        let app = XCUIApplication()
        app.terminate()
        app.launchArguments = argumentos
        app.launch()
        _ = app.wait(for: .runningForeground, timeout: 30)
        Thread.sleep(forTimeInterval: espera)
        return app
    }

    /// Toca y espera. Si despues de esperar la pantalla quedo identica a la
    /// de antes, el toque no entro, y se toca una vez mas.
    ///
    /// Paso en la segunda corrida (6/11/2026): en la primera tanda, con el
    /// simulador recien arrancado, el toque al boton del mapa llego a la app
    /// —el registro de XCUITest lo muestra sintetizado, sin error— y la app
    /// siguio en buscar. En las otras dos tandas, el mismo toque abrio el mapa.
    /// Comparar la pantalla no depende de cuanto se espere: con la barra de
    /// estado fija en 9:41, dos fotos de la misma pantalla quieta son iguales
    /// byte a byte.
    private func tocar(_ app: XCUIApplication, _ donde: CGVector, espera: TimeInterval) {
        let antes = XCUIScreen.main.screenshot().pngRepresentation
        app.coordinate(withNormalizedOffset: donde).tap()
        Thread.sleep(forTimeInterval: espera)
        if XCUIScreen.main.screenshot().pngRepresentation == antes {
            app.coordinate(withNormalizedOffset: donde).tap()
            Thread.sleep(forTimeInterval: espera)
        }
    }

    private func fotografiar(_ nombre: String) {
        let adjunto = XCTAttachment(screenshot: XCUIScreen.main.screenshot())
        adjunto.name = nombre
        // Sin esto Xcode descarta los adjuntos de un test que pasa, que son
        // justo los que hacen falta aca.
        adjunto.lifetime = .keepAlways
        add(adjunto)
    }
}
