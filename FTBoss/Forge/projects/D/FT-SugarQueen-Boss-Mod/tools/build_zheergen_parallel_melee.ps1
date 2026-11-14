param(
    [Parameter(Mandatory = $true)][string]$TaskPath,
    [switch]$Deploy
)
$ErrorActionPreference = 'Stop'
$project = [IO.Path]::GetFullPath((Split-Path -Parent $PSScriptRoot))
$task = [IO.Path]::GetFullPath($TaskPath)
if (-not $task.StartsWith((Join-Path $project '_work') + '\', [StringComparison]::OrdinalIgnoreCase)) {
    throw 'Task must remain inside project _work.'
}
$instance = Join-Path $env:APPDATA ('.minecraft\versions\FarmingTales_Forge' + [char]0x2161 + ' v1.5.2')
$target = Join-Path $instance 'mods\qys-bosses.jar'
$jdk = Join-Path $env:APPDATA '.minecraft\runtime\java-runtime-gamma-snapshot\bin'
$libraries = Join-Path $env:APPDATA '.minecraft\libraries'
function Hash([string]$file) { return (Get-FileHash -LiteralPath $file -Algorithm SHA256).Hash.ToLowerInvariant() }
function Run([string]$program, [string[]]$arguments) {
    & $program @arguments
    if ($LASTEXITCODE -ne 0) { throw "Command failed ($LASTEXITCODE): $program" }
}
function No-Game {
    $game = @(Get-CimInstance Win32_Process | Where-Object {
        $_.Name -match '^javaw?\.exe$' -and (-not $_.CommandLine -or
            $_.CommandLine -match 'forgeclient|--gameDir|net\.minecraft\.client\.main|--launchTarget')
    })
    if ($game) { throw "Minecraft running or Java identity unknown: $($game.ProcessId -join ','). Candidate retained." }
}
function EntryHash($entry) {
    $stream = $entry.Open()
    $algorithm = [Security.Cryptography.SHA256]::Create()
    try { return [BitConverter]::ToString($algorithm.ComputeHash($stream)) } finally { $stream.Dispose(); $algorithm.Dispose() }
}
$mutex = [Threading.Mutex]::new($false, 'Local\FTBossForge152Deployment')
$owned = $false
$locks = @()
try {
    if ($Deploy) {
        No-Game
        try { $owned = $mutex.WaitOne(0) } catch [Threading.AbandonedMutexException] { $owned = $true }
        if (-not $owned) { throw 'Shared deployment mutex busy.' }
        foreach ($file in @((Join-Path $instance '.ftboss-152-deploy.lock'),
                (Join-Path $instance 'mods\.qys-bosses-install.lock'), (Join-Path $project '.ftboss-source-deploy.lock'))) {
            $locks += [IO.File]::Open($file, [IO.FileMode]::OpenOrCreate, [IO.FileAccess]::ReadWrite, [IO.FileShare]::None)
        }
    }
    [IO.Directory]::CreateDirectory($task) | Out-Null
    $baseline = Join-Path $task 'baseline.jar'
    $baselineHash = Hash $target
    if (Test-Path -LiteralPath $baseline) {
        if ((Hash $baseline) -ne $baselineHash) { throw 'Baseline changed; use a fresh task directory.' }
    } else { Copy-Item -LiteralPath $target -Destination $baseline }
    $sources = [ordered]@{}
    $tracked = @('src\main\java\net\farmingtales\ftboss\entity\EasterBossEntity.java',
        'src\main\java\net\farmingtales\ftboss\balance\ManorBalanceHooks.java',
        'tools\runtime_patches\ZheergenParallelMeleePatch.java', 'tools\verification\ZheergenParallelMeleeCheck.java',
        'tools\build_zheergen_parallel_melee.ps1')
    foreach ($relative in $tracked) {
        $absolute = Join-Path $project $relative
        $sources[$absolute] = Hash $absolute
    }
    $classes = Join-Path $task 'classes'
    $payload = Join-Path $task 'payload'
    foreach ($directory in @($classes, $payload)) { [IO.Directory]::CreateDirectory($directory) | Out-Null }
    $asm = @('asm', 'asm-tree', 'asm-analysis' | ForEach-Object { Join-Path $libraries "org\ow2\asm\$_\9.7.1\$_-9.7.1.jar" })
    $classpath = (@($classes) + $asm) -join ';'
    Run (Join-Path $jdk 'javac.exe') @('--release', '17', '-encoding', 'UTF-8', '-proc:none', '-cp', ($asm -join ';'), '-d', $classes,
        (Join-Path $project 'tools\runtime_patches\ZheergenParallelMeleePatch.java'),
        (Join-Path $project 'tools\verification\ZheergenParallelMeleeCheck.java'),
        (Join-Path $project 'tools\verification\ZipStreamCheck.java'))
    Run (Join-Path $jdk 'java.exe') @('-Xverify:all', '-cp', $classpath, 'ZheergenParallelMeleePatch', 'patch', $baseline, $payload)
    $candidate = Join-Path $task 'qys-bosses.jar'
    Copy-Item -LiteralPath $baseline -Destination $candidate -Force
    Run (Join-Path $jdk 'jar.exe') @('--update', '--file', $candidate, '-C', $payload, '.')
    Run (Join-Path $jdk 'java.exe') @('-Xverify:all', '-cp', $classpath, 'ZheergenParallelMeleePatch', 'verify', $baseline, $candidate)
    Run (Join-Path $jdk 'java.exe') @('-Xverify:all', '-cp', $classpath, 'ZheergenParallelMeleeCheck', $baseline, $candidate, $task)
    Run (Join-Path $jdk 'java.exe') @('-Xverify:all', '-cp', $classpath, 'ZipStreamCheck', $candidate)
    Run 'node' @((Join-Path $project 'tools\verification\check_jar_zip_rules.cjs'), $candidate)
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $changed = @('net/farmingtales/ftboss/entity/EasterBossEntity.class', 'net/farmingtales/ftboss/balance/ManorBalanceHooks.class')
    $before = [IO.Compression.ZipFile]::OpenRead($baseline)
    $after = [IO.Compression.ZipFile]::OpenRead($candidate)
    try {
        if ($after.Entries.Count -ne $before.Entries.Count) { throw 'Unexpected archive entry count.' }
        if (@($after.Entries | Group-Object FullName | Where-Object Count -gt 1).Count) { throw 'Duplicate archive entries.' }
        foreach ($entry in $before.Entries) {
            $replacement = $after.GetEntry($entry.FullName)
            if (-not $replacement) { throw "Missing runtime entry: $($entry.FullName)" }
            $equal = (EntryHash $entry) -eq (EntryHash $replacement)
            if ($changed -contains $entry.FullName) {
                if ($equal) { throw "Expected class did not change: $($entry.FullName)" }
            } elseif (-not $equal) { throw "Unrelated runtime entry changed: $($entry.FullName)" }
        }
        $preserved = $before.Entries.Count - $changed.Count
    } finally { $before.Dispose(); $after.Dispose() }
    foreach ($file in $sources.Keys) { if ((Hash $file) -ne $sources[$file]) { throw "Source changed during build: $file" } }
    if ((Hash $target) -ne $baselineHash) { throw 'Installed baseline changed during build.' }
    $report = [ordered]@{
        status = 'PASS_OFFLINE'; baseline = $target; baselineSha256 = $baselineHash;
        candidate = $candidate; candidateSha256 = (Hash $candidate); changedEntries = $changed;
        preservedRuntimeEntries = $preserved; sources = $sources;
        behavior = (Get-Content -LiteralPath (Join-Path $task 'behavior.json') -Raw | ConvertFrom-Json);
        modelsAndAnimationsPreserved = $true; gameplayTested = $false; docsTouched = $false; savesTouched = $false;
        docsProposal = (Join-Path $project '_work\docs-proposals\zheergen-parallel-melee.md')
    }
    [IO.File]::WriteAllText((Join-Path $task 'verification.json'), ($report | ConvertTo-Json -Depth 8) + "`n", [Text.UTF8Encoding]::new($false))
    if ($Deploy) {
        $backup = Join-Path $task 'qys-bosses-before-deploy.jar'
        if (Test-Path -LiteralPath $backup) { throw 'Backup exists; do not replay completed deployment.' }
        Copy-Item -LiteralPath $target -Destination $backup
        if ((Hash $backup) -ne $baselineHash) { throw 'Backup checksum mismatch.' }
        No-Game
        Run 'powershell.exe' @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', (Join-Path $project 'tools\deploy_jar.ps1'),
            '-NoBuild', '-ArtifactPath', $candidate, '-TargetPath', $target, '-ExpectedTargetHash', $baselineHash)
        if ((Hash $target) -ne $report.candidateSha256) { throw 'Installed checksum mismatch.' }
        $receipt = [ordered]@{ installed = $true; target = $target; backup = $backup;
            installedSha256 = (Hash $target); baselineSha256 = $baselineHash;
            gameplayTested = $false; neoForgeTouched = $false; docsTouched = $false; savesTouched = $false }
        [IO.File]::WriteAllText((Join-Path $task 'deployment-receipt.json'), ($receipt | ConvertTo-Json -Depth 8) + "`n", [Text.UTF8Encoding]::new($false))
        foreach ($stream in $locks) {
            $bytes = [Text.Encoding]::UTF8.GetBytes("pid=$PID`noperation=zheergen-parallel-melee-deployed`ninstalled=$($receipt.installedSha256)`nbackup=$backup`n")
            $stream.SetLength(0)
            $stream.Write($bytes, 0, $bytes.Length)
            $stream.Flush($true)
        }
        Write-Output 'PASS: deployed Zheergen parallel melee and 65 percent phantom chance to current Forge 1.5.2 only.'
    }
    Write-Output "PASS: $preserved unrelated archive entries preserved; $candidate"
} finally {
    foreach ($stream in $locks) { $stream.Dispose() }
    if ($owned) { $mutex.ReleaseMutex() }
    $mutex.Dispose()
}
