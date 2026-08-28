package net.caaguazu.turismo.datos

import android.content.res.AssetManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import net.caaguazu.turismo.core.Analizador
import net.caaguazu.turismo.core.Falla
import net.caaguazu.turismo.core.Registro
import net.caaguazu.turismo.core.Resultado

/**
 * El mismo contrato, servido desde archivos que viajan en el APK.
 *
 * Existe porque la API real todavia no esta publicada, y porque tener las dos
 * implementaciones detras de la misma interfaz permite construir y probar las
 * pantallas enteras sin depender de que el servidor este listo.
 *
 * Los payloads son calcados de lo que devuelve el plugin, campo por campo. Los
 * textos son marcadores, no contenido: los escribe una persona.
 */
class ApiMock(private val assets: AssetManager) : Contrato {

    private companion object {
        const val ETIQUETA = "Mock"
        const val CARPETA = "mocks"

        /** Una espera corta deja ver los estados de carga que si van a ocurrir. */
        const val DEMORA_MS = 180L
    }

    override suspend fun categorias() =
        leer("categorias.json", ListSerializer(Categoria.serializer()))

    override suspend fun zonas() =
        leer("zonas.json", ListSerializer(Zona.serializer()))

    override suspend fun inventario(
        categoria: Int?,
        zona: Int?,
        buscar: String?,
        tipoItem: String?,
        pagina: Int,
        porPagina: Int,
    ): Resultado<Pagina<ItemInventario>> {
        val todo = leer("inventario.json", Pagina.serializer(ItemInventario.serializer()))
        if (todo !is Resultado.Bien) return todo

        // Los filtros se aplican aca para que la pantalla se pruebe de verdad y
        // no contra una lista que siempre devuelve lo mismo.
        val filtrados = todo.valor.items.filter { item ->
            (categoria == null || item.categoria?.id == categoria) &&
                (zona == null || item.zona?.id == zona) &&
                (tipoItem == null || item.tipoItem == tipoItem) &&
                (buscar.isNullOrBlank() || item.titulo.contains(buscar, ignoreCase = true))
        }

        val desde = (pagina - 1) * porPagina
        return Resultado.Bien(
            Pagina(
                items = filtrados.drop(desde).take(porPagina),
                total = filtrados.size,
                pagina = pagina,
                porPagina = porPagina,
            ),
        )
    }

    override suspend fun ficha(id: Int): Resultado<Ficha> =
        unoDe("fichas.json", ListSerializer(Ficha.serializer())) { it.id == id }

    override suspend fun marcadores() =
        leer("markers.json", ListSerializer(Marcador.serializer()))

    override suspend fun eventos(desde: String?, hasta: String?) =
        leer("eventos.json", Pagina.serializer(Evento.serializer()))

    override suspend fun evento(id: Int): Resultado<Evento> =
        unoDe("eventos.json", Pagina.serializer(Evento.serializer()), { it.items }) { it.id == id }

    override suspend fun recorridos() =
        leer("recorridos.json", Pagina.serializer(Recorrido.serializer()))

    override suspend fun recorrido(id: Int): Resultado<Recorrido> =
        unoDe("recorridos-detalle.json", ListSerializer(Recorrido.serializer())) { it.id == id }

    override suspend fun articulos(pagina: Int, categoria: Int?) =
        leer("articulos.json", Pagina.serializer(ResumenArticulo.serializer()))

    override suspend fun articulo(id: Int): Resultado<Articulo> =
        unoDe("articulos-detalle.json", ListSerializer(Articulo.serializer())) { it.id == id }

    override suspend fun textos(idioma: String) =
        leer("strings-$idioma.json", MapSerializer(String.serializer(), String.serializer()))

    override suspend fun medios() =
        leer("media-manifest.json", MapSerializer(String.serializer(), Medio.serializer()))

    override suspend fun delta(desde: String?) =
        leer("sync.json", Delta.serializer())

    /* --------------------------------------------------------------------- */

    private suspend fun <T> leer(archivo: String, serializador: KSerializer<T>): Resultado<T> =
        withContext(Dispatchers.IO) {
            delay(DEMORA_MS)
            val texto = try {
                assets.open("$CARPETA/$archivo").bufferedReader().use { it.readText() }
            } catch (e: Throwable) {
                Registro.fallo(ETIQUETA, "falta el mock $archivo", e)
                return@withContext Resultado.Mal(Falla.NO_ENCONTRADO)
            }
            try {
                Resultado.Bien(Analizador.decodeFromString(serializador, texto))
            } catch (e: Throwable) {
                Registro.fallo(ETIQUETA, "el mock $archivo no encaja con el modelo", e)
                Resultado.Mal(Falla.DATOS_INVALIDOS)
            }
        }

    private suspend fun <T> unoDe(
        archivo: String,
        serializador: KSerializer<List<T>>,
        coincide: (T) -> Boolean,
    ): Resultado<T> = when (val todo = leer(archivo, serializador)) {
        is Resultado.Mal -> todo
        is Resultado.Bien -> todo.valor.firstOrNull(coincide)
            ?.let { Resultado.Bien(it) }
            ?: Resultado.Mal(Falla.NO_ENCONTRADO)
    }

    private suspend fun <C, T> unoDe(
        archivo: String,
        serializador: KSerializer<C>,
        extraer: (C) -> List<T>,
        coincide: (T) -> Boolean,
    ): Resultado<T> = when (val todo = leer(archivo, serializador)) {
        is Resultado.Mal -> todo
        is Resultado.Bien -> extraer(todo.valor).firstOrNull(coincide)
            ?.let { Resultado.Bien(it) }
            ?: Resultado.Mal(Falla.NO_ENCONTRADO)
    }
}
