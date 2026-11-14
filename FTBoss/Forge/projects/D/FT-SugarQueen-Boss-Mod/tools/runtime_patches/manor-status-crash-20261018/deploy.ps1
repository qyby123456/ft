[CmdletBinding()]
param([Parameter(Mandatory = $true)][string]$Work, [Parameter(Mandatory = $true)][string]$Target)
$ErrorActionPreference = 'Stop'
$project = 'D:\ft工程文件\FT-SugarQueen-Boss-Mod'
$targetPath = [IO.Path]::GetFullPath($Target)
$allowed = Join-Path $env:APPDATA '.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\mods\qys-bosses.jar'
if ($targetPath -ne [IO.Path]::GetFullPath($allowed)) { throw 'Only current Forge 1.5.2 is allowed' }
$version = Split-Path -Parent (Split-Path -Parent $targetPath)
$stamp = [DateTimeOffset]::Now.ToString('yyyyMMdd-HHmmss-ffff')
$backup = Join-Path $Work ('deployment-before-' + $stamp + '.jar')
$candidate = Join-Path $Work ('deployment-candidate-' + $stamp + '.jar')
$locks = [Collections.Generic.List[IO.FileStream]]::new()
$shared = $null
$guard = $null
$pwsh = 'C:\Users\YOUR_USERNAME\.cache\codex-runtimes\codex-primary-runtime\dependencies\native\powershell\pwsh.exe'
try {
    $game = @(Get-CimInstance Win32_Process -Filter "Name='java.exe' OR Name='javaw.exe'" |
        Where-Object { $_.CommandLine -match 'forgeclient|--gameDir|net\.minecraft\.client\.main' })
    if ($game.Count) { throw ('Minecraft running; no deployment. PID=' + ($game.ProcessId -join ',')) }
    $sharedLock = Join-Path $version '_work\model-release-152-20261008\deployment.lock'
    $shared = [IO.File]::Open($sharedLock, [IO.FileMode]::CreateNew, [IO.FileAccess]::ReadWrite, [IO.FileShare]::None)
    foreach ($path in @((Join-Path $version '.ftboss-152-deploy.lock'), (Join-Path (Split-Path -Parent $targetPath) '.qys-bosses-install.lock'))) {
        $locks.Add([IO.File]::Open($path, [IO.FileMode]::OpenOrCreate, [IO.FileAccess]::ReadWrite, [IO.FileShare]::None))
    }
    $guard = [IO.File]::Open($targetPath, [IO.FileMode]::Open, [IO.FileAccess]::Read, [IO.FileShare]::None)
    $saved = [IO.File]::Open($backup, [IO.FileMode]::CreateNew, [IO.FileAccess]::Write, [IO.FileShare]::None)
    try { $guard.CopyTo($saved); $saved.Flush($true) }
    finally { $saved.Dispose(); $guard.Dispose(); $guard = $null }
    $before = (Get-FileHash -LiteralPath $backup -Algorithm SHA256).Hash
    & (Join-Path $PSScriptRoot 'build-test.ps1') -Work $Work -Baseline $backup -Candidate $candidate -SkipCompile
    if ((Get-FileHash -LiteralPath $targetPath -Algorithm SHA256).Hash -ne $before) { throw 'Current JAR changed during validation' }
    $after = (Get-FileHash -LiteralPath $candidate -Algorithm SHA256).Hash
    & $pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $project 'tools\deploy_jar.ps1') -NoBuild -ArtifactPath $candidate -TargetPath $targetPath -ExpectedTargetHash $before 2>&1 |
        Tee-Object -FilePath (Join-Path $Work 'deployment.log')
    if ($LASTEXITCODE -ne 0) { throw 'Guarded project deployment failed' }
    if ((Get-FileHash -LiteralPath $targetPath -Algorithm SHA256).Hash -ne $after) { throw 'Installed hash mismatch' }
    [ordered]@{
        status = 'INSTALLED_OFFLINE_VERIFIED_GAME_PENDING'
        timestamp = [DateTimeOffset]::Now.ToString('o')
        target = $targetPath
        backup = $backup
        candidate = $candidate
        beforeSHA256 = $before
        installedSHA256 = $after
        fixedCrash = 'SparrowBorrowedSpells.ready(null)'
        blockedEffects = @('ftboss:heal_ban','ftboss:shadow_erosion')
        legacyEffectsPurged = $true
        playerEffectProtectionPreserved = $true
        unrelatedRuntimeEntriesByteIdentical = $true
        inGameVerified = $false
    } | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath (Join-Path $Work 'deployment.json') -Encoding UTF8
    Write-Output "INSTALLED $before -> $after"
} finally {
    if ($guard) { $guard.Dispose() }
    foreach ($stream in $locks) { $stream.Dispose() }
    if ($shared) { $shared.Dispose(); Remove-Item -LiteralPath $sharedLock }
}
