param([string]$Catalog = (Join-Path $PSScriptRoot '..\catalog'))
$ErrorActionPreference = 'Stop'
$catalogRoot = (Resolve-Path -LiteralPath $Catalog).Path
$readyPath = Join-Path $catalogRoot 'ready.json'
if (-not (Test-Path -LiteralPath $readyPath)) { throw 'Catalog ready.json is required; do not snapshot a live draft.' }
$manifestPath = Join-Path $catalogRoot 'manifest.json'
$before = (Get-FileHash -LiteralPath $manifestPath -Algorithm SHA256).Hash.ToLowerInvariant()
$manifest = Get-Content -LiteralPath $manifestPath -Raw | ConvertFrom-Json
$ready = Get-Content -LiteralPath $readyPath -Raw | ConvertFrom-Json
if ($ready.apiVersion -ne 1 -or $ready.ready -ne $true -or $ready.manifestSha256 -ne $before) { throw 'Catalog ready marker does not cover manifest' }
if ($manifest.schemaVersion -ne 1 -or $manifest.templates.Count -lt 1) { throw 'Invalid catalog schema' }
$target = Join-Path $PSScriptRoot 'src\main\resources\data\biomedungeon'
New-Item -ItemType Directory -Force -Path (Join-Path $target 'structures') | Out-Null
$records = @()
foreach ($template in $manifest.templates) {
    if ($template.id -notmatch '^[a-z0-9][a-z0-9_/-]{0,127}$' -or $template.id.Contains('..') -or $template.resourceId -ne ('biomedungeon:' + $template.id) -or $template.accepted -ne $true) { throw 'Unsafe/unaccepted resource' }
    $source = Join-Path $catalogRoot ('data\biomedungeon\structures\' + $template.id + '.nbt')
    $hash = (Get-FileHash -LiteralPath $source -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($hash -ne $template.sha256) { throw "Source hash mismatch: $source" }
    $destination = Join-Path $target ('structures\' + $template.id + '.nbt')
    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $destination) | Out-Null
    Copy-Item -LiteralPath $source -Destination $destination
    if ((Get-FileHash -LiteralPath $destination -Algorithm SHA256).Hash.ToLowerInvariant() -ne $hash) { throw 'Snapshot copy mismatch' }
    $records += @{ id = $template.id; sha256 = $hash }
}
Copy-Item -LiteralPath $manifestPath -Destination (Join-Path $target 'manifest.json')
if ((Get-FileHash -LiteralPath $manifestPath -Algorithm SHA256).Hash.ToLowerInvariant() -ne $before -or (Get-FileHash -LiteralPath (Join-Path $target 'manifest.json') -Algorithm SHA256).Hash.ToLowerInvariant() -ne $before) { throw 'Manifest changed during snapshot' }
@{ component = 'runtime'; source = $catalogRoot; catalogReadySha256 = (Get-FileHash -LiteralPath $readyPath -Algorithm SHA256).Hash.ToLowerInvariant(); manifestSha256 = $before; templates = $records } | ConvertTo-Json -Depth 10 | Set-Content -LiteralPath (Join-Path $PSScriptRoot 'catalog-snapshot.json') -Encoding utf8
Write-Host "Verified catalog snapshot: $($records.Count) templates; manifest $before"
