$ErrorActionPreference = 'Stop'
$animVersion = '1.8.40'
$animReport = Get-Content -LiteralPath (Join-Path $PSScriptRoot 'verification.json') -Raw | ConvertFrom-Json
if ($animReport.version -ne $animVersion) { throw 'Run verify_release_v1840.py for 1.8.40 before installing.' }
$animJar = Join-Path $PSScriptRoot "build/libs/farmingtales_drink_anim-$animVersion.jar"
$animHash = (Get-FileHash -LiteralPath $animJar -Algorithm SHA256).Hash.ToLowerInvariant()
if ($animHash -ne $animReport.sha256) { throw 'Verified JAR hash no longer matches.' }
$animMods = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../../mods'))
$animName = "[农场物语-饮用与检视动画]farmingtales_drink_anim-$animVersion.jar"
$animTarget = Join-Path $animMods $animName
$animPending = $animTarget + '.pending'
$animPrevious = @(Get-ChildItem -LiteralPath $animMods -File | Where-Object { $_.Name -like '*farmingtales_drink_anim*.jar' })
if ($animPrevious.Count -ne 1 -or $animPrevious[0].Name -ne '[农场物语-饮用与检视动画]farmingtales_drink_anim-1.8.39.jar') {
    throw 'Expected exactly one installed 1.8.39 animation mod; inspect the current installation.'
}
$animOld = $animPrevious[0].FullName
$animBackup = Join-Path $PSScriptRoot ('backups/1.8.39-before-brewery-vinery/' + $animPrevious[0].Name)
$animOldHash = (Get-FileHash -LiteralPath $animOld -Algorithm SHA256).Hash
if (Test-Path -LiteralPath $animBackup) {
    if ((Get-FileHash -LiteralPath $animBackup -Algorithm SHA256).Hash -ne $animOldHash) { throw 'Backup differs from installed previous version.' }
} else {
    Copy-Item -LiteralPath $animOld -Destination $animBackup
}
$animConfig = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../../config/farmingtales_drink_anim-client.toml'))
$animConfigBackup = Join-Path $PSScriptRoot 'backups/1.8.39-before-brewery-vinery/farmingtales_drink_anim-client.toml'
$animConfigBefore = [IO.File]::ReadAllText($animConfig)
if ((Get-FileHash -LiteralPath $animConfigBackup -Algorithm SHA256).Hash -ne (Get-FileHash -LiteralPath $animConfig -Algorithm SHA256).Hash) { throw 'Configuration backup differs from current settings.' }
if ($animReport.soundVolume -ne 0.80 -or $animReport.inspectionVolumeMultiplier -ne 1.70) { throw 'Expected unchanged global gain and cloth inspection gain.' }
Copy-Item -LiteralPath $animJar -Destination $animPending
if ((Get-FileHash -LiteralPath $animPending -Algorithm SHA256).Hash.ToLowerInvariant() -ne $animHash) { throw 'Pending copy hash mismatch.' }
try {
    Remove-Item -LiteralPath $animOld
    Move-Item -LiteralPath $animPending -Destination $animTarget
    if ((Get-FileHash -LiteralPath $animTarget -Algorithm SHA256).Hash.ToLowerInvariant() -ne $animHash) { throw 'Installed JAR hash mismatch.' }
    if ([IO.File]::ReadAllText($animConfig) -cne $animConfigBefore) { throw 'Sound configuration changed unexpectedly.' }
} catch {
    if (Test-Path -LiteralPath $animTarget) { Remove-Item -LiteralPath $animTarget }
    if (!(Test-Path -LiteralPath $animOld)) { Copy-Item -LiteralPath $animBackup -Destination $animOld }
    throw
}
$animInstalled = @(Get-ChildItem -LiteralPath $animMods -File | Where-Object { $_.Name -like '*farmingtales_drink_anim*.jar' })
if ($animInstalled.Count -ne 1 -or $animInstalled[0].Name -ne $animName) { throw 'Unexpected duplicate animation JAR.' }
[ordered]@{
    installed_at = (Get-Date).ToString('o')
    version = $animVersion
    installed = $animTarget
    sha256 = $animHash
    backup = $animBackup
    previous_sha256 = $animOldHash.ToLowerInvariant()
    soundVolume = 0.80
    inspectionVolumeMultiplier = 1.70
    liquidGainMultiplier = $animReport.liquidGainMultiplier
    configuration = $animConfig
    configuration_backup = $animConfigBackup
    supported_drinks = @($animReport.profiles | ForEach-Object { $_.id })
    blockbench_projects = $animReport.blockbench.Count
    unit_tests = $animReport.unit_tests
    client_retested = $animReport.client_retested
    scope = $animReport.scope
} | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath (Join-Path $PSScriptRoot 'installation-1.8.40.json') -Encoding utf8
Write-Output "Installed $animTarget"
Write-Output "SHA256 $animHash"
