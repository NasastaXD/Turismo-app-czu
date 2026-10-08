import XCTest

/// Sostiene la app al frente para que se le pueda sacar una captura.
///
/// Existe por una razon concreta y medida: `xcrun simctl launch` deja la app
/// en `UISceneActivationStateForegroundInactive`, o sea en primer plano pero
/// sin activar, y entonces lo que el framebuffer compone —y `simctl io
/// screenshot` captura— es el escritorio de SpringBoard. La app corre, dibuja
/// el mapa con Metal y hasta pide por red con exito; lo unico que no pasa es
/// quedar activa.
///
/// `XCUIApplication().launch()` si la activa: es para lo que esta hecho. Asi
/// que este test la lanza, comprueba que quedo al frente, y se queda esperando
/// mientras el workflow fotografia desde afuera.
///
/// De paso deja su propia captura adjunta al resultado, que sirve como segunda
/// via si la del host fallara.
final class CapturaUITests: XCTestCase {

    override func setUpWithError() throws {
        // Si algo falla, que falle en el primer problema y no despues de
        // cuarenta segundos de espera inutil.
        continueAfterFailure = false
    }

    func testSostenerElMapaEnPantalla() throws {
        let app = XCUIApplication()
        app.launch()

        // Que de verdad quedo al frente, no que se lanzo nada mas. Es la
        // diferencia exacta que hacia fallar la captura.
        XCTAssertTrue(
            app.wait(for: .runningForeground, timeout: 30),
            "la app no llego a primer plano"
        )

        // El mapa tarda: hay que leer el .pmtiles del bundle, armar el estilo
        // y que MapLibre dibuje su primer cuadro. Tres capturas propias a lo
        // largo de la espera, por si el mapa aparece tarde.
        for (indice, espera) in [8, 17, 20].enumerated() {
            Thread.sleep(forTimeInterval: TimeInterval(espera))

            let captura = XCUIScreen.main.screenshot()
            let adjunto = XCTAttachment(screenshot: captura)
            adjunto.name = "captura-\(indice + 1)"
            // keepAlways: sin esto, Xcode descarta los adjuntos de un test que
            // pasa, que son justo los que hacen falta aca.
            adjunto.lifetime = .keepAlways
            add(adjunto)

            XCTAssertEqual(app.state, .runningForeground, "la app dejo el primer plano")
        }
    }
}
