param()
$ErrorActionPreference = 'Stop'
$project = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\..\..'))
$task = Join-Path $project '_work\weapon-hover-cache-20261025'
$instance = Join-Path $env:APPDATA '.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2'
$target = Join-Path $instance 'mods\qys-bosses.jar'
$candidate = Join-Path $task 'qys-bosses.jar'
$stage = Join-Path $instance 'mods\.qys-bosses-weapon-tooltip-scope.tmp'
$report = Get-Content -Raw -LiteralPath (Join-Path $task 'build-report.json') | ConvertFrom-Json
$backup = Join-Path $task ('qys-bosses-before-' + $report.baselineSha256.Substring(0, 12) + '.jar')
$locks = [Collections.Generic.List[IO.FileStream]]::new()
$mutex = [Threading.Mutex]::new($false, 'Local\FTBossForge152Deployment')
$owned = $false
function Hash([string] $File) {
    return (Get-FileHash -LiteralPath $File -Algorithm SHA256).Hash.ToLowerInvariant()
}
try {
    if (Get-Process java,javaw -ErrorAction SilentlyContinue) { throw 'Minecraft/Java is still running; no files were replaced.' }
    try { $owned = $mutex.WaitOne(0) } catch [Threading.AbandonedMutexException] { $owned = $true }
    if (-not $owned) { throw 'Another deployment holds the shared instance mutex.' }
    foreach ($path in @((Join-Path $project '.ftboss-source-deploy.lock'),
            (Join-Path $instance '.ftboss-152-deploy.lock'), (Join-Path $instance 'mods\.qys-bosses-install.lock'))) {
        $locks.Add([IO.File]::Open($path, [IO.FileMode]::OpenOrCreate, [IO.FileAccess]::ReadWrite, [IO.FileShare]::None))
    }
    if (-not $report.passed) { throw 'Candidate validation is incomplete.' }
    if ((Hash $target) -ne $report.baselineSha256) { throw 'Runtime JAR changed. Rebuild on the latest runtime before deployment.' }
    if ((Hash $candidate) -ne $report.candidateSha256) { throw 'Candidate differs from verified artifact.' }
    foreach ($property in $report.sources.PSObject.Properties) {
        if ((Hash $property.Name) -ne $property.Value) { throw "Source changed since verification: $($property.Name)" }
    }
    if (Test-Path -LiteralPath $backup) {
        if ((Hash $backup) -ne $report.baselineSha256) { throw 'Existing backup does not match baseline.' }
    } else { Copy-Item -LiteralPath $target -Destination $backup }
    if ((Hash $backup) -ne $report.baselineSha256) { throw 'Backup verification failed.' }
    if (Test-Path -LiteralPath $stage) {
        if ((Hash $stage) -ne $report.candidateSha256) { throw 'Staging file differs from verified candidate.' }
    } else { Copy-Item -LiteralPath $candidate -Destination $stage }
    if ((Hash $stage) -ne $report.candidateSha256) { throw 'Staged artifact hash mismatch.' }
    if (Get-Process java,javaw -ErrorAction SilentlyContinue) { throw 'Java started before replacement; deployment cancelled.' }
    if ((Hash $target) -ne $report.baselineSha256) { throw 'Concurrent runtime replacement detected.' }
    [IO.File]::Replace($stage, $target, [NullString]::Value)
    if ((Hash $target) -ne $report.candidateSha256) { throw 'Installed artifact hash mismatch.' }
    $receipt = [ordered]@{
        installed = $true; target = $target; backup = $backup
        baselineSha256 = $report.baselineSha256; installedSha256 = $report.candidateSha256
        gameplayTested = $false; docsTouched = $false
        deployedAt = [DateTimeOffset]::UtcNow.ToOffset([TimeSpan]::FromHours(8)).ToString('o')
    }
    $receipt | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $task 'deployment-receipt.json') -Encoding utf8NoBOM
    $message = "pid=$PID`noperation=weapon-tooltip-scope-deployed`ninstalled=$($report.candidateSha256)`nbackup=$backup`n"
    $bytes = [Text.Encoding]::UTF8.GetBytes($message)
    foreach ($stream in $locks) {
        $stream.Position = 0
        $stream.SetLength(0)
        $stream.Write($bytes, 0, $bytes.Length)
        $stream.Flush()
    }
    Write-Output "DEPLOYED AND VERIFIED: $($report.candidateSha256)"
} finally {
    foreach ($stream in $locks) { $stream.Dispose() }
    if ($owned) { $mutex.ReleaseMutex() }
    $mutex.Dispose()
}
