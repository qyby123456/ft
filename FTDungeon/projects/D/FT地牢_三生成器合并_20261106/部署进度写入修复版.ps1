param([Parameter(Mandatory=$true)][string]$Instance)
$ErrorActionPreference = 'Stop'
$instanceRoot = (Resolve-Path -LiteralPath $Instance).Path
$modsRoot = (Resolve-Path -LiteralPath (Join-Path $instanceRoot 'mods')).Path
$evidence = Join-Path $PSScriptRoot 'verification\io-fix'
$audit = Get-Content -LiteralPath (Join-Path $evidence 'artifact-audit.json') -Raw | ConvertFrom-Json
if ($audit.status -ne 'PASS') { throw 'Artifact preservation audit failed' }
$sourceJar = Join-Path $PSScriptRoot 'build\libs\ft-dungeon-mobs-forge-1.20.1-0.24.4.jar'
$expectedHash = $audit.sha256
if ((Get-FileHash -LiteralPath $sourceJar).Hash.ToLowerInvariant() -ne $expectedHash) { throw 'Artifact changed' }
foreach ($mode in @('atomic', 'resume', 'conflict')) {
    $test = Get-Content -LiteralPath (Join-Path $evidence "$mode-result.json") -Raw | ConvertFrom-Json
    if ($test.status -ne 'PASS') { throw "Verification failed: $mode" }
    if ($mode -ne 'atomic' -and $test.jarSha256 -ne $expectedHash) { throw 'Tested artifact mismatch' }
}
$running = @(Get-CimInstance Win32_Process | Where-Object { $_.Name -match '^java(w)?\.exe$' -and $_.CommandLine -and $_.CommandLine.IndexOf($instanceRoot, [StringComparison]::OrdinalIgnoreCase) -ge 0 })
if ($running.Count -gt 0) { throw 'Target instance is running' }
$oldJar = Join-Path $modsRoot 'FT地牢-Forge-1.20.1-0.24.3.jar'
if ((Get-FileHash -LiteralPath $oldJar).Hash.ToLowerInvariant() -ne $audit.oldSha256) { throw 'Installed baseline changed' }
$newJar = Join-Path $modsRoot 'FT地牢-Forge-1.20.1-0.24.4.jar'
$pendingJar = $newJar + '.pending'
if ((Test-Path -LiteralPath $newJar) -or (Test-Path -LiteralPath $pendingJar)) { throw 'Destination already exists' }
$before = @(Get-ChildItem -LiteralPath $modsRoot -File | Where-Object { $_.FullName -ne $oldJar } | ForEach-Object { "$($_.Name)|$($_.Length)|$($_.LastWriteTimeUtc.Ticks)" })
$backupRoot = Join-Path $instanceRoot ('ft-mod-backups\ftdungeonmobs\io-fix-' + (Get-Date -Format 'yyyyMMdd-HHmmss'))
New-Item -ItemType Directory -Path $backupRoot | Out-Null
$backupRoot = (Resolve-Path -LiteralPath $backupRoot).Path
if (!$backupRoot.StartsWith($instanceRoot + '\', [StringComparison]::OrdinalIgnoreCase)) { throw 'Invalid backup path' }
$backupJar = Join-Path $backupRoot (Split-Path -Leaf $oldJar)
Copy-Item -LiteralPath $oldJar -Destination $backupJar
if ((Get-FileHash -LiteralPath $backupJar).Hash.ToLowerInvariant() -ne $audit.oldSha256) { throw 'Backup mismatch' }
Copy-Item -LiteralPath $sourceJar -Destination $pendingJar
if ((Get-FileHash -LiteralPath $pendingJar).Hash.ToLowerInvariant() -ne $expectedHash) { throw 'Staged artifact mismatch' }
try {
    Move-Item -LiteralPath $oldJar -Destination (Join-Path $backupRoot 'previous-installed.jar')
    Move-Item -LiteralPath $pendingJar -Destination $newJar
} catch {
    if (!(Test-Path -LiteralPath $oldJar)) { Copy-Item -LiteralPath $backupJar -Destination $oldJar }
    throw
}
if ((Get-FileHash -LiteralPath $newJar).Hash.ToLowerInvariant() -ne $expectedHash) { throw 'Installed artifact mismatch' }
$after = @(Get-ChildItem -LiteralPath $modsRoot -File | Where-Object { $_.FullName -ne $newJar } | ForEach-Object { "$($_.Name)|$($_.Length)|$($_.LastWriteTimeUtc.Ticks)" })
if (Compare-Object $before $after) { throw 'Unrelated mod metadata changed' }
$receipt = [ordered]@{ status='DEPLOYED'; version='0.24.4'; target=$newJar; sha256=$expectedHash; backup=$backupRoot; playerWorldsModified=$false; otherModFilesUnchanged=$before.Count; gameStarted=$false; sourceProject=$PSScriptRoot; deployedAt=(Get-Date).ToString('o') }
$receipt | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $backupRoot 'deployment.json') -Encoding utf8
$receipt | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $evidence 'deployment.json') -Encoding utf8
$receipt | ConvertTo-Json
