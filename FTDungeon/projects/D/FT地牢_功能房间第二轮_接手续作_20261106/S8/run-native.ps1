param([Parameter(Mandatory=$true)][string]$Room)
$ErrorActionPreference='Stop'
$qa=Join-Path $PSScriptRoot "$Room\qa"
$dependencies=Get-Content -LiteralPath 'D:\ft工程文件\霜雪花园地牢_20261101\build\classpath\runData_minecraftClasspath.txt' | Where-Object {$_ -and $_ -notmatch 'minecraft_user_repo'}
$classpath=(@('D:\ft工程文件\FT地牢_独立复核_20261104_01a1a685\classes','D:\ft工程文件\锈铜宝库地牢_20261101\qa\vanilla-1.20.1-mapped.jar')+$dependencies)-join ';'
Copy-Item -LiteralPath 'D:\ft工程文件\FT地牢_独立复核_20261104_01a1a685\vanilla-block-tags.json' -Destination (Join-Path $qa 'vanilla-block-tags.json')
Set-Content -LiteralPath (Join-Path $qa 'classpath.txt') -Value $classpath
Push-Location $qa
try {
 & java '-Xmx1536m' '-Djava.awt.headless=true' -cp $classpath IndependentNativeAudit $qa frozen 2>&1 | Out-File -LiteralPath 'native.log'
 if($LASTEXITCODE -ne 0){throw 'Native registry audit failed'}
 & java '-Xmx1536m' '-Djava.awt.headless=true' -cp $classpath IndependentRouteAudit $qa 2>&1 | Out-File -LiteralPath 'routes.log'
 if($LASTEXITCODE -ne 0){throw 'Native route audit failed'}
} finally {Pop-Location}
