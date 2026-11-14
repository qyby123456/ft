param([Parameter(Mandatory = $true)][string]$TaskRoot)
$ErrorActionPreference = 'Stop'
$project = [IO.Path]::GetFullPath((Split-Path -Parent $PSScriptRoot))
$task = [IO.Path]::GetFullPath($TaskRoot)
if (-not $task.StartsWith($project + '\_work\', [StringComparison]::OrdinalIgnoreCase)) { throw 'Task directory escapes project workspace' }
$workspace = Join-Path $env:APPDATA ('.minecraft\versions\FarmingTales_Forge' + [char]0x2161 + ' v1.5.2')
$live = Join-Path $workspace 'mods\qys-bosses.jar'
$artifact = Join-Path $task 'qys-bosses.jar'
$baseline = Join-Path $task 'baseline.jar'
$expected = (Get-FileHash -LiteralPath $baseline).Hash
$artifactHash = (Get-FileHash -LiteralPath $artifact).Hash
$locks = @()
function Assert-Stopped {
    $running = Get-CimInstance Win32_Process -Filter "name='java.exe' OR name='javaw.exe'" |
        Where-Object { $_.CommandLine -match 'forgeclient|--gameDir|net\.minecraft\.client\.main' }
    if ($running) { throw 'Minecraft is running; deployment refused' }
}
try {
    Assert-Stopped
    foreach ($lock in @((Join-Path $workspace '.ftboss-152-deploy.lock'), (Join-Path $workspace 'mods\.qys-bosses-install.lock'))) {
        $locks += [IO.File]::Open($lock, [IO.FileMode]::OpenOrCreate, [IO.FileAccess]::ReadWrite, [IO.FileShare]::None)
    }
    if ((Get-FileHash -LiteralPath $live).Hash -ne $expected) { throw 'Live baseline changed; rebase instead of overwriting' }
    $log = [IO.File]::ReadAllText((Join-Path $task 'verification\build.log'))
    if ($log -notmatch 'PASS scoped Benger patch' -or $log -notmatch 'PASS: installed/staged JVM' -or $log -notmatch 'OK\s+\d+ entries') {
        throw 'Required class/JVM/ZIP checks are missing'
    }
    $backup = Join-Path $task 'deployment-backups'
    New-Item -ItemType Directory -Path $backup -Force | Out-Null
    $targets = @($live)
    for ($index = 0; $index -lt $targets.Count; $index++) {
        if (Test-Path -LiteralPath $targets[$index]) {
            Copy-Item -LiteralPath $targets[$index] -Destination (Join-Path $backup ($index.ToString() + '-qys-bosses.jar'))
        }
    }
    Assert-Stopped
    if ((Get-FileHash -LiteralPath $live).Hash -ne $expected) { throw 'Baseline changed before deployment' }
    & powershell.exe -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot 'deploy_jar.ps1') -NoBuild -ArtifactPath $artifact -TargetPath $live -ExpectedTargetHash $expected
    if ($LASTEXITCODE -ne 0) { throw 'Deployment script refused or failed' }
    $installed = @()
    foreach ($target in $targets) {
        if (Test-Path -LiteralPath (Split-Path -Parent $target)) {
            if ((Get-FileHash -LiteralPath $target).Hash -ne $artifactHash) { throw "Installed artifact mismatch: $target" }
            $installed += $target
        }
    }
    $report = @{ installed = $true; gameTested = $false; baselineSha256 = $expected; installedSha256 = $artifactHash; targets = $installed; backupDirectory = $backup; locksPreserved = $true }
    [IO.File]::WriteAllText((Join-Path $task 'verification\deployment.json'), ($report | ConvertTo-Json -Depth 4), [Text.UTF8Encoding]::new($false))
    Write-Output 'PASS: stopped-game deployment, baseline guard, target hashes and backups verified'
} finally {
    foreach ($stream in $locks) { $stream.Dispose() }
}
