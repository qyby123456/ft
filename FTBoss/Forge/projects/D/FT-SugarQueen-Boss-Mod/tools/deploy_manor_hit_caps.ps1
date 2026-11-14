param([Parameter(Mandatory = $true)][string]$TaskPath)
$ErrorActionPreference = 'Stop'
$project = [IO.Path]::GetFullPath((Split-Path -Parent $PSScriptRoot))
$task = [IO.Path]::GetFullPath($TaskPath)
if (-not $task.StartsWith((Join-Path $project '_work') + '\', [StringComparison]::OrdinalIgnoreCase)) {
    throw 'Task must remain inside project _work.'
}
$instance = Join-Path $env:APPDATA ('.minecraft\versions\FarmingTales_Forge' + [char]0x2161 + ' v1.5.2')
$target = Join-Path $instance 'mods\qys-bosses.jar'
$report = Get-Content -LiteralPath (Join-Path $task 'build-report.json') -Raw -Encoding UTF8 | ConvertFrom-Json
if (-not $report.passed) { throw 'Incremental build verification must pass before deployment.' }
function Hash([string]$Path) { return (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant() }
function Assert-NoGame {
    $game = @(Get-CimInstance Win32_Process | Where-Object {
        $_.Name -match '^javaw?\.exe$' -and (-not $_.CommandLine -or
            $_.CommandLine -match 'forgeclient|--gameDir|net\.minecraft\.client\.main|--launchTarget')
    })
    if ($game) { throw "Minecraft is running or Java identity unknown: $($game.ProcessId -join ','). Candidate retained." }
}
function Run-Checked([string]$Executable, [string[]]$Arguments) {
    $preference = $ErrorActionPreference
    try {
        $ErrorActionPreference = 'Continue'
        & $Executable @Arguments
        $code = $LASTEXITCODE
    } finally { $ErrorActionPreference = $preference }
    if ($code -ne 0) { throw "Deployment command failed: $Executable ($code)" }
}
$mutex = [Threading.Mutex]::new($false, 'Local\FTBossForge152Deployment')
$owned = $false
$locks = @()
$installed = $false
$backup = Join-Path $task 'qys-bosses-before-deploy.jar'
try {
    Assert-NoGame
    try { $owned = $mutex.WaitOne(0) } catch [Threading.AbandonedMutexException] { $owned = $true }
    if (-not $owned) { throw 'Shared deployment mutex is busy.' }
    foreach ($file in @((Join-Path $instance '.ftboss-152-deploy.lock'),
            (Join-Path $instance 'mods\.qys-bosses-install.lock'), (Join-Path $project '.ftboss-source-deploy.lock'))) {
        $locks += [IO.File]::Open($file, [IO.FileMode]::OpenOrCreate, [IO.FileAccess]::ReadWrite, [IO.FileShare]::None)
    }
    if ((Hash $target) -ne $report.baselineSha256 -or (Hash $report.baseline) -ne $report.baselineSha256) {
        throw 'Installed baseline changed; rebuild from the latest JAR.'
    }
    if ((Hash $report.candidate) -ne $report.candidateSha256) { throw 'Candidate changed after verification.' }
    foreach ($property in $report.sources.PSObject.Properties) {
        if ((Hash $property.Name) -ne $property.Value) { throw "Source changed after verification: $($property.Name)" }
    }
    if (Test-Path -LiteralPath $backup) { throw 'Backup exists; deployment must not be replayed.' }
    Copy-Item -LiteralPath $target -Destination $backup
    if ((Hash $backup) -ne $report.baselineSha256) { throw 'Backup checksum mismatch.' }
    Assert-NoGame
    Run-Checked 'powershell.exe' @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', (Join-Path $PSScriptRoot 'deploy_jar.ps1'),
        '-NoBuild', '-ArtifactPath', $report.candidate, '-TargetPath', $target, '-ExpectedTargetHash', $report.baselineSha256)
    $installed = $true
    if ((Hash $target) -ne $report.candidateSha256) { throw 'Installed checksum mismatch.' }
    $toolClasspath = (@($target, (Join-Path $task 'tools')) + @($report.asm)) -join ';'
    Run-Checked (Join-Path $report.jdk 'java.exe') @('-Dfile.encoding=UTF-8', '-Xverify:all', '-cp', $toolClasspath,
        'ManorHitCapRuntimeTools', 'audit', $target)
    Run-Checked (Join-Path $report.jdk 'java.exe') @('-Dfile.encoding=UTF-8', '-Xverify:all', '-cp',
        (@($target, $report.runtimeDependencies) -join ';'), 'ManorHitCapRuntimeTools', 'link')
    Run-Checked (Join-Path $report.jdk 'java.exe') @('-Dfile.encoding=UTF-8', '-cp', $toolClasspath, 'ZipStreamCheck', $target)
    $zone = [TimeZoneInfo]::FindSystemTimeZoneById('China Standard Time')
    $receipt = [ordered]@{ installed = $true; deployedAt = [TimeZoneInfo]::ConvertTime([DateTimeOffset]::UtcNow, $zone).ToString('O');
        target = $target; backup = $backup; baselineSha256 = $report.baselineSha256; installedSha256 = (Hash $target);
        normalFinalHitCap = 100; sculkSpecialFinalHitCap = 25; rolling20TickBudget = 400;
        changedEntries = $report.changedEntries; addedEntries = $report.addedEntries; preservedEntries = $report.preservedEntries;
        gameplayTested = $false; neoForgeTouched = $false; fix2Touched = $false; desktopTouched = $false; docsTouched = $false }
    [IO.File]::WriteAllText((Join-Path $task 'deployment-receipt.json'),
        ($receipt | ConvertTo-Json -Depth 8) + "`n", [Text.UTF8Encoding]::new($false))
    foreach ($stream in $locks) {
        $bytes = [Text.Encoding]::UTF8.GetBytes("pid=$PID`noperation=manor-hit-caps-deployed`ninstalled=$($receipt.installedSha256)`nbackup=$backup`n")
        $stream.SetLength(0)
        $stream.Write($bytes, 0, $bytes.Length)
        $stream.Flush($true)
    }
    Write-Output "PASS deployed 100 / 25 hit caps to current Forge 1.5.2 only: $($receipt.installedSha256)"
} catch {
    if ($installed -and (Hash $target) -eq $report.candidateSha256 -and (Hash $backup) -eq $report.baselineSha256) {
        Assert-NoGame
        Run-Checked 'powershell.exe' @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', (Join-Path $PSScriptRoot 'deploy_jar.ps1'),
            '-NoBuild', '-ArtifactPath', $backup, '-TargetPath', $target, '-ExpectedTargetHash', $report.candidateSha256)
        Write-Warning 'Post-install verification failed; the prior baseline was restored.'
    }
    throw
} finally {
    foreach ($stream in $locks) { $stream.Dispose() }
    if ($owned) { $mutex.ReleaseMutex() }
    $mutex.Dispose()
}
