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
if (-not $report.passed) { throw 'Build verification must pass before deployment.' }
if ([IO.Path]::GetFullPath($report.candidate) -ne (Join-Path $task 'qys-bosses.jar') -or
        [IO.Path]::GetFullPath($report.baseline) -ne (Join-Path $task 'baseline.jar')) { throw 'Unexpected artifact paths.' }
function Hash([string]$file) { return (Get-FileHash -LiteralPath $file -Algorithm SHA256).Hash.ToLowerInvariant() }
function Assert-NoGame {
    $game = @(Get-CimInstance Win32_Process | Where-Object {
        $_.Name -match '^javaw?\.exe$' -and (-not $_.CommandLine -or
            $_.CommandLine -match 'forgeclient|--gameDir|net\.minecraft\.client\.main|--launchTarget')
    })
    if ($game) { throw "Minecraft running or Java identity unknown: $($game.ProcessId -join ','). Candidate retained." }
}
function Verify-Candidate([string]$file) {
    $java = Join-Path $env:APPDATA '.minecraft\runtime\java-runtime-gamma-snapshot\bin\java.exe'
    & $java -Xverify:all -cp $report.verificationClasspath XuanlanPlayerFlywheelPatch verify `
        $report.baseline $file $report.classes $report.mappings
    if ($LASTEXITCODE -ne 0) { throw 'Runtime delta verification failed.' }
}
$mutex = [Threading.Mutex]::new($false, 'Local\FTBossForge152Deployment')
$owned = $false
$locks = @()
try {
    Assert-NoGame
    try { $owned = $mutex.WaitOne(0) } catch [Threading.AbandonedMutexException] { $owned = $true }
    if (-not $owned) { throw 'Shared deployment mutex busy.' }
    foreach ($file in @((Join-Path $instance '.ftboss-152-deploy.lock'),
            (Join-Path $instance 'mods\.qys-bosses-install.lock'), (Join-Path $project '.ftboss-source-deploy.lock'))) {
        $locks += [IO.File]::Open($file, [IO.FileMode]::OpenOrCreate, [IO.FileAccess]::ReadWrite, [IO.FileShare]::None)
    }
    if ((Hash $target) -ne $report.baselineSha256) { throw 'Installed baseline changed; rebuild from current JAR.' }
    if ((Hash $report.baseline) -ne $report.baselineSha256) { throw 'Baseline snapshot changed.' }
    if ((Hash $report.candidate) -ne $report.candidateSha256) { throw 'Candidate changed after verification.' }
    foreach ($property in $report.sources.PSObject.Properties) {
        if ((Hash $property.Name) -ne $property.Value) { throw "Source changed after verification: $($property.Name)" }
    }
    Verify-Candidate $report.candidate
    $backup = Join-Path $task 'qys-bosses-before-deploy.jar'
    if (Test-Path -LiteralPath $backup) { throw 'Backup exists; do not replay a completed deployment.' }
    Copy-Item -LiteralPath $target -Destination $backup
    if ((Hash $backup) -ne $report.baselineSha256) { throw 'Backup checksum mismatch.' }
    Assert-NoGame
    & powershell.exe -NoProfile -ExecutionPolicy Bypass -File (Join-Path $project 'tools\deploy_jar.ps1') `
        -NoBuild -ArtifactPath $report.candidate -TargetPath $target -ExpectedTargetHash $report.baselineSha256
    if ($LASTEXITCODE -ne 0) { throw "Guarded deployment failed: $LASTEXITCODE" }
    if ((Hash $target) -ne $report.candidateSha256) { throw 'Installed checksum mismatch.' }
    Verify-Candidate $target
    $receipt = [ordered]@{ installed = $true; target = $target; backup = $backup;
        installedSha256 = (Hash $target); baselineSha256 = $report.baselineSha256; balance = $report.balance;
        gameplayTested = $false; savesTouched = $false; neoForgeTouched = $false; docsTouched = $false;
        docsProposal = $report.docsProposal }
    [IO.File]::WriteAllText((Join-Path $task 'deployment-receipt.json'),
        ($receipt | ConvertTo-Json -Depth 8) + "`n", [Text.UTF8Encoding]::new($false))
    foreach ($stream in $locks) {
        $bytes = [Text.Encoding]::UTF8.GetBytes("pid=$PID`noperation=xuanlan-player-flywheel-balance`ninstalled=$($receipt.installedSha256)`nbackup=$backup`n")
        $stream.SetLength(0)
        $stream.Write($bytes, 0, $bytes.Length)
        $stream.Flush($true)
    }
    Write-Output 'PASS: player flywheel balance deployed to the current Forge 1.5.2 instance only.'
} finally {
    foreach ($stream in $locks) { $stream.Dispose() }
    if ($owned) { $mutex.ReleaseMutex() }
    $mutex.Dispose()
}
