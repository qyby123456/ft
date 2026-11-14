param()
$ErrorActionPreference = 'Stop'
$project = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\..\..'))
$selection = Get-Content -Raw -LiteralPath (Join-Path $project '_work\armor-resistances-latest.json') | ConvertFrom-Json
$task = [IO.Path]::GetFullPath($selection.task)
$workRoot = [IO.Path]::GetFullPath((Join-Path $project '_work')) + [IO.Path]::DirectorySeparatorChar
if (-not $task.StartsWith($workRoot, [StringComparison]::OrdinalIgnoreCase)) { throw 'Task directory escapes the project workspace.' }
$instance = Join-Path $env:APPDATA '.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2'
$target = Join-Path $instance 'mods\qys-bosses.jar'
$candidate = Join-Path $task 'qys-bosses.jar'
$stage = Join-Path $instance 'mods\.qys-bosses-armor-resistances-rollback.tmp'
$backup = Join-Path $task 'qys-bosses-before-deploy.jar'
$report = Get-Content -Raw -LiteralPath (Join-Path $task 'build-report.json') | ConvertFrom-Json
$locks = [Collections.Generic.List[IO.FileStream]]::new()
function Hash([string] $File) { return (Get-FileHash -LiteralPath $File -Algorithm SHA256).Hash.ToLowerInvariant() }
function CheckStopped {
    $games = Get-CimInstance Win32_Process | Where-Object { $_.Name -in @('java.exe', 'javaw.exe') -and $_.CommandLine -match 'forgeclient|--gameDir' }
    if ($games) { throw 'Minecraft is running. Exit before deployment.' }
}
try {
    foreach ($path in @((Join-Path $project '.ftboss-source-deploy.lock'), (Join-Path $instance '.ftboss-152-deploy.lock'), (Join-Path $instance 'mods\.qys-bosses-install.lock'))) {
        $locks.Add([IO.File]::Open($path, [IO.FileMode]::OpenOrCreate, [IO.FileAccess]::ReadWrite, [IO.FileShare]::None))
    }
    CheckStopped
    if (-not $report.passed) { throw 'Candidate validation incomplete.' }
    if ((Hash $target) -ne $report.baselineSha256) { throw 'Installed baseline changed; rebuild before deployment.' }
    if ((Hash $candidate) -ne $report.candidateSha256) { throw 'Candidate checksum mismatch.' }
    foreach ($property in $report.sources.PSObject.Properties) {
        if ((Hash $property.Name) -ne $property.Value) { throw "Source changed after validation: $($property.Name)" }
    }
    if (Test-Path -LiteralPath $backup) {
        if ((Hash $backup) -ne $report.baselineSha256) { throw 'Unexpected backup checksum.' }
    } else { Copy-Item -LiteralPath $target -Destination $backup }
    if ((Hash $backup) -ne $report.baselineSha256) { throw 'Backup verification failed.' }
    CheckStopped
    if ((Hash $target) -ne $report.baselineSha256) { throw 'Concurrent deployment detected.' }
    & powershell.exe -NoProfile -ExecutionPolicy Bypass -File (Join-Path $project 'tools\deploy_jar.ps1') -NoBuild -ArtifactPath $candidate -TargetPath $target -ExpectedTargetHash $report.baselineSha256
    if ($LASTEXITCODE -ne 0) { throw "Shared deployment failed: $LASTEXITCODE" }
    if ((Hash $target) -ne $report.candidateSha256) {
        Copy-Item -LiteralPath $backup -Destination $stage
        [IO.File]::Replace($stage, $target, [NullString]::Value)
        throw 'Installed checksum mismatch; original restored.'
    }
    [ordered]@{ installed = $true; target = $target; backup = $backup; installedSha256 = $report.candidateSha256
        baselineSha256 = $report.baselineSha256; gameplayTested = $false; docsTouched = $false
        deployedAt = [DateTimeOffset]::UtcNow.ToOffset([TimeSpan]::FromHours(8)).ToString('o') } |
        ConvertTo-Json | Set-Content -LiteralPath (Join-Path $task 'deployment-receipt.json') -Encoding utf8
    $message = "pid=$PID`noperation=armor-resistances-deployed`ninstalled=$($report.candidateSha256)`nbackup=$backup`n"
    $bytes = [Text.Encoding]::UTF8.GetBytes($message)
    foreach ($stream in $locks) { $stream.Position = 0; $stream.SetLength(0); $stream.Write($bytes, 0, $bytes.Length); $stream.Flush() }
    Write-Output "DEPLOYED: $target"
    Write-Output "SHA256: $($report.candidateSha256)"
} finally { foreach ($stream in $locks) { $stream.Dispose() } }
