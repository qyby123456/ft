param([switch]$Deploy)
$ErrorActionPreference = 'Stop'
$root = [IO.Path]::GetFullPath((Split-Path -Parent $PSScriptRoot))
$work = Join-Path $root '_work\spark-YlP6kNivxD-20261027'
$release = Join-Path $work ('release-' + [DateTime]::UtcNow.ToString('yyyyMMddTHHmmssfffZ'))
$instance = Join-Path $env:APPDATA ('.minecraft\versions\FarmingTales_Forge' + [char]0x2161 + ' v1.5.2')
$target = Join-Path $instance 'mods\qys-bosses.jar'
$jdk = Join-Path $env:APPDATA '.minecraft\runtime\java-runtime-gamma-snapshot\bin'
$libraries = Join-Path $env:APPDATA '.minecraft\libraries'
$asm = @('asm', 'asm-tree', 'asm-analysis', 'asm-commons') | ForEach-Object {
    Join-Path $libraries "org\ow2\asm\$_\9.8\$_-9.8.jar"
}
$tools = Join-Path $work 'tools'
$classpath = (@($tools) + $asm) -join ';'
$locks = @()
$mutex = [Threading.Mutex]::new($false, 'Local\FTBossForge152Deployment')
$owned = $false
function Hash([string]$path) { return (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash.ToLowerInvariant() }
function Run([string]$program, [string[]]$arguments, [string]$label) {
    & $program @arguments 2>&1 | Tee-Object -FilePath (Join-Path $release ($label + '.log'))
    if ($LASTEXITCODE -ne 0) { throw "$label failed: $LASTEXITCODE" }
}
function No-Game {
    $games = @(Get-CimInstance Win32_Process | Where-Object {
        $_.Name -match '^javaw?\.exe$' -and (-not $_.CommandLine -or
            $_.CommandLine -match 'forgeclient|--gameDir|net\.minecraft\.client\.main|--launchTarget')
    })
    if ($games) { throw 'Minecraft is running or Java identity is unknown; no installed JAR changes.' }
}
try {
    if ($Deploy) {
        No-Game
        try { $owned = $mutex.WaitOne(0) } catch [Threading.AbandonedMutexException] { $owned = $true }
        if (-not $owned) { throw 'Deployment mutex busy.' }
        foreach ($path in @((Join-Path $root '.ftboss-source-deploy.lock'), (Join-Path $instance '.ftboss-152-deploy.lock'),
                (Join-Path $instance 'mods\.qys-bosses-install.lock'))) {
            $locks += [IO.File]::Open($path, [IO.FileMode]::OpenOrCreate, [IO.FileAccess]::ReadWrite, [IO.FileShare]::None)
        }
    }
    [IO.Directory]::CreateDirectory($release) | Out-Null
    [IO.Directory]::CreateDirectory($tools) | Out-Null
    $baseline = Join-Path $release 'baseline.jar'
    $candidate = Join-Path $release 'qys-bosses.jar'
    $payload = Join-Path $release 'payload'
    $hashes = [ordered]@{}
    foreach ($relative in @('src\main\java\net\farmingtales\ftboss\item\OwnerRawExposureFrames.java',
            'tools\runtime_patches\SparkRawExposurePatch.java', 'tools\verification\SparkRawExposureCheck.java',
            'tools\build_spark_raw_exposure.ps1')) {
        $absolute = Join-Path $root $relative
        $hashes[$absolute] = Hash $absolute
    }
    $baselineHash = Hash $target
    Copy-Item -LiteralPath $target -Destination $baseline
    Run (Join-Path $jdk 'javac.exe') @('--release', '17', '-encoding', 'UTF-8', '-proc:none', '-cp', ($asm -join ';'), '-d', $tools,
        (Join-Path $root 'src\main\java\net\farmingtales\ftboss\item\OwnerRawExposureFrames.java'),
        (Join-Path $root 'tools\runtime_patches\SparkRawExposurePatch.java'),
        (Join-Path $root 'tools\verification\SparkRawExposureCheck.java'),
        (Join-Path $root 'tools\verification\ZipStreamCheck.java')) 'compile'
    Run (Join-Path $jdk 'java.exe') @('-Xverify:all', '-cp', $classpath, 'SparkRawExposurePatch', 'patch', $baseline, $payload,
        '515df5804621e3af91fa7a8af6e3bd98ee6a8a59436a5bd5ac59ed65952c602d') 'patch'
    Copy-Item -LiteralPath $baseline -Destination $candidate
    Run (Join-Path $jdk 'jar.exe') @('--update', '--file', $candidate, '-C', $payload, 'net', '-C', $tools,
        'net/farmingtales/ftboss/item/OwnerRawExposureFrames.class') 'package'
    Run (Join-Path $jdk 'java.exe') @('-Xverify:all', '-cp', $classpath, 'SparkRawExposurePatch', 'verify', $baseline, $candidate) 'preservation'
    Run (Join-Path $jdk 'java.exe') @('-Xverify:all', '-cp', $classpath, 'SparkRawExposureCheck', $baseline, $candidate, $release) 'differential'
    Run (Join-Path $jdk 'java.exe') @('-Xverify:all', '-cp', $classpath, 'ZipStreamCheck', $candidate) 'zip-stream'
    Run 'node' @((Join-Path $root 'tools\verification\check_jar_zip_rules.cjs'), $candidate) 'zip-rules'
    $forge = Join-Path $env:USERPROFILE '.gradle\caches\forge_gradle\minecraft_user_repo\net\minecraftforge\forge\1.20.1-47.4.10_mapped_official_1.20.1\forge-1.20.1-47.4.10_mapped_official_1.20.1.jar'
    $dependencies = @($candidate, $forge, $tools) + $asm
    $dependencies += @(Get-ChildItem -LiteralPath $libraries -Recurse -File -Filter '*.jar' | ForEach-Object FullName)
    $dependencies += @(Get-ChildItem -LiteralPath (Join-Path $root 'libs') -File -Filter '*.jar' | ForEach-Object FullName)
    $runtimeArgs = Join-Path $release 'runtime.args'
    [IO.File]::WriteAllLines($runtimeArgs, @('-Xverify:all', '-cp', ('"' + (($dependencies -join ';').Replace('\', '/')) + '"'),
        'SparkRawExposureCheck', '--runtime'), [Text.Encoding]::GetEncoding(936))
    Run (Join-Path $jdk 'java.exe') @('-Dfile.encoding=UTF-8', ('@' + $runtimeArgs)) 'runtime-load'
    foreach ($file in $hashes.Keys) { if ((Hash $file) -ne $hashes[$file]) { throw "Source changed: $file" } }
    if ((Hash $target) -ne $baselineHash) { throw 'Installed baseline changed during build; refuse overwrite.' }
    $candidateHash = Hash $candidate
    if ($Deploy) {
        No-Game
        Run 'powershell.exe' @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', (Join-Path $root 'tools\deploy_jar.ps1'),
            '-NoBuild', '-ArtifactPath', $candidate, '-TargetPath', $target, '-ExpectedTargetHash', $baselineHash) 'deploy'
        if ((Hash $target) -ne $candidateHash) { throw 'Installed checksum mismatch.' }
    }
    $receipt = [ordered]@{
        status = 'PASS_OFFLINE'; sourceReport = 'https://spark.lucko.me/YlP6kNivxD'; release = $release;
        baseline = $baseline; baselineSha256 = $baselineHash; candidate = $candidate; candidateSha256 = $candidateHash;
        target = $target; deployed = [bool]$Deploy; sourceHashes = $hashes;
        behavior = (Get-Content -LiteralPath (Join-Path $release 'raw-exposure-behavior.json') -Raw | ConvertFrom-Json);
        gameplayTested = $false; configTouched = $false; savesTouched = $false; docsTouched = $false; docsImpact = 'none'
    }
    [IO.File]::WriteAllText((Join-Path $work 'deployment-receipt.json'), ($receipt | ConvertTo-Json -Depth 8) + "`n", [Text.UTF8Encoding]::new($false))
    Write-Output "PASS: raw-exposure optimization; deployed=$Deploy; $candidate"
} finally {
    foreach ($handle in $locks) { $handle.Dispose() }
    if ($owned) { $mutex.ReleaseMutex() }
    $mutex.Dispose()
}
