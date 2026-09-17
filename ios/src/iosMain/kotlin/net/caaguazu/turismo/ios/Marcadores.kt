package net.caaguazu.turismo.ios

import net.caaguazu.turismo.core.Bitacora
import net.caaguazu.turismo.core.Cache
import net.caaguazu.turismo.core.DecodificadorTolerante
import net.caaguazu.turismo.core.Falla
import net.caaguazu.turismo.core.Http
import net.caaguazu.turismo.core.Resultado
import net.caaguazu.turismo.datos.Marcador
import platform.Foundation.NSBundle
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

/**
 * Los pines del mapa, traidos del panel.
 *
 * Es el camino de datos completo en iOS: NSURLSession, la cache en disco, el
 * decodificador tolerante y los modelos del contrato — todo compartido con
 * Android salvo las treinta lineas de `pedirHttp`.
 *
 * `/mapa/markers` es a proposito el primer endpoint que se trae: un `Marcador`
 * es `id`, `tipo`, `lat`, `lng` y `categoria`, o sea **ningun texto**. Todo
 * texto visible sale de `Textos.t(...)` y `Textos` todavia no cruzo, asi que
 * cualquier otra pantalla quedaria a medias; esto se ve entero.
 *
 * Y es lo que mantiene el mapa retroactivo: los pines viajan aparte de los
 * tiles, asi que se registra un lugar nuevo y aparece sin regenerar el .pmtiles
 * ni publicar una version nueva.
 */
object Marcadores {

    private const val ETIQUETA = "MarcadoresIos"
    private const val CLAVE_URL_BASE = "URL_BASE"

    private val cache: Cache by lazy { Cache(carpetaDeCache()) }
    private val http: Http by lazy { Http(cache) }

    suspend fun traer(): Resultado<List<Marcador>> {
        val base = urlBase() ?: return Resultado.Mal(Falla.DATOS_INVALIDOS)

        return when (val cuerpo = http.obtener(base + "mapa/markers")) {
            is Resultado.Mal -> cuerpo
            is Resultado.Bien -> {
                val marcadores = DecodificadorTolerante.lista(
                    texto = cuerpo.valor.texto,
                    origen = "mapa/markers",
                    elemento = Marcador.serializer(),
                )
                Bitacora.info(ETIQUETA, "${marcadores.size} marcadores, deCache=${cuerpo.valor.deCache}")
                Resultado.Bien(marcadores)
            }
        }
    }

    /**
     * La URL base sale del Info.plist y no del codigo.
     *
     * Es la misma regla que del lado Android, donde vive en un buildConfigField:
     * nunca quemada en el codigo, aunque hoy solo exista una. Del lado iOS el
     * equivalente es el Info.plist, que se genera desde `ios/xcode/project.yml`.
     */
    private fun urlBase(): String? {
        val valor = NSBundle.mainBundle.objectForInfoDictionaryKey(CLAVE_URL_BASE) as? String
        if (valor.isNullOrBlank()) {
            Bitacora.fallo(ETIQUETA, "falta $CLAVE_URL_BASE en el Info.plist")
            return null
        }
        return if (valor.endsWith("/")) valor else "$valor/"
    }

    /**
     * La carpeta de cache del sistema. iOS la puede vaciar cuando necesita
     * espacio, que es exactamente lo que corresponde para algo descartable.
     */
    private fun carpetaDeCache(): String {
        val carpetas = NSSearchPathForDirectoriesInDomains(
            directory = NSCachesDirectory,
            domainMask = NSUserDomainMask,
            expandTilde = true,
        )
        val raiz = carpetas.firstOrNull() as? String ?: "/tmp"
        return "$raiz/api"
    }
}

/**
 * Los marcadores como GeoJSON, que es lo que entiende la capa del mapa.
 *
 * Se arma a mano en lugar de construir objetos de spatial-k: son dos campos por
 * punto y asi no hace falta otra API de por medio. El orden es
 * `[lng, lat]` —el de GeoJSON, no el que se dice en voz alta—, que es la
 * inversion mas facil de cometer en todo esto.
 */
fun List<Marcador>.comoGeoJson(): String {
    val puntos = joinToString(",") { marcador ->
        """{"type":"Feature","id":${marcador.id},""" +
            """"properties":{"tipo":"${marcador.tipo}","categoria":${marcador.categoria ?: -1}},""" +
            """"geometry":{"type":"Point","coordinates":[${marcador.lng},${marcador.lat}]}}"""
    }
    return """{"type":"FeatureCollection","features":[$puntos]}"""
}
