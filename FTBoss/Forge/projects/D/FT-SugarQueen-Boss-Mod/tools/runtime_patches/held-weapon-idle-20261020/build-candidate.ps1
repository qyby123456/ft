#requires -Version 7.0
param(
    [Parameter(Mandatory = $true)][string]$BaselineJar,
    [Parameter(Mandatory = $true)][string]$OutputDirectory
)
$ErrorActionPreference = 'Stop'
$project = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '../../..')).Path
$output = [IO.Path]::GetFullPath($OutputDirectory)
$workRoot = [IO.Path]::GetFullPath((Join-Path $project '_work')) + [IO.Path]::DirectorySeparatorChar
if (-not $output.StartsWith($workRoot, [StringComparison]::OrdinalIgnoreCase)) { throw 'Output must stay inside project/_work.' }
if (Test-Path -LiteralPath $output) { throw 'Output exists; use a fresh directory to avoid mixing candidates.' }
$baseline = (Resolve-Path -LiteralPath $BaselineJar).Path
$gameRoot = Join-Path $env:APPDATA '.minecraft'
$jdk = Join-Path $gameRoot 'runtime/java-runtime-gamma-snapshot/bin'
$mapped = Join-Path $env:USERPROFILE '.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.20.1-47.4.10_mapped_official_1.20.1/forge-1.20.1-47.4.10_mapped_official_1.20.1.jar'
$version = Get-Content -LiteralPath (Join-Path $gameRoot 'versions/FarmingTales_ForgeⅡ v1.5.2/FarmingTales_ForgeⅡ v1.5.2.json') -Raw -Encoding UTF8 | ConvertFrom-Json
$libraries = @($version.libraries | ForEach-Object { if ($_.downloads.artifact.path) { Join-Path $gameRoot ('libraries/' + $_.downloads.artifact.path) } } | Where-Object { Test-Path -LiteralPath $_ })
$asm = @('asm', 'asm-tree', 'asm-util', 'asm-analysis') | ForEach-Object { Join-Path $gameRoot "libraries/org/ow2/asm/$_/9.8/$_-9.8.jar" }
New-Item -ItemType Directory -Path $output | Out-Null
Copy-Item -LiteralPath $baseline -Destination (Join-Path $output 'baseline.jar')
Push-Location -LiteralPath $output
try {
    & (Join-Path $jdk 'javac.exe') -proc:none -encoding UTF-8 -cp "$mapped;baseline.jar" -d template-classes (Join-Path $PSScriptRoot 'template/net/farmingtales/ftboss/item/OwnerRepairContinuation.java')
    if ($LASTEXITCODE) { throw 'Helper template compilation failed.' }
    $sources = @('PatchHeldIdle', 'VerifyHeldIdle', 'HeldIdleParity', 'SocketCountParity', 'DamageNoopParity', 'LoadVerify') | ForEach-Object { Join-Path $PSScriptRoot ($_.ToString() + '.java') }
    & (Join-Path $jdk 'javac.exe') -proc:none -encoding UTF-8 -cp ($asm -join ';') -d tool-classes @sources (Join-Path $project 'tools/verification/ZipStreamCheck.java')
    if ($LASTEXITCODE) { throw 'Tool compilation failed.' }
    $toolCp = (@('tool-classes') + $asm) -join ';'
    & (Join-Path $jdk 'java.exe') -cp $toolCp PatchHeldIdle baseline.jar template-classes overlay | Tee-Object -FilePath patch.log
    if ($LASTEXITCODE) { throw 'Pinned production methods changed; inspect before adapting this patch.' }
    Copy-Item -LiteralPath baseline.jar -Destination candidate.jar
    & (Join-Path $jdk 'jar.exe') uf candidate.jar -C overlay net
    if ($LASTEXITCODE) { throw 'Candidate packaging failed.' }
    & (Join-Path $jdk 'java.exe') -cp $toolCp VerifyHeldIdle baseline.jar candidate.jar | Tee-Object -FilePath delta.log
    if ($LASTEXITCODE) { throw 'Unexpected JAR entry or method changes.' }
    & (Join-Path $jdk 'java.exe') -Xverify:all -cp $toolCp SocketCountParity baseline.jar candidate.jar | Tee-Object -FilePath socket-parity.log
    if ($LASTEXITCODE) { throw 'Socket differential regression failed.' }
    & (Join-Path $jdk 'java.exe') -Xverify:all -cp $toolCp DamageNoopParity baseline.jar candidate.jar | Tee-Object -FilePath damage-noop-parity.log
    if ($LASTEXITCODE) { throw 'Damage no-op differential regression failed.' }
    $srg = Join-Path $gameRoot 'libraries/net/minecraft/client/1.20.1-20230612.114412/client-1.20.1-20230612.114412-srg.jar'
    $runtimeCp = (@('tool-classes', 'candidate.jar', $srg, $mapped) + $libraries) -join ';'
    & (Join-Path $jdk 'java.exe') -Xverify:all -cp $runtimeCp LoadVerify | Tee-Object -FilePath load-verify.log
    if ($LASTEXITCODE) { throw 'JVM verification failed.' }
    & (Join-Path $jdk 'java.exe') -Xverify:all -cp $runtimeCp HeldIdleParity baseline.jar candidate.jar | Tee-Object -FilePath idle-parity.log
    if ($LASTEXITCODE) { throw 'Idle/authorization context regression failed.' }
    & (Join-Path $jdk 'java.exe') -cp tool-classes ZipStreamCheck candidate.jar | Tee-Object -FilePath zip-crc.log
    if ($LASTEXITCODE) { throw 'JDK ZIP/CRC check failed.' }
    & node (Join-Path $project 'tools/verification/check_jar_zip_rules.cjs') candidate.jar | Tee-Object -FilePath zip-rules.log
    if ($LASTEXITCODE) { throw 'ZIP structure check failed.' }
    Get-FileHash -LiteralPath candidate.jar -Algorithm SHA256 | Format-List
    Write-Output 'CANDIDATE_ONLY: independent Forge world regression for this exact hash is still required before deployment.'
} finally { Pop-Location }
