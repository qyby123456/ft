$ErrorActionPreference = 'Stop'
$animReport = Get-Content -LiteralPath (Join-Path $PSScriptRoot 'verification-1.8.74.json') -Raw -Encoding UTF8 | ConvertFrom-Json
$animBackupDir = Join-Path $PSScriptRoot 'backups/1.8.73-before-pineapple-first'
$animBaseline = Get-Content -LiteralPath (Join-Path $animBackupDir 'baseline.json') -Raw -Encoding UTF8 | ConvertFrom-Json
$animJar = Join-Path $PSScriptRoot 'build/libs/farmingtales_drink_anim-1.8.74.jar'
$animHash = (Get-FileHash -LiteralPath $animJar -Algorithm SHA256).Hash.ToLowerInvariant()
if ($animReport.version -ne '1.8.74' -or $animHash -ne $animReport.sha256) { throw 'JAR differs from verified release.' }
$animPack = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../..'))
$animMods = Join-Path $animPack 'mods'
$animConfig = Join-Path $animPack 'config/farmingtales_drink_anim-client.toml'
$animConfigHash = (Get-FileHash -LiteralPath $animConfig -Algorithm SHA256).Hash.ToLowerInvariant()
if ($animConfigHash -ne $animReport.configuration_sha256) { throw 'Configuration changed since verification.' }
$animPrevious = @(Get-ChildItem -LiteralPath $animMods -File | Where-Object { $_.Name -like '*drink_anim*.jar' })
if ($animPrevious.Count -ne 1) { throw 'Expected a single animation JAR.' }
# Preserve the user's current filename prefix; verify content against the saved baseline below.
$animOld = $animPrevious[0].FullName
$animBackup = Join-Path $animBackupDir ([IO.Path]::GetFileName($animBaseline.installed_path))
foreach ($animFile in @($animOld, $animBackup)) {
    if ((Get-FileHash -LiteralPath $animFile -Algorithm SHA256).Hash.ToLowerInvariant() -ne $animReport.previous_sha256) { throw 'Baseline or backup hash changed.' }
}
$animTargetName = $animPrevious[0].Name.Replace('1.8.73.jar', '1.8.74.jar')
if ($animTargetName -eq $animPrevious[0].Name) { throw 'Unexpected baseline filename.' }
$animTarget = Join-Path $animMods $animTargetName
$animPending = $animTarget + '.pending'
if ((Test-Path -LiteralPath $animTarget) -or (Test-Path -LiteralPath $animPending)) { throw 'Target already exists.' }
Copy-Item -LiteralPath $animJar -Destination $animPending
if ((Get-FileHash -LiteralPath $animPending -Algorithm SHA256).Hash.ToLowerInvariant() -ne $animHash) { throw 'Pending copy hash mismatch.' }
try {
    Remove-Item -LiteralPath $animOld
    Move-Item -LiteralPath $animPending -Destination $animTarget
    if ((Get-FileHash -LiteralPath $animTarget -Algorithm SHA256).Hash.ToLowerInvariant() -ne $animHash) { throw 'Installed JAR hash mismatch.' }
    if ((Get-FileHash -LiteralPath $animConfig -Algorithm SHA256).Hash.ToLowerInvariant() -ne $animConfigHash) { throw 'Configuration changed during installation.' }
    $animInstalled = @(Get-ChildItem -LiteralPath $animMods -File | Where-Object { $_.Name -like '*drink_anim*.jar' })
    if ($animInstalled.Count -ne 1 -or $animInstalled[0].FullName -ne $animTarget) { throw 'Unexpected duplicate animation JAR.' }
} catch {
    if (Test-Path -LiteralPath $animTarget) { Remove-Item -LiteralPath $animTarget }
    if (!(Test-Path -LiteralPath $animOld)) { Copy-Item -LiteralPath $animBackup -Destination $animOld }
    throw
}
[ordered]@{
    installed_at = (Get-Date).ToString('o')
    version = '1.8.74'
    installed = $animTarget
    sha256 = $animHash
    backup = $animBackup
    previous_sha256 = $animReport.previous_sha256
    unit_tests = $animReport.unit_tests
    client_logs = $animReport.client_logs
    scope = $animReport.scope
} | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath (Join-Path $PSScriptRoot 'installation-1.8.74.json') -Encoding utf8
Write-Output "Installed $animTarget"
Write-Output "SHA256 $animHash"
