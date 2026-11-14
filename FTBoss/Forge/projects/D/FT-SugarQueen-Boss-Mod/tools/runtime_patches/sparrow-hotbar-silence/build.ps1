param([switch]$Deploy)
$ErrorActionPreference = 'Stop'
$project = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../..'))
$task = Join-Path $project '_work/sparrow-hotbar-silence-20261025'
$instance = Join-Path $env:APPDATA ('.minecraft/versions/FarmingTales_Forge' + [char]0x2161 + ' v1.5.2')
$target = Join-Path $instance 'mods/qys-bosses.jar'
$baseline = Join-Path $task 'baseline-current.jar'
$jdk = Join-Path $env:APPDATA '.minecraft/runtime/java-runtime-gamma-snapshot/bin'
$previous = Join-Path $project '_work/zicai-theme-zheergen-trail-20261025'
$classes = Join-Path $task 'classes'
$official = Join-Path $task 'compiled-official'
$payload = Join-Path $task 'payload'
$hierarchy = Join-Path $task 'hierarchy'
$mapped = Join-Path $project 'build/fg_cache/net/minecraftforge/forge/1.20.1-47.4.10_mapped_official_1.20.1/forge-1.20.1-47.4.10_mapped_official_1.20.1.jar'
$mappings = Join-Path $project 'build/createMcpToSrg/output.tsrg'
$srg = Get-ChildItem (Join-Path $env:USERPROFILE '.gradle/caches/modules-2/files-2.1/net.minecraftforge/srgutils/0.4.15') -Recurse -Filter '*.jar' | Select-Object -First 1 -ExpandProperty FullName
$dependencies = Join-Path $previous 'compile-dependencies.jar'
$runtime = Join-Path $previous 'runtime-dependencies.jar'
$toolClasspath = "$classes;$dependencies;$srg"
$sources = @('src/main/java/net/farmingtales/ftboss/boss/ai/SparrowDisruption.java',
    'src/main/java/net/farmingtales/ftboss/util/SparrowDisruptionRules.java',
    'tools/verification/SparrowDisruptionRulesCheck.java', 'tools/verification/SparrowDisruptionJarCheck.java') |
    ForEach-Object { Join-Path $project $_ }
function Hash([string]$file) { return (Get-FileHash -LiteralPath $file -Algorithm SHA256).Hash.ToLowerInvariant() }
function Run([string]$executable, [string[]]$arguments) {
    & $executable @arguments
    if ($LASTEXITCODE -ne 0) { throw "$executable failed: $LASTEXITCODE" }
}
function No-Game {
    $game = @(Get-CimInstance Win32_Process | Where-Object {
        $_.Name -match '^(Minecraft|Minecraft\.Windows)\.exe$' -or
        $_.Name -match '^javaw?\.exe$' -and (-not $_.CommandLine -or $_.CommandLine -match 'forgeclient|--gameDir|net\.minecraft\.client\.main|--launchTarget')
    })
    if ($game) { throw "Minecraft running or Java identity unknown: $($game.ProcessId -join ','). Candidate retained." }
}
New-Item -ItemType Directory -Path $classes,$official,$payload,$hierarchy -Force | Out-Null
$hashes = [ordered]@{}
foreach ($file in $sources + @($PSCommandPath)) { $hashes[$file] = Hash $file }
$baselineHash = Hash $baseline
Run "$jdk/javac.exe" @('--release', '17', '-encoding', 'UTF-8', '-proc:none', '-cp', $toolClasspath, '-d', $classes,
    (Join-Path $project 'tools/runtime_patches/diamond-alloy-durability-20261020/RemapRuntimeNames.java'),
    (Join-Path $project 'tools/runtime_patches/zicai-curio-silence-20261018/RemapSilence.java'),
    (Join-Path $project 'tools/verification/SparrowDisruptionJarCheck.java'),
    (Join-Path $project 'tools/verification/ZipStreamCheck.java'))
$officialBaseline = Join-Path $task 'baseline-official.jar'
Run "$jdk/java.exe" @('-cp', $toolClasspath, 'RemapRuntimeNames', $mappings, $baseline, $officialBaseline)
Run "$jdk/javac.exe" (@('-J-Duser.language=en', '--release', '17', '-encoding', 'UTF-8', '-proc:none', '-implicit:none',
    '-sourcepath', (Join-Path $task 'empty-sourcepath'), '-cp', "$officialBaseline;$dependencies", '-d', $official) + $sources[0..1] + $sources[2])
Run "$jdk/java.exe" @('-Xverify:all', '-cp', $official, 'SparrowDisruptionRulesCheck')
Push-Location -LiteralPath $hierarchy
try { Run "$jdk/jar.exe" @('--extract', '--file', $officialBaseline, 'net/farmingtales/ftboss/entity/EasterBossEntity.class') }
finally { Pop-Location }
Run "$jdk/java.exe" @('-cp', $toolClasspath, 'RemapSilence', $mappings, $official, $payload, $mapped, $hierarchy)
$entries = @('net/farmingtales/ftboss/boss/ai/SparrowDisruption.class', 'net/farmingtales/ftboss/util/SparrowDisruptionRules.class')
$candidate = Join-Path $task 'qys-bosses.jar'
Copy-Item -LiteralPath $baseline -Destination $candidate -Force
$jarArguments = @('--update', '--file', $candidate)
foreach ($entry in $entries) { $jarArguments += @('-C', $payload, $entry) }
Run "$jdk/jar.exe" $jarArguments
Run "$jdk/java.exe" @('-Xverify:all', '-cp', "$candidate;$classes;$runtime", 'SparrowDisruptionJarCheck', $baseline, $candidate)
Run "$jdk/java.exe" @('-cp', $classes, 'ZipStreamCheck', $candidate)
Run 'node.exe' @((Join-Path $project 'tools/verification/check_jar_zip_rules.cjs'), $candidate)
foreach ($file in $hashes.Keys) { if ((Hash $file) -ne $hashes[$file]) { throw "Source changed during build: $file" } }
$report = [ordered]@{ passed = $true; baselineSha256 = $baselineHash; candidate = $candidate;
    candidateSha256 = (Hash $candidate); sources = $hashes; entries = $entries; gameplayTested = $false }
[IO.File]::WriteAllText((Join-Path $task 'build-report.json'), ($report | ConvertTo-Json -Depth 8) + "`n", [Text.UTF8Encoding]::new($false))
if (-not $Deploy) { return }
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
    if ((Hash $target) -ne $baselineHash) { throw 'Installed baseline changed; rebuild from current JAR.' }
    $backup = Join-Path $task 'qys-bosses-before-deploy.jar'
    if (Test-Path -LiteralPath $backup) { throw 'Backup already exists; do not replay deployment.' }
    Copy-Item -LiteralPath $target -Destination $backup
    if ((Hash $backup) -ne $baselineHash) { throw 'Backup checksum mismatch.' }
    No-Game
    Run 'powershell.exe' @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', (Join-Path $project 'tools/deploy_jar.ps1'),
        '-NoBuild', '-ArtifactPath', $candidate, '-TargetPath', $target, '-ExpectedTargetHash', $baselineHash)
    if ((Hash $target) -ne $report.candidateSha256) { throw 'Installed checksum mismatch.' }
    Run "$jdk/java.exe" @('-Xverify:all', '-cp', "$target;$classes;$runtime", 'SparrowDisruptionJarCheck', $backup, $target)
    $receipt = [ordered]@{ installed = $true; target = $target; backup = $backup;
        baselineSha256 = $baselineHash; installedSha256 = (Hash $target); gameplayTested = $false; docsTouched = $false }
    [IO.File]::WriteAllText((Join-Path $task 'deployment-receipt.json'), ($receipt | ConvertTo-Json -Depth 8) + "`n", [Text.UTF8Encoding]::new($false))
    foreach ($stream in $locks) {
        $bytes = [Text.Encoding]::UTF8.GetBytes("pid=$PID`noperation=sparrow-hotbar-silence-deployed`ninstalled=$($receipt.installedSha256)`nbackup=$backup`n")
        $stream.SetLength(0)
        $stream.Write($bytes, 0, $bytes.Length)
        $stream.Flush($true)
    }
    Write-Output 'PASS: Sparrow hotbar silence deployed to current Forge 1.5.2 only.'
} finally {
    foreach ($stream in $locks) { $stream.Dispose() }
    if ($owned) { $mutex.ReleaseMutex() }
    $mutex.Dispose()
}
