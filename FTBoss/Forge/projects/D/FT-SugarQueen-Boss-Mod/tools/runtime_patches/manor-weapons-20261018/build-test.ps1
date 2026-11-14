[CmdletBinding()]
param(
    [string]$Baseline = (Join-Path $PSScriptRoot 'baseline.jar'),
    [string]$Candidate = (Join-Path $PSScriptRoot 'candidate.jar'),
    [switch]$SkipCompile
)
$ErrorActionPreference = 'Stop'
$libraries = Join-Path $env:APPDATA '.minecraft\libraries'
$dependencies = @(
    (Join-Path $libraries 'org\ow2\asm\asm\9.7.1\asm-9.7.1.jar'),
    (Join-Path $libraries 'org\ow2\asm\asm-tree\9.7.1\asm-tree-9.7.1.jar'),
    (Join-Path $libraries 'org\ow2\asm\asm-commons\9.7.1\asm-commons-9.7.1.jar'),
    (Join-Path $libraries 'org\ow2\asm\asm-analysis\9.7.1\asm-analysis-9.7.1.jar'),
    (Join-Path $libraries 'com\google\code\gson\gson\2.10\gson-2.10.jar'),
    (Join-Path $libraries 'com\google\guava\guava\31.1-jre\guava-31.1-jre.jar')
)
$classes = Join-Path $PSScriptRoot 'classes'
$classpath = $classes + ';' + ($dependencies -join ';')
$java17 = Join-Path $env:APPDATA '.minecraft\runtime\java-runtime-gamma-snapshot\bin\java.exe'
if (-not $SkipCompile) {
    New-Item -ItemType Directory -Path $classes -Force | Out-Null
    $sources = @(Get-ChildItem -LiteralPath $PSScriptRoot -File -Filter '*.java')
    $rules = Join-Path $PSScriptRoot 'src\main\java\net\farmingtales\ftboss\util\ManorWeaponBalanceRules.java'
    & javac --release 17 -encoding UTF-8 -proc:none -cp ($classpath + ';' + (Join-Path $PSScriptRoot 'baseline-classes')) -d $classes @($sources.FullName) $rules
    if ($LASTEXITCODE -ne 0) { throw 'Tool/rule compilation failed' }
    & java -cp $classpath ManorWeaponFixtures (Join-Path $PSScriptRoot 'fixtures-src')
    if ($LASTEXITCODE -ne 0) { throw 'Fixture source generation failed' }
    $fixtures = @(Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot 'fixtures-src') -File -Filter '*.java' -Recurse)
    $hooks = Join-Path $PSScriptRoot 'src\main\java\net\farmingtales\ftboss\item\ManorTridentDamageHooks.java'
    & javac --release 17 -encoding UTF-8 -proc:none -cp $classpath -d (Join-Path $classes 'donor') @($fixtures.FullName) $hooks
    if ($LASTEXITCODE -ne 0) { throw 'Fixture/donor compilation failed' }
}
& $java17 -Xverify:all -cp $classpath PatchManorWeapons $Baseline $Candidate (Join-Path $PSScriptRoot 'baseline.jar') $classes 2>&1 | Tee-Object -FilePath (Join-Path $PSScriptRoot 'merge.log')
if ($LASTEXITCODE -ne 0) { throw 'Scoped merge failed' }
& $java17 -Xverify:all -cp $classpath VerifyManorWeapons $Candidate $Baseline $PSScriptRoot $dependencies[-1] 2>&1 | Tee-Object -FilePath (Join-Path $PSScriptRoot 'verification.log')
if ($LASTEXITCODE -ne 0) { throw 'Weapon verification failed' }
& node (Join-Path $PSScriptRoot 'check_jar_zip_rules.cjs') $Candidate 2>&1 | Tee-Object -FilePath (Join-Path $PSScriptRoot 'zip-rules.log')
if ($LASTEXITCODE -ne 0) { throw 'ZIP header rules failed' }
& $java17 -cp $classes ZipStreamCheck $Candidate 2>&1 | Tee-Object -FilePath (Join-Path $PSScriptRoot 'zip-stream.log')
if ($LASTEXITCODE -ne 0) { throw 'ZIP stream verification failed' }
$previous = Join-Path (Split-Path -Parent $PSScriptRoot) '庄园Boss_护甲减半与移除麻雀百分比治疗_20261018'
& $java17 -Xverify:all -cp ((Join-Path $previous 'classes') + ';' + ($dependencies -join ';')) VerifyManorArmorAndSupport $Candidate (Join-Path $previous 'baseline.jar') $PSScriptRoot 2>&1 | Tee-Object -FilePath (Join-Path $PSScriptRoot 'armor-support-regression.log')
if ($LASTEXITCODE -ne 0) { throw 'Armor/support regression failed' }
$entryClasses = Join-Path (Split-Path -Parent $PSScriptRoot) '庄园Boss_进场前复制Buff_20261015\classes'
& $java17 -Xverify:all -cp ($entryClasses + ';' + ($dependencies -join ';')) VerifyManorEntryBuffs $Candidate $PSScriptRoot 2>&1 | Tee-Object -FilePath (Join-Path $PSScriptRoot 'entry-regression.log')
if ($LASTEXITCODE -ne 0) { throw 'Entry Buff regression failed' }
$domainClasses = Join-Path (Split-Path -Parent $PSScriptRoot) '庄园领域_红圈判定与强化_20261013\classes'
& $java17 -Xverify:all -cp ($domainClasses + ';' + ($dependencies -join ';')) VerifyDomainClientLifecycle $Candidate $PSScriptRoot 2>&1 | Tee-Object -FilePath (Join-Path $PSScriptRoot 'domain-client-regression.log')
if ($LASTEXITCODE -ne 0) { throw 'Domain client regression failed' }
