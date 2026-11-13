$ErrorActionPreference = 'Stop'
$project = Split-Path -Parent $PSScriptRoot
$game = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2'
$mods = Join-Path $game 'mods'
$acceptance = Get-Content -LiteralPath (Join-Path $project 'output/style-rebuild-v034/acceptance.json') -Raw | ConvertFrom-Json
if ($acceptance.status -ne 'PASS' -or $acceptance.version -ne '0.34.0') { throw 'Final acceptance required' }
$candidate = $acceptance.candidate
$previous = Join-Path $mods 'FT地牢-Forge-1.20.1-0.33.0.jar'
$installed = Join-Path $mods 'FT地牢-Forge-1.20.1-0.34.0.jar'
$pending = Join-Path $mods 'FT地牢-Forge-1.20.1-0.34.0.jar.pending'
$expectedPrevious = '0f1396d0e0b0d5e8a7264477228789b64d14f6f6a103f8cea93d6af458047bfb'
function Get-Hash([string]$Path) { (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant() }
if ((Get-Hash $candidate) -ne $acceptance.jarSha256) { throw 'Candidate hash mismatch' }
if ((Get-Hash $previous) -ne $expectedPrevious) { throw 'Installed 0.33 baseline differs' }
if ((Test-Path -LiteralPath $installed) -or (Test-Path -LiteralPath $pending)) { throw 'Target already exists' }
$unrelated = @{}
foreach ($file in Get-ChildItem -LiteralPath $mods -File) {
    if ($file.FullName -ne $previous) { $unrelated[$file.Name] = Get-Hash $file.FullName }
}
$backup = Join-Path $game ('ft-mod-backups\ftdungeonmobs\all-room-rebuild-' + (Get-Date -Format 'yyyyMMdd-HHmmss'))
$backupFile = Join-Path $backup ([IO.Path]::GetFileName($previous))
foreach ($target in @($previous, $installed, $pending, $backup, $backupFile)) {
    $absolute = [IO.Path]::GetFullPath($target)
    if (-not $absolute.StartsWith(([IO.Path]::GetFullPath($game) + '\'), [StringComparison]::OrdinalIgnoreCase)) { throw 'Path outside instance' }
}
New-Item -ItemType Directory -Path $backup | Out-Null
Copy-Item -LiteralPath $candidate -Destination $pending
if ((Get-Hash $pending) -ne $acceptance.jarSha256) { throw 'Staged hash mismatch' }
$movedPrevious = $false
$installedNew = $false
try {
    Move-Item -LiteralPath $previous -Destination $backupFile
    $movedPrevious = $true
    Move-Item -LiteralPath $pending -Destination $installed
    $installedNew = $true
    if ((Get-Hash $installed) -ne $acceptance.jarSha256 -or (Get-Hash $backupFile) -ne $expectedPrevious) { throw 'Installed or backup hash mismatch' }
    $others = @(Get-ChildItem -LiteralPath $mods -File | Where-Object FullName -ne $installed)
    if ($others.Count -ne $unrelated.Count) { throw 'Unrelated mod count changed' }
    foreach ($file in $others) {
        if ($unrelated[$file.Name] -ne (Get-Hash $file.FullName)) { throw ('Unrelated mod changed: ' + $file.Name) }
    }
    $active = @(Get-ChildItem -LiteralPath $mods -File -Filter 'FT地牢-Forge-*.jar')
    if ($active.Count -ne 1) { throw 'Multiple active FT Dungeon jars' }
    $report = [ordered]@{status='PASS';version='0.34.0';deployedAt=(Get-Date -Format o);installedPath=$installed;sha256=$acceptance.jarSha256;backupPath=$backupFile;backupSha256=$expectedPrevious;activeFtDungeonJarCount=$active.Count;otherModFilesVerified=$others.Count;formalSavesConfigKubeJsWritten=$false;clientPlayTested=$false}
    $report | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath (Join-Path $project 'output/style-rebuild-v034/deployment.json') -Encoding utf8
    $report | ConvertTo-Json -Depth 8
} catch {
    if ($installedNew -and (Test-Path -LiteralPath $installed)) { Move-Item -LiteralPath $installed -Destination (Join-Path $backup 'failed-candidate.jar') }
    if ($movedPrevious -and (Test-Path -LiteralPath $backupFile)) { Move-Item -LiteralPath $backupFile -Destination $previous }
    throw
}
