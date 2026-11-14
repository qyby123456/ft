param([switch]$VerifyOnly)
$ErrorActionPreference = 'Stop'
$baseline = Join-Path $PSScriptRoot 'runtime-reference\qys-bosses.jar'
$metadata = Get-Content -LiteralPath (Join-Path $PSScriptRoot 'runtime-reference\baseline.json') -Raw -Encoding utf8 | ConvertFrom-Json
$actual = (Get-FileHash -LiteralPath $baseline -Algorithm SHA256).Hash.ToLowerInvariant()
if ($actual -ne $metadata.sha256) { throw 'Current runtime baseline checksum mismatch' }
$assets = Get-Content -LiteralPath (Join-Path $PSScriptRoot 'runtime-reference\runtime-assets.json') -Raw -Encoding utf8 | ConvertFrom-Json -AsHashtable
foreach ($name in $assets.Keys) {
    $path = Join-Path $PSScriptRoot ('src\main\resources\' + $name)
    if (-not (Test-Path -LiteralPath $path) -or (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash.ToLowerInvariant() -ne $assets[$name]) {
        throw "Runtime resource has changed; this command cannot package new source changes: $name"
    }
}
if ($VerifyOnly) {
    Write-Host "Current runtime baseline and $($assets.Count) assets match; no output generated."
    return
}
$destination = Join-Path $PSScriptRoot 'build\current-baseline\qys-bosses.jar'
New-Item -ItemType Directory -Path (Split-Path $destination -Parent) -Force | Out-Null
Copy-Item -LiteralPath $baseline -Destination $destination -Force
if ((Get-FileHash -LiteralPath $destination -Algorithm SHA256).Hash.ToLowerInvariant() -ne $metadata.sha256) { throw 'Output checksum mismatch' }
Write-Host "Preserved runtime artifact: $destination"
Write-Host 'This reproduces the accepted runtime artifact. It does not compile Java, apply patches, or deploy to Minecraft.'
