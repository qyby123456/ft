param(
    [Parameter(Mandatory = $true)][string]$TaskPath,
    [switch]$VerifyOnly
)
$ErrorActionPreference = 'Stop'
$project = [IO.Path]::GetFullPath((Split-Path -Parent $PSScriptRoot))
$task = [IO.Path]::GetFullPath($TaskPath)
if (-not $task.StartsWith((Join-Path $project '_work') + '\', [StringComparison]::OrdinalIgnoreCase)) {
    throw 'Task must stay inside project _work.'
}
$report = Get-Content -LiteralPath (Join-Path $task 'build-report.json') -Raw -Encoding UTF8 | ConvertFrom-Json
$instance = Join-Path $env:APPDATA ('.minecraft\versions\FarmingTales_Forge' + [char]0x2161 + ' v1.5.2')
$target = Join-Path $instance 'mods\qys-bosses.jar'
function Hash([string]$file) { (Get-FileHash -LiteralPath $file -Algorithm SHA256).Hash.ToLowerInvariant() }
function Assert-Hashes {
    if (-not $report.passed -or (Hash $report.candidate) -ne $report.candidateSha256 -or
            (Hash $report.baseline) -ne $report.baselineSha256 -or (Hash $target) -ne $report.baselineSha256) {
        throw 'Verification, candidate, baseline or installed hash mismatch; rebuild against the current JAR.'
    }
    foreach ($property in $report.sourceHashes.PSObject.Properties) {
        if ((Hash $property.Name) -ne $property.Value) { throw "Source changed: $($property.Name)" }
    }
}
function Assert-NoGame {
    $game = @(Get-CimInstance Win32_Process | Where-Object {
        $_.Name -match '^(Minecraft|Minecraft\.Windows)\.exe$' -or
        $_.Name -match '^javaw?\.exe$' -and (-not $_.CommandLine -or
            $_.CommandLine -match 'forgeclient|--gameDir|net\.minecraft\.client\.main|--launchTarget|@')
    })
    if ($game) { throw "Minecraft running or Java identity unknown: $($game.ProcessId -join ','). Candidate retained." }
}
Assert-Hashes
if ($VerifyOnly) {
    Write-Output 'PASS fixed-stats candidate, baseline, current installation and source hashes. No deployment.'
    return
}
if (Test-Path -LiteralPath (Join-Path $task 'deployment-receipt.json')) { throw 'Deployment already recorded; preserve the backup.' }
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
    Assert-Hashes
    $backup = Join-Path $task 'qys-bosses-before-deploy.jar'
    if (-not (Test-Path -LiteralPath $backup)) { Copy-Item -LiteralPath $target -Destination $backup }
    if ((Hash $backup) -ne $report.baselineSha256) { throw 'Backup hash mismatch.' }
    Assert-NoGame
    & powershell.exe -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot 'deploy_jar.ps1') `
        -NoBuild -ArtifactPath $report.candidate -TargetPath $target -ExpectedTargetHash $report.baselineSha256
    if ($LASTEXITCODE -ne 0) { throw "Guarded deployment failed: $LASTEXITCODE" }
    if ((Hash $target) -ne $report.candidateSha256) { throw 'Installed hash mismatch.' }
    $receipt = [ordered]@{ installed = $true; target = $target; backup = $backup;
        baselineSha256 = $report.baselineSha256; installedSha256 = Hash $target;
        healthPerPartner = 2000; birdArmor = 55; gameTested = $false;
        neoForgeTouched = $false; desktopTouched = $false; docsTouched = $false }
    [IO.File]::WriteAllText((Join-Path $task 'deployment-receipt.json'),
        ($receipt | ConvertTo-Json -Depth 8) + "`n", [Text.UTF8Encoding]::new($false))
    Write-Output 'PASS fixed 2000/2000 health and 55 bird armor installed to current Forge 1.5.2 only.'
} finally {
    foreach ($stream in $locks) { $stream.Dispose() }
    if ($owned) { $mutex.ReleaseMutex() }
    $mutex.Dispose()
}
