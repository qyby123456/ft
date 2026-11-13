$ErrorActionPreference = 'Stop'
$animVersion = '1.8.41'
$animReport = Get-Content -LiteralPath (Join-Path $PSScriptRoot 'verification-1.8.41.json') -Raw | ConvertFrom-Json
$animJar = Join-Path $PSScriptRoot "build/libs/farmingtales_drink_anim-$animVersion.jar"
$animHash = (Get-FileHash -LiteralPath $animJar -Algorithm SHA256).Hash.ToLowerInvariant()
if ($animReport.version -ne $animVersion -or $animHash -ne $animReport.sha256) { throw 'Release does not match verification.' }
$animPack = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../..'))
$animMods = Join-Path $animPack 'mods'
$animConfig = Join-Path $animPack 'config/farmingtales_drink_anim-client.toml'
$animConfigHash = (Get-FileHash -LiteralPath $animConfig -Algorithm SHA256).Hash.ToLowerInvariant()
if ($animConfigHash -ne $animReport.configuration_sha256) { throw 'Configuration changed since verification.' }
$animPrevious = @(Get-ChildItem -LiteralPath $animMods -File | Where-Object { $_.Name -like '*farmingtales_drink_anim*.jar' })
if ($animPrevious.Count -ne 1 -or $animPrevious[0].Name -ne '[农场物语-饮用与检视动画]farmingtales_drink_anim-1.8.40.jar') {
    throw 'Expected exactly one installed 1.8.40 animation mod.'
}
$animOld = $animPrevious[0].FullName
$animBackup = Join-Path $PSScriptRoot ('backups/1.8.40-before-foam-corks/' + $animPrevious[0].Name)
if ((Get-FileHash -LiteralPath $animOld -Algorithm SHA256).Hash.ToLowerInvariant() -ne $animReport.previous_sha256) { throw 'Installed baseline changed.' }
if ((Get-FileHash -LiteralPath $animBackup -Algorithm SHA256).Hash.ToLowerInvariant() -ne $animReport.previous_sha256) { throw 'Backup differs from installed baseline.' }
$animTarget = Join-Path $animMods "[农场物语-饮用与检视动画]farmingtales_drink_anim-$animVersion.jar"
$animPending = $animTarget + '.pending'
Copy-Item -LiteralPath $animJar -Destination $animPending
if ((Get-FileHash -LiteralPath $animPending -Algorithm SHA256).Hash.ToLowerInvariant() -ne $animHash) { throw 'Staged JAR hash mismatch.' }
try {
    Remove-Item -LiteralPath $animOld
    Move-Item -LiteralPath $animPending -Destination $animTarget
    if ((Get-FileHash -LiteralPath $animTarget -Algorithm SHA256).Hash.ToLowerInvariant() -ne $animHash) { throw 'Installed JAR hash mismatch.' }
    if ((Get-FileHash -LiteralPath $animConfig -Algorithm SHA256).Hash.ToLowerInvariant() -ne $animConfigHash) { throw 'Configuration changed during installation.' }
} catch {
    if (Test-Path -LiteralPath $animTarget) { Remove-Item -LiteralPath $animTarget }
    if (!(Test-Path -LiteralPath $animOld)) { Copy-Item -LiteralPath $animBackup -Destination $animOld }
    throw
}
$animInstalled = @(Get-ChildItem -LiteralPath $animMods -File | Where-Object { $_.Name -like '*farmingtales_drink_anim*.jar' })
if ($animInstalled.Count -ne 1 -or $animInstalled[0].FullName -ne $animTarget) { throw 'Unexpected duplicate animation JAR.' }
[ordered]@{
    installed_at = (Get-Date).ToString('o')
    version = $animVersion
    installed = $animTarget
    sha256 = $animHash
    backup = $animBackup
    previous_sha256 = $animReport.previous_sha256
    configuration_sha256 = $animConfigHash
    unit_tests = $animReport.unit_tests
    client_retested = $animReport.client_retested
    client_profiles = $animReport.client_profiles
    foam_items = $animReport.foam_items
    scope = $animReport.scope
} | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath (Join-Path $PSScriptRoot 'installation-1.8.41.json') -Encoding utf8
Write-Output "Installed $animTarget"
Write-Output "SHA256 $animHash"
