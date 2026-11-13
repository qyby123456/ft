$ErrorActionPreference = 'Stop'
$instanceRoot = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2'
$releaseRoot = Join-Path $PSScriptRoot 'release'
$verification = Get-Content -Raw -Encoding UTF8 -LiteralPath (Join-Path $releaseRoot 'verification.json') | ConvertFrom-Json
if ($verification.native_tests_passed -ne 12) { throw '实体与地牢回归测试记录不完整' }
$activeGame = Get-CimInstance Win32_Process -Filter "Name='java.exe' OR Name='javaw.exe'" | Where-Object {
    $_.CommandLine -and $_.CommandLine.Contains($instanceRoot) -and $_.CommandLine -match 'BootstrapLauncher|minecraftforge.server|forgeclient|forgeserver'
}
if ($activeGame) { throw '当前整合包正在运行，请保存退出后再执行安装脚本' }
$modsRoot = [IO.Path]::GetFullPath((Join-Path $instanceRoot 'mods'))
$oldJar = Join-Path $modsRoot 'FT地牢-Forge-1.20.1-0.23.0.jar'
$newJar = Join-Path $modsRoot 'FT地牢-Forge-1.20.1-0.24.0.jar'
$package = Join-Path $releaseRoot 'FT地牢-Forge-1.20.1-0.24.0.jar'
if (Test-Path -LiteralPath $newJar) { throw '0.24.0已存在，拒绝覆盖' }
if ((Get-FileHash -LiteralPath $oldJar -Algorithm SHA256).Hash -ne $verification.baseline_sha256) { throw '已安装版本已发生变化，请先合并最新改动' }
if ((Get-FileHash -LiteralPath $package -Algorithm SHA256).Hash -ne $verification.sha256) { throw '候选JAR校验失败' }
$activeJars = @(Get-ChildItem -LiteralPath $modsRoot -File | Where-Object { $_.Name -match '^FT地牢-.*\.jar$' })
if ($activeJars.Count -ne 1 -or $activeJars[0].FullName -ne $oldJar) { throw '发现额外FT地牢活动JAR，拒绝重复安装' }
$backup = [IO.Path]::GetFullPath((Join-Path $instanceRoot ('ft-mod-backups\ftdungeonmobs\creature-import-' + (Get-Date -Format 'yyyyMMdd-HHmmss-fff'))))
if (-not $backup.StartsWith($instanceRoot + '\ft-mod-backups\ftdungeonmobs\', [StringComparison]::OrdinalIgnoreCase)) { throw '备份路径超出预期目录' }
New-Item -ItemType Directory -Path $backup | Out-Null
$savedJar = Join-Path $backup ([IO.Path]::GetFileName($oldJar))
$pending = $newJar + '.pending'
if (Test-Path -LiteralPath $pending) { throw '存在未完成暂存文件' }
Copy-Item -LiteralPath $package -Destination $pending
if ((Get-FileHash -LiteralPath $pending -Algorithm SHA256).Hash -ne $verification.sha256) { throw '暂存校验失败' }
Move-Item -LiteralPath $oldJar -Destination $savedJar
try {
    Move-Item -LiteralPath $pending -Destination $newJar
    if ((Get-FileHash -LiteralPath $newJar -Algorithm SHA256).Hash -ne $verification.sha256) { throw '安装后校验失败' }
} catch {
    if (Test-Path -LiteralPath $newJar) { Move-Item -LiteralPath $newJar -Destination (Join-Path $backup 'failed-candidate.jar') }
    Copy-Item -LiteralPath $savedJar -Destination $oldJar
    throw
}
$receipt = [ordered]@{ installed = $newJar; sha256 = $verification.sha256; backup = $savedJar; playerWorldFilesModified = $false; installedAt = (Get-Date).ToString('o') }
$receipt | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $backup 'receipt.json') -Encoding utf8
$receipt | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $releaseRoot 'installation.json') -Encoding utf8
Write-Output ('已安装FT地牢0.24.0；备份：' + $savedJar)
