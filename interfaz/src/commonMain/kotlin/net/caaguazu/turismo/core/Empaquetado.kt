package net.caaguazu.turismo.core

/**
 * Lo que viaja dentro de la app y se lee como archivo.
 *
 * Hoy son los tres juegos de textos de respaldo, que son el piso de la interfaz
 * cuando no hay red: el primer arranque de alguien que instalo la app y todavia
 * no tiene señal tiene que verse con palabras, no con claves entre angulos.
 *
 * Es sincrono, y eso es deliberado: los textos se necesitan **antes** de la
 * primera composicion, asi que una lectura suspendida obligaria a dibujar un
 * cuadro sin textos y corregirlo despues. Las dos plataformas pueden leer un
 * recurso empaquetado sin bloquear nada relevante — son unos kilobytes de un
 * archivo que el sistema ya tiene abierto.
 *
 * Los archivos viven en `interfaz/src/androidMain/assets/`, que el APK fusiona
 * solo, y el proyecto de Xcode referencia esa misma carpeta. Una sola copia:
 * un juego de textos que se actualiza en un lado y no en el otro seria la peor
 * forma de que las dos apps dejen de decir lo mismo.
 */
expect object Empaquetado {

    /**
     * El contenido de un archivo empaquetado, o null si no esta.
     *
     * La ruta es relativa y con barras, como `"textos/es.json"`. Que falte no
     * es un fallo: el panel puede ofrecer un idioma para el que todavia no
     * viajan textos, y ahi el castellano es la respuesta correcta.
     */
    fun texto(ruta: String): String?
}
