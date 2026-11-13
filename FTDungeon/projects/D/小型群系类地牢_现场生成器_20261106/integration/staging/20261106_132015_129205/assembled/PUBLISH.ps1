$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
foreach ($log in @('build-final.log','selftest-final.log','registry-validation-final.log','world-final.log','restart-prepare-pass.log','restart-resume.log')) {
    if (-not (Select-String -LiteralPath (Join-Path $root $log) -Pattern 'BUILD SUCCESSFUL' -Quiet)) { throw "Not a passed build/run: $log" }
}
if (-not (Select-String -LiteralPath (Join-Path $root 'selftest-final.log') -Pattern 'PASS 21 atomic' -Quiet)) { throw 'Self-test assertion marker missing' }
if (-not (Select-String -LiteralPath (Join-Path $root 'registry-validation-final.log') -Pattern 'RUNTIME_VALIDATION_PASS' -Quiet)) { throw 'Actual Forge registry test marker missing' }
$worldLog = Get-Content -LiteralPath (Join-Path $root 'world-final.log') -Raw
if ($worldLog -notmatch 'All 2 required tests passed' -or $worldLog -notmatch 'physical_flat_return_route=PASS tickets_released=PASS' -or $worldLog -notmatch 'RUNTIME_WORLD_PASS occupied_preflight_zero_writes') { throw 'Actual world assertions missing or zero tests' }
$prepare = Get-Content -LiteralPath (Join-Path $root 'restart-prepare-pass.log') -Raw
$resume = Get-Content -LiteralPath (Join-Path $root 'restart-resume.log') -Raw
$prepared = [regex]::Match($prepare, 'RUNTIME_RESTART_PREPARED process=(\d+) (ftd-[a-f0-9]+).*operationHash=([a-f0-9]{64})')
$resumed = [regex]::Match($resume, 'RUNTIME_RESTART_PASS process=(\d+) id=(ftd-[a-f0-9]+).*operationHash=([a-f0-9]{64})')
if (-not $prepared.Success -or -not $resumed.Success -or $prepared.Groups[1].Value -eq $resumed.Groups[1].Value -or $prepared.Groups[2].Value -ne $resumed.Groups[2].Value -or $prepared.Groups[3].Value -ne $resumed.Groups[3].Value) { throw 'Distinct process/same instance and operation hash proof missing' }
'PASS 2/2 real Forge world tests: operator commands, dry-run, occupied preflight zero writes, exact readback, pause/revalidation, committed idempotency, collision/floor return path, ticket release. Separate-process clean interrupted recovery PASS; forced kill/powerloss NOT_RUN.' | Set-Content -LiteralPath (Join-Path $root 'world-test-result.txt') -Encoding utf8
& (Join-Path $root 'VERIFY-JAR.ps1')
$worldEvidence = @()
foreach ($relativeWorld in @('run/gametest-final/world','run/restart-gametest/world')) {
    $worldRoot = Join-Path $root $relativeWorld
    foreach ($item in (Get-ChildItem -LiteralPath $worldRoot -Recurse -File | Where-Object { $_.Name -eq 'level.dat' -or $_.Extension -eq '.mca' -or $_.FullName.Contains('ft_biome_dungeon_generator') })) {
        $worldEvidence += @{ path = [IO.Path]::GetRelativePath($root, $item.FullName).Replace('\','/'); bytes = $item.Length; sha256 = (Get-FileHash -LiteralPath $item.FullName -Algorithm SHA256).Hash.ToLowerInvariant() }
    }
}
@{ scope = 'Post-shutdown world and ownership journal hash evidence; worlds retained in runtime/run, not copied to integration staging'; files = $worldEvidence } | ConvertTo-Json -Depth 10 | Set-Content -LiteralPath (Join-Path $root 'world-evidence.json') -Encoding utf8
$files = [ordered]@{}
$inputs = @('build.gradle','settings.gradle','gradle.properties','BUILD.ps1','SNAPSHOT-CATALOG.ps1','PUBLISH.ps1','VERIFY-JAR.ps1','jar-audit.json','world-evidence.json','README.md','catalog-snapshot.json','progress.json','ISSUES.json','build-final.log','selftest-final.log','registry-validation-final.log','world-final.log','world-test-result.txt','restart-prepare-pass.log','restart-resume.log','amethyst-class-audit.log')
$inputs += Get-ChildItem -LiteralPath (Join-Path $root 'src') -File -Recurse | ForEach-Object { [IO.Path]::GetRelativePath($root, $_.FullName) }
$inputs += Get-ChildItem -LiteralPath (Join-Path $root 'gradle') -File -Recurse | ForEach-Object { [IO.Path]::GetRelativePath($root, $_.FullName) }
$inputs += 'build\libs\ft-biome-dungeon-generator-forge-1.20.1-0.1.0-candidate.jar'
foreach ($relative in ($inputs | Sort-Object -Unique)) {
    $path = Join-Path $root $relative
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { throw "Missing frozen input $path" }
    $files[$relative.Replace('\','/')] = (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash.ToLowerInvariant()
}
$core = Get-Content -LiteralPath (Join-Path $root '..\core\ready.json') -Raw | ConvertFrom-Json
foreach ($source in $core.sources) {
    $actual = (Get-FileHash -LiteralPath (Join-Path $root ('..\core\' + $source.path)) -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($actual -ne $source.sha256) { throw 'Real core changed after ready' }
}
$ready = [ordered]@{
    component = 'runtime'; apiVersion = 1; ready = $true; status = 'READY_ISOLATED_CANDIDATE'
    componentRoot = $root; sourceRoot = 'src/main/java'; artifact = 'build/libs/ft-biome-dungeon-generator-forge-1.20.1-0.1.0-candidate.jar'
    artifactSha256 = $files['build/libs/ft-biome-dungeon-generator-forge-1.20.1-0.1.0-candidate.jar']
    files = $files
    coreReadySha256 = (Get-FileHash -LiteralPath (Join-Path $root '..\core\ready.json') -Algorithm SHA256).Hash.ToLowerInvariant()
    coreSourceHashes = $core.sources
    catalogSnapshot = 'catalog-snapshot.json'
    commands = @('plan <x> <y> <z> <biome> <rooms> <ordinal>','build <id> CONFIRM_ISOLATED_AIRSPACE','pause <id>','status <id>','inspect <id>','list')
    tests = @(
        @{ command = '.\BUILD.ps1 -Task build -Offline'; result = 'PASS'; log = 'build-final.log' },
        @{ command = '.\BUILD.ps1 -Task runtimeSelfTest -Offline'; result = 'PASS 21 atomic/journal/recovery-policy assertions'; log = 'selftest-final.log' },
        @{ command = '.\BUILD.ps1 -Task runData -Offline'; result = 'See exact RUNTIME_VALIDATION_PASS count in log; real Forge registry/core/catalog'; log = 'registry-validation-final.log' },
        @{ command = '.\BUILD.ps1 -Task runGameTestServer -Offline -GradleOptions -PgameTestDir=run/gametest-final'; result = (Get-Content -LiteralPath (Join-Path $root 'world-test-result.txt') -Raw).Trim(); log = 'world-final.log' },
        @{ command = 'runGameTestServer -PrestartStage=prepare then resume -PgameTestDir=run/restart-gametest'; result = 'See RUNTIME_RESTART_PREPARED/LOADED/PASS and distinct process IDs'; logs = @('restart-prepare-pass.log','restart-resume.log') }
    )
    limitations = @('Isolated high-altitude entirely air-only world; explicit marker and operator confirmation required','Only two unchanged accepted normal S8 room types','Entity and block-entity payloads rejected; no restoration support','No ACID transaction or rollback guarantee','Additional mods rejected; no third-party claims integration or operator direct-write interception','Clean interrupted separate-process restart tested; forced-kill and powerloss NOT_RUN','Collision/floor return-route check only certifies tested actual layout; full combat/entrance progression not certified','Source-run world tests; reobfuscated candidate not deployed or installed into a separate release server')
}
$ready | ConvertTo-Json -Depth 30 | Set-Content -LiteralPath (Join-Path $root 'ready.json') -Encoding utf8
Write-Host "Published runtime frozen candidate: $($ready.artifactSha256)"
