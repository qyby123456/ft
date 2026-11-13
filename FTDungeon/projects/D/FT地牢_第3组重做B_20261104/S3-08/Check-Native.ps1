param([string]$TestRoot)
$ErrorActionPreference = 'Stop'
if (!$TestRoot) { $TestRoot = Join-Path ([IO.Path]::GetTempPath()) ('FT-S3-08-test-' + [guid]::NewGuid().ToString('N')) }
$TestRoot = [IO.Path]::GetFullPath($TestRoot)
if ((Test-Path -LiteralPath $TestRoot) -or $TestRoot -match '\\(saves|mods|config)(\\|$)') { throw 'Use a new independent temporary directory.' }
New-Item -ItemType Directory -Path (Join-Path $TestRoot 'classes') -Force | Out-Null
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'room.nbt') -Destination (Join-Path $TestRoot 'room.nbt')
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'route-targets.json') -Destination (Join-Path $TestRoot 'route-targets.json')
Copy-Item -LiteralPath 'D:\ft工程文件\FT地牢_独立复核_20261104_01a1a685\vanilla-block-tags.json' -Destination (Join-Path $TestRoot 'vanilla-block-tags.json')
$dependencies = Get-Content -LiteralPath 'D:\ft工程文件\霜雪花园地牢_20261101\build\classpath\runData_minecraftClasspath.txt' | Where-Object { $_.Trim() -and $_ -notmatch 'minecraft_user_repo' }
$classpath = (@('D:\ft工程文件\锈铜宝库地牢_20261101\qa\vanilla-1.20.1-mapped.jar') + $dependencies) -join ';'
Push-Location -LiteralPath $TestRoot
try {
    & javac -encoding UTF-8 -cp $classpath -d (Join-Path $TestRoot 'classes') (Join-Path $PSScriptRoot 'NativeQA.java') 2>&1 | Out-File -LiteralPath (Join-Path $TestRoot 'compile.log') -Encoding utf8
    if ($LASTEXITCODE -ne 0) { throw "Compile failed: $TestRoot" }
    & java '-Djava.awt.headless=true' -cp ((Join-Path $TestRoot 'classes') + ';' + $classpath) NativeQA $TestRoot $TestRoot 2>&1 | Out-File -LiteralPath (Join-Path $TestRoot 'run.log') -Encoding utf8
    $exitCode = $LASTEXITCODE
    foreach ($name in @('compile.log', 'run.log', 'native-qa.json')) {
        $file = Join-Path $TestRoot $name
        if (Test-Path -LiteralPath $file) { Copy-Item -LiteralPath $file -Destination (Join-Path $PSScriptRoot $name) }
    }
    if ($exitCode -ne 0) { throw "Native check failed: $TestRoot" }
    Write-Output "PASS; isolated test directory: $TestRoot"
} finally { Pop-Location }
