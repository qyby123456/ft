param([Parameter(Mandatory=$true)][int]$GameProcessId, [Parameter(Mandatory=$true)][string]$Instance)
$ErrorActionPreference = 'Stop'
$log = Join-Path $PSScriptRoot 'release\deferred-install.log'
try {
    Write-Output "等待游戏进程 $GameProcessId 退出" | Set-Content -LiteralPath $log -Encoding utf8
    if (Get-Process -Id $GameProcessId -ErrorAction SilentlyContinue) {
        Wait-Process -Id $GameProcessId -ErrorAction SilentlyContinue
    }
    & (Join-Path $PSScriptRoot '安装出生房修复.ps1') -Instance $Instance *>> $log
} catch {
    $_ | Out-String | Add-Content -LiteralPath $log -Encoding utf8
    exit 1
}
