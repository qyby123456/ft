param([Parameter(Mandatory)][string]$TaskPath)
$ErrorActionPreference = 'Stop'
$project = [IO.Path]::GetFullPath((Split-Path -Parent $PSScriptRoot))
$task = [IO.Path]::GetFullPath($TaskPath)
if (-not $task.StartsWith((Join-Path $project '_work') + '\', [StringComparison]::OrdinalIgnoreCase)) {
    throw 'Task must remain inside project _work.'
}
$instanceName = 'FarmingTales_Forge' + [char]0x2161 + ' v1.5.2'
$instance = Join-Path $env:APPDATA ('.minecraft\versions\' + $instanceName)
$target = Join-Path $instance 'mods\qys-bosses.jar'
$report = Get-Content -LiteralPath (Join-Path $task 'build-report.json') -Raw -Encoding UTF8 | ConvertFrom-Json
$verification = Get-Content -LiteralPath (Join-Path $task 'verification.json') -Raw -Encoding UTF8 | ConvertFrom-Json
if (-not $report.passed -or -not $verification.passed) { throw 'Validation must pass before deployment.' }
function No-Game {
    $processes = @(Get-CimInstance Win32_Process | Where-Object {
        $_.Name -match '^(Minecraft|Minecraft\.Windows)\.exe$' -or
        $_.Name -match '^javaw?\.exe$' -and (-not $_.CommandLine -or
        $_.CommandLine -match 'forgeclient|--gameDir|net\.minecraft\.client\.main|--launchTarget')
    })
    if ($processes) { throw "Minecraft running or Java identity unknown: $($processes.ProcessId -join ','). Candidate retained." }
}
function Hash([string]$file) { return (Get-FileHash -LiteralPath $file -Algorithm SHA256).Hash.ToLowerInvariant() }
$keyBytes = [Text.Encoding]::UTF8.GetBytes($target.ToLowerInvariant())
$sha = [Security.Cryptography.SHA256]::Create()
try { $key = ([BitConverter]::ToString($sha.ComputeHash($keyBytes))).Replace('-', '') } finally { $sha.Dispose() }
$mutex = [Threading.Mutex]::new($false, 'Local\FTBossForge152Deployment')
$scoped = [Threading.Mutex]::new($false, 'Local\FTBoss152-JarDeploy-' + $key)
$owned = $false
$scopedOwned = $false
$locks = @()
try {
    No-Game
    try { $owned = $mutex.WaitOne(0) } catch [Threading.AbandonedMutexException] { $owned = $true }
    if (-not $owned) { throw 'Shared deployment mutex busy.' }
    try { $scopedOwned = $scoped.WaitOne(0) } catch [Threading.AbandonedMutexException] { $scopedOwned = $true }
    if (-not $scopedOwned) { throw 'Target deployment mutex busy.' }
    foreach ($file in @((Join-Path $instance '.ftboss-152-deploy.lock'),
            (Join-Path $instance 'mods\.qys-bosses-install.lock'))) {
        $locks += [IO.File]::Open($file, [IO.FileMode]::OpenOrCreate, [IO.FileAccess]::ReadWrite, [IO.FileShare]::None)
    }
    No-Game
    if ((Hash $target) -ne $report.baselineSha256) { throw 'Installed baseline changed; rebuild from current JAR.' }
    if ((Hash $report.candidate) -ne $report.candidateSha256) { throw 'Candidate changed after checks.' }
    foreach ($property in $report.sourceHashes.PSObject.Properties) {
        if ((Hash $property.Name) -ne $property.Value) { throw "Source changed after checks: $($property.Name)" }
    }
    $backup = Join-Path $task 'qys-bosses-before-deploy.jar'
    if (Test-Path -LiteralPath $backup) { throw 'Deployment backup already exists; do not reuse a completed stage.' }
    Copy-Item -LiteralPath $target -Destination $backup
    if ((Hash $backup) -ne $report.baselineSha256) { throw 'Fresh baseline backup mismatch.' }
    No-Game
    $shell = (Get-Command powershell.exe).Source
    & $shell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $project 'tools\deploy_jar.ps1') `
        -NoBuild -ArtifactPath $report.candidate -TargetPath $target -ExpectedTargetHash $report.baselineSha256
    if ($LASTEXITCODE -ne 0) { throw "Guarded deployment failed: $LASTEXITCODE" }
    if ((Hash $target) -ne $report.candidateSha256) { throw 'Installed hash mismatch.' }
    $jdk = Join-Path $env:APPDATA '.minecraft\runtime\java-runtime-gamma-snapshot\bin\java.exe'
    $classpath = $target + ';' + (Join-Path $task 'classes') + ';' + (Join-Path $task 'runtime-dependencies.jar')
    & $jdk -Xverify:all -cp $classpath AppleBlastLightCheck $backup $target (Join-Path $task 'installed-verification.json')
    if ($LASTEXITCODE -ne 0) { throw 'Installed bytecode regression failed.' }
    $receipt = [ordered]@{ installed = $true; target = $target; backup = $backup;
        baselineSha256 = $report.baselineSha256; installedSha256 = (Hash $target);
        gameTested = $false; neoForgeTouched = $false; changedMethods = 5; addedClasses = 5 }
    [IO.File]::WriteAllText((Join-Path $task 'deployment-receipt.json'),
        ($receipt | ConvertTo-Json -Depth 8) + "`n", [Text.UTF8Encoding]::new($false))
    foreach ($stream in $locks) {
        $bytes = [Text.Encoding]::UTF8.GetBytes("pid=$PID`noperation=apple-blast-light-deployed`ninstalled=$($receipt.installedSha256)`nbackup=$backup`n")
        $stream.SetLength(0)
        $stream.Position = 0
        $stream.Write($bytes, 0, $bytes.Length)
        $stream.Flush($true)
    }
    Write-Output 'PASS: current Forge 1.5.2 JAR installed and read-back verified.'
} finally {
    foreach ($stream in $locks) { $stream.Dispose() }
    if ($scopedOwned) { $scoped.ReleaseMutex() }
    if ($owned) { $mutex.ReleaseMutex() }
    $scoped.Dispose()
    $mutex.Dispose()
}
