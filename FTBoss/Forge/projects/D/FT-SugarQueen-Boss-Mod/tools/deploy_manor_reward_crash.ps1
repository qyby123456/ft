param([Parameter(Mandatory = $true)][string]$TaskPath)
$ErrorActionPreference = 'Stop'
$project = Split-Path -Parent $PSScriptRoot
$task = (Resolve-Path -LiteralPath $TaskPath).Path
if (-not $task.StartsWith((Join-Path $project '_work') + '\', [StringComparison]::OrdinalIgnoreCase)) {
    throw 'Task escapes project _work.'
}
$instanceName = 'FarmingTales_Forge' + [char]0x2161 + ' v1.5.2'
$instance = Join-Path $env:APPDATA ".minecraft\versions\$instanceName"
$target = Join-Path $instance 'mods\qys-bosses.jar'
$report = Get-Content -LiteralPath (Join-Path $task 'build-report.json') -Raw -Encoding UTF8 | ConvertFrom-Json
if (-not $report.passed -or -not $report.negativeControlReproduced) { throw 'Verified build and negative control required.' }
function Hash([string]$File) { return (Get-FileHash -LiteralPath $File -Algorithm SHA256).Hash.ToLowerInvariant() }
function No-Game {
    $game = @(Get-CimInstance Win32_Process -Filter "Name = 'java.exe' OR Name = 'javaw.exe'" | Where-Object {
        -not $_.CommandLine -or $_.CommandLine -match 'forgeclient|--gameDir|net\.minecraft\.client\.main|--launchTarget'
    })
    if ($game) { throw "Minecraft is running or Java identity unknown: $($game.ProcessId -join ','). Candidate retained." }
}
$mutex = [Threading.Mutex]::new($false, 'Local\FTBossForge152Deployment')
$owned = $false
$locks = @()
$attempted = $false
$backup = Join-Path $task 'qys-bosses-before-deploy.jar'
try {
    No-Game
    try { $owned = $mutex.WaitOne(0) } catch [Threading.AbandonedMutexException] { $owned = $true }
    if (-not $owned) { throw 'Shared deployment mutex busy.' }
    foreach ($file in @((Join-Path $instance '.ftboss-152-deploy.lock'),
            (Join-Path $instance 'mods\.qys-bosses-install.lock'), (Join-Path $project '.ftboss-source-deploy.lock'))) {
        $locks += [IO.File]::Open($file, [IO.FileMode]::OpenOrCreate, [IO.FileAccess]::ReadWrite, [IO.FileShare]::None)
    }
    if ((Hash $target) -ne $report.baselineSha256) { throw 'Installed JAR changed; rebuild from latest baseline.' }
    if ((Hash $report.candidate) -ne $report.candidateSha256) { throw 'Candidate changed after verification.' }
    foreach ($property in $report.sources.PSObject.Properties) {
        if ((Hash $property.Name) -ne $property.Value) { throw "Source changed after verification: $($property.Name)" }
    }
    if (Test-Path -LiteralPath $backup) { throw 'Deployment backup exists; do not replay.' }
    Copy-Item -LiteralPath $target -Destination $backup
    if ((Hash $backup) -ne $report.baselineSha256) { throw 'Backup checksum mismatch.' }
    No-Game
    $attempted = $true
    & powershell.exe -NoProfile -ExecutionPolicy Bypass -File (Join-Path $project 'tools\deploy_jar.ps1') `
        -NoBuild -ArtifactPath $report.candidate -TargetPath $target -ExpectedTargetHash $report.baselineSha256
    if ($LASTEXITCODE -ne 0) { throw "Guarded deployment failed: $LASTEXITCODE" }
    if ((Hash $target) -ne $report.candidateSha256) { throw 'Installed checksum mismatch.' }
    $jdk = Join-Path $env:APPDATA '.minecraft\runtime\java-runtime-gamma-snapshot\bin'
    $asm = @('asm', 'asm-tree', 'asm-analysis', 'asm-commons') | ForEach-Object {
        Join-Path $env:APPDATA ".minecraft\libraries\org\ow2\asm\$_\9.7.1\$_-9.7.1.jar"
    }
    $toolClasspath = ((@((Join-Path $task 'tools')) + $asm) -join ';')
    & (Join-Path $jdk 'java.exe') -Xverify:all -cp $toolClasspath ManorRewardDamagePatch verify $backup $target
    if ($LASTEXITCODE -ne 0) { throw 'Installed archive or production getter verification failed.' }
    & (Join-Path $jdk 'java.exe') -Xverify:all -cp ($target + ';' + $toolClasspath) ManorPlayerBalanceCheck event-only $target
    if ($LASTEXITCODE -ne 0) { throw 'Installed event linkage verification failed.' }
    & (Join-Path $jdk 'java.exe') -cp $toolClasspath ZipStreamCheck $target
    if ($LASTEXITCODE -ne 0) { throw 'Installed ZIP stream verification failed.' }
    $receipt = [ordered]@{ installed = $true; target = $target; backup = $backup; installedSha256 = (Hash $target);
        baselineSha256 = $report.baselineSha256; changedEntries = $report.changedEntries;
        productionNameEventAssertions = 128; gameplayTested = $false; neoForgeTouched = $false; docsTouched = $false; docsImpact = 'none' }
    [IO.File]::WriteAllText((Join-Path $task 'deployment-receipt.json'),
        ($receipt | ConvertTo-Json -Depth 8) + "`n", [Text.UTF8Encoding]::new($false))
    foreach ($stream in $locks) {
        $bytes = [Text.Encoding]::UTF8.GetBytes("pid=$PID`noperation=manor-reward-damage-crash-fixed`ninstalled=$($receipt.installedSha256)`nbackup=$backup`n")
        $stream.SetLength(0)
        $stream.Write($bytes, 0, $bytes.Length)
        $stream.Flush($true)
    }
    Write-Output "PASS deployed and reverified current Forge 1.5.2 only; SHA256=$($receipt.installedSha256)"
    Write-Output "Backup: $backup"
} catch {
    if ($attempted -and (Test-Path -LiteralPath $backup) -and (Hash $target) -eq $report.candidateSha256) {
        No-Game
        & powershell.exe -NoProfile -ExecutionPolicy Bypass -File (Join-Path $project 'tools\deploy_jar.ps1') `
            -NoBuild -ArtifactPath $backup -TargetPath $target -ExpectedTargetHash $report.candidateSha256
        if ($LASTEXITCODE -ne 0) { throw 'Deployment failed and guarded rollback failed; inspect backup and target.' }
    }
    throw
} finally {
    foreach ($stream in $locks) { $stream.Dispose() }
    if ($owned) { $mutex.ReleaseMutex() }
    $mutex.Dispose()
}
