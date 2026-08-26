# Datos de mapa — Caaguazú

Tiles vectoriales offline. La app los lee desde un archivo local: no hace
peticiones de red, no necesita API key ni cuenta de ningún servicio.

## Archivos

El recorte del distrito es el que usa la app y vive directamente en sus assets, en
`app/src/main/assets/map/caaguazu.pmtiles`, para no tener el mismo archivo dos veces
en el repositorio. Aquí queda el del departamento, todavía sin usar.

| Archivo | Cobertura | Área | Tamaño |
|---|---|---|---|
| _(en `app/src/main/assets/map/caaguazu.pmtiles`)_ | Ciudad de Caaguazú y alrededores | ~942 km² | 2,0 MB |
| `caaguazu-departamento.pmtiles` | Departamento de Caaguazú completo | ~12.546 km² | 11 MB |

Ambos cubren z0–z15. El renderizador vectorial sobre-amplía hasta z18-19 sin
pérdida de nitidez, así que z15 basta para detalle a pie de calle.

Capas incluidas: `earth`, `water`, `landuse`, `roads`, `buildings`,
`places`, `pois`.

## Cómo se generaron

Origen: mapa base Protomaps del planeta, build **20260825** (137,5 GB), recortado
por rangos HTTP sin descargar el planeta entero.

```sh
go install github.com/protomaps/go-pmtiles@latest

PLANET=https://build.protomaps.com/20260825.pmtiles

# Distrito (caja de ~942 km² centrada en la ciudad) — va a los assets de la app
go-pmtiles extract $PLANET ../app/src/main/assets/map/caaguazu.pmtiles \
  --bbox=-56.1751,-25.6118,-55.8696,-25.3342 --maxzoom=15

# Departamento (polígono real, relación OSM 389890)
go-pmtiles extract $PLANET caaguazu-departamento.pmtiles \
  --region=caaguazu-departamento.geojson --maxzoom=15
```

Los builds del planeta se borran a las pocas semanas. Para regenerar hay que
usar una fecha reciente, no la de arriba.

### Nota sobre los límites administrativos

El distrito de Caaguazú **no está mapeado como relación en OpenStreetMap**. Solo
existen el departamento (relación 389890) y la mancha urbana de la ciudad
(relación 5166667, 37 km²). Por eso el recorte del distrito usa una caja
cuadrada de 30,7 × 30,7 km centrada en la ciudad en lugar del límite real.

## Atribución (obligatoria)

Los datos son de OpenStreetMap, bajo licencia ODbL. La app **debe** mostrar en
pantalla, visible sobre el mapa:

    © OpenStreetMap
