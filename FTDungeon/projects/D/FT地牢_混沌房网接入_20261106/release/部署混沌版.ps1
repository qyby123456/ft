$ErrorActionPreference = 'Stop'
$instanceRoot = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2'
$releaseRoot = if (Test-Path -LiteralPath (Join-Path $PSScriptRoot 'verification.json')) { $PSScriptRoot } else { Join-Path $PSScriptRoot 'release' }
$verification = Get-Content -LiteralPath (Join-Path $releaseRoot 'verification.json') -Raw | ConvertFrom-Json
if ($verification.nativeTestsPassed -ne 8) { throw '原生测试验证记录不完整' }
$activeGame = Get-CimInstance Win32_Process -Filter "Name='java.exe' OR Name='javaw.exe'" | Where-Object {
    $_.CommandLine -and $_.CommandLine.Contains('--gameDir') -and $_.CommandLine.Contains($instanceRoot)
}
if ($activeGame) { throw '当前Forge实例仍在运行，请保存退出游戏后重新运行此脚本。' }
$modsRoot = [IO.Path]::GetFullPath((Join-Path $instanceRoot 'mods'))
$oldJar = [IO.Path]::GetFullPath((Join-Path $modsRoot 'FT地牢-Forge-1.20.1-0.21.0.jar'))
$newName = 'FT地牢-Forge-1.20.1-0.22.0.jar'
$newJar = [IO.Path]::GetFullPath((Join-Path $modsRoot $newName))
$package = Join-Path $releaseRoot $newName
if ([IO.Path]::GetDirectoryName($oldJar) -ne $modsRoot -or [IO.Path]::GetDirectoryName($newJar) -ne $modsRoot) { throw '目标路径超出mods目录' }
if (Test-Path -LiteralPath $newJar) { throw '0.22.0已存在，请核对安装状态，脚本不会覆盖。' }
if ((Get-FileHash -LiteralPath $oldJar -Algorithm SHA256).Hash -ne $verification.previousJarSha256) { throw '已安装FT地牢与本次基线不符，拒绝覆盖其他修改。' }
if ((Get-FileHash -LiteralPath $package -Algorithm SHA256).Hash -ne $verification.jarSha256) { throw '候选JAR校验失败' }
$backupRoot = [IO.Path]::GetFullPath((Join-Path $instanceRoot 'ft-mod-backups\ftdungeonmobs\chaos-r5'))
$backup = [IO.Path]::GetFullPath((Join-Path $backupRoot (Get-Date -Format 'yyyyMMdd-HHmmss-fff')))
if (-not $backup.StartsWith($backupRoot + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) { throw '备份路径校验失败' }
New-Item -ItemType Directory -Path $backup | Out-Null
$savedJar = Join-Path $backup ([IO.Path]::GetFileName($oldJar))
Copy-Item -LiteralPath $oldJar -Destination $savedJar
if ((Get-FileHash -LiteralPath $savedJar -Algorithm SHA256).Hash -ne $verification.previousJarSha256) { throw '备份校验失败' }
$pending = $newJar + '.pending'
if (Test-Path -LiteralPath $pending) { throw '存在未完成部署文件，请人工核对。' }
Copy-Item -LiteralPath $package -Destination $pending
if ((Get-FileHash -LiteralPath $pending -Algorithm SHA256).Hash -ne $verification.jarSha256) { throw '暂存校验失败，原JAR未改动' }
Move-Item -LiteralPath $oldJar -Destination (Join-Path $backup 'original-disabled.jar')
try {
    Move-Item -LiteralPath $pending -Destination $newJar
} catch {
    Copy-Item -LiteralPath $savedJar -Destination $oldJar
    throw
}
$receipt = [ordered]@{ installed = $newJar; sha256 = $verification.jarSha256; backup = $backup; playerWorldsModified = $false; installedAt = (Get-Date).ToString('o') }
$receipt | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $backup 'receipt.json') -Encoding utf8
Write-Output ('已安装混沌地牢0.22.0，备份：' + $backup)
