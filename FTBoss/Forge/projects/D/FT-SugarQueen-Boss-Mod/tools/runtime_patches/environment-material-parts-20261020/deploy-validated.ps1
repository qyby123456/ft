$ErrorActionPreference = 'Stop'
$project = 'D:\ft工程文件\FT-SugarQueen-Boss-Mod'
$task = Join-Path $project '_work/environment-material-parts-20261020'
$instance = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2'
$target = Join-Path $instance 'mods/qys-bosses.jar'
$jdk = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot\bin'
$candidate = Join-Path $task 'qys-bosses.jar'
$asm = @('asm','asm-tree','asm-commons','asm-util','asm-analysis') | ForEach-Object { 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\libraries\org\ow2\asm\'+$_+'\9.7.1\'+$_+'-9.7.1.jar' }
$classpath = (@((Join-Path $task 'tools')) + $asm) -join ';'
$run = Join-Path $task 'client-candidate06/evidence'
$matrix = Get-Content -LiteralPath (Join-Path $run 'ENVIRONMENT_MATERIAL_RESULT.json') -Raw -Encoding UTF8 | ConvertFrom-Json
$ui = Get-Content -LiteralPath (Join-Path $run 'UI_SWITCH_RESULT.json') -Raw -Encoding UTF8 | ConvertFrom-Json
$rules = Get-Content -LiteralPath (Join-Path $run 'RULE_CACHE_RESULT.json') -Raw -Encoding UTF8 | ConvertFrom-Json
$bow = Get-Content -LiteralPath (Join-Path $run 'BOW_LOAD.json') -Raw -Encoding UTF8 | ConvertFrom-Json
$pure = Get-Content -LiteralPath (Join-Path $task 'evidence/PURE_RULES.json') -Raw -Encoding UTF8 | ConvertFrom-Json
if ($matrix.status -ne 'PASS_REGISTERED_MATERIAL_INSTALL_REMOVE_NBT_AND_GUARDS' -or @($matrix.matrix).Count -ne 9 -or @($matrix.matrix | Where-Object { $_.status -ne 'PASS' -or $_.materials -ne 66 }).Count -or @($matrix.registeredMaterials).Count -ne 33 -or $matrix.checks -lt 5900) { throw 'Material matrix incomplete' }
if ($pure.status -notlike 'PASS_*' -or $pure.checks -lt 200 -or $pure.addedMaterials -ne 33) { throw 'Pure material rules incomplete' }
if ($ui.status -ne 'PASS_REAL_CLIENT_UI_SWITCH_AND_RETURN' -or @($ui.cases).Count -ne 18 -or @($ui.cases | Where-Object { -not $_.returnedToHub -or $_.placeTakeReloadAndConservation -ne 'PASS' -or $_.offering -ne 'irons_spellbooks:arcane_essence' }).Count) { throw 'Arcane essence normal mouse UI incomplete' }
if (@($ui.cases | Where-Object { $_.dragAndRightClick -notin @('PASS','LEGACY_UNCHANGED') -or @($_.normalClickAckMs).Count -ne 5 -or $_.unchangedFrameReuse -ne 'PASS' }).Count) { throw 'Mouse/presentation regression incomplete' }
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
    $expected = (Get-FileHash -LiteralPath (Join-Path $task 'baseline.jar') -Algorithm SHA256).Hash
    $hash = (Get-FileHash -LiteralPath $candidate -Algorithm SHA256).Hash
    if ((Get-FileHash -LiteralPath $target -Algorithm SHA256).Hash -ne $expected) { throw 'Baseline changed: rebase and retest required' }
    & (Join-Path $jdk 'java.exe') -Xverify:all -cp $classpath MethodPatch check (Join-Path $task 'baseline.jar') $candidate
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
    & (Join-Path $jdk 'java.exe') -Xverify:all -cp $classpath MethodPatch check $backup $target
    if ($LASTEXITCODE) { throw 'Readback delta failed' }
    $receipt = [ordered]@{status='DEPLOYED_AND_READBACK_VERIFIED';target=$target;sha256=$hash.ToLowerInvariant();baselineSha256=$expected.ToLowerInvariant();backup=$backup;explicitMaterialRules=33;registeredMaterials=@($matrix.registeredMaterials).Count;matrixCases=(@($matrix.registeredMaterials).Count*9*2);matrixChecks=$matrix.checks;arcaneEssenceMouseCases=18;bowCrossbowCases=28;pureChecks=$pure.checks;authorChecks=$rules.checks;productionWorldWrites=0;docsWrites=0;docsProposal='_work/docs-proposals/environment-material-parts.md'}
    $text = $receipt | ConvertTo-Json -Depth 6
    [IO.File]::WriteAllText((Join-Path $release 'DEPLOYMENT.json'),$text+"`n",[Text.UTF8Encoding]::new($false))
    Write-Output $text
} finally { foreach ($lock in $locks) { $lock.Dispose() } }
