param([switch]$VerifyOnly)
$ErrorActionPreference = 'Stop'
$project = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$task = Join-Path $project '_work\xuanlan-zhuni-pair-20261027'
$report = Get-Content -LiteralPath (Join-Path $task 'build-report.json') -Raw -Encoding UTF8 | ConvertFrom-Json
$instanceName = 'FarmingTales_Forge' + [char]0x2161 + ' v1.5.2'
$instance = [IO.Path]::GetFullPath((Join-Path $env:APPDATA ('.minecraft\versions\' + $instanceName)))
$target = Join-Path $instance 'mods\qys-bosses.jar'
function Hash([string]$file) { return (Get-FileHash -LiteralPath $file -Algorithm SHA256).Hash.ToLowerInvariant() }
function Assert-Hashes {
    if (-not $report.passed) { throw 'Build and all verification gates must pass.' }
    if ((Hash $report.candidate) -ne $report.candidateSha256) { throw 'Candidate changed after verification.' }
    if ((Hash $report.baseline) -ne $report.baselineSha256) { throw 'Build baseline changed.' }
    if ((Hash $target) -ne $report.baselineSha256) { throw 'Installed JAR changed; rebuild against current runtime.' }
    foreach ($property in $report.sourceHashes.PSObject.Properties) {
        if ((Hash $property.Name) -ne $property.Value) { throw "Source changed after checks: $($property.Name)" }
    }
    foreach ($property in $report.sourceHooks.PSObject.Properties) {
        $source = Get-Content -LiteralPath $property.Name -Raw -Encoding UTF8
        foreach ($hook in $property.Value) {
            if (-not $source.Contains([string]$hook)) { throw "Mirrored source hook changed: $($property.Name): $hook" }
        }
    }
}
function Assert-NoGame {
    $game = @(Get-CimInstance Win32_Process | Where-Object {
        $_.Name -match '^(Minecraft|Minecraft\.Windows)\.exe$' -or
        $_.Name -match '^javaw?\.exe$' -and (-not $_.CommandLine -or $_.CommandLine -match 'forgeclient|--gameDir|net\.minecraft\.client\.main|--launchTarget')
    })
    if ($game) { throw "Minecraft running or Java identity unknown: $($game.ProcessId -join ','). Candidate retained; no deployment." }
}
Assert-Hashes
if ($VerifyOnly) {
    Write-Output 'PASS candidate, baseline, current installation and source hashes match. No deployment performed.'
    return
}
$mutex = [Threading.Mutex]::new($false, 'Local\FTBossForge152Deployment')
$owned = $false
$locks = @()
try {
    Assert-NoGame
    try { $owned = $mutex.WaitOne(0) } catch [Threading.AbandonedMutexException] { $owned = $true }
    if (-not $owned) { throw 'Shared deployment mutex busy.' }
    foreach ($file in @((Join-Path $instance '.ftboss-152-deploy.lock'), (Join-Path $instance 'mods\.qys-bosses-install.lock'))) {
        $locks += [IO.File]::Open($file, [IO.FileMode]::OpenOrCreate, [IO.FileAccess]::ReadWrite, [IO.FileShare]::None)
    }
    Assert-Hashes
    $backup = Join-Path $task 'qys-bosses-before-deploy.jar'
    if (Test-Path -LiteralPath $backup) { throw 'Deployment backup already exists; inspect previous receipt.' }
    Copy-Item -LiteralPath $target -Destination $backup
    if ((Hash $backup) -ne $report.baselineSha256) { throw 'Fresh backup mismatch.' }
    Assert-NoGame
    & powershell.exe -NoProfile -ExecutionPolicy Bypass -File (Join-Path $project 'tools\deploy_jar.ps1') `
        -NoBuild -ArtifactPath $report.candidate -TargetPath $target -ExpectedTargetHash $report.baselineSha256
    if ($LASTEXITCODE -ne 0) { throw "Guarded deployment failed: $LASTEXITCODE" }
    if ((Hash $target) -ne $report.candidateSha256) { throw 'Installed hash mismatch.' }
    $receipt = [ordered]@{ installed = $true; target = $target; backup = $backup;
        baselineSha256 = $report.baselineSha256; installedSha256 = (Hash $target);
        gameTested = $false; neoForgeTouched = $false; desktopTouched = $false; docsTouched = $false }
    [IO.File]::WriteAllText((Join-Path $task 'deployment-receipt.json'), ($receipt | ConvertTo-Json -Depth 8) + "`n", [Text.UTF8Encoding]::new($false))
    Write-Output 'PASS current Forge 1.5.2 installed and read-back hash verified. Other instances unchanged.'
} finally {
    foreach ($stream in $locks) { $stream.Dispose() }
    if ($owned) { $mutex.ReleaseMutex() }
    $mutex.Dispose()
}
