[CmdletBinding()]
param(
    [string]$Baseline = (Join-Path $PSScriptRoot 'baseline.jar'),
    [string]$Candidate = (Join-Path $PSScriptRoot 'candidate.jar'),
    [switch]$SkipCompile,
    [string]$SourceRoot
)
$ErrorActionPreference = 'Stop'
$project = 'D:\ft工程文件\FT-SugarQueen-Boss-Mod'
if (-not $SourceRoot) {
    $SourceRoot = Join-Path $PSScriptRoot 'src\main\java'
    if (-not (Test-Path -LiteralPath $SourceRoot)) { $SourceRoot = Join-Path $project 'src\main\java' }
}
$libraries = Join-Path $env:APPDATA '.minecraft\libraries'
$asm = @('asm','asm-tree','asm-commons','asm-analysis') | ForEach-Object { Join-Path $libraries "org\ow2\asm\$_\9.7.1\$_-9.7.1.jar" }
$srg = 'C:\Users\YOUR_USERNAME\.gradle\caches\modules-2\files-2.1\net.minecraftforge\srgutils\0.4.15\ca408b131759478f164e010fae0d73997e125fb5\srgutils-0.4.15.jar'
$gson = Join-Path $libraries 'com\google\code\gson\gson\2.10\gson-2.10.jar'
$classes = Join-Path $PSScriptRoot 'classes'
$classpath = $classes + ';' + (($asm + $srg + $gson) -join ';')
$java17 = Join-Path $env:APPDATA '.minecraft\runtime\java-runtime-gamma-snapshot\bin\java.exe'
$mapped = Join-Path $project 'build\fg_cache\net\minecraftforge\forge\1.20.1-47.4.10_mapped_official_1.20.1\forge-1.20.1-47.4.10_mapped_official_1.20.1.jar'
if (-not $SkipCompile) {
    New-Item -ItemType Directory -Path $classes -Force | Out-Null
    $dependencies = @($mapped, (Join-Path $libraries 'net\minecraftforge\javafmllanguage\1.20.1-47.4.10\javafmllanguage-1.20.1-47.4.10.jar'), (Join-Path $project 'build\classes\java\main'))
    $dependencies += @(Get-ChildItem -LiteralPath (Join-Path $project 'libs') -File -Filter '*.jar' | Select-Object -ExpandProperty FullName)
    $dependencies += @(Get-ChildItem -LiteralPath $libraries -Recurse -File -Filter '*.jar' | Select-Object -ExpandProperty FullName)
    $sourceClasspath = $dependencies -join ';'
    $sourceFiles = @('net\farmingtales\ftboss\boss\ai\ZicaiCurioSilence.java','net\farmingtales\ftboss\util\ZicaiCurioSilenceRules.java') | ForEach-Object { Join-Path $SourceRoot $_ }
    $arguments = @('--release','17','-encoding','UTF-8','-proc:none','-implicit:none','-sourcepath',(Join-Path $PSScriptRoot 'empty-sourcepath'),'-cp',$sourceClasspath,'-d',(Join-Path $PSScriptRoot 'compiled-official')) + $sourceFiles
    $encoded = $arguments | ForEach-Object { '"' + $_.Replace('\','/') + '"' }
    [IO.File]::WriteAllLines((Join-Path $PSScriptRoot 'javac.args'), $encoded, [Text.UTF8Encoding]::new($false))
    & javac '-J-Duser.language=en' '-J-Duser.country=US' ('@' + (Join-Path $PSScriptRoot 'javac.args'))
    if ($LASTEXITCODE -ne 0) { throw 'Production compilation failed' }
    $tools = @(Get-ChildItem -LiteralPath $PSScriptRoot -File -Filter '*.java' | Select-Object -ExpandProperty FullName)
    if (-not (Test-Path -LiteralPath (Join-Path $PSScriptRoot 'VerifyZicaiSilence.java'))) { $tools += Join-Path $project 'tools\verification\VerifyZicaiSilence.java' }
    if (-not (Test-Path -LiteralPath (Join-Path $PSScriptRoot 'ZipStreamCheck.java'))) { $tools += Join-Path $project 'tools\verification\ZipStreamCheck.java' }
    & javac --release 17 -encoding UTF-8 -proc:none -cp $classpath -d $classes $tools
    if ($LASTEXITCODE -ne 0) { throw 'Tool compilation failed' }
    & $java17 -cp $classpath RemapSilence (Join-Path $project 'build\createMcpToSrg\output.tsrg') (Join-Path $PSScriptRoot 'compiled-official') (Join-Path $PSScriptRoot 'compiled-runtime') $mapped (Join-Path $project 'build\classes\java\main')
    if ($LASTEXITCODE -ne 0) { throw 'Production remap failed' }
}
& $java17 -Xverify:all -cp $classpath PatchZicaiSilence $Baseline $Candidate (Join-Path $PSScriptRoot 'compiled-runtime') 2>&1 | Tee-Object -FilePath (Join-Path $PSScriptRoot 'merge.log')
if ($LASTEXITCODE -ne 0) { throw 'Incremental JAR merge failed' }
& $java17 -Xverify:all -cp $classpath VerifyZicaiSilence $Candidate $Baseline $PSScriptRoot 2>&1 | Tee-Object -FilePath (Join-Path $PSScriptRoot 'verification.log')
if ($LASTEXITCODE -ne 0) { throw 'Silence verification failed' }
$zipRules = Join-Path $PSScriptRoot 'check_jar_zip_rules.cjs'
if (-not (Test-Path -LiteralPath $zipRules)) { $zipRules = Join-Path $project 'tools\verification\check_jar_zip_rules.cjs' }
& node $zipRules $Candidate 2>&1 | Tee-Object -FilePath (Join-Path $PSScriptRoot 'zip-rules.log')
if ($LASTEXITCODE -ne 0) { throw 'ZIP rules failed' }
& $java17 -cp $classes ZipStreamCheck $Candidate 2>&1 | Tee-Object -FilePath (Join-Path $PSScriptRoot 'zip-stream.log')
if ($LASTEXITCODE -ne 0) { throw 'ZIP stream failed' }
