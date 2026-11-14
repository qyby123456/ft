param([Parameter(Mandatory = $true)][string]$TaskRoot)
$ErrorActionPreference = 'Stop'
$project = [IO.Path]::GetFullPath((Split-Path -Parent $PSScriptRoot))
$task = [IO.Path]::GetFullPath($TaskRoot)
if (-not $task.StartsWith($project + '\_work\', [StringComparison]::OrdinalIgnoreCase)) { throw 'Task directory escapes project workspace' }
$workspace = Join-Path $env:APPDATA ('.minecraft\versions\FarmingTales_Forge' + [char]0x2161 + ' v1.5.2')
$live = Join-Path $workspace 'mods\qys-bosses.jar'
$artifact = Join-Path $task 'qys-bosses.jar'
$report = Get-Content -LiteralPath (Join-Path $task 'verification\patch-report.json') -Raw | ConvertFrom-Json
$expected = (Get-FileHash -LiteralPath (Join-Path $task 'baseline.jar')).Hash
$artifactHash = (Get-FileHash -LiteralPath $artifact).Hash
if (-not $report.passed -or $report.baselineSha256 -ne $expected -or $report.artifactSha256 -ne $artifactHash) {
    throw 'Candidate preservation checks missing or outdated'
}
$playback = [IO.File]::ReadAllText((Join-Path $task 'verification\playback-test.log'))
$build = [IO.File]::ReadAllText((Join-Path $task 'verification\incremental-build.log'))
if ($playback -notmatch 'PASS: actual vanilla playback helper' -or $build -notmatch 'PASS: isolated incremental javac') {
    throw 'Required playback or compilation checks missing'
}
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
    $backup = Join-Path $task 'deployment-backups'
    New-Item -ItemType Directory -Path $backup -Force | Out-Null
    $backupFile = Join-Path $backup 'qys-bosses.before-hurt-sounds.jar'
    if (Test-Path -LiteralPath $backupFile) { throw 'Deployment backup already exists; refusing overwrite' }
    Copy-Item -LiteralPath $live -Destination $backupFile
    if ((Get-FileHash -LiteralPath $backupFile).Hash -ne $expected) { throw 'Backup hash mismatch' }
    Assert-Stopped
    & powershell.exe -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot 'deploy_jar.ps1') -NoBuild -ArtifactPath $artifact -TargetPath $live -ExpectedTargetHash $expected
    if ($LASTEXITCODE -ne 0) { throw 'Deployment refused or failed' }
    if ((Get-FileHash -LiteralPath $live).Hash -ne $artifactHash) { throw 'Installed hash mismatch' }
    $deployment = @{ installed = $true; gameTested = $false; baselineSha256 = $expected; installedSha256 = $artifactHash; target = $live; backup = $backupFile }
    [IO.File]::WriteAllText((Join-Path $task 'verification\deployment.json'), ($deployment | ConvertTo-Json -Depth 4), [Text.UTF8Encoding]::new($false))
    Write-Output 'PASS: stopped-game deployment, baseline guard, target hash and recoverable backup'
} finally {
    foreach ($stream in $locks) { $stream.Dispose() }
}
