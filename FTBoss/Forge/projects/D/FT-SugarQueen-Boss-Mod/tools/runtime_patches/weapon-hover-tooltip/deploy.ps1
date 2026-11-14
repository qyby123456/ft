$ErrorActionPreference = 'Stop'
$project = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../..'))
$task = Join-Path $project '_work/weapon-hover-lag-20261025'
$instance = Join-Path $env:APPDATA ('.minecraft/versions/FarmingTales_Forge' + [char]0x2161 + ' v1.5.2')
$target = Join-Path $instance 'mods/qys-bosses.jar'
$report = Get-Content -LiteralPath (Join-Path $task 'build-report.json') -Raw -Encoding UTF8 | ConvertFrom-Json
if (-not $report.passed) { throw 'Candidate verification must pass.' }
function Hash([string]$file) { return (Get-FileHash -LiteralPath $file -Algorithm SHA256).Hash.ToLowerInvariant() }
function No-Game {
    $game = @(Get-CimInstance Win32_Process | Where-Object {
        $_.Name -match '^(Minecraft|Minecraft\.Windows)\.exe$' -or
        $_.Name -match '^javaw?\.exe$' -and (-not $_.CommandLine -or $_.CommandLine -match 'forgeclient|--gameDir|net\.minecraft\.client\.main|--launchTarget')
    })
    if ($game) { throw "Minecraft still running: $($game.ProcessId -join ','). Candidate retained without deployment." }
}
$mutex = [Threading.Mutex]::new($false, 'Local\FTBossForge152Deployment')
$owned = $false
$locks = @()
try {
    No-Game
    try { $owned = $mutex.WaitOne(0) } catch [Threading.AbandonedMutexException] { $owned = $true }
    if (-not $owned) { throw 'Shared deployment mutex busy.' }
    foreach ($file in @((Join-Path $instance '.ftboss-152-deploy.lock'),
            (Join-Path $instance 'mods/.qys-bosses-install.lock'), (Join-Path $project '.ftboss-source-deploy.lock'))) {
        $locks += [IO.File]::Open($file, [IO.FileMode]::OpenOrCreate, [IO.FileAccess]::ReadWrite, [IO.FileShare]::None)
    }
    if ((Hash $target) -ne $report.baselineSha256) { throw 'Installed baseline changed; rebase candidate on latest JAR.' }
    if ((Hash $report.candidate) -ne $report.candidateSha256) { throw 'Candidate changed after verification.' }
    foreach ($property in $report.sources.PSObject.Properties) {
        if ((Hash $property.Name) -ne $property.Value) { throw "Source changed after verification: $($property.Name)" }
    }
    $backup = Join-Path $task 'qys-bosses-before-deploy.jar'
    if (Test-Path -LiteralPath $backup) { throw 'Deployment backup exists; do not replay installation.' }
    Copy-Item -LiteralPath $target -Destination $backup
    if ((Hash $backup) -ne $report.baselineSha256) { throw 'Backup checksum mismatch.' }
    No-Game
    & powershell.exe -NoProfile -ExecutionPolicy Bypass -File (Join-Path $project 'tools/deploy_jar.ps1') `
        -NoBuild -ArtifactPath $report.candidate -TargetPath $target -ExpectedTargetHash $report.baselineSha256
    if ($LASTEXITCODE -ne 0) { throw 'Guarded deployment failed.' }
    if ((Hash $target) -ne $report.candidateSha256) { throw 'Installed checksum mismatch.' }
    $jdk = Join-Path $env:APPDATA '.minecraft/runtime/java-runtime-gamma-snapshot/bin/java.exe'
    $runtime = Join-Path $project '_work/zicai-theme-zheergen-trail-20261025/runtime-dependencies.jar'
    & $jdk -Xverify:all -cp "$target;$(Join-Path $task 'classes');$runtime" WeaponHoverTooltipCheck $backup $target
    if ($LASTEXITCODE -ne 0) { throw 'Installed runtime verification failed.' }
    $receipt = [ordered]@{ installed = $true; target = $target; backup = $backup;
        baselineSha256 = $report.baselineSha256; installedSha256 = (Hash $target); gameplayTested = $false; docsTouched = $false }
    [IO.File]::WriteAllText((Join-Path $task 'deployment-receipt.json'), ($receipt | ConvertTo-Json -Depth 8) + "`n", [Text.UTF8Encoding]::new($false))
    foreach ($stream in $locks) {
        $bytes = [Text.Encoding]::UTF8.GetBytes("pid=$PID`noperation=weapon-hover-tooltip-deployed`ninstalled=$($receipt.installedSha256)`nbackup=$backup`n")
        $stream.SetLength(0)
        $stream.Write($bytes, 0, $bytes.Length)
        $stream.Flush($true)
    }
    Write-Output 'PASS hover tooltip optimization installed in current Forge 1.5.2 only.'
} finally {
    foreach ($stream in $locks) { $stream.Dispose() }
    if ($owned) { $mutex.ReleaseMutex() }
    $mutex.Dispose()
}
