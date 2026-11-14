param([Parameter(Mandatory = $true)][string]$TaskPath)
$ErrorActionPreference = 'Stop'
$project = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\..\..'))
$task = [IO.Path]::GetFullPath($TaskPath)
$workRoot = [IO.Path]::GetFullPath((Join-Path $project '_work')) + [IO.Path]::DirectorySeparatorChar
if (-not $task.StartsWith($workRoot, [StringComparison]::OrdinalIgnoreCase)) { throw 'Task path escapes workspace.' }
$jdk = Join-Path $env:APPDATA '.minecraft\runtime\java-runtime-gamma-snapshot\bin'
$dependencies = Join-Path $project '_work\zicai-theme-zheergen-trail-20261025\compile-dependencies.jar'
$runtime = Join-Path $project '_work\equipment-attribute-slots-20261027-f921f9fce0\runtime-classpath.jar'
$classes = Join-Path $task 'tools-classes'
$baseline = Join-Path $task 'baseline.jar'
$candidate = Join-Path $task 'qys-bosses.jar'
$selected = Join-Path $task 'selected-classes'
$sources = @(
    'src/main/java/net/farmingtales/ftboss/FTBossMod.java',
    'src/main/java/net/farmingtales/ftboss/boss/BossHandler.java',
    'src/main/java/net/farmingtales/ftboss/boss/SugarQueenBoss.java',
    'src/main/java/net/farmingtales/ftboss/boss/MeatBoss.java',
    'tools/verification/check_trade_config.cjs',
    'tools/runtime_patches/RemoveLegacyStatueTradePatch.java',
    'tools/runtime_patches/legacy-statue-retirement/verify.ps1',
    'tools/runtime_patches/legacy-statue-retirement/deploy.ps1'
)
$absent = @(
    'src/main/java/net/farmingtales/ftboss/trade/TradeHandler.java',
    'src/main/resources/data/ftboss/loot_tables/blocks/statue.json',
    'src/main/resources/data/ftboss/loot_tables/blocks/butcher_statue.json'
)
function Hash([string]$File) { return (Get-FileHash -LiteralPath $File -Algorithm SHA256).Hash.ToLowerInvariant() }
function CheckExit([string]$Operation) { if ($LASTEXITCODE -ne 0) { throw "$Operation failed: $LASTEXITCODE" } }
$sourceHashes = [ordered]@{}
foreach ($relative in $sources) { $sourceHashes[$relative] = Hash (Join-Path $project $relative) }
New-Item -ItemType Directory -Path $classes -Force | Out-Null
& (Join-Path $jdk 'javac.exe') -proc:none -encoding UTF-8 -cp $dependencies -d $classes (Join-Path $project 'tools/runtime_patches/RemoveLegacyStatueTradePatch.java') (Join-Path $project 'tools/verification/FoodWeaponBuildLinkCheck.java') (Join-Path $project 'tools/verification/ZipStreamCheck.java')
CheckExit 'Patch tool compilation'
& (Join-Path $jdk 'java.exe') -Xverify:all -cp "$classes;$dependencies" RemoveLegacyStatueTradePatch $baseline $candidate $selected (Join-Path $task 'runtime-verification.txt')
CheckExit 'Runtime patch and payload preservation'
& (Join-Path $jdk 'java.exe') -Xverify:all -cp "$candidate;$classes;$runtime" FoodWeaponBuildLinkCheck $selected $candidate 2>&1 | Tee-Object -FilePath (Join-Path $task 'strict-link-check.log')
CheckExit 'Strict member linking'
& (Join-Path $jdk 'java.exe') -cp $classes ZipStreamCheck $candidate
CheckExit 'ZIP stream verification'
foreach ($test in @('check_trade_config.cjs', 'check_balance_nerfs.cjs', 'check_sugar_queen_summons.cjs')) {
    & node (Join-Path $project "tools/verification/$test")
    CheckExit $test
}
& node (Join-Path $project 'tools/verification/check_jar_zip_rules.cjs') $candidate
CheckExit 'Forge ZIP rules'
foreach ($relative in $sources) {
    if ((Hash (Join-Path $project $relative)) -ne $sourceHashes[$relative]) { throw "Source changed during verification: $relative" }
}
foreach ($relative in $absent) {
    if (Test-Path -LiteralPath (Join-Path $project $relative)) { throw "Retired source restored: $relative" }
}
[ordered]@{
    passed = $true
    baselineSha256 = Hash $baseline
    candidateSha256 = Hash $candidate
    configSha256 = Hash (Join-Path $task 'ftboss_trades.json.backup')
    sources = $sourceHashes
    absent = $absent
    changedClassCount = 3
    preservedPayloads = [int]([regex]::Match((Get-Content -Raw -LiteralPath (Join-Path $task 'runtime-verification.txt')), 'payloads: (\d+)').Groups[1].Value)
    existingTestLimitations = @('check_boss_drop_minions.cjs expects spawnAtLocation, but unchanged BossMercy uses NonManorBossLootRouter.spawnAtBoss; legacy fixture fails before this task touches its inputs.')
    gameplayTested = $false
    fullGradleBuild = $false
    buildMode = 'Fresh runtime-baseline archive; exact bytecode retirement only; no stale Gradle output copied'
} | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath (Join-Path $task 'build-report.json') -Encoding utf8
Write-Output "VERIFIED: $candidate"
