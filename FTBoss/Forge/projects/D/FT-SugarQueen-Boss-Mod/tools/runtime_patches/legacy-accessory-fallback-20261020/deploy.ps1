param([switch]$RebaseAndVerify)
$ErrorActionPreference = 'Stop'
$root = 'D:\ft工程文件\FT-SugarQueen-Boss-Mod'
$task = Join-Path $root '_work/legacy-accessory-fallback-20261020'
$runtime = Join-Path $env:APPDATA '.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2'
$target = Join-Path $runtime 'mods/qys-bosses.jar'
$artifact = Join-Path $task 'deploy-candidate.jar'
$baseline = Join-Path $task 'predeploy-baseline.jar'
$flavor = 'final'
$expected = (Get-FileHash -LiteralPath $baseline -Algorithm SHA256).Hash
$hash = (Get-FileHash -LiteralPath $artifact -Algorithm SHA256).Hash
$locks = @()
try {
    foreach ($lock in @((Join-Path $root '.ftboss-source-deploy.lock'), (Join-Path $runtime '.ftboss-152-deploy.lock'), (Join-Path $runtime 'mods/.qys-bosses-install.lock'))) {
        $locks += [IO.File]::Open($lock, [IO.FileMode]::OpenOrCreate, [IO.FileAccess]::ReadWrite, [IO.FileShare]::None)
    }
    $game = Get-CimInstance Win32_Process | Where-Object { $_.Name -match '^javaw?\.exe$' -and $_.CommandLine -match 'forgeclient|--gameDir|net\.minecraft\.client\.main' }
    if ($game) { throw "Minecraft is running: $($game.ProcessId -join ','); deployment not performed" }
    if ($RebaseAndVerify) {
        $baseline = Join-Path $task 'locked-baseline.jar'
        $artifact = Join-Path $task 'locked-candidate.jar'
        $flavor = 'locked'
        if (Test-Path -LiteralPath $baseline) { throw 'Locked baseline already exists; refusing overwrite' }
        Copy-Item -LiteralPath $target -Destination $baseline
        $expected = (Get-FileHash -LiteralPath $baseline -Algorithm SHA256).Hash
        $java = Join-Path $env:APPDATA '.minecraft/runtime/java-runtime-gamma-snapshot/bin/java.exe'
        $asm = @('asm','asm-tree','asm-commons','asm-util','asm-analysis') | ForEach-Object { Join-Path $env:APPDATA ".minecraft/libraries/org/ow2/asm/$_/9.7.1/$_-9.7.1.jar" }
        $classpath = (@((Join-Path $task 'tools')) + $asm) -join ';'
        & $java -Xverify:all -cp $classpath LegacyAccessoryPatch $baseline (Join-Path $task 'payload') $artifact 2>&1 | Tee-Object -FilePath (Join-Path $task 'evidence/locked-exact-delta.log')
        if ($LASTEXITCODE -ne 0) { throw 'Locked overlay verification failed' }
        & $java -Xverify:all -cp $classpath WeaponUiJarClosureCheck $artifact 'net/farmingtales/ftboss/client/StatueHubScreen' 2>&1 | Tee-Object -FilePath (Join-Path $task 'evidence/locked-closure.log')
        if ($LASTEXITCODE -ne 0) { throw 'Locked class closure failed' }
        & $java -cp $classpath ZipStreamCheck $artifact 2>&1 | Tee-Object -FilePath (Join-Path $task 'evidence/locked-zip-stream.log')
        if ($LASTEXITCODE -ne 0) { throw 'Locked ZIP stream failed' }
        & node (Join-Path $root 'tools/verification/check_jar_zip_rules.cjs') $artifact
        if ($LASTEXITCODE -ne 0) { throw 'Locked ZIP rules failed' }
        & node (Join-Path $PSScriptRoot 'prepare-probe.cjs') $flavor $artifact
        if ($LASTEXITCODE -ne 0) { throw 'Locked probe preparation failed' }
        & (Join-Path $task 'launch.ps1') -Flavor $flavor
        $process = Get-Content -LiteralPath (Join-Path $task 'client-locked/evidence/PROCESS.json') -Raw -Encoding UTF8 | ConvertFrom-Json
        Wait-Process -Id $process.pid -Timeout 1200
        $hash = (Get-FileHash -LiteralPath $artifact -Algorithm SHA256).Hash
    }
    if ((Get-FileHash -LiteralPath $target -Algorithm SHA256).Hash -ne $expected) { throw 'Target changed; rebase required' }
    $probe = Get-Content -LiteralPath (Join-Path $task ('client-'+$flavor+'/evidence/UI_SWITCH_RESULT.json')) -Raw -Encoding UTF8 | ConvertFrom-Json
    if ($probe.status -ne 'PASS' -or $probe.cases.Count -ne 12) { throw 'Final client acceptance is incomplete' }
    if ((Get-FileHash -LiteralPath (Join-Path $task ('client-'+$flavor+'/mods/qys-bosses.jar')) -Algorithm SHA256).Hash -ne $hash) { throw 'Final client did not test exact artifact' }
    Copy-Item -LiteralPath $target -Destination (Join-Path $task 'predeploy-backup.jar')
    & (Join-Path $root 'tools/deploy_jar.ps1') -NoBuild -ArtifactPath $artifact -TargetPath $target -ExpectedTargetHash $expected
    if ($LASTEXITCODE -ne 0) { throw 'Shared deployment script failed' }
    $actual = (Get-FileHash -LiteralPath $target -Algorithm SHA256).Hash
    if ($actual -ne $hash) { throw 'Deployment readback mismatch' }
    [ordered]@{status='DEPLOYED_SHA256_VERIFIED'; baseline=$expected; sha256=$actual; target=$target; artifact=$artifact; changedEntries=@('net/farmingtales/ftboss/client/StatueHubScreen.class'); changedMethods=@('openSwordCavity','openBuildAdjustment'); testedCases=$probe.cases.Count; checks=$probe.checks; completedAt=(Get-Date).ToString('o')} | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath (Join-Path $task 'evidence/DEPLOYMENT.json') -Encoding utf8
} finally {
    foreach ($lock in $locks) { $lock.Dispose() }
}
