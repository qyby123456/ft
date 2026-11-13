param([string]$TestRoot)
$ErrorActionPreference = 'Stop'
$source = Split-Path -Parent $PSScriptRoot
if (!$TestRoot) { $TestRoot = Join-Path ([System.IO.Path]::GetTempPath()) ('FT-S3-01-evidence-' + [guid]::NewGuid().ToString('N')) }
$TestRoot = [System.IO.Path]::GetFullPath($TestRoot)
if (Test-Path -LiteralPath $TestRoot) { throw 'TestRoot must be a new independent temporary directory.' }
if ($TestRoot -match '\\(saves|mods|config)(\\|$)') { throw 'Refusing game directory.' }
New-Item -ItemType Directory -Path $TestRoot | Out-Null
New-Item -ItemType Directory -Path (Join-Path $TestRoot 'classes') | Out-Null
$dependencies = Get-Content -LiteralPath 'D:\ft工程文件\霜雪花园地牢_20261101\build\classpath\runData_minecraftClasspath.txt' | Where-Object { $_.Trim() -and $_ -notmatch 'minecraft_user_repo' }
$classpath = (@('D:\ft工程文件\锈铜宝库地牢_20261101\qa\vanilla-1.20.1-mapped.jar') + $dependencies) -join ';'
Push-Location -LiteralPath $TestRoot
try {
    & javac -encoding UTF-8 -cp $classpath -d (Join-Path $TestRoot 'classes') (Join-Path $PSScriptRoot 'RouteEvidence.java') 2>&1 | Out-File -LiteralPath (Join-Path $TestRoot 'compile.log') -Encoding utf8
    if ($LASTEXITCODE -ne 0) { throw 'Native compilation failed; see compile.log' }
    & java '-Djava.awt.headless=true' -cp ((Join-Path $TestRoot 'classes') + ';' + $classpath) RouteEvidence $source $TestRoot 2>&1 | Out-File -LiteralPath (Join-Path $TestRoot 'run.log') -Encoding utf8
    if ($LASTEXITCODE -ne 0) { throw 'Native route check failed; see run.log' }
    Write-Output $TestRoot
} finally { Pop-Location }
