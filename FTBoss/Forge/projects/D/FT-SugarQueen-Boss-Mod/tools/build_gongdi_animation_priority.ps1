param(
    [string]$BaselinePath,
    [string]$TaskPath
)
$ErrorActionPreference = 'Stop'
$project = [IO.Path]::GetFullPath((Split-Path -Parent $PSScriptRoot))
$instance = Join-Path $env:APPDATA ('.minecraft\versions\FarmingTales_Forge' + [char]0x2161 + ' v1.5.2')
if (-not $BaselinePath) { $BaselinePath = Join-Path $instance 'mods\qys-bosses.jar' }
if (-not $TaskPath) { $TaskPath = Join-Path $project '_work\gongdi-animation-priority-20261026-a1' }
$baseline = [IO.Path]::GetFullPath($BaselinePath)
$task = [IO.Path]::GetFullPath($TaskPath)
if (-not $task.StartsWith((Join-Path $project '_work') + '\', [StringComparison]::OrdinalIgnoreCase)) {
    throw 'Task must remain inside project _work.'
}
[IO.Directory]::CreateDirectory($task) | Out-Null
$jdk = Join-Path $env:APPDATA '.minecraft\runtime\java-runtime-gamma-snapshot\bin'
$libraries = Join-Path $env:APPDATA '.minecraft\libraries'
function Hash([string]$file) { return (Get-FileHash -LiteralPath $file -Algorithm SHA256).Hash.ToLowerInvariant() }
function Run([string]$program, [string[]]$arguments) {
    & $program @arguments
    if ($LASTEXITCODE -ne 0) { throw "Command failed ($LASTEXITCODE): $program" }
}
$baselineHash = Hash $baseline
$backup = Join-Path $task 'baseline-qys-bosses.jar'
if (Test-Path -LiteralPath $backup) {
    if ((Hash $backup) -ne $baselineHash) { throw 'Baseline changed; use a fresh task directory.' }
} else {
    Copy-Item -LiteralPath $baseline -Destination $backup
}
$payload = Join-Path $task 'payload'
$classes = Join-Path $task 'classes'
$fixtures = Join-Path $task 'fixtures'
$beforeFixtures = Join-Path $task 'baseline-fixtures'
foreach ($directory in @($payload, $classes, $fixtures, $beforeFixtures)) {
    [IO.Directory]::CreateDirectory($directory) | Out-Null
}
$asm = @('asm', 'asm-tree', 'asm-analysis' | ForEach-Object { Join-Path $libraries "org\ow2\asm\$_\9.7.1\$_-9.7.1.jar" })
$gson = @(Get-ChildItem -LiteralPath (Join-Path $libraries 'com\google\code\gson\gson') -Recurse -Filter '*.jar' |
    Where-Object { $_.Name -notmatch 'sources|javadoc' } | Sort-Object FullName | Select-Object -Last 1 -ExpandProperty FullName)
$joml = @(Get-ChildItem -LiteralPath (Join-Path $libraries 'org\joml\joml') -Recurse -Filter '*.jar' |
    Where-Object { $_.Name -notmatch 'sources|javadoc' } | Sort-Object FullName | Select-Object -Last 1 -ExpandProperty FullName)
if ($gson.Count -ne 1 -or $joml.Count -ne 1) { throw 'Gson and JOML runtime libraries required.' }
$helper = Join-Path $project 'src\main\java\net\farmingtales\ftboss\client\render\GongdiAnimationPriority.java'
$patcher = Join-Path $project 'tools\runtime_patches\GongdiAnimationPriorityPatch.java'
$checks = Join-Path $project 'tools\verification\GongdiAnimationPriorityCheck.java'
Run (Join-Path $jdk 'javac.exe') @('--release', '17', '-encoding', 'UTF-8', '-d', $payload, $helper)
Run (Join-Path $jdk 'javac.exe') @('--release', '17', '-encoding', 'UTF-8', '-cp', ($asm -join ';'), '-d', $classes,
    $patcher, (Join-Path $project 'tools\verification\ZipStreamCheck.java'))
$toolClasspath = (@($classes) + $asm) -join ';'
Run (Join-Path $jdk 'java.exe') @('-Xverify:all', '-cp', $toolClasspath, 'GongdiAnimationPriorityPatch', 'patch', $backup, $payload)
$candidate = Join-Path $task 'qys-bosses.jar'
Copy-Item -LiteralPath $backup -Destination $candidate -Force
Run (Join-Path $jdk 'jar.exe') @('--update', '--file', $candidate, '-C', $payload, '.')
Run (Join-Path $jdk 'java.exe') @('-Xverify:all', '-cp', $toolClasspath, 'GongdiAnimationPriorityPatch', 'verify', $candidate, $backup)
Run (Join-Path $jdk 'java.exe') @('-Xverify:all', '-cp', $toolClasspath, 'GongdiAnimationPriorityPatch', 'fixture', $candidate, $fixtures)
Run (Join-Path $jdk 'java.exe') @('-Xverify:all', '-cp', $toolClasspath, 'GongdiAnimationPriorityPatch', 'fixture', $backup, $beforeFixtures)
$fixtureSources = @(Get-ChildItem -LiteralPath (Join-Path $project 'tools\verification\gongdi_animation_fixtures') -Recurse -Filter '*.java' |
    Sort-Object FullName | Select-Object -ExpandProperty FullName)
$testClasspath = (@($fixtures, $candidate) + $gson + $joml) -join ';'
Run (Join-Path $jdk 'javac.exe') (@('--release', '17', '-encoding', 'UTF-8', '-cp', $testClasspath, '-d', $fixtures, $checks) + $fixtureSources)
$beforeClasspath = (@($beforeFixtures, $fixtures, $backup, $payload) + $gson + $joml) -join ';'
foreach ($mode in @('timeline', 'arbitration')) {
    $failure = & (Join-Path $jdk 'java.exe') -Xverify:all -cp $beforeClasspath GongdiAnimationPriorityCheck $task $mode 2>&1
    $exitCode = $LASTEXITCODE
    $text = $failure -join "`n"
    [IO.File]::WriteAllText((Join-Path $task "baseline-$mode.log"), $text + "`n", [Text.UTF8Encoding]::new($false))
    if ($exitCode -eq 0 -or $text -notmatch 'AssertionError: (Ordinary attack must activate|Released native cue must not cover)') {
        throw "Baseline did not reproduce the expected $mode regression: $text"
    }
    Write-Output "PASS: baseline reproduces $mode regression"
}
Run (Join-Path $jdk 'java.exe') @('-Xverify:all', '-cp', $testClasspath, 'GongdiAnimationPriorityCheck', $task)
Run (Join-Path $jdk 'java.exe') @('-Xverify:all', '-cp', $toolClasspath, 'ZipStreamCheck', $candidate)
Run 'node' @((Join-Path $project 'tools\verification\check_jar_zip_rules.cjs'), $candidate)
Add-Type -AssemblyName System.IO.Compression.FileSystem
function EntryHash($entry) {
    $stream = $entry.Open()
    $algorithm = [Security.Cryptography.SHA256]::Create()
    try { return [BitConverter]::ToString($algorithm.ComputeHash($stream)) } finally { $stream.Dispose(); $algorithm.Dispose() }
}
$changed = @('net/farmingtales/ftboss/client/render/GongdiHumanoidRenderer.class',
    'net/farmingtales/ftboss/client/render/GongdiHumanoidTimeline.class')
$added = 'net/farmingtales/ftboss/client/render/GongdiAnimationPriority.class'
$before = [IO.Compression.ZipFile]::OpenRead($backup)
$after = [IO.Compression.ZipFile]::OpenRead($candidate)
try {
    if ($after.Entries.Count -ne $before.Entries.Count + 1) { throw 'Unexpected archive entry count.' }
    if (@($after.Entries | Group-Object FullName | Where-Object Count -gt 1).Count) { throw 'Duplicate archive entries.' }
    if (-not $after.GetEntry($added) -or $before.GetEntry($added)) { throw 'Unexpected helper entry.' }
    foreach ($entry in $before.Entries) {
        $replacement = $after.GetEntry($entry.FullName)
        if (-not $replacement) { throw "Missing runtime entry: $($entry.FullName)" }
        $equal = (EntryHash $entry) -eq (EntryHash $replacement)
        if ($changed -contains $entry.FullName) {
            if ($equal) { throw "Expected class did not change: $($entry.FullName)" }
        } elseif (-not $equal) {
            throw "Unrelated runtime entry changed: $($entry.FullName)"
        }
    }
    $preserved = $before.Entries.Count - $changed.Count
    foreach ($asset in @('fx/gongdi_basic_attacks.json', 'textures/entity/easter/gongdi.png', 'textures/item/black_cleaver_geo.png')) {
        $entry = $after.GetEntry('assets/ftboss/' + $asset)
        if (-not $entry) { throw "Missing preview asset: $asset" }
        $destination = Join-Path $task ('preview-assets/' + $asset)
        [IO.Directory]::CreateDirectory((Split-Path -Parent $destination)) | Out-Null
        [IO.Compression.ZipFileExtensions]::ExtractToFile($entry, $destination, $true)
    }
} finally {
    $before.Dispose()
    $after.Dispose()
}
Run 'node' @((Join-Path $project 'tools\verification\preview_gongdi_animation_priority.cjs'), $task)
if ((Hash $baseline) -ne $baselineHash -or (Hash $backup) -ne $baselineHash) { throw 'Installed baseline changed during verification.' }
$sourceFiles = @($helper, $patcher, $checks, $PSCommandPath,
    (Join-Path $project 'tools\verification\preview_gongdi_animation_priority.cjs'),
    (Join-Path $project 'tools\deploy_gongdi_animation_priority.ps1')) + $fixtureSources
$sources = [ordered]@{}
foreach ($file in $sourceFiles) { $sources[$file] = Hash $file }
$report = [ordered]@{
    status = 'PASS_OFFLINE'; baseline = $baseline; baselineSha256 = $baselineHash;
    candidate = $candidate; candidateSha256 = (Hash $candidate); sources = $sources;
    changedEntries = $changed; addedEntry = $added; preservedRuntimeEntries = $preserved;
    behavior = (Get-Content -LiteralPath (Join-Path $task 'behavior.json') -Raw | ConvertFrom-Json);
    baselineTimelineRegressionReproduced = $true; baselinePriorityRegressionReproduced = $true;
    preview = (Join-Path $task 'animation-four-views.png'); previewVisuallyReviewed = $false;
    gameplayTested = $false; docsTouched = $false; docsImpact = 'none'; savesTouched = $false
}
[IO.File]::WriteAllText((Join-Path $task 'verification.json'), ($report | ConvertTo-Json -Depth 8) + "`n", [Text.UTF8Encoding]::new($false))
Write-Output "PASS: $preserved runtime entries preserved; candidate: $candidate"
