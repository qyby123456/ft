param([string]$Flavor='rarity02')
$ErrorActionPreference = 'Stop'
$project = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../..'))
$task = Join-Path $project '_work/ingot-material-parts-20261020'
$instance = Join-Path $env:APPDATA ('.minecraft/versions/FarmingTales_Forge'+[char]0x2161+' v1.5.2')
$target = Join-Path $instance 'mods/qys-bosses.jar'
$candidate = Join-Path $task 'qys-bosses.jar'
$baseline = Join-Path $task 'baseline/qys-bosses.jar'
$jdk = Join-Path $env:APPDATA '.minecraft/runtime/java-runtime-gamma-snapshot/bin'
$run = Join-Path $task ('client-'+$Flavor+'/evidence')
$matrix = Get-Content -LiteralPath (Join-Path $run 'ENVIRONMENT_MATERIAL_RESULT.json') -Raw -Encoding UTF8 | ConvertFrom-Json
$ui = Get-Content -LiteralPath (Join-Path $run 'UI_SWITCH_RESULT.json') -Raw -Encoding UTF8 | ConvertFrom-Json
$rules = Get-Content -LiteralPath (Join-Path $run 'RULE_CACHE_RESULT.json') -Raw -Encoding UTF8 | ConvertFrom-Json
$bow = Get-Content -LiteralPath (Join-Path $run 'BOW_LOAD.json') -Raw -Encoding UTF8 | ConvertFrom-Json
$pure = Get-Content -LiteralPath (Join-Path $task 'evidence/INGOT_RULES.json') -Raw -Encoding UTF8 | ConvertFrom-Json
$bonus = Get-Content -LiteralPath (Join-Path $run 'MATERIAL_BONUS_RESULT.json') -Raw -Encoding UTF8 | ConvertFrom-Json
$equipped = Get-Content -LiteralPath (Join-Path $run 'EQUIPPED_BONUS_RESULT.json') -Raw -Encoding UTF8 | ConvertFrom-Json
$bonusPure = Get-Content -LiteralPath (Join-Path $task 'evidence/BONUS_PURE.json') -Raw -Encoding UTF8 | ConvertFrom-Json
$parity = Get-Content -LiteralPath (Join-Path $task 'evidence/MOD_BINARY_PARITY.json') -Raw -Encoding UTF8 | ConvertFrom-Json
if ($matrix.status -ne 'PASS_REGISTERED_MATERIAL_INSTALL_REMOVE_NBT_AND_GUARDS' -or @($matrix.matrix).Count -ne 9 -or @($matrix.matrix | Where-Object { $_.status -ne 'PASS' -or $_.materials -ne 162 }).Count -or @($matrix.registeredMaterials).Count -ne 81 -or @($matrix.absentOptionalMaterials).Count -or $matrix.checks -lt 14000) { throw 'Material matrix incomplete' }
if ($pure.status -notlike 'PASS_*' -or $pure.checks -lt 350 -or $pure.addedMaterials -ne 35 -or $pure.existingIngots -ne 9) { throw 'Pure ingot rules incomplete' }
if ($parity.status -ne 'PASS_ALL_OTHER_MODS_IDENTICAL') { throw 'Other mods differ' }
if ($bonus.status -ne 'PASS_REAL_ATTRIBUTE_EVENTS_RARITY_AND_INVALIDATION' -or $bonus.cases -ne 1458 -or $bonus.checks -lt 20000) { throw 'Material attribute event checks incomplete' }
if ($equipped.status -ne 'PASS_PLAYER_ATTRIBUTES_INSTALL_AND_REMOVE' -or $equipped.checks -ne 36) { throw 'Equipped player attributes incomplete' }
if ($bonusPure.status -ne 'PASS_RARITY_DEDUP_CAPS_WHITELIST' -or $bonusPure.checks -lt 80) { throw 'Pure bonus rules incomplete' }
if ($ui.status -ne 'PASS_REAL_CLIENT_UI_SWITCH_AND_RETURN' -or @($ui.cases).Count -ne 18 -or @($ui.cases | Where-Object { -not $_.returnedToHub -or $_.placeTakeReloadAndConservation -ne 'PASS' -or -not $_.offering }).Count) { throw 'Normal mouse UI incomplete' }
if (@($ui.cases | Where-Object { $_.dragAndRightClick -notin @('PASS','LEGACY_UNCHANGED') -or @($_.normalClickAckMs).Count -ne 5 -or $_.unchangedFrameReuse -ne 'PASS' }).Count) { throw 'Mouse regression incomplete' }
if ($bow.status -notlike 'PASS_*' -or @($bow.cases).Count -ne 28 -or @($bow.cases | Where-Object status -ne 'PASS').Count) { throw 'Bow/crossbow regression incomplete' }
if ($rules.status -notlike 'PASS_*' -or $rules.checks -lt 250) { throw 'Author cache checks incomplete' }
if ($ui.candidateSha256 -ne (Get-FileHash -LiteralPath $candidate -Algorithm SHA256).Hash.ToLowerInvariant()) { throw 'Tested candidate differs' }
$locks = [Collections.Generic.List[IO.FileStream]]::new()
try {
    foreach ($lock in @((Join-Path $project '.ftboss-source-deploy.lock'),(Join-Path $instance '.ftboss-152-deploy.lock'),(Join-Path (Split-Path -Parent $target) '.qys-bosses-install.lock'))) {
        $locks.Add([IO.File]::Open($lock,[IO.FileMode]::OpenOrCreate,[IO.FileAccess]::ReadWrite,[IO.FileShare]::None))
    }
    $game = @(Get-CimInstance Win32_Process -Filter "Name='java.exe' OR Name='javaw.exe'" | Where-Object { $_.CommandLine -match 'forgeclient|--gameDir|net\.minecraft\.client\.main' })
    if ($game.Count) { throw ('Minecraft still running: ' + ($game.ProcessId -join ',')) }
    $expected = (Get-FileHash -LiteralPath $baseline -Algorithm SHA256).Hash
    $hash = (Get-FileHash -LiteralPath $candidate -Algorithm SHA256).Hash
    if ((Get-FileHash -LiteralPath $target -Algorithm SHA256).Hash -ne $expected) { throw 'Baseline changed: rebase and retest required' }
    & (Join-Path $jdk 'java.exe') -Xverify:all -cp (Join-Path $task 'tools') SingleClassOverlay check $baseline $candidate
    if ($LASTEXITCODE) { throw 'Exact delta failed' }
    & (Join-Path $jdk 'java.exe') -Xverify:all -cp (Join-Path $task 'tools') ZipStreamCheck $candidate
    if ($LASTEXITCODE) { throw 'ZIP stream failed' }
    $release = Join-Path $task 'release'
    New-Item -ItemType Directory -Path $release -Force | Out-Null
    $backup = Join-Path $release 'qys-bosses.before.jar'
    if (Test-Path -LiteralPath $backup) { throw 'Immutable release already exists' }
    Copy-Item -LiteralPath $target -Destination $backup
    $artifact = Join-Path $release 'qys-bosses.jar'
    Copy-Item -LiteralPath $candidate -Destination $artifact
    & (Join-Path $project 'tools/deploy_jar.ps1') -NoBuild -ArtifactPath $artifact -TargetPath $target -ExpectedTargetHash $expected
    if ($LASTEXITCODE) { throw 'Deployment refused' }
    if ((Get-FileHash -LiteralPath $target -Algorithm SHA256).Hash -ne $hash) { throw 'Readback mismatch' }
    & (Join-Path $jdk 'java.exe') -Xverify:all -cp (Join-Path $task 'tools') SingleClassOverlay check $backup $target
    if ($LASTEXITCODE) { throw 'Readback delta failed' }
    $receipt = [ordered]@{status='DEPLOYED_AND_READBACK_VERIFIED';target=$target;sha256=$hash.ToLowerInvariant();baselineSha256=$expected.ToLowerInvariant();backup=$backup;addedRawMaterials=35;registeredMaterials=81;matrixCases=1458;matrixChecks=$matrix.checks;normalMouseCases=18;rarityBonusCases=$bonus.cases;rarityBonusChecks=$bonus.checks;equippedAttributeChecks=$equipped.checks;bonusPureChecks=$bonusPure.checks;bowCrossbowCases=28;pureChecks=$pure.checks;authorChecks=$rules.checks;productionWorldWrites=0;docsWrites=0;docsProposal='_work/docs-proposals/ingot-material-parts.md'}
    $text = $receipt | ConvertTo-Json -Depth 6
    [IO.File]::WriteAllText((Join-Path $release 'DEPLOYMENT.json'),$text+[char]10,[Text.UTF8Encoding]::new($false))
    Write-Output $text
} finally { foreach ($lock in $locks) { $lock.Dispose() } }
