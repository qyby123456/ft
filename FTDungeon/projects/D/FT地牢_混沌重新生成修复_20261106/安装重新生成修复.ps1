param([Parameter(Mandatory=$true)][string]$Instance)
$ErrorActionPreference = 'Stop'
$instanceRoot = (Resolve-Path -LiteralPath $Instance).Path
$modsRoot = (Resolve-Path -LiteralPath (Join-Path $instanceRoot 'mods')).Path
$verification = Get-Content -LiteralPath (Join-Path $PSScriptRoot 'release\verification.json') -Raw | ConvertFrom-Json
$running = @(Get-CimInstance Win32_Process -Filter "Name='java.exe' OR Name='javaw.exe'" | Where-Object {
    $_.CommandLine -and $_.CommandLine.IndexOf($instanceRoot, [StringComparison]::OrdinalIgnoreCase) -ge 0
})
if ($running.Count -gt 0) { throw '当前整合包正在运行，请保存退出后安装' }
$oldJar = Join-Path $modsRoot 'FT地牢-Forge-1.20.1-0.24.2.jar'
$newJar = Join-Path $modsRoot 'FT地牢-Forge-1.20.1-0.24.3.jar'
$candidate = Join-Path $PSScriptRoot 'release\FT地牢-Forge-1.20.1-0.24.3.jar'
$active = @(Get-ChildItem -LiteralPath $modsRoot -File | Where-Object { $_.Name -match '^FT地牢-.*\.jar$' })
if ($active.Count -ne 1 -or $active[0].FullName -ne $oldJar) { throw '活动地牢版本已变化，需要先合并最新改动' }
if ((Get-FileHash -LiteralPath $oldJar -Algorithm SHA256).Hash.ToLowerInvariant() -ne $verification.baselineSha256) { throw '已安装基线校验不符' }
if ((Get-FileHash -LiteralPath $candidate -Algorithm SHA256).Hash.ToLowerInvariant() -ne $verification.sha256) { throw '候选包校验不符' }
$backupRoot = [IO.Path]::GetFullPath((Join-Path $instanceRoot 'ft-mod-backups\ftdungeonmobs'))
$backup = [IO.Path]::GetFullPath((Join-Path $backupRoot ('chaos-regeneration-' + (Get-Date -Format 'yyyyMMdd-HHmmss-fff'))))
if (-not $backup.StartsWith($backupRoot + '\', [StringComparison]::OrdinalIgnoreCase)) { throw '备份路径超出指定目录' }
if ((Test-Path -LiteralPath $newJar) -or (Test-Path -LiteralPath ($newJar + '.pending'))) { throw '目标文件已存在' }
New-Item -ItemType Directory -Path $backup | Out-Null
$savedJar = Join-Path $backup ([IO.Path]::GetFileName($oldJar))
$pending = $newJar + '.pending'
Copy-Item -LiteralPath $candidate -Destination $pending
if ((Get-FileHash -LiteralPath $pending -Algorithm SHA256).Hash.ToLowerInvariant() -ne $verification.sha256) { throw '暂存文件校验失败' }
Move-Item -LiteralPath $oldJar -Destination $savedJar
try {
    Move-Item -LiteralPath $pending -Destination $newJar
    if ((Get-FileHash -LiteralPath $newJar -Algorithm SHA256).Hash.ToLowerInvariant() -ne $verification.sha256) { throw '安装后校验失败' }
} catch {
    if (Test-Path -LiteralPath $newJar) { Move-Item -LiteralPath $newJar -Destination (Join-Path $backup 'failed-candidate.jar') }
    Copy-Item -LiteralPath $savedJar -Destination $oldJar
    throw
}
[ordered]@{
    status = 'DEPLOYED_RESTART_REQUIRED'
    version = $verification.version
    target = $newJar
    sha256 = $verification.sha256
    backup = $savedJar
    playerWorldsModifiedOffline = $false
    installedAt = [DateTimeOffset]::Now.ToString('o')
} | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $PSScriptRoot 'release\installation.json') -Encoding utf8
Write-Output "已安装重新生成修复版：$newJar"
Write-Output "原包备份：$savedJar"
