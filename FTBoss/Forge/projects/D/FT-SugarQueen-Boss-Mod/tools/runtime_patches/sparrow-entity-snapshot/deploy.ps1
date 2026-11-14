$ErrorActionPreference = 'Stop'
$project = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\..\..'))
$selection = Get-Content -LiteralPath (Join-Path $project '_work\sparrow-entity-snapshot-latest.json') -Raw -Encoding utf8 | ConvertFrom-Json
$task = [IO.Path]::GetFullPath($selection.task)
if (-not $task.StartsWith((Join-Path $project '_work\'), [StringComparison]::OrdinalIgnoreCase)) { throw 'Task escapes workspace.' }
$report = Get-Content -LiteralPath (Join-Path $task 'build-report.json') -Raw -Encoding utf8 | ConvertFrom-Json
$instanceName = 'FarmingTales_Forge' + [char]0x2161 + ' v1.5.2'
$instance = Join-Path $env:APPDATA ('.minecraft\versions\' + $instanceName)
$target = Join-Path $instance 'mods\qys-bosses.jar'
$candidate = Join-Path $task 'qys-bosses.jar'
$backup = Join-Path $task 'qys-bosses-before-deploy.jar'
$stage = $target + '.sparrow-snapshot.tmp'
$locks = New-Object 'System.Collections.Generic.List[System.IO.FileStream]'
function Hash([string]$Path) { return (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant() }
function CheckStopped {
    if (Get-Process -Name java,javaw -ErrorAction SilentlyContinue) { throw 'Exit all Java processes before deployment.' }
}
try {
    foreach ($path in @((Join-Path $project '.ftboss-source-deploy.lock'),
            (Join-Path $instance '.ftboss-152-deploy.lock'), (Join-Path $instance 'mods\.qys-bosses-install.lock'))) {
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
    Copy-Item -LiteralPath $candidate -Destination $stage -Force
    if ((Hash $stage) -ne $report.candidateSha256) { throw 'Staged candidate checksum mismatch.' }
    CheckStopped
    if ((Hash $target) -ne $report.baselineSha256) { throw 'Concurrent deployment detected.' }
    [IO.File]::Replace($stage, $target, [System.Management.Automation.Language.NullString]::Value)
    if ((Hash $target) -ne $report.candidateSha256) {
        Copy-Item -LiteralPath $backup -Destination $stage -Force
        [IO.File]::Replace($stage, $target, [System.Management.Automation.Language.NullString]::Value)
        throw 'Installed checksum mismatch; original restored.'
    }
    [ordered]@{ installed = $true; target = $target; backup = $backup; installedSha256 = $report.candidateSha256
        baselineSha256 = $report.baselineSha256; gameplayTested = $false; docsTouched = $false; docsImpact = 'none'
        deployedAt = [DateTimeOffset]::UtcNow.ToOffset([TimeSpan]::FromHours(8)).ToString('o') } |
        ConvertTo-Json | Set-Content -LiteralPath (Join-Path $task 'deployment-receipt.json') -Encoding utf8
    $message = "pid=$PID`noperation=sparrow-entity-snapshot-deployed`ninstalled=$($report.candidateSha256)`nbackup=$backup`n"
    $bytes = [Text.Encoding]::UTF8.GetBytes($message)
    foreach ($stream in $locks) { $stream.Position = 0; $stream.SetLength(0); $stream.Write($bytes, 0, $bytes.Length); $stream.Flush() }
    Write-Output "DEPLOYED: $target"
    Write-Output "BACKUP: $backup"
    Write-Output "SHA256: $($report.candidateSha256)"
} finally { foreach ($stream in $locks) { $stream.Dispose() } }
