package net.caaguazu.turismo

import java.io.File

/**
 * Todas las fuentes Kotlin del proyecto, para los guardianes que las revisan.
 *
 * Existe porque el codigo dejo de estar en una carpeta. Ahora son cinco:
 * los tres juegos de fuentes de este modulo y las dos cascaras, la de Android
 * y la de iOS.
 *
 * Que estén las cinco no es completitud por completitud. Los guardianes son
 * los que hacen que las reglas del proyecto sigan siendo ciertas dentro de tres
 * meses, y una regla que se revisa en un modulo y no en otro deja de ser una
 * regla: seria cuestion de tiempo que un literal visible o un radio a mano
 * apareciera del lado que nadie mira. Con esto, **la version de iOS queda
 * cubierta por los mismos guardianes que la de Android**, que antes no lo
 * estaba.
 *
 * Las rutas son relativas al modulo :interfaz, que es desde donde Gradle corre
 * estas pruebas.
 */
object FuentesDelProyecto {

    private val CARPETAS = listOf(
        // Lo compartido: las pantallas y el sistema visual.
        "src/commonMain/kotlin/net/caaguazu/turismo",
        // Lo que cada plataforma resuelve a su manera.
        "src/androidMain/kotlin/net/caaguazu/turismo",
        "src/iosMain/kotlin/net/caaguazu/turismo",
        // Las dos cascaras.
        "../app/src/main/java/net/caaguazu/turismo",
        "../ios/src/iosMain/kotlin/net/caaguazu/turismo",
    )

    /** Solo lo de interfaz: el sistema visual y las pantallas. */
    private val CARPETAS_DE_INTERFAZ = listOf(
        "src/commonMain/kotlin/net/caaguazu/turismo/ui",
        "src/androidMain/kotlin/net/caaguazu/turismo/ui",
        "src/iosMain/kotlin/net/caaguazu/turismo/ui",
    )

    fun todas(exentos: Set<String> = emptySet()): List<File> = recorrer(CARPETAS, exentos)

    fun deInterfaz(exentos: Set<String> = emptySet()): List<File> =
        recorrer(CARPETAS_DE_INTERFAZ, exentos)

    private fun recorrer(carpetas: List<String>, exentos: Set<String>): List<File> =
        carpetas
            .map(::File)
            // Una carpeta que no existe no es un fallo: `iosMain/ui` puede
            // quedarse sin archivos si algun dia todo el mapa cruza. Lo que si
            // es un fallo es que no quede ninguna, y eso lo dice el check de
            // abajo — que es lo que evita que un guardian pase en verde
            // porque no encontro nada que revisar.
            .filter { it.isDirectory }
            .flatMap { it.walkTopDown().toList() }
            .filter { it.isFile && it.extension == "kt" && it.name !in exentos }
            .also {
                check(it.isNotEmpty()) {
                    "No se encontro ninguna fuente que revisar en: ${carpetas.joinToString()}"
                }
            }
}
