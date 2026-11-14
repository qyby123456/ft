param([Parameter(Mandatory = $true)][string]$TaskPath)
$ErrorActionPreference = 'Stop'
$project = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\..\..'))
$task = [IO.Path]::GetFullPath($TaskPath)
$workRoot = [IO.Path]::GetFullPath((Join-Path $project '_work')) + [IO.Path]::DirectorySeparatorChar
if (-not $task.StartsWith($workRoot, [StringComparison]::OrdinalIgnoreCase)) { throw 'Task path escapes workspace.' }
$instance = Join-Path $env:APPDATA ('.minecraft\versions\FarmingTales_Forge' + [char]0x2161 + ' v1.5.2')
$target = Join-Path $instance 'mods\qys-bosses.jar'
$config = Join-Path $instance 'config\ftboss_trades.json'
$candidate = Join-Path $task 'qys-bosses.jar'
$backup = Join-Path $task 'baseline.jar'
$configBackup = Join-Path $task 'ftboss_trades.json.backup'
$report = Get-Content -Raw -Encoding UTF8 -LiteralPath (Join-Path $task 'build-report.json') | ConvertFrom-Json
$locks = [Collections.Generic.List[IO.FileStream]]::new()
function Hash([string]$File) { return (Get-FileHash -LiteralPath $File -Algorithm SHA256).Hash.ToLowerInvariant() }
function CheckStopped {
    $games = Get-CimInstance Win32_Process | Where-Object { $_.Name -in @('java.exe', 'javaw.exe') -and $_.CommandLine -match 'forgeclient|--gameDir' }
    if ($games) { throw 'Minecraft is running; exit before deployment.' }
}
try {
    foreach ($path in @((Join-Path $project '.ftboss-source-deploy.lock'), (Join-Path $instance '.ftboss-152-deploy.lock'), (Join-Path $instance 'mods\.qys-bosses-install.lock'))) {
        $locks.Add([IO.File]::Open($path, [IO.FileMode]::OpenOrCreate, [IO.FileAccess]::ReadWrite, [IO.FileShare]::None))
    }
    CheckStopped
    if (-not $report.passed) { throw 'Candidate verification incomplete.' }
    if ((Hash $target) -ne $report.baselineSha256 -or (Hash $backup) -ne $report.baselineSha256) { throw 'Installed baseline changed; rebuild before deployment.' }
    if ((Hash $candidate) -ne $report.candidateSha256) { throw 'Candidate checksum mismatch.' }
    if ((Hash $configBackup) -ne $report.configSha256) { throw 'Config backup checksum mismatch.' }
    foreach ($property in $report.sources.PSObject.Properties) {
        if ((Hash (Join-Path $project $property.Name)) -ne $property.Value) { throw "Source changed after verification: $($property.Name)" }
    }
    foreach ($relative in $report.absent) {
        if (Test-Path -LiteralPath (Join-Path $project $relative)) { throw "Retired source restored: $relative" }
    }
    if ((Test-Path -LiteralPath $config) -and (Hash $config) -ne $report.configSha256) { throw 'Legacy config changed after backup.' }
    & powershell.exe -NoProfile -ExecutionPolicy Bypass -File (Join-Path $project 'tools\deploy_jar.ps1') -NoBuild -ArtifactPath $candidate -TargetPath $target -ExpectedTargetHash $report.baselineSha256
    if ($LASTEXITCODE -ne 0) { throw "Shared deployment failed: $LASTEXITCODE" }
    CheckStopped
    if ((Hash $target) -ne $report.candidateSha256) { throw 'Installed checksum mismatch.' }
    if (Test-Path -LiteralPath $config) {
        if ((Hash $config) -ne $report.configSha256) { throw 'Config changed during deployment; retained for safety.' }
        Remove-Item -LiteralPath $config
    }
    [ordered]@{
        installed = $true
        target = $target
        installedSha256 = $report.candidateSha256
        baselineSha256 = $report.baselineSha256
        backup = $backup
        retiredConfig = $config
        configBackup = $configBackup
        gameplayTested = $false
        docsTouched = $false
        deployedAt = [DateTimeOffset]::UtcNow.ToOffset([TimeSpan]::FromHours(8)).ToString('o')
    } | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $task 'deployment-receipt.json') -Encoding utf8
    $message = "pid=$PID`noperation=legacy-statue-retirement-deployed`ninstalled=$($report.candidateSha256)`nbackup=$backup`n"
    $bytes = [Text.Encoding]::UTF8.GetBytes($message)
    foreach ($stream in $locks) { $stream.Position = 0; $stream.SetLength(0); $stream.Write($bytes, 0, $bytes.Length); $stream.Flush() }
    Write-Output "DEPLOYED: $target"
    Write-Output "SHA256: $($report.candidateSha256)"
} finally { foreach ($stream in $locks) { $stream.Dispose() } }
