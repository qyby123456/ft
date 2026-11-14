param([Parameter(Mandatory = $true)][string]$TaskPath)
$ErrorActionPreference = 'Stop'
$project = [IO.Path]::GetFullPath((Split-Path -Parent $PSScriptRoot))
$task = [IO.Path]::GetFullPath($TaskPath)
if (-not $task.StartsWith((Join-Path $project '_work') + '\', [StringComparison]::OrdinalIgnoreCase)) {
    throw 'Task must stay inside project _work.'
}
if (Test-Path -LiteralPath (Join-Path $task 'deployment-receipt.json')) { throw 'Already deployed; preserve the existing backup.' }
$instance = Join-Path $env:APPDATA ('.minecraft\versions\FarmingTales_Forge' + [char]0x2161 + ' v1.5.2')
$target = Join-Path $instance 'mods\qys-bosses.jar'
function Hash([string]$file) { (Get-FileHash -LiteralPath $file -Algorithm SHA256).Hash.ToLowerInvariant() }
function No-Game {
    $game = @(Get-CimInstance Win32_Process | Where-Object {
        $_.Name -match '^javaw?\.exe$' -and (-not $_.CommandLine -or
            $_.CommandLine -match 'forgeclient|--gameDir|net\.minecraft\.client\.main|--launchTarget|@')
    })
    if ($game) { throw "Minecraft is running or Java identity unknown: $($game.ProcessId -join ',')." }
}
$mutex = [Threading.Mutex]::new($false, 'Local\FTBossForge152Deployment')
$owned = $false
$locks = @()
try {
    No-Game
    try { $owned = $mutex.WaitOne(0) } catch [Threading.AbandonedMutexException] { $owned = $true }
    if (-not $owned) { throw 'Shared deployment mutex busy.' }
    foreach ($file in @((Join-Path $instance '.ftboss-152-deploy.lock'),
            (Join-Path $instance 'mods\.qys-bosses-install.lock'), (Join-Path $project '.ftboss-source-deploy.lock'))) {
        $locks += [IO.File]::Open($file, [IO.FileMode]::OpenOrCreate, [IO.FileAccess]::ReadWrite, [IO.FileShare]::None)
    }
    $report = Get-Content -LiteralPath (Join-Path $task 'build-receipt.json') -Raw -Encoding UTF8 | ConvertFrom-Json
    $comparison = Get-Content -LiteralPath (Join-Path $task 'comparison.json') -Raw -Encoding UTF8 | ConvertFrom-Json
    if (-not $report.verified -or -not $comparison.passed -or (Hash $target) -ne $report.baselineSha256 -or
            (Hash $report.candidate) -ne $report.candidateSha256 -or
            $comparison.candidateSha256 -ne $report.candidateSha256 -or
            $comparison.baselineSha256 -ne $report.baselineSha256) { throw 'Build, comparison or installed baseline mismatch.' }
    foreach ($property in $report.sourceSha256.PSObject.Properties) {
        if ((Hash (Join-Path $project $property.Name)) -ne $property.Value) { throw "Source changed: $($property.Name)" }
    }
    $backup = Join-Path $task 'qys-bosses-before-deploy.jar'
    if (-not (Test-Path -LiteralPath $backup)) { Copy-Item -LiteralPath $target -Destination $backup }
    if ((Hash $backup) -ne $report.baselineSha256) { throw 'Backup hash mismatch.' }
    No-Game
    & powershell.exe -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot 'deploy_jar.ps1') `
        -NoBuild -ArtifactPath $report.candidate -TargetPath $target -ExpectedTargetHash $report.baselineSha256
    if ($LASTEXITCODE -ne 0) { throw "Deployment failed: $LASTEXITCODE" }
    if ((Hash $target) -ne $report.candidateSha256) { throw 'Installed hash mismatch.' }
    $receipt = [ordered]@{ installed = $true; target = $target; backup = $backup;
        installedSha256 = Hash $target; baselineSha256 = $report.baselineSha256;
        isolatedFullPackRecipeTested = $true; trackedWeaponGameplayTested = $false;
        neoForgeTouched = $false; productionKubejsTouched = $false; docsTouched = $false }
    [IO.File]::WriteAllText((Join-Path $task 'deployment-receipt.json'),
        ($receipt | ConvertTo-Json -Depth 8) + "`n", [Text.UTF8Encoding]::new($false))
    foreach ($stream in $locks) {
        $bytes = [Text.Encoding]::UTF8.GetBytes("pid=$PID`noperation=kjs-native-projection-deployed`ninstalled=$($receipt.installedSha256)`nbackup=$backup`n")
        $stream.SetLength(0)
        $stream.Write($bytes, 0, $bytes.Length)
        $stream.Flush($true)
    }
    Write-Output 'PASS: Native projection scan optimization deployed to current Forge 1.5.2 only.'
} finally {
    foreach ($stream in $locks) { $stream.Dispose() }
    if ($owned) { $mutex.ReleaseMutex() }
    $mutex.Dispose()
}
