<#
.SYNOPSIS
  Prepara la firma de la app de iPhone desde Windows, sin una Mac.

.DESCRIPTION
  Apple firma con un certificado de distribucion, y ese certificado se pide con
  una "solicitud" (CSR) que en una Mac se hace con Acceso a Llaveros. Esto hace
  lo mismo con OpenSSL, que viene con Git para Windows.

  Son dos pasos, con una visita a developer.apple.com en el medio:

    1) .\preparar-firma.ps1 -Paso solicitud -Correo tu@correo -Nombre "Tu Nombre"
         Crea la clave privada y la solicitud. La solicitud se sube a Apple.

    2) .\preparar-firma.ps1 -Paso secretos `
         -Certificado  C:\Descargas\distribution.cer `
         -Perfil       C:\Descargas\Turismo_App_Store.mobileprovision `
         -ClaveApi     C:\Descargas\AuthKey_ABC123XYZ.p8 `
         -Equipo ABCDE12345 -IdClave ABC123XYZ -Emisor 00000000-0000-0000-0000-000000000000
         Arma el .p12 y deja los siete secretos listos. Con -Cargar, ademas los
         carga en GitHub con `gh` (hay que haber hecho `gh auth login` antes).

  Todo queda en la carpeta firma-ios dentro de tu usuario, FUERA del
  repositorio, a proposito: la clave privada no puede terminar en un commit.
  Guarda esa carpeta en un lugar seguro: sin la clave privada el certificado no
  sirve, y hay que pedir otro.

  Funciona en Windows PowerShell 5.1 (el que trae Windows) y en PowerShell 7.
#>
param(
    [Parameter(Mandatory = $true)][ValidateSet('solicitud', 'secretos')][string]$Paso,
    [string]$Correo,
    [string]$Nombre,
    [string]$Certificado,
    [string]$Perfil,
    [string]$ClaveApi,
    [string]$Equipo,
    [string]$IdClave,
    [string]$Emisor,
    [switch]$Cargar,
    [string]$Repo = 'NasastaXD/Turismo-app-czu',
    [string]$Carpeta = (Join-Path $HOME 'firma-ios')
)

$ErrorActionPreference = 'Stop'

function Fallar([string]$mensaje) {
    Write-Host ''
    Write-Host "ERROR: $mensaje" -ForegroundColor Red
    exit 1
}

function Paso([string]$texto) {
    Write-Host ''
    Write-Host "== $texto" -ForegroundColor Cyan
}

# OpenSSL: el del PATH, o el que trae Git para Windows. El de mingw64 es un
# programa nativo de Windows, y desde PowerShell no sufre la conversion de
# rutas que rompe `-subj "/CN=..."` cuando se lo llama desde Git Bash.
function Buscar-OpenSSL {
    $enPath = Get-Command openssl -CommandType Application -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($enPath) { return $enPath.Source }
    $candidatos = @(
        "$env:ProgramFiles\Git\mingw64\bin\openssl.exe",
        "$env:ProgramFiles\Git\usr\bin\openssl.exe",
        "${env:ProgramFiles(x86)}\Git\mingw64\bin\openssl.exe",
        "$env:LOCALAPPDATA\Programs\Git\mingw64\bin\openssl.exe"
    )
    foreach ($c in $candidatos) { if ($c -and (Test-Path $c)) { return $c } }
    Fallar 'No encuentro OpenSSL. Instala Git para Windows (git-scm.com) y vuelve a correr esto.'
}

# No se llama OpenSSL: PowerShell no distingue mayusculas, y una funcion con
# ese nombre le gana al programa en Get-Command.
function Correr-OpenSSL {
    & $script:openssl @args
    if ($LASTEXITCODE -ne 0) { Fallar "openssl $($args[0]) fallo (codigo $LASTEXITCODE)." }
}

function A-Base64([string]$ruta) {
    return [Convert]::ToBase64String([IO.File]::ReadAllBytes($ruta))
}

$script:openssl = Buscar-OpenSSL
Write-Host "OpenSSL: $script:openssl"
New-Item -ItemType Directory -Force -Path $Carpeta | Out-Null
$clavePrivada = Join-Path $Carpeta 'clave-privada.key'
$solicitud = Join-Path $Carpeta 'solicitud.certSigningRequest'

# ---------------------------------------------------------------------------
if ($Paso -eq 'solicitud') {
    if (-not $Correo -or -not $Nombre) {
        Fallar 'Faltan -Correo y -Nombre: van dentro de la solicitud que se manda a Apple.'
    }
    if (Test-Path $clavePrivada) {
        Fallar ("Ya hay una clave privada en $clavePrivada. Si la pisas, el certificado que " +
            'pediste con ella deja de servir. Si de verdad quieres empezar de cero, ' +
            'mueve esa carpeta a otro lado y vuelve a correr.')
    }

    Paso 'Clave privada'
    Correr-OpenSSL genrsa -out $clavePrivada 2048

    # Los datos van en un archivo de configuracion y no en -subj: asi un nombre
    # con espacios o tildes no depende de como cada consola pasa las comillas.
    $config = Join-Path $Carpeta 'solicitud.cnf'
    @(
        '[ req ]',
        'distinguished_name = dn',
        'prompt = no',
        'utf8 = yes',
        'string_mask = utf8only',
        '[ dn ]',
        "emailAddress = $Correo",
        "CN = $Nombre",
        'C = PY'
    ) -join "`n" | ForEach-Object {
        # UTF-8 SIN marca de orden: con ASCII las tildes salian como '?', y con
        # la marca que agrega Set-Content -Encoding UTF8 en PowerShell 5.1,
        # OpenSSL no reconoce la primera seccion del archivo.
        [IO.File]::WriteAllText($config, $_ + "`n", (New-Object System.Text.UTF8Encoding($false)))
    }

    Paso 'Solicitud de certificado (CSR)'
    Correr-OpenSSL req -new -key $clavePrivada -out $solicitud -config $config

    Write-Host ''
    Write-Host 'Listo. Ahora, en el navegador:' -ForegroundColor Green
    Write-Host '  1. developer.apple.com/account/resources/certificates -> boton +'
    Write-Host '  2. Elige "Apple Distribution" -> Continue'
    Write-Host "  3. Sube este archivo: $solicitud"
    Write-Host '  4. Descarga el certificado (distribution.cer)'
    Write-Host ''
    Write-Host "NO borres $Carpeta : ahi esta la clave privada que hace falta en el paso 2." -ForegroundColor Yellow
    exit 0
}

# ---------------------------------------------------------------------------
# Paso 'secretos'
foreach ($par in @(@('Certificado', $Certificado), @('Perfil', $Perfil))) {
    if (-not $par[1]) { Fallar "Falta -$($par[0])." }
    if (-not (Test-Path $par[1])) { Fallar "No existe el archivo de -$($par[0]): $($par[1])" }
}
if (-not (Test-Path $clavePrivada)) {
    Fallar "No esta la clave privada ($clavePrivada). Se crea en el paso 'solicitud', y el certificado tiene que haberse pedido con ESA solicitud."
}

Paso 'Certificado de Apple a PEM'
$pem = Join-Path $Carpeta 'certificado.pem'
Correr-OpenSSL x509 -inform DER -in $Certificado -out $pem

# Que el certificado y la clave sean pareja. Si no lo son, el certificado se
# pidio con otra solicitud, y el .p12 saldria inservible sin que nada avise
# hasta el runner.
$mod1 = (& $script:openssl x509 -in $pem -noout -modulus)
$mod2 = (& $script:openssl rsa -in $clavePrivada -noout -modulus)
if ($mod1 -ne $mod2) {
    Fallar 'El certificado no corresponde a esta clave privada: se pidio con otra solicitud. Pide uno nuevo subiendo la solicitud de esta carpeta.'
}
$sujeto = (& $script:openssl x509 -in $pem -noout -subject)
Write-Host "certificado: $sujeto"
if ($sujeto -notmatch 'Distribution') {
    Write-Host 'AVISO: no parece un certificado de distribucion ("Apple Distribution"). Para la tienda tiene que ser ese.' -ForegroundColor Yellow
}

Paso 'Paquete .p12 (certificado + clave privada)'
# Una contrasena al azar: solo protege el archivo en el viaje a GitHub, y
# viaja ella misma como otro secreto.
$azar = New-Object byte[] 18
[System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($azar)
$claveP12 = ([Convert]::ToBase64String($azar)) -replace '[+/=]', ''
$p12 = Join-Path $Carpeta 'distribucion.p12'
# 3DES y SHA1, y no lo que OpenSSL 3 usa por omision (AES-256): el llavero de
# macOS rechaza el otro con "MAC verification failed", que parece un error de
# contrasena y no lo es.
Correr-OpenSSL pkcs12 -export -inkey $clavePrivada -in $pem -out $p12 -name 'Apple Distribution' `
    -passout "pass:$claveP12" -certpbe PBE-SHA1-3DES -keypbe PBE-SHA1-3DES -macalg sha1

$secretos = [ordered]@{
    'IOS_CERTIFICADO_P12_BASE64' = (A-Base64 $p12)
    'IOS_CERTIFICADO_CLAVE'      = $claveP12
    'IOS_PERFIL_BASE64'          = (A-Base64 $Perfil)
}
if ($Equipo) { $secretos['IOS_TEAM_ID'] = $Equipo.Trim() }
if ($ClaveApi) {
    if (-not (Test-Path $ClaveApi)) { Fallar "No existe el archivo de -ClaveApi: $ClaveApi" }
    $contenidoP8 = Get-Content -Raw -Path $ClaveApi
    if ($contenidoP8 -notmatch 'BEGIN PRIVATE KEY') { Fallar 'El archivo de -ClaveApi no parece un .p8 de App Store Connect.' }
    $secretos['ASC_KEY_P8'] = $contenidoP8.Trim()
}
if ($IdClave) { $secretos['ASC_KEY_ID'] = $IdClave.Trim() }
if ($Emisor) { $secretos['ASC_ISSUER_ID'] = $Emisor.Trim() }

Paso 'Secretos'
$dirSecretos = Join-Path $Carpeta 'secretos'
New-Item -ItemType Directory -Force -Path $dirSecretos | Out-Null
foreach ($nombre in $secretos.Keys) {
    $ruta = Join-Path $dirSecretos "$nombre.txt"
    [IO.File]::WriteAllText($ruta, $secretos[$nombre])
    Write-Host ("  {0,-28} {1,6} caracteres  ->  {2}" -f $nombre, $secretos[$nombre].Length, $ruta)
}

$todos = @('IOS_TEAM_ID', 'IOS_CERTIFICADO_P12_BASE64', 'IOS_CERTIFICADO_CLAVE', 'IOS_PERFIL_BASE64', 'ASC_KEY_ID', 'ASC_ISSUER_ID', 'ASC_KEY_P8')
$faltan = $todos | Where-Object { -not $secretos.Contains($_) }
if ($faltan) {
    Write-Host ''
    Write-Host ('Faltan (pasalos con -Equipo, -IdClave, -Emisor y -ClaveApi): ' + ($faltan -join ', ')) -ForegroundColor Yellow
}

if ($Cargar) {
    Paso "Cargando en GitHub ($Repo)"
    if (-not (Get-Command gh -ErrorAction SilentlyContinue)) {
        Fallar 'No esta `gh` (GitHub CLI). Instalalo de cli.github.com, corre `gh auth login`, y vuelve a correr con -Cargar. O cargalos a mano: los archivos de arriba.'
    }
    foreach ($nombre in $secretos.Keys) {
        # Como argumento, que llega exacto. Por tuberia PowerShell agrega un
        # salto de linea al final, y en la contrasena del .p12 eso la vuelve
        # incorrecta. Solo lo que no entra en una linea de comando de Windows
        # (32 767 caracteres) va por tuberia; el workflow recorta los bordes.
        $valor = $secretos[$nombre]
        if ($valor.Length -lt 30000) {
            & gh secret set $nombre --repo $Repo --body $valor
        } else {
            $valor | & gh secret set $nombre --repo $Repo
        }
        if ($LASTEXITCODE -ne 0) { Fallar "gh no pudo cargar $nombre." }
        Write-Host "  cargado: $nombre"
    }
    Write-Host ''
    Write-Host 'Listo. Ya se puede correr "Publicar iOS (App Store)" desde la pestana Actions.' -ForegroundColor Green
} else {
    Write-Host ''
    Write-Host 'Para cargarlos a mano: GitHub -> el repositorio -> Settings -> Secrets and variables -> Actions -> New repository secret.' -ForegroundColor Green
    Write-Host 'Un secreto por archivo de la carpeta "secretos": el nombre del archivo es el nombre del secreto, y el contenido, el valor.'
}
Write-Host ''
Write-Host "Guarda $Carpeta en un lugar seguro y no lo subas a ningun repositorio." -ForegroundColor Yellow
