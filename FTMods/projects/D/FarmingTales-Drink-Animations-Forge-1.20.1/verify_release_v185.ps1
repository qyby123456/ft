$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
Add-Type -AssemblyName System.IO.Compression.FileSystem
function Hash($path) { (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash.ToLowerInvariant() }
function ReadEntry($zip, $name) {
    $entry = $zip.GetEntry($name)
    if (!$entry) { throw "Missing JAR resource: $name" }
    $reader = [IO.StreamReader]::new($entry.Open())
    try { $reader.ReadToEnd() } finally { $reader.Dispose() }
}
$backup = 'backups/1.8.4-before-aged-wines'
$jar = 'build/libs/farmingtales_drink_anim-1.8.5.jar'
$priorJar = Join-Path $backup '[农场物语-饮用与检视动画]farmingtales_drink_anim-1.8.4.jar'
$previous = Get-Content -LiteralPath "$backup/verification.json" -Raw | ConvertFrom-Json
if ((Hash $priorJar) -ne $previous.sha256) { throw 'Previous JAR backup mismatch' }
$built = [IO.Compression.ZipFile]::OpenRead((Resolve-Path -LiteralPath $jar).Path)
$prior = [IO.Compression.ZipFile]::OpenRead((Resolve-Path -LiteralPath $priorJar).Path)
try {
    $profiles = @(ReadEntry $built 'assets/farmingtales_drink_anim/drink_profiles.json' | ConvertFrom-Json)
    $meta = ReadEntry $built 'META-INF/mods.toml'
    foreach ($value in @('version="1.8.5"','license="ARR"','authors="紫菜"','displayName="农场物语-饮用与检视动画"')) {
        if (!$meta.Contains($value)) { throw "Missing metadata: $value" }
    }
    $changes = @()
    foreach ($entry in $built.Entries | Where-Object { !$_.FullName.EndsWith('/') }) {
        $oldEntry = $prior.GetEntry($entry.FullName)
        $a = $entry.Open()
        $b = if ($oldEntry) { $oldEntry.Open() } else { $null }
        try {
            $same = $b -and ((Get-FileHash -InputStream $a).Hash -eq (Get-FileHash -InputStream $b).Hash)
            if (!$same) { $changes += $entry.FullName }
        } finally { $a.Dispose(); if ($b) { $b.Dispose() } }
    }
    if ($changes.Count -ne 2 -or $changes -notcontains 'META-INF/mods.toml' -or $changes -notcontains 'com/farmingtales/drinkanim/math/DrinkCatalog.class') {
        throw "Unexpected JAR differences: $changes"
    }
    if (@(Compare-Object @($prior.Entries.FullName) @($built.Entries.FullName)).Count) { throw 'Unexpected added or removed JAR entries' }
} finally { $built.Dispose(); $prior.Dispose() }
$registration = Get-Content -LiteralPath '../../../kubejs/startup_scripts/jiqi/aging_barrel.js' -Raw
$wineSection = [regex]::Match($registration, '(?s)const FTAGING_WINES = \[(.*?)\r?\n\];').Groups[1].Value
$registered = @([regex]::Matches($wineSection, 'name:\s*"([^"]+)"') | ForEach-Object { 'farmingtales:' + $_.Groups[1].Value })
$bottles = @($profiles | Where-Object style -ne 'CUP')
if ($bottles.Count -ne 24 -or @(Compare-Object @($bottles.id) $registered).Count) { throw 'Aged registration does not match the bottle catalog' }
$aliases = [ordered]@{}
foreach ($p in $bottles) { foreach ($suffix in @('_i','_g','_d')) { $aliases[$p.id + $suffix] = $p.id } }
$count = 0
foreach ($file in Get-ChildItem 'build/test-results/test/TEST-*.xml') {
    $suite = ([xml](Get-Content -LiteralPath $file.FullName -Raw)).testsuite
    if ([int]$suite.failures -or [int]$suite.errors -or [int]$suite.skipped) { throw "Test failure or skip: $($file.Name)" }
    $count += [int]$suite.tests
}
if ($count -ne 92) { throw "Expected 92 tests, got $count" }
if (!(Get-Content 'preview/build-1.8.5.log' -Raw).Contains('BUILD SUCCESSFUL')) { throw 'Build did not succeed' }
$jarTime = (Get-Item -LiteralPath $jar).LastWriteTimeUtc
if (@(Get-ChildItem src/main,src/test -Recurse -File | Where-Object LastWriteTimeUtc -gt $jarTime).Count) { throw 'Sources changed after build' }
$config = '../../../config/farmingtales_drink_anim-client.toml'
if ((Hash $config) -ne (Hash "$backup/farmingtales_drink_anim-client.toml")) { throw 'Client configuration changed' }
[ordered]@{
    version='1.8.5'; sha256=(Hash $jar); unit_tests=$count; profiles=$profiles; aged_aliases=$aliases
    changed_jar_entries=$changes; configuration_sha256=(Hash $config)
    soundVolume=0.80; inspectionVolumeMultiplier=1.70; liquidGainMultiplier=0.5
    blockbench=@(Get-Content 'blockbench/酒水扩展-v34/导出检查.json' -Raw | ConvertFrom-Json)
    build_log_sha256=(Hash 'preview/build-1.8.5.log'); client_retested=$false
    scope='1.8.5: 72 aged bottle variants share their canonical model, animation, liquid and decoration profiles. Item identity and original consumption remain intact. 92 unit tests passed. Only DrinkCatalog and version metadata differ from 1.8.4. No client or world launched; existing v34 models reused.'
} | ConvertTo-Json -Depth 20 | Set-Content -LiteralPath verification.json -Encoding utf8
Write-Output "Verified 1.8.5: $count tests, $($aliases.Count) aged variants, unchanged models and sounds."
