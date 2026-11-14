param()
$ErrorActionPreference = 'Stop'
$project = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\..\..'))
$task = Join-Path $project '_work\qingyi-balance-20261027'
$instanceName = 'FarmingTales_Forge' + [char]0x2161 + ' v1.5.2'
$instance = Join-Path $env:APPDATA ('.minecraft\versions\' + $instanceName)
$target = Join-Path $instance 'mods\qys-bosses.jar'
$candidate = Join-Path $task 'qys-bosses.jar'
$locks = [Collections.Generic.List[IO.FileStream]]::new()
$mutex = [Threading.Mutex]::new($false, 'Local\FTBossForge152Deployment')
$owned = $false
function Hash([string] $File) {
    return (Get-FileHash -LiteralPath $File -Algorithm SHA256).Hash.ToLowerInvariant()
}
try {
    $game = Get-CimInstance Win32_Process -Filter "Name='java.exe' OR Name='javaw.exe'" |
        Where-Object { $_.CommandLine -match 'forgeclient|--gameDir|net\.minecraft\.client\.main' }
    if ($game) { throw 'Minecraft is running; exit the game before deployment.' }
    try { $owned = $mutex.WaitOne(0) } catch [Threading.AbandonedMutexException] { $owned = $true }
    if (-not $owned) { throw 'Another deployment holds the instance mutex.' }
    foreach ($lock in @((Join-Path $project '.ftboss-source-deploy.lock'),
            (Join-Path $instance '.ftboss-152-deploy.lock'), (Join-Path $instance 'mods\.qys-bosses-install.lock'))) {
        $locks.Add([IO.File]::Open($lock, [IO.FileMode]::OpenOrCreate, [IO.FileAccess]::ReadWrite, [IO.FileShare]::None))
    }
    $report = Get-Content -LiteralPath (Join-Path $task 'build-report.json') -Raw -Encoding utf8 | ConvertFrom-Json
    if (-not $report.passed) { throw 'Candidate verification did not pass.' }
    if ((Hash $target) -ne $report.baselineSha256) { throw 'Runtime changed; rebuild against the current runtime first.' }
    if ((Hash $candidate) -ne $report.candidateSha256) { throw 'Candidate changed after verification.' }
    foreach ($source in $report.sources.PSObject.Properties) {
        if ((Hash $source.Name) -ne $source.Value) { throw "Source changed since build: $($source.Name)" }
    }
    $backup = Join-Path $task ('qys-bosses-before-' + $report.baselineSha256.Substring(0, 12) + '.jar')
    if (-not (Test-Path -LiteralPath $backup)) { Copy-Item -LiteralPath $target -Destination $backup }
    if ((Hash $backup) -ne $report.baselineSha256) { throw 'Backup checksum mismatch.' }
    & powershell.exe -NoProfile -ExecutionPolicy Bypass -File (Join-Path $project 'tools\deploy_jar.ps1') `
        -NoBuild -ArtifactPath $candidate -TargetPath $target -ExpectedTargetHash $report.baselineSha256
    if ($LASTEXITCODE -ne 0) { throw "Deployment refused or failed: $LASTEXITCODE" }
    if ((Hash $target) -ne $report.candidateSha256) { throw 'Installed artifact checksum mismatch.' }
    [ordered]@{
        installed = $true; target = $target; backup = $backup
        baselineSha256 = $report.baselineSha256; installedSha256 = $report.candidateSha256
        gameplayTested = $false; docsTouched = $false
        deployedAt = [DateTimeOffset]::UtcNow.ToOffset([TimeSpan]::FromHours(8)).ToString('o')
    } | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $task 'deployment-receipt.json') -Encoding utf8
    Write-Output "DEPLOYED AND VERIFIED: $($report.candidateSha256)"
} finally {
    foreach ($stream in $locks) { $stream.Dispose() }
    if ($owned) { $mutex.ReleaseMutex() }
    $mutex.Dispose()
}
