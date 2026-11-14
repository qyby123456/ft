param(
    [ValidateRange(1, 86400)][int]$TimeoutSeconds = 4800,
    [ValidateRange(1, 3600)][int]$PollSeconds = 20
)

$ErrorActionPreference = 'Stop'
$deadline = [DateTime]::UtcNow.AddSeconds($TimeoutSeconds)
Write-Output 'Waiting for Minecraft to exit. No jar will be replaced while the game process exists.'
while ([DateTime]::UtcNow -lt $deadline) {
    $game = Get-CimInstance Win32_Process -Filter "name='java.exe' OR name='javaw.exe'" -ErrorAction Stop |
        Where-Object { $_.CommandLine -match 'forgeclient|--gameDir|net\.minecraft\.client\.main' }
    if (-not $game) {
        & powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot 'deploy_jar.ps1') -NoBuild
        exit $LASTEXITCODE
    }
    Start-Sleep -Seconds $PollSeconds
}
Write-Warning 'Timed out without deploying. The artifact remains in build/libs.'
exit 2
