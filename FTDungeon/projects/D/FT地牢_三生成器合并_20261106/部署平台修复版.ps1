param([Parameter(Mandatory=$true)][string]$Instance)
$ErrorActionPreference = 'Stop'
$instanceRoot = (Resolve-Path -LiteralPath $Instance).Path
$modsRoot = (Resolve-Path -LiteralPath (Join-Path $instanceRoot 'mods')).Path
$sourceJar = Join-Path $PSScriptRoot 'build\libs\ft-dungeon-mobs-forge-1.20.1-0.24.2.jar'
$expectedHash = '4f9ddcfc060fe56719f5f1fd7c4d1baed62a4b174886b09e7b8329d42c948304'
if ((Get-FileHash -LiteralPath $sourceJar -Algorithm SHA256).Hash.ToLowerInvariant() -ne $expectedHash) { throw 'Merged artifact changed' }
foreach ($mode in @('recover', 'legacy', 'blocked')) {
    $test = Get-Content -LiteralPath (Join-Path $PSScriptRoot "verification\arrival-fix\$mode-result.json") -Raw | ConvertFrom-Json
    if ($test.status -ne ('PASS_' + $mode.ToUpperInvariant()) -or $test.jarSha256 -ne $expectedHash) { throw "Installed $mode verification not passed for this artifact" }
}
$gate = Get-Content -LiteralPath (Join-Path $PSScriptRoot 'verification\arrival-fix\native-tests\ready.json') -Raw | ConvertFrom-Json
if ($gate.status -notlike 'PASS*') { throw 'Native arrival geometry verification not passed' }
$audit = Get-Content -LiteralPath (Join-Path $PSScriptRoot 'verification\arrival-fix\artifact-audit.json') -Raw | ConvertFrom-Json
if ($audit.status -ne 'PASS' -or $audit.jarSha256 -ne $expectedHash -or !$audit.nativeSourceHashesMatch) { throw 'Latest creature preservation audit failed' }
$running = @(Get-CimInstance Win32_Process | Where-Object { $_.Name -match '^java(w)?\.exe$' -and $_.CommandLine -and $_.CommandLine.IndexOf($instanceRoot, [StringComparison]::OrdinalIgnoreCase) -ge 0 })
if ($running.Count -gt 0) { throw 'Target Minecraft instance is running' }
$oldJar = Join-Path $modsRoot 'FT地牢-Forge-1.20.1-0.24.1.jar'
$oldHash = '962c66b751e1a7bb72773f9867f5324cfbb0a575f826efa909233d07c4c58a9b'
if ((Get-FileHash -LiteralPath $oldJar -Algorithm SHA256).Hash.ToLowerInvariant() -ne $oldHash) { throw 'Installed baseline changed; refusing overwrite' }
$newJar = Join-Path $modsRoot 'FT地牢-Forge-1.20.1-0.24.2.jar'
$pendingJar = $newJar + '.pending'
if ((Test-Path -LiteralPath $newJar) -or (Test-Path -LiteralPath $pendingJar)) { throw 'Destination already exists' }
$before = @(Get-ChildItem -LiteralPath $modsRoot -File | Where-Object { $_.FullName -ne $oldJar } | ForEach-Object { [pscustomobject]@{ name=$_.Name; bytes=$_.Length; lastWrite=$_.LastWriteTimeUtc.ToString('o') } })
$backupRoot = Join-Path $instanceRoot ('ft-mod-backups\ftdungeonmobs\arrival-fix-' + (Get-Date -Format 'yyyyMMdd-HHmmss'))
New-Item -ItemType Directory -Path $backupRoot -Force | Out-Null
$backupRoot = (Resolve-Path -LiteralPath $backupRoot).Path
if (!$backupRoot.StartsWith($instanceRoot + '\', [StringComparison]::OrdinalIgnoreCase)) { throw 'Backup path outside target instance' }
$backupJar = Join-Path $backupRoot (Split-Path -Leaf $oldJar)
Copy-Item -LiteralPath $oldJar -Destination $backupJar
if ((Get-FileHash -LiteralPath $backupJar -Algorithm SHA256).Hash.ToLowerInvariant() -ne $oldHash) { throw 'Backup mismatch' }
$configs = @(Get-ChildItem -LiteralPath (Join-Path $instanceRoot 'config') -File | Where-Object { $_.Name -match '^(ftdungeon|fteclipse)' })
foreach ($config in $configs) { Copy-Item -LiteralPath $config.FullName -Destination $backupRoot }
Copy-Item -LiteralPath $sourceJar -Destination $pendingJar
if ((Get-FileHash -LiteralPath $pendingJar -Algorithm SHA256).Hash.ToLowerInvariant() -ne $expectedHash) { throw 'Staged artifact mismatch' }
try {
    Move-Item -LiteralPath $oldJar -Destination (Join-Path $backupRoot 'previous-installed.jar')
    Move-Item -LiteralPath $pendingJar -Destination $newJar
    if ((Get-FileHash -LiteralPath $newJar -Algorithm SHA256).Hash.ToLowerInvariant() -ne $expectedHash) { throw 'Installed artifact mismatch' }
} catch {
    if (!(Test-Path -LiteralPath $oldJar)) { Copy-Item -LiteralPath $backupJar -Destination $oldJar }
    throw
}
$after = @(Get-ChildItem -LiteralPath $modsRoot -File | Where-Object { $_.FullName -ne $newJar } | ForEach-Object { [pscustomobject]@{ name=$_.Name; bytes=$_.Length; lastWrite=$_.LastWriteTimeUtc.ToString('o') } })
if (Compare-Object ($before | ConvertTo-Json -Compress) ($after | ConvertTo-Json -Compress)) { throw 'Unrelated mod metadata changed' }
$receipt = [ordered]@{ status='DEPLOYED'; version='0.24.2'; target=$newJar; sha256=$expectedHash; oldSha256=$oldHash; backup=$backupRoot; otherModFilesUnchanged=$before.Count; playerWorldsModified=$false; gameStarted=$false; sourceProject=$PSScriptRoot; existingSarcophagusCheckFailurePreserved=$true; deployedAt=(Get-Date).ToString('o') }
$receipt | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath (Join-Path $backupRoot 'deployment.json') -Encoding utf8
$receipt | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath (Join-Path $PSScriptRoot 'verification\arrival-fix\deployment.json') -Encoding utf8
$receipt | ConvertTo-Json -Depth 4
