param()
$ErrorActionPreference = 'Stop'
$project = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\..\..'))
$selection = Get-Content -Raw -LiteralPath (Join-Path $project '_work\armor-blessings-latest.json') | ConvertFrom-Json
$task = [IO.Path]::GetFullPath($selection.task)
$workRoot = [IO.Path]::GetFullPath((Join-Path $project '_work')) + [IO.Path]::DirectorySeparatorChar
if (-not $task.StartsWith($workRoot, [StringComparison]::OrdinalIgnoreCase)) { throw 'Task directory escapes the project workspace.' }
$instance = Join-Path $env:APPDATA '.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2'
$target = Join-Path $instance 'mods\qys-bosses.jar'
$candidate = Join-Path $task 'qys-bosses.jar'
$stage = Join-Path $instance 'mods\.qys-bosses-armor-blessings.tmp'
$backup = Join-Path $task 'qys-bosses-before-deploy.jar'
$report = Get-Content -Raw -LiteralPath (Join-Path $task 'build-report.json') | ConvertFrom-Json
$locks = [Collections.Generic.List[IO.FileStream]]::new()
function Hash([string] $File) { return (Get-FileHash -LiteralPath $File -Algorithm SHA256).Hash.ToLowerInvariant() }
function CheckStopped {
    if (Get-Process java,javaw -ErrorAction SilentlyContinue) { throw 'Java is running. Exit Minecraft before deployment.' }
}
try {
    foreach ($path in @((Join-Path $project '.ftboss-source-deploy.lock'), (Join-Path $instance '.ftboss-152-deploy.lock'))) {
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
    if (Test-Path -LiteralPath $stage) {
        if ((Hash $stage) -ne $report.candidateSha256) { throw 'Unexpected staging checksum.' }
    } else { Copy-Item -LiteralPath $candidate -Destination $stage }
    if ((Hash $backup) -ne $report.baselineSha256 -or (Hash $stage) -ne $report.candidateSha256) { throw 'Backup/staging verification failed.' }
    CheckStopped
    if ((Hash $target) -ne $report.baselineSha256) { throw 'Concurrent deployment detected.' }
    [IO.File]::Replace($stage, $target, [NullString]::Value)
    if ((Hash $target) -ne $report.candidateSha256) { throw 'Installed checksum mismatch.' }
    $receipt = [ordered]@{ installed = $true; target = $target; backup = $backup; installedSha256 = $report.candidateSha256
        baselineSha256 = $report.baselineSha256; armorBlessings = 100; weaponBlessings = 100; statues = 10
        armorBlessingsPerStatue = 10; gameplayTested = $false; docsTouched = $false
        deployedAt = [DateTimeOffset]::UtcNow.ToOffset([TimeSpan]::FromHours(8)).ToString('o') }
    $receipt | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $task 'deployment-receipt.json') -Encoding utf8
    $message = "pid=$PID`noperation=armor-blessings-deployed`ninstalled=$($report.candidateSha256)`nbackup=$backup`n"
    $bytes = [Text.Encoding]::UTF8.GetBytes($message)
    foreach ($stream in $locks) { $stream.Position = 0; $stream.SetLength(0); $stream.Write($bytes, 0, $bytes.Length); $stream.Flush() }
    Write-Output "DEPLOYED: $target"
    Write-Output "SHA256: $($report.candidateSha256)"
} finally { foreach ($stream in $locks) { $stream.Dispose() } }
