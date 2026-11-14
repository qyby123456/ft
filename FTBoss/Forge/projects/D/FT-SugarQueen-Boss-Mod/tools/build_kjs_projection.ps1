param([Parameter(Mandatory = $true)][string]$TaskPath)
$ErrorActionPreference = 'Stop'
$project = [IO.Path]::GetFullPath((Split-Path -Parent $PSScriptRoot))
$task = [IO.Path]::GetFullPath($TaskPath)
if (-not $task.StartsWith((Join-Path $project '_work') + '\', [StringComparison]::OrdinalIgnoreCase)) {
    throw 'Task must stay inside project _work.'
}
$baseline = Join-Path $task 'baseline.jar'
if (-not (Test-Path -LiteralPath $baseline)) { throw 'Provide a reviewed baseline.jar snapshot first.' }
$candidate = Join-Path $task 'qys-bosses.jar'
$jdk = 'C:\Program Files\Zulu\zulu-21\bin'
$java17 = Join-Path $env:APPDATA '.minecraft\runtime\java-runtime-gamma-snapshot\bin\java.exe'
$libraries = Join-Path $env:APPDATA '.minecraft\libraries'
$asm = @('asm', 'asm-tree', 'asm-analysis', 'asm-util', 'asm-commons') | ForEach-Object {
    Join-Path $libraries "org\ow2\asm\$_\9.8\$_-9.8.jar"
}
$tools = Join-Path $task 'tools'
$overlay = Join-Path $task 'overlay'
$empty = Join-Path $task 'empty'
New-Item -ItemType Directory -Path $tools, $empty -Force | Out-Null
$sources = @('tools\runtime_patches\KjsNativeProjectionPatch.java',
    'tools\verification\KjsNativeProjectionCheck.java', 'tools\verification\ZipStreamCheck.java') |
    ForEach-Object { Join-Path $project $_ }
$fixture = Join-Path $project 'tools\verification\KjsProjectionFixture.java'
function Run([string]$program, [object[]]$arguments, [string]$label) {
    $output = & $program @arguments 2>&1 | Out-String
    $exit = $LASTEXITCODE
    [IO.File]::WriteAllText((Join-Path $task "$label.log"), $output, [Text.UTF8Encoding]::new($false))
    if ($exit -ne 0) { throw "$label failed ($exit): $output" }
    Write-Host "$label PASS"
    return $output
}
function Hash([string]$path) { (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash.ToLowerInvariant() }
$null = Run (Join-Path $jdk 'javac.exe') (@('--release', '17', '-encoding', 'UTF-8', '-proc:none',
    '-sourcepath', $empty, '-cp', ($asm -join ';'), '-d', $tools) + $sources) 'compile-checks'
$classpath = (@($tools) + $asm) -join ';'
$java = Join-Path $jdk 'java.exe'
$null = Run $java @('-Xverify:all', '-cp', $classpath, 'KjsNativeProjectionPatch', 'patch', $baseline, $overlay) 'patch'
Copy-Item -LiteralPath $baseline -Destination $candidate -Force
$null = Run (Join-Path $jdk 'jar.exe') @('--update', '--file', $candidate, '-C', $overlay, '.') 'pack'
$null = Run $java @('-Xverify:all', '-cp', $classpath, 'KjsNativeProjectionPatch', 'verify', $baseline, $candidate) 'verify-payload'
$results = @()
foreach ($label in @('before', 'after')) {
    $classes = Join-Path $task "fixture-$label"
    New-Item -ItemType Directory -Path $classes -Force | Out-Null
    $null = Run (Join-Path $jdk 'javac.exe') @('--release', '17', '-encoding', 'UTF-8', '-proc:none',
        '-sourcepath', $empty, '-d', $classes, $fixture) "compile-fixture-$label"
    $artifact = $baseline
    $bounded = 'false'
    if ($label -eq 'after') { $artifact = $candidate; $bounded = 'true' }
    $null = Run $java @('-Xverify:all', '-cp', $classpath, 'KjsNativeProjectionCheck', $artifact, $classes) "transplant-$label"
    $results += Run $java17 @('-Xverify:all', '-cp', $classes, 'KjsProjectionFixture', $bounded) "cases-$label"
}
if ($results[0] -cne $results[1]) { throw 'Original/patched runtime methods produced different fixture results.' }
$null = Run $java @('-cp', $tools, 'ZipStreamCheck', $candidate) 'verify-zip-stream'
$node = (Get-Command node -ErrorAction Stop).Source
$null = Run $node @((Join-Path $project 'tools\verification\check_jar_zip_rules.cjs'), $candidate) 'verify-zip-rules'
$sourceHashes = [ordered]@{}
foreach ($source in @($sources) + @($fixture, $PSCommandPath)) {
    $sourceHashes[$source.Substring($project.Length + 1)] = Hash $source
}
$receipt = [ordered]@{ baselineSha256 = Hash $baseline; candidateSha256 = Hash $candidate;
    candidate = $candidate; verified = $true; changedClasses = 1; changedMethods = 4; addedPrivateHelpers = 1;
    differentialRuntimeCases = 'pass'; java17VerifyAll = 'pass'; trackedCapacity = 4096;
    zipStream = 'pass'; zipRules = 'pass'; sourceSha256 = $sourceHashes }
[IO.File]::WriteAllText((Join-Path $task 'build-receipt.json'),
    ($receipt | ConvertTo-Json -Depth 8) + "`n", [Text.UTF8Encoding]::new($false))
Write-Output ($receipt | ConvertTo-Json -Depth 8)
