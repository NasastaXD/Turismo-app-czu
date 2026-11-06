<#
.SYNOPSIS
  Te lleva de la mano para firmar la app de iPhone: abre cada pagina de Apple,
  espera a que bajes cada archivo, los encuentra solo en Descargas y carga todo
  en GitHub. Al final, si queres, arranca la compilacion.

.DESCRIPTION
  Se corre asi, desde la carpeta docs\app-store del repositorio:

    powershell -ExecutionPolicy Bypass -File .\firmar-facil.ps1

  Necesita Git para Windows (trae OpenSSL) y GitHub CLI con `gh auth login`
  hecho. El trabajo de verdad lo hace preparar-firma.ps1; esto solo ordena los
  pasos y pregunta lo justo.

  Si se corta a la mitad, se vuelve a correr: lo que ya esta hecho (la clave
  privada, los archivos bajados) se reconoce y no se repite.
#>
param(
    [string]$Repo = 'NasastaXD/Turismo-app-czu',
    [string]$Descargas = (Join-Path $HOME 'Downloads'),
    [string]$Carpeta = (Join-Path $HOME 'firma-ios')
)

$ErrorActionPreference = 'Stop'
$preparar = Join-Path $PSScriptRoot 'preparar-firma.ps1'

function Titulo([string]$t) { Write-Host ''; Write-Host "=== $t ===" -ForegroundColor Cyan }
function Abrir([string]$url) {
    Write-Host "Abriendo: $url"
    try { Start-Process $url } catch { Write-Host "(abrilo a mano en el navegador)" -ForegroundColor Yellow }
}
function Esperar([string]$que) { [void](Read-Host "Cuando $que, apreta Enter") }
function Preguntar([string]$que) {
    do { $r = (Read-Host $que).Trim() } while (-not $r)
    return $r
}
function Fallar([string]$m) { Write-Host ''; Write-Host "ERROR: $m" -ForegroundColor Red; exit 1 }

# El archivo mas nuevo de Descargas con ese patron, confirmado. Si no esta o
# no es, se pide la ruta.
function Buscar([string]$patron, [string]$que) {
    $f = Get-ChildItem -Path $Descargas -Filter $patron -File -ErrorAction SilentlyContinue |
        Sort-Object LastWriteTime -Descending | Select-Object -First 1
    if ($f) {
        $r = Read-Host "Encontre $($f.Name) ($($f.LastWriteTime)). Es ese? [S/n]"
        if ($r -notmatch '^[nN]') { return $f.FullName }
    }
    do {
        $ruta = (Read-Host "Arrastra aca el archivo $que (o escribi la ruta)").Trim('"', ' ')
    } while (-not (Test-Path $ruta))
    return $ruta
}

# ---------------------------------------------------------------------------
Titulo 'Antes de empezar'
if (-not (Test-Path $preparar)) { Fallar "No encuentro preparar-firma.ps1 al lado de este script." }
if (-not (Get-Command gh -ErrorAction SilentlyContinue)) {
    Fallar 'Falta GitHub CLI. Instalalo de cli.github.com, corre "gh auth login" y volve a correr esto.'
}
& gh auth status *> $null
if ($LASTEXITCODE -ne 0) { Fallar 'GitHub CLI no tiene sesion. Corre "gh auth login" y volve a correr esto.' }
Write-Host 'GitHub CLI: listo.'

# ---------------------------------------------------------------------------
Titulo '1 de 6 - Tu Team ID'
Abrir 'https://developer.apple.com/account'
Write-Host 'En "Membership details" esta el Team ID: 10 letras y numeros.'
$equipo = Preguntar 'Pegalo aca'

# ---------------------------------------------------------------------------
Titulo '2 de 6 - Registrar la app'
Abrir 'https://developer.apple.com/account/resources/identifiers/add/bundleId'
Write-Host 'Elegi "App IDs" > "App". Description: Caaguazu Go'
Write-Host 'Bundle ID: Explicit > net.caaguazu.turismo  > Continue > Register'
Write-Host '(Si ya estaba registrado de antes, segui de largo.)'
Esperar 'este registrado'

# ---------------------------------------------------------------------------
Titulo '3 de 6 - El certificado'
if (Test-Path (Join-Path $Carpeta 'clave-privada.key')) {
    Write-Host "Ya hay una clave privada en $Carpeta : uso esa."
} else {
    $correo = Preguntar 'Tu correo (el de la cuenta de Apple)'
    $nombre = Preguntar 'Tu nombre'
    & $preparar -Paso solicitud -Correo $correo -Nombre $nombre -Carpeta $Carpeta | Out-Null
    if ($LASTEXITCODE -ne 0) { Fallar 'No se pudo crear la solicitud (ver arriba).' }
}
$solicitud = Join-Path $Carpeta 'solicitud.certSigningRequest'
Write-Host "La solicitud es: $solicitud"
try { Start-Process explorer.exe "/select,`"$solicitud`"" } catch { }
Abrir 'https://developer.apple.com/account/resources/certificates/add'
Write-Host 'Elegi "Apple Distribution" > Continue > subi ese archivo > Continue > Download.'
Esperar 'se haya bajado distribution.cer'
$cer = Buscar '*.cer' 'distribution.cer'

# ---------------------------------------------------------------------------
Titulo '4 de 6 - El perfil'
Abrir 'https://developer.apple.com/account/resources/profiles/add'
Write-Host 'En "Distribution" elegi "App Store Connect" > Continue'
Write-Host 'App ID: net.caaguazu.turismo > el certificado que recien creaste'
Write-Host 'Nombre: Caaguazu App Store > Generate > Download'
Esperar 'se haya bajado el .mobileprovision'
$perfil = Buscar '*.mobileprovision' 'del perfil (.mobileprovision)'

# ---------------------------------------------------------------------------
Titulo '5 de 6 - La clave para subir a Apple'
Abrir 'https://appstoreconnect.apple.com/access/integrations/api'
Write-Host 'Team Keys > "+" > Name: GitHub, Access: App Manager > Generate > Download'
Write-Host 'OJO: Apple deja bajarla UNA sola vez. Despues copiala a un lugar seguro.'
Esperar 'se haya bajado AuthKey_....p8'
$p8 = Buscar 'AuthKey_*.p8' 'AuthKey_XXXX.p8'
# El Key ID esta en el nombre del archivo: AuthKey_<KEYID>.p8
$idClave = [IO.Path]::GetFileNameWithoutExtension($p8) -replace '^AuthKey_', ''
Write-Host "Key ID (sale del nombre del archivo): $idClave"
$emisor = Preguntar 'Issuer ID (el codigo largo con guiones, arriba de la tabla de claves)'

# ---------------------------------------------------------------------------
Titulo '6 de 6 - Cargar todo en GitHub'
& $preparar -Paso secretos -Certificado $cer -Perfil $perfil -ClaveApi $p8 `
    -Equipo $equipo -IdClave $idClave -Emisor $emisor -Cargar -Repo $Repo -Carpeta $Carpeta
if ($LASTEXITCODE -ne 0) { Fallar 'No se pudieron cargar los secretos (ver arriba).' }

# Copia de la clave de la API junto a la clave privada: las dos se pierden juntas o se guardan juntas.
Copy-Item $p8 -Destination $Carpeta -Force

# ---------------------------------------------------------------------------
Titulo 'Listo'
Write-Host 'Falta una sola cosa en la web, si no la hiciste: crear la app en App Store Connect.'
Write-Host '  Apps > "+" > New App: Caaguazu Go, Spanish (Mexico), bundle net.caaguazu.turismo, SKU caaguazu-turismo'
Abrir 'https://appstoreconnect.apple.com/apps'
$r = Read-Host 'Con la app creada, arranco la compilacion ahora? (tarda ~25 min y llega sola a TestFlight) [S/n]'
if ($r -notmatch '^[nN]') {
    & gh workflow run 'publicar-ios.yml' --repo $Repo --ref main
    if ($LASTEXITCODE -eq 0) {
        Write-Host 'Arranco. Se sigue en GitHub > Actions > "Publicar iOS (App Store)".' -ForegroundColor Green
        Abrir "https://github.com/$Repo/actions/workflows/publicar-ios.yml"
    }
}
Write-Host ''
Write-Host "Guarda la carpeta $Carpeta en un pendrive: ahi esta todo lo de la firma." -ForegroundColor Yellow
