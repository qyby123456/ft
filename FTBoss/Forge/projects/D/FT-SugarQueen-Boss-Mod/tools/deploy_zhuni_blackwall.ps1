param()
$ErrorActionPreference = 'Stop'
$project = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$task = Join-Path $project '_work\zhuni-blackwall-20261020'
$report = Get-Content -LiteralPath (Join-Path $task 'build-report.json') -Raw -Encoding UTF8 | ConvertFrom-Json
$verification = Get-Content -LiteralPath (Join-Path $task 'verification.json') -Raw -Encoding UTF8 | ConvertFrom-Json
$instanceName = 'FarmingTales_Forge' + [char]0x2161 + ' v1.5.2'
$instance = [IO.Path]::GetFullPath((Join-Path $env:APPDATA ('.minecraft\versions\' + $instanceName)))
$target = Join-Path $instance 'mods\qys-bosses.jar'
if (-not $report.passed -or -not $verification.passed) { throw 'Build and exact-delta verification must pass.' }
function Hash([string]$file) { return (Get-FileHash -LiteralPath $file -Algorithm SHA256).Hash.ToLowerInvariant() }
function No-Game {
    $game = @(Get-CimInstance Win32_Process | Where-Object {
        $_.Name -match '^(Minecraft|Minecraft\.Windows)\.exe$' -or
        $_.Name -match '^javaw?\.exe$' -and (-not $_.CommandLine -or $_.CommandLine -match 'forgeclient|--gameDir|net\.minecraft\.client\.main|--launchTarget')
    })
    if ($game) { throw "Minecraft running or Java identity unknown: $($game.ProcessId -join ','). Candidate retained." }
}
$mutex = [Threading.Mutex]::new($false, 'Local\FTBossForge152Deployment')
$owned = $false
$locks = @()
try {
    No-Game
    try { $owned = $mutex.WaitOne(0) } catch [Threading.AbandonedMutexException] { $owned = $true }
    if (-not $owned) { throw 'Shared deployment mutex busy.' }
    foreach ($file in @((Join-Path $instance '.ftboss-152-deploy.lock'), (Join-Path $instance 'mods\.qys-bosses-install.lock'))) {
        $locks += [IO.File]::Open($file, [IO.FileMode]::OpenOrCreate, [IO.FileAccess]::ReadWrite, [IO.FileShare]::None)
    }
    if ((Hash $target) -ne $report.baselineSha256) { throw 'Installed baseline changed; rebuild from current JAR.' }
    if ((Hash $report.candidate) -ne $report.candidateSha256) { throw 'Candidate changed after checks.' }
    foreach ($property in $report.sourceHashes.PSObject.Properties) {
        if ((Hash $property.Name) -ne $property.Value) { throw "Source changed after checks: $($property.Name)" }
    }
    $backup = Join-Path $task 'qys-bosses-before-deploy.jar'
    if (Test-Path -LiteralPath $backup) { throw 'Backup already exists; do not reuse completed deployment stage.' }
    Copy-Item -LiteralPath $target -Destination $backup
    if ((Hash $backup) -ne $report.baselineSha256) { throw 'Fresh backup mismatch.' }
    No-Game
    $deployArguments = @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', (Join-Path $project 'tools\deploy_jar.ps1'),
        '-NoBuild', '-ArtifactPath', $report.candidate, '-TargetPath', $target, '-ExpectedTargetHash', $report.baselineSha256)
    & powershell.exe @deployArguments
    if ($LASTEXITCODE -ne 0) { throw "Guarded deployment failed: $LASTEXITCODE" }
    if ((Hash $target) -ne $report.candidateSha256) { throw 'Installed hash mismatch.' }
    $minecraft = Join-Path $env:APPDATA '.minecraft'
    $java = Join-Path $minecraft 'runtime\java-runtime-gamma-snapshot\bin\java.exe'
    $asm = @('asm', 'asm-tree', 'asm-commons', 'asm-util', 'asm-analysis') | ForEach-Object {
        Join-Path $minecraft ("libraries\org\ow2\asm\$_\9.7.1\$_-9.7.1.jar")
    }
    $classpath = @((Join-Path $task 'tools')) + $asm
    & $java -Xverify:all -cp ($classpath -join ';') ZhuniBlackwallJarCheck $backup $target (Join-Path $task 'installed-verification.json')
    if ($LASTEXITCODE -ne 0) { throw 'Installed exact-delta check failed.' }
    $receipt = [ordered]@{ installed = $true; target = $target; backup = $backup;
        baselineSha256 = $report.baselineSha256; installedSha256 = (Hash $target);
        gameTested = $false; neoForgeTouched = $false; desktopTouched = $false }
    $newline = [Environment]::NewLine
    [IO.File]::WriteAllText((Join-Path $task 'deployment-receipt.json'), ($receipt | ConvertTo-Json -Depth 8) + $newline, [Text.UTF8Encoding]::new($false))
    foreach ($stream in $locks) {
        $bytes = [Text.Encoding]::UTF8.GetBytes("pid=$PID" + $newline + "operation=zhuni-blackwall-deployed" + $newline + "installed=$($receipt.installedSha256)" + $newline + "backup=$backup" + $newline)
        $stream.SetLength(0)
        $stream.Write($bytes, 0, $bytes.Length)
        $stream.Flush($true)
    }
    Write-Output 'PASS current Forge 1.5.2 installed and read-back verified; no other instance modified.'
} finally {
    foreach ($stream in $locks) { $stream.Dispose() }
    if ($owned) { $mutex.ReleaseMutex() }
    $mutex.Dispose()
}
