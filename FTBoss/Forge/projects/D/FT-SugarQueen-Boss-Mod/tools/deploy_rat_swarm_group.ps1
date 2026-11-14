param([Parameter(Mandatory=$true)][string]$Instance)

$ErrorActionPreference = 'Stop'
$project = [IO.Path]::GetFullPath((Split-Path -Parent $PSScriptRoot))
$instanceRoot = (Resolve-Path -LiteralPath $Instance).Path
$target = Join-Path $instanceRoot 'mods/qys-bosses.jar'
$task = Join-Path $project '_work/rat-swarm-group-20261012'
$python = Join-Path $env:USERPROFILE '.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe'
$stage = Join-Path $task ('deployment-' + [Guid]::NewGuid().ToString('N'))
$mutex = [Threading.Mutex]::new($false, 'Local\FTBossForge152Deployment')
$owned = $false
$workspaceLock = $null
$installLock = $null
$guard = $null

function Assert-GameStopped {
    $games = @(Get-CimInstance Win32_Process | Where-Object {
        $_.Name -match '^javaw?\.exe$' -and (-not $_.CommandLine -or $_.CommandLine -match 'forgeclient|--gameDir|--launchTarget|net\.minecraft\.(client|server)\.main|minecraft_server|server\.jar')
    })
    if ($games.Count) { throw "Minecraft is running; no deployment (PID $($games.ProcessId -join ','))." }
}

try {
    Assert-GameStopped
    try { $owned = $mutex.WaitOne(0) } catch [Threading.AbandonedMutexException] { $owned = $true }
    if (-not $owned) { throw 'Another deployment is active; retry after it completes.' }
    $workspaceLock = [IO.File]::Open((Join-Path $instanceRoot '.ftboss-152-deploy.lock'), 'OpenOrCreate', 'ReadWrite', 'None')
    $installLock = [IO.File]::Open((Join-Path $instanceRoot 'mods/.qys-bosses-install.lock'), 'OpenOrCreate', 'ReadWrite', 'None')
    Assert-GameStopped
    New-Item -ItemType Directory -Path $stage | Out-Null
    $guard = [IO.File]::Open($target, 'Open', 'Read', 'Read')
    $snapshot = Join-Path $stage 'latest-baseline.jar'
    $snapshotStream = [IO.File]::Open($snapshot, 'CreateNew', 'Write', 'None')
    try { $guard.CopyTo($snapshotStream); $snapshotStream.Flush($true) } finally { $snapshotStream.Dispose() }
    $baselineHash = (Get-FileHash -LiteralPath $snapshot).Hash
    $candidateRoot = Join-Path $stage 'candidate'
    & $python (Join-Path $PSScriptRoot 'build_rat_swarm_group.py') assemble $snapshot $candidateRoot
    if ($LASTEXITCODE -ne 0) { throw 'Incremental build or validation failed; live jar untouched.' }
    $report = Get-Content -LiteralPath (Join-Path $candidateRoot 'build-receipt.json') -Raw -Encoding UTF8 | ConvertFrom-Json
    if ($report.checks -ne 'passed' -or $report.baselineSha256 -ne $baselineHash) { throw 'Invalid candidate receipt.' }
    Assert-GameStopped
    if ((Get-FileHash -LiteralPath $target).Hash -ne $baselineHash) { throw 'Live jar changed; refusing overwrite.' }
    $guard.Dispose()
    $guard = $null
    & powershell.exe -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot 'deploy_jar.ps1') -NoBuild -ArtifactPath $report.candidate -TargetPath $target -ExpectedTargetHash $baselineHash
    if ($LASTEXITCODE -ne 0) { throw 'Current-instance deployment failed.' }
    if ((Get-FileHash -LiteralPath $target).Hash -ne $report.candidateSha256) { throw 'Installed hash mismatch.' }
    $receipt = [ordered]@{ status='INSTALLED'; target=$target; backup=$snapshot; baselineSha256=$baselineHash;
        installedSha256=$report.candidateSha256; candidate=$report.candidate; rulesTested=$true;
        runtimeLinked=$true; unrelatedEntriesPreserved=$true; gameTested=$false; neoForgeModified=$false;
        desktopModified=$false; fix2Modified=$false }
    [IO.File]::WriteAllText((Join-Path $stage 'deployment-receipt.json'), ($receipt | ConvertTo-Json -Depth 8), [Text.UTF8Encoding]::new($false))
    Write-Output "INSTALLED: $target"
    Write-Output "RECEIPT: $(Join-Path $stage 'deployment-receipt.json')"
} finally {
    if ($guard) { $guard.Dispose() }
    if ($installLock) { $installLock.Dispose() }
    if ($workspaceLock) { $workspaceLock.Dispose() }
    if ($owned) { $mutex.ReleaseMutex() }
    $mutex.Dispose()
}
