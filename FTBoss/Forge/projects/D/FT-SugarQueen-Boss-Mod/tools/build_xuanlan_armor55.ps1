param(
    [Parameter(Mandatory = $true)][string]$TaskPath,
    [switch]$Deploy
)
$ErrorActionPreference = 'Stop'
$project = [IO.Path]::GetFullPath((Split-Path -Parent $PSScriptRoot))
$task = [IO.Path]::GetFullPath($TaskPath)
if (-not $task.StartsWith((Join-Path $project '_work') + '\', [StringComparison]::OrdinalIgnoreCase)) {
    throw 'Task must stay inside project _work.'
}
$instance = Join-Path $env:APPDATA ('.minecraft\versions\FarmingTales_Forge' + [char]0x2161 + ' v1.5.2')
$target = Join-Path $instance 'mods\qys-bosses.jar'
$jdk = Join-Path $env:APPDATA '.minecraft\runtime\java-runtime-gamma-snapshot\bin'
function Hash([string]$file) { (Get-FileHash -LiteralPath $file -Algorithm SHA256).Hash.ToLowerInvariant() }
function Run([string]$program, [string[]]$arguments) {
    & $program @arguments
    if ($LASTEXITCODE -ne 0) { throw "Command failed ($LASTEXITCODE): $program" }
}
function Assert-NoGame {
    $game = @(Get-CimInstance Win32_Process | Where-Object {
        $_.Name -match '^(Minecraft|Minecraft\.Windows)\.exe$' -or
        $_.Name -match '^javaw?\.exe$' -and (-not $_.CommandLine -or
            $_.CommandLine -match 'forgeclient|--gameDir|net\.minecraft\.client\.main|--launchTarget|@')
    })
    if ($game) { throw "Minecraft running or Java identity unknown: $($game.ProcessId -join ','). Candidate retained." }
}
function Save-Json([string]$name, $value) {
    [IO.File]::WriteAllText((Join-Path $task $name), ($value | ConvertTo-Json -Depth 8) + "`n", [Text.UTF8Encoding]::new($false))
}
if (Test-Path -LiteralPath (Join-Path $task 'deployment-receipt.json')) { throw 'Deployment already recorded; use a fresh task directory.' }
foreach ($directory in @($task, (Join-Path $task 'payload'), (Join-Path $task 'tools'), (Join-Path $task 'empty-sourcepath'))) {
    [IO.Directory]::CreateDirectory($directory) | Out-Null
}
$baselineHash = Hash $target
$baseline = Join-Path $task 'baseline.jar'
if (Test-Path -LiteralPath $baseline) {
    if ((Hash $baseline) -ne $baselineHash) { throw 'Installed baseline changed; use a fresh task directory.' }
} else { Copy-Item -LiteralPath $target -Destination $baseline }
$rule = Join-Path $project 'src\main\java\net\farmingtales\ftboss\util\ManorArmorBalanceRules.java'
$check = Join-Path $project 'tools\verification\ManorArmorBalanceCheck.java'
$zipCheck = Join-Path $project 'tools\verification\ZipStreamCheck.java'
$zipRules = Join-Path $project 'tools\verification\check_jar_zip_rules.cjs'
$sourceHashes = [ordered]@{}
foreach ($file in @($rule, $check, $zipCheck, $zipRules, $PSCommandPath,
        (Join-Path $project 'tools\verification\YouziCloneWeaponsCheck.java'))) { $sourceHashes[$file] = Hash $file }
function Assert-Inputs {
    if ((Hash $target) -ne $baselineHash -or (Hash $baseline) -ne $baselineHash) { throw 'Runtime baseline changed during build.' }
    foreach ($file in $sourceHashes.Keys) {
        if ((Hash $file) -ne $sourceHashes[$file]) { throw "Source changed during build: $file" }
    }
}
Save-Json 'build-report.json' ([ordered]@{ passed = $false; state = 'building' })
$payload = Join-Path $task 'payload'
$tools = Join-Path $task 'tools'
$asm = @('asm', 'asm-tree', 'asm-analysis' | ForEach-Object {
    Join-Path $env:APPDATA ".minecraft\libraries\org\ow2\asm\$_\9.7.1\$_-9.7.1.jar"
})
$classpath = (@($tools) + $asm) -join ';'
Run (Join-Path $jdk 'javac.exe') @('--release', '17', '-encoding', 'UTF-8', '-proc:none', '-implicit:none',
    '-sourcepath', (Join-Path $task 'empty-sourcepath'), '-cp', $baseline, '-d', $payload, $rule)
Run (Join-Path $jdk 'javac.exe') @('--release', '17', '-encoding', 'UTF-8', '-proc:none', '-cp', ($asm -join ';'),
    '-d', $tools, $check, $zipCheck)
$candidate = Join-Path $task 'qys-bosses.jar'
Copy-Item -LiteralPath $baseline -Destination $candidate -Force
Run (Join-Path $jdk 'jar.exe') @('--update', '--file', $candidate, '-C', $payload, 'net')
Run (Join-Path $jdk 'java.exe') @('-Xverify:all', '-cp', $classpath, 'ManorArmorBalanceCheck', $baseline, $candidate)
Run (Join-Path $jdk 'java.exe') @('-Xverify:all', '-cp', $classpath, 'ZipStreamCheck', $candidate)
Run 'node' @($zipRules, $candidate)
Assert-Inputs
$candidateHash = Hash $candidate
Save-Json 'build-report.json' ([ordered]@{ passed = $true; baseline = $baseline; baselineSha256 = $baselineHash;
    candidate = $candidate; candidateSha256 = $candidateHash; sourceHashes = $sourceHashes;
    finalXuanlanArmor = 55; changedClasses = @('net/farmingtales/ftboss/util/ManorArmorBalanceRules.class');
    gameTested = $false; docsTouched = $false })
Write-Output "PASS verified Xuanlan armor 55 candidate: $candidate"
if (-not $Deploy) { return }
$mutex = [Threading.Mutex]::new($false, 'Local\FTBossForge152Deployment')
$owned = $false
$locks = @()
try {
    Assert-NoGame
    try { $owned = $mutex.WaitOne(0) } catch [Threading.AbandonedMutexException] { $owned = $true }
    if (-not $owned) { throw 'Shared deployment mutex busy.' }
    foreach ($file in @((Join-Path $instance '.ftboss-152-deploy.lock'),
            (Join-Path $instance 'mods\.qys-bosses-install.lock'), (Join-Path $project '.ftboss-source-deploy.lock'))) {
        $locks += [IO.File]::Open($file, [IO.FileMode]::OpenOrCreate, [IO.FileAccess]::ReadWrite, [IO.FileShare]::None)
    }
    Assert-Inputs
    if ((Hash $candidate) -ne $candidateHash) { throw 'Candidate changed after verification.' }
    $backup = Join-Path $task 'qys-bosses-before-deploy.jar'
    if (-not (Test-Path -LiteralPath $backup)) { Copy-Item -LiteralPath $target -Destination $backup }
    if ((Hash $backup) -ne $baselineHash) { throw 'Backup hash mismatch.' }
    Assert-NoGame
    Run 'powershell.exe' @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', (Join-Path $PSScriptRoot 'deploy_jar.ps1'),
        '-NoBuild', '-ArtifactPath', $candidate, '-TargetPath', $target, '-ExpectedTargetHash', $baselineHash)
    if ((Hash $target) -ne $candidateHash) { throw 'Installed hash mismatch.' }
    Save-Json 'deployment-receipt.json' ([ordered]@{ installed = $true; target = $target; backup = $backup;
        baselineSha256 = $baselineHash; installedSha256 = $candidateHash; finalXuanlanArmor = 55;
        gameTested = $false; neoForgeTouched = $false; desktopTouched = $false; docsTouched = $false })
    Write-Output 'PASS fixed final Xuanlan armor 55 installed to current Forge 1.5.2 only.'
} finally {
    foreach ($stream in $locks) { $stream.Dispose() }
    if ($owned) { $mutex.ReleaseMutex() }
    $mutex.Dispose()
}
