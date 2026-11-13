$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
$lock = Get-Content -LiteralPath (Join-Path $root 'baseline-lock.json') -Raw | ConvertFrom-Json
$baselineReady = Join-Path $root '..\ready.json'
if ((Get-FileHash -LiteralPath $baselineReady -Algorithm SHA256).Hash.ToLowerInvariant() -ne $lock.baselineReadySha256) { throw 'Baseline ready changed' }
$baseline = Get-Content -LiteralPath $baselineReady -Raw | ConvertFrom-Json
foreach ($property in $baseline.files.PSObject.Properties) {
    if ((Get-FileHash -LiteralPath (Join-Path $root ('..\' + $property.Name)) -Algorithm SHA256).Hash.ToLowerInvariant() -ne $property.Value) { throw "Baseline frozen file changed: $($property.Name)" }
}
foreach ($name in @('payload-world-final.log','be-restart-prepare.log','be-restart-resume.log','build-v2-final.log','selftest-v2.log')) {
    if (-not (Select-String -LiteralPath (Join-Path $root $name) -Pattern 'BUILD SUCCESSFUL' -Quiet)) { throw "Incomplete/failed test: $name" }
}
$world = Get-Content -LiteralPath (Join-Path $root 'payload-world-final.log') -Raw
if ($world -notmatch 'All 2 required tests passed' -or $world -notmatch 'PAYLOAD_V2_WORLD_PASS.*completeCells=237000 preservedBE=96' -or $world -notmatch 'ROLE_BE_V2_WORLD_PASS.*inventoryConflict=QUARANTINED_ZERO_OVERWRITE') { throw 'Required original-room and actual-role world assertions missing' }
$prepared = [regex]::Match((Get-Content -LiteralPath (Join-Path $root 'be-restart-prepare.log') -Raw), 'BE_V2_RESTART_PREPARED process=(\d+) id=(ftd-[a-f0-9]+).*operationHash=([a-f0-9]{64})')
$resumed = [regex]::Match((Get-Content -LiteralPath (Join-Path $root 'be-restart-resume.log') -Raw), 'BE_V2_RESTART_PASS process=(\d+) id=(ftd-[a-f0-9]+).*operationHash=([a-f0-9]{64})')
if (-not $prepared.Success -or -not $resumed.Success -or $prepared.Groups[1].Value -eq $resumed.Groups[1].Value -or $prepared.Groups[2].Value -ne $resumed.Groups[2].Value -or $prepared.Groups[3].Value -ne $resumed.Groups[3].Value) { throw 'Separate BE process/same instance/hash proof missing' }
$jar = Join-Path $root 'build\libs\ft-biome-dungeon-generator-forge-1.20.1-0.2.0-experimental.jar'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [IO.Compression.ZipFile]::OpenRead($jar)
try {
    if (@($zip.Entries | Group-Object FullName | Where-Object Count -gt 1).Count -gt 0) { throw 'Duplicate JAR entries' }
    if (@($zip.Entries | Where-Object { $_.FullName -match '(GameTests|CandidateFixtures|RuntimeValidation|ftbd_payloadtest|ftbd_berestart|ftbd_restarttest)' }).Count -gt 0) { throw 'Unaccepted fixtures or test injection code leaked into production JAR' }
    foreach ($entryName in @('cn/ft/biomedungeon/runtime/PayloadPolicy.class','cn/ft/biomedungeon/runtime/PayloadWorld.class','cn/ft/biomedungeon/core/DungeonPlanner.class')) {
        $entry = $zip.GetEntry($entryName)
        if ($null -eq $entry) { throw "Required production class missing: $entryName" }
        $stream = $entry.Open(); $memory = [IO.MemoryStream]::new()
        try { $stream.CopyTo($memory); $bytes = $memory.ToArray(); if (($bytes[6]*256 + $bytes[7]) -ne 61) { throw 'Not Java17 class' } }
        finally { $stream.Dispose(); $memory.Dispose() }
    }
    $jarAudit = @{ status = 'PASS'; sha256 = (Get-FileHash -LiteralPath $jar -Algorithm SHA256).Hash.ToLowerInvariant(); bytes = (Get-Item -LiteralPath $jar).Length; javaMajor = 61; testFixturesBundled = $false; originalCandidatePromotion = $false; worldScope = 'Source-run tests, not installed release JAR' }
    $jarAudit | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $root 'jar-audit.json') -Encoding utf8
} finally { $zip.Dispose() }
$evidence = @()
foreach ($relative in @('run/payload-v2-b/world','run/be-restart-v2/world')) {
    foreach ($item in (Get-ChildItem -LiteralPath (Join-Path $root $relative) -Recurse -File | Where-Object { $_.Name -eq 'level.dat' -or $_.Extension -eq '.mca' -or $_.FullName.Contains('ft_biome_dungeon_generator_v2') })) {
        $evidence += @{ path = [IO.Path]::GetRelativePath($root, $item.FullName).Replace('\','/'); bytes = $item.Length; sha256 = (Get-FileHash -LiteralPath $item.FullName -Algorithm SHA256).Hash.ToLowerInvariant() }
    }
}
@{ scope = 'Retained isolated world/native BE save/journal post-shutdown hashes'; files = $evidence } | ConvertTo-Json -Depth 10 | Set-Content -LiteralPath (Join-Path $root 'world-evidence.json') -Encoding utf8
$paths = @('build.gradle','settings.gradle','gradle.properties','BUILD.ps1','freeze-fixtures.py','PUBLISH-NEXT.ps1','baseline-lock.json','core-snapshot-ready.json','fixtures.json','README.md','progress.json','ISSUES.json','jar-audit.json','world-evidence.json','payload-world-final.log','be-restart-prepare.log','be-restart-resume.log','build-v2-final.log','selftest-v2.log','build/libs/ft-biome-dungeon-generator-forge-1.20.1-0.2.0-experimental.jar')
foreach ($folder in @('src','gradle','core-snapshot')) {
    $paths += Get-ChildItem -LiteralPath (Join-Path $root $folder) -File -Recurse | ForEach-Object { [IO.Path]::GetRelativePath($root, $_.FullName).Replace('\','/') }
}
$files = [ordered]@{}
foreach ($path in ($paths | Sort-Object -Unique)) { $files[$path] = (Get-FileHash -LiteralPath (Join-Path $root $path) -Algorithm SHA256).Hash.ToLowerInvariant() }
$ready = [ordered]@{
    component = 'runtime-next'; apiVersion = 1; runtimeJournalVersion = 2; ready = $true
    status = 'READY_CAPABILITY_ONLY_FULL_BIOME_BLOCKED'; completeGeneratorReady = $false; productionPromotion = $false
    baselineReadyUnchanged = $true; baselineFrozenFilesVerified = @($baseline.files.PSObject.Properties).Count
    artifact = 'build/libs/ft-biome-dungeon-generator-forge-1.20.1-0.2.0-experimental.jar'; artifactSha256 = $jarAudit.sha256
    files = $files
    tests = @(
        @{ command = '.\BUILD.ps1 -Task runGameTestServer -Offline -GradleOptions -PpayloadTest=true,-PgameTestDir=run/payload-v2-b'; result = 'PASS 2/2 actual world tests: 5 original full rooms x4 rotations,237000 cells,96BEs, native survival/delayed readback; actual original07 entrance+04 hall+09 treasury 146150cells22BEs, journal reload+inventory conflict quarantine'; log = 'payload-world-final.log' },
        @{ command = 'runGameTestServer -PbeRestartStage=prepare then resume -PgameTestDir=run/be-restart-v2'; result = 'PASS real distinct JVM BE-bearing interrupted save/resume; same instance and operation hash, full native state/BE readback'; processPrepare = $prepared.Groups[1].Value; processResume = $resumed.Groups[1].Value; logs = @('be-restart-prepare.log','be-restart-resume.log') },
        @{ command = '.\BUILD.ps1 -Task build -Offline'; result = 'PASS reobfuscated experimental Java17 JAR, separate test fixtures excluded'; log = 'build-v2-final.log' },
        @{ command = '.\BUILD.ps1 -Task runtimeSelfTest -Offline'; result = 'See actual 21 assertions marker in log'; log = 'selftest-v2.log' }
    )
    limitations = @('Full nine-room S8 not finished: actual S8-01 stair interface and S8-08 lava/vine blocked','Catalog candidates accepted=false remain unpromoted; production JAR packages only accepted baseline manifest','Only single chest/barrel/banner subset supported; no arbitrary nested item tags, loot tables, double chests, spawners or signs','No preexisting player BE rollback: target must be entirely air','Distinct-process clean interrupted BE recovery tested; force-kill/powerloss NOT_RUN','Five full-room fixtures native survival and delayed readback do not prove every far-away chunk random-ticked','Three-role actual assembly is not an all-nine-room/all-eight-biome release or combat/loot/route certification','World tests source-run; release JAR not installed into separate server')
}
$ready | ConvertTo-Json -Depth 20 | Set-Content -LiteralPath (Join-Path $root 'ready.json') -Encoding utf8
Write-Host "Published next capability candidate $($ready.artifactSha256); baseline untouched; full generator NOT complete"
