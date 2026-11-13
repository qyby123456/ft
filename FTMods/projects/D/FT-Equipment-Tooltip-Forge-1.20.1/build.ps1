param(
    [string]$Instance = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2',
    [string]$Jdk = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
)

$ErrorActionPreference = 'Stop'
$project = $PSScriptRoot
$build = Join-Path $project 'build'
$classes = Join-Path $build 'classes'
$cache = Join-Path $env:USERPROFILE '.gradle\caches\forge_gradle'
$mapped = Join-Path $cache 'minecraft_user_repo\net\minecraftforge\forge\1.20.1-47.4.10_mapped_official_1.20.1\forge-1.20.1-47.4.10_mapped_official_1.20.1.jar'
$mapping = Join-Path $cache 'minecraft_user_repo\de\oceanlabs\mcp\mcp_config\1.20.1-20230612.114412\srg_to_official_1.20.1.tsrg'
$remapper = Join-Path $cache 'maven_downloader\net\minecraftforge\ForgeAutoRenamingTool\1.0.2\ForgeAutoRenamingTool-1.0.2-all.jar'
$libraryRoot = Join-Path $env:APPDATA '.minecraft\libraries'
$version = Get-Content -LiteralPath (Join-Path $Instance ((Split-Path -Leaf $Instance) + '.json')) -Raw | ConvertFrom-Json
$libraries = @($version.libraries | ForEach-Object {
    $allowed = !$_.rules
    foreach ($rule in $_.rules) {
        if (!$rule.os -or !$rule.os.name -or $rule.os.name -eq 'windows') {
            $allowed = $rule.action -eq 'allow'
        }
    }
    if ($allowed -and $_.downloads.artifact.path -and $_.downloads.artifact.path -notmatch 'natives-') {
        $path = Join-Path $libraryRoot $_.downloads.artifact.path
        if (!(Test-Path -LiteralPath $path)) { throw "Missing local library: $path" }
        $path
    }
})
$libraries += @('javafmllanguage', 'fmlcore', 'fmlloader') | ForEach-Object {
    $path = Join-Path $libraryRoot "net\minecraftforge\$_\1.20.1-47.4.10\$_-1.20.1-47.4.10.jar"
    if (!(Test-Path -LiteralPath $path)) { throw "Missing local Forge library: $path" }
    $path
}
$null = New-Item -ItemType Directory -Force -Path $classes, (Join-Path $build 'tests'), (Join-Path $build 'libs')
$classpath = (@($mapped) + $libraries) -join ';'

function Invoke-JavaTool([string]$Tool, [string[]]$ToolArguments) {
    Push-Location -LiteralPath $project
    try {
        & (Join-Path $Jdk "bin\$Tool.exe") @ToolArguments
        if ($LASTEXITCODE -ne 0) { throw "$Tool failed with exit code $LASTEXITCODE" }
    } finally {
        Pop-Location
    }
}

function Write-ArgumentFile([string]$Path, [string[]]$Arguments) {
    [IO.File]::WriteAllLines($Path, @($Arguments | ForEach-Object {
        '"' + $_.Replace('\', '/') + '"'
    }), [Text.Encoding]::GetEncoding(936))
}

$sources = @(Get-ChildItem -LiteralPath (Join-Path $project 'src\main\java') -Recurse -File -Filter '*.java' | Select-Object -ExpandProperty FullName)
$compileArguments = Join-Path $build 'compile.args'
Write-ArgumentFile $compileArguments (@('--release', '17', '-encoding', 'UTF-8', '-proc:none', '-cp', $classpath, '-d', $classes) + $sources)
Invoke-JavaTool 'javac' @('@' + $compileArguments)
$testSources = @(Get-ChildItem -LiteralPath (Join-Path $project 'src\test\java') -Recurse -File -Filter '*.java' | Select-Object -ExpandProperty FullName)
$testArguments = Join-Path $build 'test-compile.args'
Write-ArgumentFile $testArguments (@('--release', '17', '-encoding', 'UTF-8', '-proc:none', '-cp', "$classes;$classpath", '-d', (Join-Path $build 'tests')) + $testSources)
Invoke-JavaTool 'javac' @('@' + $testArguments)
$runArguments = Join-Path $build 'test-run.args'
$clientResources = Join-Path $cache 'minecraft_repo\versions\1.20.1\client-extra.jar'
Write-ArgumentFile $runArguments @('-Xverify:all', '-cp', "$(Join-Path $build 'tests');$classes;$(Join-Path $project 'src\main\resources');$clientResources;$classpath", 'net.farmingtales.equipmenttooltip.EquipmentTooltipCheck')
Invoke-JavaTool 'java' @('@' + $runArguments)
$developmentJar = Join-Path $build 'equipment-tooltip-dev.jar'
$artifact = Join-Path $build 'libs\ft_equipment_tooltip-1.0.0.jar'
Invoke-JavaTool 'jar' @('--create', '--file', $developmentJar, '-C', $classes, '.', '-C', (Join-Path $project 'src\main\resources'), '.')
$remapArguments = Join-Path $build 'remap.args'
Write-ArgumentFile $remapArguments (@('-jar', $remapper, '--input', $developmentJar, '--output', $artifact, '--map', $mapping, '--reverse', '--threads', '2', '--lib', $mapped) + @($libraries | ForEach-Object { '--lib'; $_ }))
Invoke-JavaTool 'java' @('@' + $remapArguments)
$srg = Join-Path $cache 'minecraft_user_repo\net\minecraftforge\forge\1.20.1-47.4.10\forge-1.20.1-47.4.10-srg.jar'
$productionClasspath = (@((Join-Path $build 'tests'), $artifact, $clientResources, $srg) + $libraries) -join ';'
$linkArguments = Join-Path $build 'production-link.args'
Write-ArgumentFile $linkArguments @('-Xverify:all', '-cp', $productionClasspath, 'net.farmingtales.equipmenttooltip.ProductionLinkCheck', $artifact)
Invoke-JavaTool 'java' @('@' + $linkArguments)
[PSCustomObject]@{
    Path = $artifact
    Bytes = (Get-Item -LiteralPath $artifact).Length
    SHA256 = (Get-FileHash -LiteralPath $artifact -Algorithm SHA256).Hash
    GameTested = $false
    AttributeAndTooltipChecks = 2037
    ProductionMemberLinkCheck = $true
} | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $build 'verification.json') -Encoding UTF8
Get-Content -LiteralPath (Join-Path $build 'verification.json')
