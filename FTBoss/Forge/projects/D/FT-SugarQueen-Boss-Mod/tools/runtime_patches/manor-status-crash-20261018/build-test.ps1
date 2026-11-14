[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$Work,
    [string]$Baseline,
    [string]$Candidate,
    [switch]$SkipCompile
)
$ErrorActionPreference = 'Stop'
$project = 'D:\ft工程文件\FT-SugarQueen-Boss-Mod'
if (-not $Baseline) { $Baseline = Join-Path $Work 'baseline.jar' }
if (-not $Candidate) { $Candidate = Join-Path $Work 'candidate.jar' }
$libraries = Join-Path $env:APPDATA '.minecraft\libraries'
$asm = @('asm','asm-tree','asm-commons','asm-analysis') | ForEach-Object { Join-Path $libraries "org\ow2\asm\$_\9.7.1\$_-9.7.1.jar" }
$srg = 'C:\Users\YOUR_USERNAME\.gradle\caches\modules-2\files-2.1\net.minecraftforge\srgutils\0.4.15\ca408b131759478f164e010fae0d73997e125fb5\srgutils-0.4.15.jar'
$gson = Join-Path $libraries 'com\google\code\gson\gson\2.10\gson-2.10.jar'
$classes = Join-Path $Work 'classes'
$classpath = $classes + ';' + (($asm + $srg + $gson) -join ';')
$java17 = Join-Path $env:APPDATA '.minecraft\runtime\java-runtime-gamma-snapshot\bin\java.exe'
$mapped = Join-Path $project 'build\fg_cache\net\minecraftforge\forge\1.20.1-47.4.10_mapped_official_1.20.1\forge-1.20.1-47.4.10_mapped_official_1.20.1.jar'
if (-not $SkipCompile) {
    New-Item -ItemType Directory -Path $classes -Force | Out-Null
    $dependencies = @($mapped, (Join-Path $libraries 'net\minecraftforge\javafmllanguage\1.20.1-47.4.10\javafmllanguage-1.20.1-47.4.10.jar'), (Join-Path $project 'build\classes\java\main'))
    $dependencies += @(Get-ChildItem -LiteralPath (Join-Path $project 'libs') -File -Filter '*.jar' | Select-Object -ExpandProperty FullName)
    $dependencies += @(Get-ChildItem -LiteralPath $libraries -Recurse -File -Filter '*.jar' | Select-Object -ExpandProperty FullName)
    $sourceFiles = @('boss\ai\ManorStatusImmunity.java','integ\spells\SparrowBorrowedSpells.java','effect\HealBanEffect.java','boss\ai\ManorHealthBalance.java','util\ManorSupportRules.java') | ForEach-Object { Join-Path $Work ('src\main\java\net\farmingtales\ftboss\' + $_) }
    $arguments = @('--release','17','-encoding','UTF-8','-proc:none','-implicit:none','-sourcepath',(Join-Path $Work 'empty-sourcepath'),'-cp',($dependencies -join ';'),'-d',(Join-Path $Work 'compiled-official')) + $sourceFiles
    $encoded = $arguments | ForEach-Object { '"' + $_.Replace('\','/') + '"' }
    [IO.File]::WriteAllLines((Join-Path $Work 'javac.args'), $encoded, [Text.UTF8Encoding]::new($false))
    & javac '-J-Duser.language=en' '-J-Duser.country=US' ('@' + (Join-Path $Work 'javac.args'))
    if ($LASTEXITCODE -ne 0) { throw 'Production compilation failed' }
    $tools = @((Join-Path $PSScriptRoot 'PatchManorStatusCrash.java'), (Join-Path $project 'tools\runtime_patches\zicai-curio-silence-20261018\RemapSilence.java'), (Join-Path $project 'tools\runtime_patches\manor-entry-buffs\DomainVisualMethodMerge.java'), (Join-Path $project 'tools\verification\VerifyManorStatusCrash.java'), (Join-Path $project 'tools\verification\ZipStreamCheck.java'))
    & javac --release 17 -encoding UTF-8 -proc:none -cp $classpath -d $classes $tools
    if ($LASTEXITCODE -ne 0) { throw 'Tool compilation failed' }
    & $java17 -cp $classpath RemapSilence (Join-Path $project 'build\createMcpToSrg\output.tsrg') (Join-Path $Work 'compiled-official') (Join-Path $Work 'compiled-runtime') $mapped (Join-Path $project 'build\classes\java\main')
    if ($LASTEXITCODE -ne 0) { throw 'Runtime remap failed' }
}
& $java17 -Xverify:all -cp $classpath PatchManorStatusCrash $Baseline $Candidate (Join-Path $Work 'compiled-runtime') (Join-Path $Work 'baseline.jar') 2>&1 | Tee-Object -FilePath (Join-Path $Work 'merge.log')
if ($LASTEXITCODE -ne 0) { throw 'Runtime patch merge failed' }
& $java17 -Xverify:all -cp $classpath VerifyManorStatusCrash $Candidate $Baseline $Work 2>&1 | Tee-Object -FilePath (Join-Path $Work 'verification.log')
if ($LASTEXITCODE -ne 0) { throw 'Regression tests failed' }
& node (Join-Path $project 'tools\verification\check_jar_zip_rules.cjs') $Candidate 2>&1 | Tee-Object -FilePath (Join-Path $Work 'zip-rules.log')
if ($LASTEXITCODE -ne 0) { throw 'ZIP rules failed' }
& $java17 -cp $classes ZipStreamCheck $Candidate 2>&1 | Tee-Object -FilePath (Join-Path $Work 'zip-stream.log')
if ($LASTEXITCODE -ne 0) { throw 'ZIP stream failed' }
